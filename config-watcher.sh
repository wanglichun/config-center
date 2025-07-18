#!/bin/bash

# 配置变更监听脚本
# 监听ZooKeeper中的配置变更，并更新本地配置文件

MACHINE_ID=${1:-machine-001}
APP_NAME="demo-app"
ENVIRONMENT="dev"
GROUP_NAME="redis"
CONFIG_KEY="host"
ZK_HOST="localhost:2181"
LOCAL_CONFIG_DIR="/app/config"

echo "=== 启动配置监听器 ==="
echo "机器ID: $MACHINE_ID"
echo "应用: $APP_NAME"
echo "环境: $ENVIRONMENT"
echo "分组: $GROUP_NAME"
echo "配置键: $CONFIG_KEY"
echo "本地配置目录: $LOCAL_CONFIG_DIR"

# 创建本地配置目录
mkdir -p $LOCAL_CONFIG_DIR

# 监听配置变更的函数
watch_config() {
    echo "开始监听配置变更..."
    
    # 使用zookeeper-shell监听配置节点
    docker exec zk1 zookeeper-shell $ZK_HOST get /config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY 2>/dev/null | while read -r line; do
        if [[ $line =~ ^[0-9a-zA-Z] ]]; then
            echo "检测到配置变更: $line"
            update_local_config "$line"
        fi
    done
}

# 更新本地配置文件的函数
update_local_config() {
    local new_value=$1
    local config_file="$LOCAL_CONFIG_DIR/application.properties"
    
    echo "更新本地配置文件: $config_file"
    echo "新配置值: $new_value"
    
    # 更新配置文件
    echo "host=$new_value" > $config_file
    echo "更新时间: $(date)" >> $config_file
    
    echo "本地配置文件已更新"
    echo "=== 当前配置内容 ==="
    cat $config_file
    echo "===================="
}

# 监听通知节点的函数
watch_notifications() {
    echo "开始监听配置变更通知..."
    
    # 监听通知节点
    docker exec zk1 zookeeper-shell $ZK_HOST ls /config-center/config-center/notifications/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY 2>/dev/null | while read -r line; do
        if [[ $line == *"$MACHINE_ID"* ]]; then
            echo "收到配置变更通知: $line"
            # 获取最新配置
            get_latest_config
        fi
    done
}

# 获取最新配置的函数
get_latest_config() {
    echo "获取最新配置..."
    
    # 从ZooKeeper获取最新配置
    local config_value=$(docker exec zk1 zookeeper-shell $ZK_HOST get /config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY 2>/dev/null | grep -v "WATCHER" | grep -v "Connecting" | grep -v "WatchedEvent" | tail -1)
    
    if [ -n "$config_value" ]; then
        echo "获取到最新配置: $config_value"
        update_local_config "$config_value"
        
        # 删除通知节点
        docker exec zk1 zookeeper-shell $ZK_HOST delete /config-center/notifications/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY/$MACHINE_ID 2>/dev/null
    fi
}

# 定期检查配置的函数
periodic_check() {
    echo "启动定期配置检查..."
    
    while true; do
        echo "$(date): 检查配置更新..."
        get_latest_config
        sleep 30
    done
}

# 主函数
main() {
    echo "配置监听器启动..."
    
    # 启动定期检查（后台运行）
    periodic_check &
    
    # 启动通知监听（后台运行）
    watch_notifications &
    
    # 启动配置监听
    watch_config
}

# 启动主函数
main 