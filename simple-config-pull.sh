#!/bin/sh

# 简单的配置拉取脚本
ZK_HOST="config-center-zookeeper"
CONFIG_PATH="/config-center/configs/demo-app/dev/common/debug.enabled"

echo "正在从ZooKeeper获取配置: $CONFIG_PATH"

# 使用telnet连接ZooKeeper
echo "get $CONFIG_PATH" | telnet $ZK_HOST 2181 2>/dev/null | grep -v "Trying" | grep -v "Connected" | grep -v "Escape" | tail -1

echo "配置获取完成" 