#!/bin/bash

echo "=== 在ZooKeeper服务端查看配置 ==="

# 1. 检查ZooKeeper服务状态
echo "1. 检查ZooKeeper服务状态..."
if echo "ruok" | nc localhost 2181 | grep -q "imok"; then
    echo "✅ ZooKeeper服务正常运行"
else
    echo "❌ ZooKeeper服务异常"
    exit 1
fi

# 2. 查看节点统计
echo ""
echo "2. 查看节点统计..."
echo "总节点数: $(echo 'stat' | nc localhost 2181 | grep 'Node count' | awk '{print $3}')"

# 3. 查看config-center命名空间的配置
echo ""
echo "3. 查看config-center命名空间的配置..."

# 使用ZooKeeper的四字命令查看路径
echo "查看config-center/configs路径:"
echo "ls /config-center/configs" | nc localhost 2181 2>/dev/null || echo "无法直接访问，尝试其他方法"

# 4. 使用ZooKeeper的get命令获取配置值
echo ""
echo "4. 尝试获取具体配置值..."

# 尝试获取数据库配置
echo "获取jdbc.url配置:"
echo "get /config-center/configs/demo-app/dev/database/jdbc.url" | nc localhost 2181 2>/dev/null || echo "无法获取"

echo ""
echo "获取jdbc.username配置:"
echo "get /config-center/configs/demo-app/dev/database/jdbc.username" | nc localhost 2181 2>/dev/null || echo "无法获取"

echo ""
echo "获取jdbc.password配置:"
echo "get /config-center/configs/demo-app/dev/database/jdbc.password" | nc localhost 2181 2>/dev/null || echo "无法获取"

echo ""
echo "获取redis.host配置:"
echo "get /config-center/configs/demo-app/dev/redis/host" | nc localhost 2181 2>/dev/null || echo "无法获取"

echo ""
echo "获取redis.port配置:"
echo "get /config-center/configs/demo-app/dev/redis/port" | nc localhost 2181 2>/dev/null || echo "无法获取"

# 5. 查看ZooKeeper的监控信息
echo ""
echo "5. 查看ZooKeeper监控信息..."
echo "config-center命名空间统计:"
echo "mntr" | nc localhost 2181 | grep "config-center" | head -5

# 6. 使用ZooKeeper的wchp命令查看路径
echo ""
echo "6. 查看ZooKeeper路径信息..."
echo "wchp" | nc localhost 2181

echo ""
echo "=== 配置查看完成 ==="
echo ""
echo "如果zkcli客户端可以查询到配置，说明配置确实存在。"
echo "可能的原因："
echo "1. 配置存在于config-center命名空间中"
echo "2. 需要使用正确的ZooKeeper客户端工具访问"
echo "3. 权限或连接方式的问题" 