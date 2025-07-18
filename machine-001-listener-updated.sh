#!/bin/bash

# 机器1简单配置监听脚本
# 通过宿主机获取ZooKeeper数据并同步到机器001

MACHINE_ID="machine-001"
APP_NAME="demo-app"
ENVIRONMENT="dev"
LOCAL_CONFIG_DIR="/tmp/machine-001-config"

echo "=== 机器1简单配置监听器启动 ==="
echo "机器ID: $MACHINE_ID"
echo "应用: $APP_NAME"
echo "环境: $ENVIRONMENT"
echo "本地配置目录: $LOCAL_CONFIG_DIR"

# 创建本地配置目录
mkdir -p $LOCAL_CONFIG_DIR

# 处理通知的函数
process_notification() {
    local config_key=$1
    local new_value=$2
    local timestamp=$3
    
    echo "收到配置变更通知:"
    echo "配置键: $config_key"
    echo "新值: $new_value"
    echo "时间戳: $timestamp"
    
    # 更新本地配置文件
    local config_file="$LOCAL_CONFIG_DIR/$config_key.properties"
    echo "$config_key=$new_value" > "$config_file"
    echo "更新时间: $(date)" >> "$config_file"
    
    echo "本地配置已更新: $config_file"
    cat "$config_file"
    echo "===================="
}

# 监听通知的函数
watch_notifications() {
    echo "开始监听通知..."
    
    while true; do
        # 检查redis host通知
        local notification_path="/config-center/notifications/$APP_NAME/$ENVIRONMENT/redis/host/$MACHINE_ID"
        local notification_data=$(/usr/local/Cellar/zookeeper/3.9.3/libexec/bin/zkCli.sh -server localhost:2181 get "$notification_path" 2>/dev/null | tail -1)
        
        if [ -n "$notification_data" ] && [ "$notification_data" != "null" ]; then
            # 解析通知数据
            local config_key=$(echo "$notification_data" | grep -o '"configKey":"[^"]*"' | cut -d'"' -f4)
            local new_value=$(echo "$notification_data" | grep -o '"newValue":"[^"]*"' | cut -d'"' -f4)
            local timestamp=$(echo "$notification_data" | grep -o '"timestamp":[0-9]*' | cut -d':' -f2)
            
            process_notification "$config_key" "$new_value" "$timestamp"
            
            # 删除通知节点
            /usr/local/Cellar/zookeeper/3.9.3/libexec/bin/zkCli.sh -server localhost:2181 delete "$notification_path" 2>/dev/null
            echo "通知节点已删除: $notification_path"
        fi
        
        # 检查redis port通知
        local port_notification_path="/config-center/notifications/$APP_NAME/$ENVIRONMENT/redis/port/$MACHINE_ID"
        local port_notification_data=$(/usr/local/Cellar/zookeeper/3.9.3/libexec/bin/zkCli.sh -server localhost:2181 get "$port_notification_path" 2>/dev/null | tail -1)
        
        if [ -n "$port_notification_data" ] && [ "$port_notification_data" != "null" ]; then
            # 解析通知数据
            local config_key=$(echo "$port_notification_data" | grep -o '"configKey":"[^"]*"' | cut -d'"' -f4)
            local new_value=$(echo "$port_notification_data" | grep -o '"newValue":"[^"]*"' | cut -d'"' -f4)
            local timestamp=$(echo "$port_notification_data" | grep -o '"timestamp":[0-9]*' | cut -d':' -f2)
            
            process_notification "$config_key" "$new_value" "$timestamp"
            
            # 删除通知节点
            /usr/local/Cellar/zookeeper/3.9.3/libexec/bin/zkCli.sh -server localhost:2181 delete "$port_notification_path" 2>/dev/null
            echo "通知节点已删除: $port_notification_path"
        fi
        
        # 检查database通知
        local db_notification_path="/config-center/notifications/$APP_NAME/$ENVIRONMENT/database/jdbc.url/$MACHINE_ID"
        local db_notification_data=$(/usr/local/Cellar/zookeeper/3.9.3/libexec/bin/zkCli.sh -server localhost:2181 get "$db_notification_path" 2>/dev/null | tail -1)
        
        if [ -n "$db_notification_data" ] && [ "$db_notification_data" != "null" ]; then
            local config_key=$(echo "$db_notification_data" | grep -o '"configKey":"[^"]*"' | cut -d'"' -f4)
            local new_value=$(echo "$db_notification_data" | grep -o '"newValue":"[^"]*"' | cut -d'"' -f4)
            local timestamp=$(echo "$db_notification_data" | grep -o '"timestamp":[0-9]*' | cut -d':' -f2)
            
            process_notification "$config_key" "$new_value" "$timestamp"
            
            /usr/local/Cellar/zookeeper/3.9.3/libexec/bin/zkCli.sh -server localhost:2181 delete "$db_notification_path" 2>/dev/null
            echo "通知节点已删除: $db_notification_path"
        fi
        
        # 检查common通知
        local common_notification_path="/config-center/notifications/$APP_NAME/$ENVIRONMENT/common/app.version/$MACHINE_ID"
        local common_notification_data=$(/usr/local/Cellar/zookeeper/3.9.3/libexec/bin/zkCli.sh -server localhost:2181 get "$common_notification_path" 2>/dev/null | tail -1)
        
        if [ -n "$common_notification_data" ] && [ "$common_notification_data" != "null" ]; then
            local config_key=$(echo "$common_notification_data" | grep -o '"configKey":"[^"]*"' | cut -d'"' -f4)
            local new_value=$(echo "$common_notification_data" | grep -o '"newValue":"[^"]*"' | cut -d'"' -f4)
            local timestamp=$(echo "$common_notification_data" | grep -o '"timestamp":[0-9]*' | cut -d':' -f2)
            
            process_notification "$config_key" "$new_value" "$timestamp"
            
            /usr/local/Cellar/zookeeper/3.9.3/libexec/bin/zkCli.sh -server localhost:2181 delete "$common_notification_path" 2>/dev/null
            echo "通知节点已删除: $common_notification_path"
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