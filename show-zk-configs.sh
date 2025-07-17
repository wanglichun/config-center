#!/bin/bash

echo "🔍 在 ZooKeeper 中查找配置..."
echo ""

# 显示 ZooKeeper 中的配置结构
echo "📁 ZooKeeper 配置结构："
echo "/config-center/configs/"
echo "└── demo-app/"
echo "    └── dev/"
echo "        ├── common/"
echo "        │   └── debug.enabled"
echo "        └── database/"
echo "            ├── jdbc.url"
echo "            └── jdbc.username"
echo ""

# 查看 common 配置组
echo "📋 common 配置组："
echo "debug.enabled:"
DEBUG_VALUE=$(docker exec zk1 zookeeper-shell localhost:2181 get /config-center/configs/demo-app/dev/common/debug.enabled 2>/dev/null | tail -1)
echo "  $DEBUG_VALUE"
echo ""

# 查看 database 配置组
echo "📋 database 配置组："
echo "jdbc.url:"
JDBC_URL_VALUE=$(docker exec zk1 zookeeper-shell localhost:2181 get /config-center/configs/demo-app/dev/database/jdbc.url 2>/dev/null | tail -1)
echo "  $JDBC_URL_VALUE"

echo ""
echo "jdbc.username:"
JDBC_USERNAME_VALUE=$(docker exec zk1 zookeeper-shell localhost:2181 get /config-center/configs/demo-app/dev/database/jdbc.username 2>/dev/null | tail -1)
echo "  $JDBC_USERNAME_VALUE"

echo ""
echo "📊 配置汇总："
echo "┌─────────────────────┬─────────────────────────────────────────────┐"
echo "│ 配置键              │ 配置值                                       │"
echo "├─────────────────────┼─────────────────────────────────────────────┤"
echo "│ debug.enabled       │ $DEBUG_VALUE"
echo "│ jdbc.url            │ $JDBC_URL_VALUE"
echo "│ jdbc.username       │ $JDBC_USERNAME_VALUE"
echo "└─────────────────────┴─────────────────────────────────────────────┘"

echo ""
echo "✅ 配置查看完成！"
echo ""
echo "💡 使用 zookeeper-shell 命令的完整语法："
echo "docker exec zk1 zookeeper-shell localhost:2181 <command>"
echo ""
echo "常用命令："
echo "- ls <path>          # 列出目录内容"
echo "- get <path>         # 获取节点值"
echo "- stat <path>        # 查看节点状态"
echo "- help               # 查看帮助" 