#!/bin/bash

# 配置变更监听器脚本
# 监听ZooKeeper中的配置变更通知，并自动拉取最新配置

ZK_HOST="config-center-zookeeper"
INSTANCE_ID="simple-container"
APP_NAME="demo-app"
ENVIRONMENT="dev"
GROUP_NAME="common"

# 通知路径
NOTIFICATION_PATH="/config-center/notifications/$APP_NAME/$ENVIRONMENT/$GROUP_NAME"
# 配置路径
CONFIG_PATH="/config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME"

echo "=== 配置变更监听器启动 ==="
echo "实例ID: $INSTANCE_ID"
echo "应用: $APP_NAME"
echo "环境: $ENVIRONMENT"
echo "分组: $GROUP_NAME"
echo "通知路径: $NOTIFICATION_PATH"
echo "配置路径: $CONFIG_PATH"
echo "=========================="

# 创建配置存储目录
mkdir -p /tmp/configs

# 监听函数
listen_for_changes() {
    echo "开始监听配置变更通知..."
    
    while true; do
        # 检查是否有针对本实例的通知
        NOTIFICATION_NODE="$NOTIFICATION_PATH/*/$INSTANCE_ID"
        
        # 使用zkCli.sh检查通知节点
        NOTIFICATIONS=$(docker exec $ZK_HOST zkCli.sh -server localhost:2181 ls $NOTIFICATION_PATH 2>/dev/null | grep -v "Connecting to" | grep -v "INFO" | grep -v "WATCHER" | grep -v "Exiting JVM" | tr -d '[]' | tr ',' ' ')
        
        if [ -n "$NOTIFICATIONS" ]; then
            for config_key in $NOTIFICATIONS; do
                # 检查是否有针对本实例的通知
                NOTIFICATION_DATA=$(docker exec $ZK_HOST zkCli.sh -server localhost:2181 get $NOTIFICATION_PATH/$config_key/$INSTANCE_ID 2>/dev/null | grep -v "Connecting to" | grep -v "INFO" | grep -v "WATCHER" | grep -v "Exiting JVM" | tail -n 1)
                
                if [ -n "$NOTIFICATION_DATA" ]; then
                    echo "收到配置变更通知: $config_key"
                    echo "通知数据: $NOTIFICATION_DATA"
                    
                    # 解析通知数据
                    CONFIG_KEY=$(echo $NOTIFICATION_DATA | grep -o '"configKey":"[^"]*"' | cut -d'"' -f4)
                    NEW_VALUE=$(echo $NOTIFICATION_DATA | grep -o '"newValue":"[^"]*"' | cut -d'"' -f4)
                    TIMESTAMP=$(echo $NOTIFICATION_DATA | grep -o '"timestamp":[0-9]*' | cut -d':' -f2)
                    
                    echo "配置键: $CONFIG_KEY"
                    echo "新值: $NEW_VALUE"
                    echo "时间戳: $TIMESTAMP"
                    
                    # 从ZooKeeper拉取最新配置
                    pull_latest_config $CONFIG_KEY
                    
                    # 删除通知节点（表示已处理）
                    docker exec $ZK_HOST zkCli.sh -server localhost:2181 delete $NOTIFICATION_PATH/$config_key/$INSTANCE_ID 2>/dev/null
                    echo "已处理通知并删除通知节点"
                fi
            done
        fi
        
        # 等待5秒后继续检查
        sleep 5
    done
}

# 拉取最新配置函数
pull_latest_config() {
    local config_key=$1
    echo "正在拉取配置: $config_key"
    
    # 从ZooKeeper获取最新配置值
    CONFIG_VALUE=$(docker exec $ZK_HOST zkCli.sh -server localhost:2181 get $CONFIG_PATH/$config_key 2>/dev/null | grep -v "Connecting to" | grep -v "INFO" | grep -v "WATCHER" | grep -v "Exiting JVM" | tail -n 1)
    
    if [ -n "$CONFIG_VALUE" ]; then
        echo "获取到配置值: $CONFIG_VALUE"
        
        # 保存配置到本地文件
        echo "$CONFIG_VALUE" > "/tmp/configs/$config_key"
        echo "配置已保存到: /tmp/configs/$config_key"
        
        # 显示当前所有配置
        echo "当前所有配置:"
        ls -la /tmp/configs/
        echo "配置内容:"
        for file in /tmp/configs/*; do
            if [ -f "$file" ]; then
                echo "$(basename $file): $(cat $file)"
            fi
        done
    else
        echo "无法获取配置值"
    fi
}

# 初始化：拉取所有已订阅的配置
initialize_configs() {
    echo "初始化配置..."
    
    # 这里可以根据实际订阅的配置列表来拉取
    # 目前我们知道订阅了debug.enabled
    pull_latest_config "debug.enabled"
}

# 主函数
main() {
    echo "配置变更监听器启动中..."
    
    # 等待ZooKeeper连接
    echo "等待ZooKeeper连接..."
    sleep 3
    
    # 初始化配置
    initialize_configs
    
    # 开始监听
    listen_for_changes
}

# 启动主函数
main 