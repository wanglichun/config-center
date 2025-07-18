#!/bin/bash

# 改进的配置监听脚本
MACHINE_ID=${1:-machine-001}
APP_NAME="demo-app"
ENVIRONMENT="dev"
GROUP_NAME="redis"
CONFIG_KEY="host"
LOCAL_CONFIG_FILE="/app/config/application.properties"
CHECK_INTERVAL=10

echo "=== 配置监听器启动 ==="
echo "机器ID: $MACHINE_ID"
echo "监听配置: $APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY"
echo "本地配置文件: $LOCAL_CONFIG_FILE"
echo "检查间隔: ${CHECK_INTERVAL}秒"

# 获取ZooKeeper配置的函数
get_zk_config() {
    local config_path="/config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY"
    local result=$(docker exec zk1 zookeeper-shell localhost:2181 get $config_path 2>/dev/null)
    
    # 使用sed获取最后一行（配置值）
    local config_value=$(echo "$result" | sed -n '$p')
    echo "$config_value"
}

# 更新本地配置的函数
update_local_config() {
    local new_value=$1
    echo "更新配置: host=$new_value"
    
    # 备份原配置
    if [ -f "$LOCAL_CONFIG_FILE" ]; then
        cp "$LOCAL_CONFIG_FILE" "${LOCAL_CONFIG_FILE}.bak"
    fi
    
    # 写入新配置
    echo "host=$new_value" > "$LOCAL_CONFIG_FILE"
    echo "更新时间: $(date)" >> "$LOCAL_CONFIG_FILE"
    
    echo "配置已更新:"
    cat "$LOCAL_CONFIG_FILE"
    echo "===================="
}

# 主循环
echo "开始监听配置变更..."
last_config=""

while true; do
    echo "$(date): 检查配置更新..."
    
    # 获取当前ZooKeeper配置
    current_config=$(get_zk_config)
    
    if [ -n "$current_config" ] && [ "$current_config" != "$last_config" ]; then
        echo "检测到配置变更: $last_config -> $current_config"
        update_local_config "$current_config"
        last_config="$current_config"
    else
        echo "配置无变化: $current_config"
    fi
    
    sleep $CHECK_INTERVAL
done 