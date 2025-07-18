#!/bin/bash

# 机器1配置监听脚本（修改版）
# 使用curl获取ZooKeeper数据，监听ZooKeeper通知并更新本地配置

MACHINE_ID="machine-001"
APP_NAME="demo-app"
ENVIRONMENT="dev"
LOCAL_CONFIG_DIR="/tmp/machine-001-config"
ZK_HOST="zk1:2181"

echo "=== 机器1配置监听器启动（修改版）==="
echo "机器ID: $MACHINE_ID"
echo "应用: $APP_NAME"
echo "环境: $ENVIRONMENT"
echo "本地配置目录: $LOCAL_CONFIG_DIR"
echo "ZooKeeper主机: $ZK_HOST"

# 创建本地配置目录
mkdir -p $LOCAL_CONFIG_DIR

# 使用curl获取ZooKeeper数据的函数
get_zk_data() {
    local path=$1
    # 使用ZooKeeper REST API（如果可用）或者通过配置中心API
    local result=$(curl -s "http://config-center:9090/config-center/api/zk/data?path=$path" 2>/dev/null)
    echo "$result"
}

# 使用curl删除ZooKeeper节点的函数
delete_zk_node() {
    local path=$1
    # 通过配置中心API删除节点
    curl -s -X DELETE "http://config-center:9090/config-center/api/zk/node?path=$path" 2>/dev/null
}

# 处理通知的函数
process_notification() {
    local notification_path=$1
    local notification_data=$2
    
    echo "收到通知: $notification_path"
    echo "通知数据: $notification_data"
    
    # 解析通知数据
    local config_key=$(echo "$notification_data" | grep -o '"configKey":"[^"]*"' | cut -d'"' -f4)
    local new_value=$(echo "$notification_data" | grep -o '"newValue":"[^"]*"' | cut -d'"' -f4)
    local timestamp=$(echo "$notification_data" | grep -o '"timestamp":[0-9]*' | cut -d':' -f2)
    
    echo "配置键: $config_key"
    echo "新值: $new_value"
    echo "时间戳: $timestamp"
    
    # 更新本地配置文件
    local config_file="$LOCAL_CONFIG_DIR/$config_key.properties"
    echo "$config_key=$new_value" > "$config_file"
    echo "更新时间: $(date)" >> "$config_file"
    
    echo "本地配置已更新: $config_file"
    cat "$config_file"
    
    # 删除通知节点
    delete_zk_node "$notification_path"
    echo "通知节点已删除: $notification_path"
    echo "===================="
}

# 监听通知的函数
watch_notifications() {
    echo "开始监听通知..."
    
    while true; do
        # 检查redis host通知
        local notification_path="/config-center/notifications/$APP_NAME/$ENVIRONMENT/redis/host/$MACHINE_ID"
        local notification_data=$(get_zk_data "$notification_path")
        
        if [ -n "$notification_data" ] && [ "$notification_data" != "null" ]; then
            process_notification "$notification_path" "$notification_data"
        fi
        
        # 检查database通知
        local db_notification_path="/config-center/notifications/$APP_NAME/$ENVIRONMENT/database/jdbc.url/$MACHINE_ID"
        local db_notification_data=$(get_zk_data "$db_notification_path")
        
        if [ -n "$db_notification_data" ] && [ "$db_notification_data" != "null" ]; then
            process_notification "$db_notification_path" "$db_notification_data"
        fi
        
        # 检查common通知
        local common_notification_path="/config-center/notifications/$APP_NAME/$ENVIRONMENT/common/app.version/$MACHINE_ID"
        local common_notification_data=$(get_zk_data "$common_notification_path")
        
        if [ -n "$common_notification_data" ] && [ "$common_notification_data" != "null" ]; then
            process_notification "$common_notification_path" "$common_notification_data"
        fi
        
        sleep 2
    done
}

# 显示当前配置的函数
show_current_configs() {
    echo "=== 当前本地配置 ==="
    if [ -d "$LOCAL_CONFIG_DIR" ]; then
        for config_file in "$LOCAL_CONFIG_DIR"/*.properties; do
            if [ -f "$config_file" ]; then
                echo "配置文件: $(basename "$config_file")"
                cat "$config_file"
                echo "---"
            fi
        done
    else
        echo "本地配置目录不存在"
    fi
}

# 主函数
main() {
    echo "配置监听器启动..."
    
    # 显示当前配置
    show_current_configs
    
    # 开始监听
    watch_notifications
}

# 启动主函数
main 