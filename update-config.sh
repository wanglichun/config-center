#!/bin/bash

# 简单的配置更新脚本
MACHINE_ID=${1:-machine-001}
APP_NAME="demo-app"
ENVIRONMENT="dev"
GROUP_NAME="redis"
CONFIG_KEY="host"
LOCAL_CONFIG_FILE="/app/config/application.properties"

echo "=== 更新配置 ==="
echo "机器ID: $MACHINE_ID"

# 从ZooKeeper获取最新配置
echo "从ZooKeeper获取配置..."
CONFIG_VALUE=$(docker exec zk1 zookeeper-shell localhost:2181 get /config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY 2>/dev/null | tail -1)

if [ -n "$CONFIG_VALUE" ] && [ "$CONFIG_VALUE" != "Connecting to localhost:2181" ] && [ "$CONFIG_VALUE" != "WATCHER::" ] && [ "$CONFIG_VALUE" != "WatchedEvent state:SyncConnected type:None path:null" ]; then
    echo "获取到配置值: $CONFIG_VALUE"
    
    # 更新本地配置文件
    echo "host=$CONFIG_VALUE" > $LOCAL_CONFIG_FILE
    echo "更新时间: $(date)" >> $LOCAL_CONFIG_FILE
    
    echo "配置文件已更新:"
    cat $LOCAL_CONFIG_FILE
else
    echo "未获取到有效配置值"
    echo "原始输出: $CONFIG_VALUE"
fi 