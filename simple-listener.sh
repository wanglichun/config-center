#!/bin/bash

# 简单的配置监听脚本 - 使用nc连接ZooKeeper
INSTANCE_ID="simple-container"
NOTIFICATION_PATH="/config-center/notifications/demo-app/dev/common"
ZK_SERVER="config-center-zookeeper"
ZK_PORT="2181"

echo "开始监听配置变更通知..."

while true; do
    # 检查是否有针对本实例的通知
    NOTIFICATIONS=$(echo "ls $NOTIFICATION_PATH" | nc $ZK_SERVER $ZK_PORT 2>/dev/null | grep -v "Connecting to" | grep -v "INFO" | grep -v "WATCHER" | grep -v "Exiting JVM" | tr -d '[]' | tr ',' ' ')
    
    if [ -n "$NOTIFICATIONS" ]; then
        for config_key in $NOTIFICATIONS; do
            # 检查是否有针对本实例的通知
            NOTIFICATION_DATA=$(echo "get $NOTIFICATION_PATH/$config_key/$INSTANCE_ID" | nc $ZK_SERVER $ZK_PORT 2>/dev/null | grep -v "Connecting to" | grep -v "INFO" | grep -v "WATCHER" | grep -v "Exiting JVM" | tail -n 1)
            
            if [ -n "$NOTIFICATION_DATA" ]; then
                echo "收到配置变更通知: $config_key"
                echo "通知数据: $NOTIFICATION_DATA"
                
                # 从ZooKeeper获取最新配置
                CONFIG_VALUE=$(echo "get /config-center/configs/demo-app/dev/common/$config_key" | nc $ZK_SERVER $ZK_PORT 2>/dev/null | grep -v "Connecting to" | grep -v "INFO" | grep -v "WATCHER" | grep -v "Exiting JVM" | tail -n 1)
                
                if [ -n "$CONFIG_VALUE" ]; then
                    echo "获取到配置值: $CONFIG_VALUE"
                    echo "$CONFIG_VALUE" > "/tmp/$config_key"
                    echo "配置已保存到: /tmp/$config_key"
                fi
                
                # 删除通知节点
                echo "delete $NOTIFICATION_PATH/$config_key/$INSTANCE_ID" | nc $ZK_SERVER $ZK_PORT 2>/dev/null
                echo "已处理通知并删除通知节点"
            fi
        done
    fi
    
    sleep 3
done 