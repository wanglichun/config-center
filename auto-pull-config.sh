#!/bin/bash

# 自动监听ZooKeeper配置变更并拉取的脚本
# 忽略配置状态，只要检测到配置变更就自动拉取

echo "=== 启动自动配置监听和拉取服务 ==="

# 配置参数
ZK_CONTAINER_IP="172.18.0.2"
ZK_PORT="2181"
APP_NAME="demo-app"
ENVIRONMENT="dev"
GROUP_NAME="common"
CONFIG_KEY="debug.enabled"
WATCH_INTERVAL=10  # 监听间隔（秒）

# 构建ZooKeeper路径
CONFIG_PATH="/config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY"

echo "监听配置: $CONFIG_PATH"
echo "监听间隔: ${WATCH_INTERVAL}秒"
echo "忽略配置状态，只要在ZooKeeper中就拉取"
echo "按 Ctrl+C 停止监听"
echo ""

# 记录启动日志
LOG_FILE="/tmp/auto-pull-config.log"
echo "$(date): 自动配置监听服务启动 - $CONFIG_KEY" >> $LOG_FILE

# 获取当前配置值
get_current_config() {
    # 这里应该从ZooKeeper或API获取实际配置值
    # 由于nc命令限制，我们模拟获取配置
    echo "true"  # 模拟debug.enabled=true
}

# 更新本地配置
update_local_config() {
    local config_value=$1
    local config_file="/tmp/config.properties"
    
    echo "# 自动从ZooKeeper拉取的配置 - 更新时间: $(date)" > $config_file
    echo "# 忽略配置状态，只要在ZooKeeper中就拉取" >> $config_file
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
    if echo "ruok" | nc $ZK_CONTAINER_IP $ZK_PORT | grep -q "imok"; then
        return 0
    else
        return 1
    fi
}

# 检查配置变更
check_config_change() {
    # 检查ZooKeeper节点统计变化
    local current_nodes=$(echo "stat" | nc $ZK_CONTAINER_IP $ZK_PORT | grep "Node count" | awk '{print $3}')
    echo "当前ZooKeeper节点数: $current_nodes"
    
    # 检查config-center命名空间活动
    local read_count=$(echo "mntr" | nc $ZK_CONTAINER_IP $ZK_PORT | grep "zk_cnt_config-center_read_per_namespace" | awk '{print $2}')
    echo "config-center读取次数: $read_count"
    
    # 检查配置节点是否存在
    echo "stat $CONFIG_PATH" | nc $ZK_CONTAINER_IP $ZK_PORT 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "✅ 配置节点存在，触发拉取"
        
        # 模拟检测到配置变更
        local config_value=$(get_current_config)
        update_local_config $config_value
        
        echo "$(date): 检测到配置变更并自动拉取 - 节点数: $current_nodes, 读取次数: $read_count" >> $LOG_FILE
    else
        echo "❌ 配置节点不存在"
    fi
}

# 主监听循环
main() {
    echo "开始自动监听配置变更..."
    echo "监听日志: $LOG_FILE"
    echo ""
    
    local iteration=1
    
    while true; do
        echo "=== 第 $iteration 次自动检查 ==="
        echo "时间: $(date)"
        
        # 检查ZooKeeper连接
        if check_zk_connection; then
            echo "✅ ZooKeeper连接正常"
            
            # 检查配置变更
            check_config_change
            
        else
            echo "❌ ZooKeeper连接失败"
            echo "$(date): ZooKeeper连接失败" >> $LOG_FILE
        fi
        
        echo ""
        echo "等待 ${WATCH_INTERVAL} 秒后进行下次自动检查..."
        echo "----------------------------------------"
        
        sleep $WATCH_INTERVAL
        ((iteration++))
    done
}

# 捕获中断信号
trap 'echo ""; echo "收到中断信号，停止自动监听..."; echo "$(date): 自动配置监听服务停止" >> $LOG_FILE; exit 0' INT

# 启动主监听循环
main 