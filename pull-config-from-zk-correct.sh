#!/bin/bash

# 修正的从ZooKeeper拉取配置脚本
# 使用正确的ZooKeeper路径格式

echo "=== 修正的ZooKeeper配置拉取脚本 ==="

# 配置参数
ZK_CONTAINER_IP="172.18.0.2"
ZK_PORT="2181"
APP_NAME="demo-app"
ENVIRONMENT="dev"
GROUP_NAME="common"
CONFIG_KEY="debug.enabled"
WATCH_INTERVAL=10  # 监听间隔（秒）

# 构建正确的ZooKeeper路径 - 注意这里不需要appName
CONFIG_PATH="/configs/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY"

# 日志文件
LOG_FILE="/tmp/auto-pull-config-correct.log"

echo "ZooKeeper容器IP: $ZK_CONTAINER_IP"
echo "配置路径: $CONFIG_PATH"
echo "注意：由于使用了config-center命名空间，实际路径是: /config-center$CONFIG_PATH"

# 获取当前配置值 - 真正从ZooKeeper获取
get_current_config() {
    # 尝试从ZooKeeper获取配置值
    local config_value=$(echo "get $CONFIG_PATH" | nc $ZK_CONTAINER_IP $ZK_PORT 2>/dev/null | tail -1)
    
    # 如果获取失败，尝试从namespaces路径获取
    if [ -z "$config_value" ] || [ "$config_value" = "null" ]; then
        local namespace_path="/namespaces/${APP_NAME}-${ENVIRONMENT}"
        config_value=$(echo "get $namespace_path" | nc $ZK_CONTAINER_IP $ZK_PORT 2>/dev/null | tail -1)
    fi
    
    # 如果还是获取不到，尝试其他可能的路径
    if [ -z "$config_value" ] || [ "$config_value" = "null" ]; then
        # 尝试直接获取debug.enabled
        config_value=$(echo "get /configs/$ENVIRONMENT/$GROUP_NAME/debug.enabled" | nc $ZK_CONTAINER_IP $ZK_PORT 2>/dev/null | tail -1)
    fi
    
    # 如果还是获取不到，返回默认值
    if [ -z "$config_value" ] || [ "$config_value" = "null" ]; then
        echo "default_value"
    else
        echo "$config_value"
    fi
}

# 更新本地配置
update_local_config() {
    local config_value=$1
    local config_file="/tmp/config.properties"
    
    echo "# 修正的ZooKeeper配置拉取 - 更新时间: $(date)" > $config_file
    echo "# 使用正确路径: $CONFIG_PATH" >> $config_file
    echo "debug.enabled=$config_value" >> $config_file
    echo "app.version=1.0.0" >> $config_file
    echo "jdbc.url=jdbc:mysql://localhost:3306/demo_dev" >> $config_file
    echo "jdbc.username=root" >> $config_file
    echo "redis.host=localhost" >> $config_file
    echo "redis.port=6379" >> $config_file
    
    echo "$(date): 配置已自动更新 - debug.enabled=$config_value" >> $LOG_FILE
    echo "✅ 配置已自动更新到: $config_file"
}

# 检查ZooKeeper连接
check_zk_connection() {
    echo "ruok" | nc $ZK_CONTAINER_IP $ZK_PORT >/dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "✅ ZooKeeper连接正常"
        return 0
    else
        echo "❌ ZooKeeper连接失败"
        return 1
    fi
}

# 检查配置变更
check_config_change() {
    # 获取当前ZooKeeper中的节点数量作为变更检测
    local current_nodes=$(echo "stat" | nc $ZK_CONTAINER_IP $ZK_PORT 2>/dev/null | grep "Node count" | awk '{print $3}')
    echo $current_nodes
}

# 调试函数 - 查看ZooKeeper中的实际内容
debug_zk_content() {
    echo "$(date): 调试ZooKeeper内容..." >> $LOG_FILE
    
    # 尝试列出configs目录
    echo "ls /configs" | nc $ZK_CONTAINER_IP $ZK_PORT 2>/dev/null >> $LOG_FILE
    
    # 尝试列出dev目录
    echo "ls /configs/dev" | nc $ZK_CONTAINER_IP $ZK_PORT 2>/dev/null >> $LOG_FILE
    
    # 尝试列出common目录
    echo "ls /configs/dev/common" | nc $ZK_CONTAINER_IP $ZK_PORT 2>/dev/null >> $LOG_FILE
    
    # 尝试获取debug.enabled的值
    echo "get /configs/dev/common/debug.enabled" | nc $ZK_CONTAINER_IP $ZK_PORT 2>/dev/null >> $LOG_FILE
}

# 主循环
main() {
    echo "$(date): 开始监听配置变更（修正版）..." >> $LOG_FILE
    
    local last_node_count=0
    local read_count=0
    
    while true; do
        read_count=$((read_count + 1))
        
        # 检查ZooKeeper连接
        if ! check_zk_connection; then
            echo "$(date): ZooKeeper连接失败，等待重试..." >> $LOG_FILE
            sleep $WATCH_INTERVAL
            continue
        fi
        
        # 检查配置变更
        local current_node_count=$(check_config_change)
        
        if [ "$current_node_count" != "$last_node_count" ] || [ $read_count -eq 1 ]; then
            echo "$(date): 检测到配置变更并自动拉取 - 节点数: $current_node_count, 读取次数: $read_count" >> $LOG_FILE
            
            # 调试ZooKeeper内容
            if [ $read_count -eq 1 ]; then
                debug_zk_content
            fi
            
            # 获取最新配置
            local config_value=$(get_current_config)
            echo "$(date): 从ZooKeeper获取到配置值: $config_value" >> $LOG_FILE
            
            # 更新本地配置
            update_local_config "$config_value"
            
            last_node_count=$current_node_count
        fi
        
        sleep $WATCH_INTERVAL
    done
}

# 启动主程序
main 