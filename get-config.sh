#!/bin/bash

# 快速获取 ZooKeeper 中的配置内容
echo "🔍 获取 ZooKeeper 配置内容..."
echo ""

# 获取配置值
DEBUG_VALUE=$(docker exec zk1 zookeeper-shell localhost:2181 get /config-center/configs/demo-app/dev/common/debug.enabled 2>/dev/null | tail -1)
JDBC_URL_VALUE=$(docker exec zk1 zookeeper-shell localhost:2181 get /config-center/configs/demo-app/dev/database/jdbc.url 2>/dev/null | tail -1)
JDBC_USERNAME_VALUE=$(docker exec zk1 zookeeper-shell localhost:2181 get /config-center/configs/demo-app/dev/database/jdbc.username 2>/dev/null | tail -1)

# 显示配置内容
echo "📊 当前配置内容："
echo "┌─────────────────────┬─────────────────────────────────────────────┐"
echo "│ 配置键              │ 配置值                                       │"
echo "├─────────────────────┼─────────────────────────────────────────────┤"
echo "│ debug.enabled       │ $DEBUG_VALUE"
echo "│ jdbc.url            │ $JDBC_URL_VALUE"
echo "│ jdbc.username       │ $JDBC_USERNAME_VALUE"
echo "└─────────────────────┴─────────────────────────────────────────────┘"

echo ""
echo "✅ 配置获取完成！" 