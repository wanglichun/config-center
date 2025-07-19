#!/bin/sh

# 从ZooKeeper拉取debug.enabled配置
ZK_HOST="config-center-zookeeper"
CONFIG_PATH="/config-center/configs/demo-app/dev/common/debug.enabled"

echo "正在从ZooKeeper拉取debug.enabled配置..."

# 使用nc命令连接ZooKeeper并获取配置
echo "get $CONFIG_PATH" | nc $ZK_HOST 2181 | grep -A 1 "debug.enabled" | tail -n 1

echo "配置拉取完成" 