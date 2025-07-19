#!/bin/bash

# 使用ZooKeeper客户端工具拉取配置的脚本
# 通过ZooKeeper容器中的客户端工具获取真实配置

echo "=== 使用ZooKeeper客户端工具拉取配置 ==="

# 配置参数
ZK_CONTAINER_NAME="config-center-zookeeper"
ZK_HOST="172.18.0.2"  # ZooKeeper容器的IP地址
ZK_PORT="2181"
APP_NAME="demo-app"
ENVIRONMENT="dev"
GROUP_NAME="common"
CONFIG_KEY="debug.enabled"
WATCH_INTERVAL=10  # 监听间隔（秒）

# 构建正确的ZooKeeper路径
CONFIG_PATH="/config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY"

# 日志文件
LOG_FILE="/tmp/zk-client-pull-config.log"

echo "ZooKeeper容器: $ZK_CONTAINER_NAME"
echo "ZooKeeper地址: $ZK_HOST:$ZK_PORT"
echo "配置路径: $CONFIG_PATH"

# 获取当前配置值 - 使用ZooKeeper客户端工具
get_current_config() {
    echo "$(date): 使用ZooKeeper客户端获取配置: $CONFIG_PATH" >> $LOG_FILE
    
    # 使用ZooKeeper容器中的客户端工具获取配置
    local config_value=$(docker exec $ZK_CONTAINER_NAME zkCli.sh -server $ZK_HOST:$ZK_PORT get $CONFIG_PATH 2>/dev/null | tail -1)
    echo "$(date): 获取到的配置值: '$config_value'" >> $LOG_FILE
    
    # 如果获取失败或为空，返回错误信息
    if [ -z "$config_value" ] || [ "$config_value" = "null" ]; then
        echo "$(date): 无法从ZooKeeper获取配置值" >> $LOG_FILE
        echo "ERROR_NO_CONFIG"
    else
        echo "$(date): 成功获取配置值: '$config_value'" >> $LOG_FILE
        echo "$config_value"
    fi
}

# 更新本地配置
update_local_config() {
    local config_value=$1
    local config_file="/tmp/config.properties"
    
    echo "# 使用ZooKeeper客户端工具拉取的配置 - 更新时间: $(date)" > $config_file
    echo "# 配置路径: $CONFIG_PATH" >> $config_file
    echo "debug.enabled=$config_value" >> $config_file
    echo "app.version=1.0.0" >> $config_file
    echo "jdbc.url=jdbc:mysql://localhost:3306/demo_dev" >> $config_file
    echo "jdbc.username=root" >> $config_file
    echo "redis.host=localhost" >> $config_file
    echo "redis.port=6379" >> $config_file
    
    echo "$(date): 配置已更新到文件 - debug.enabled=$config_value" >> $LOG_FILE
    echo "✅ 配置已更新到: $config_file"
}

# 检查ZooKeeper连接
check_zk_connection() {
    docker exec $ZK_CONTAINER_NAME zkCli.sh -server $ZK_HOST:$ZK_PORT ruok 2>/dev/null | grep -q "imok"
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
    local current_nodes=$(docker exec $ZK_CONTAINER_NAME zkCli.sh -server $ZK_HOST:$ZK_PORT stat 2>/dev/null | grep "Node count" | awk '{print $3}')
    echo $current_nodes
}

# 主循环
main() {
    echo "$(date): 开始使用ZooKeeper客户端工具监听配置变更..." >> $LOG_FILE
    
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
            
            # 获取最新配置
            local config_value=$(get_current_config)
            echo "$(date): 从ZooKeeper获取到配置值: $config_value" >> $LOG_FILE
            
            # 如果获取到真实配置，更新本地配置
            if [ "$config_value" != "ERROR_NO_CONFIG" ]; then
                update_local_config "$config_value"
            else
                echo "$(date): 无法获取配置，跳过更新" >> $LOG_FILE
            fi
            
            last_node_count=$current_node_count
        fi
        
        sleep $WATCH_INTERVAL
    done
}

# 启动主程序
main 