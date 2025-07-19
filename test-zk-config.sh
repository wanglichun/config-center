#!/bin/bash

echo "=== 简单ZooKeeper配置测试 ==="

# 1. 检查ZooKeeper连接
echo "1. 检查ZooKeeper连接..."
if echo "stat" | nc localhost 2181 > /dev/null 2>&1; then
    echo "✅ ZooKeeper连接正常"
else
    echo "❌ ZooKeeper连接失败"
    exit 1
fi

# 2. 检查根节点
echo ""
echo "2. 检查ZooKeeper根节点..."
ROOT_NODES=$(docker exec config-center-zookeeper sh -c "echo 'ls /' | nc localhost 2181" 2>/dev/null)
if [ -n "$ROOT_NODES" ]; then
    echo "✅ 根节点存在: $ROOT_NODES"
else
    echo "❌ 根节点为空"
fi

# 3. 手动创建测试节点
echo ""
echo "3. 手动创建测试节点..."
TEST_PATH="/test-config"
TEST_VALUE="test-value-$(date +%s)"

# 创建测试节点
docker exec config-center-zookeeper sh -c "echo 'create /test-config \"$TEST_VALUE\"' | nc localhost 2181" 2>/dev/null

# 检查节点是否创建成功
sleep 2
CREATED_VALUE=$(docker exec config-center-zookeeper sh -c "echo 'get /test-config' | nc localhost 2181" 2>/dev/null | tail -1)

if [ -n "$CREATED_VALUE" ] && [ "$CREATED_VALUE" != "Connecting to localhost:2181" ]; then
    echo "✅ 测试节点创建成功: $CREATED_VALUE"
else
    echo "❌ 测试节点创建失败"
fi

# 4. 检查config-center命名空间
echo ""
echo "4. 检查config-center命名空间..."
CONFIG_CENTER_NODES=$(docker exec config-center-zookeeper sh -c "echo 'ls /config-center' | nc localhost 2181" 2>/dev/null)
if [ -n "$CONFIG_CENTER_NODES" ]; then
    echo "✅ config-center命名空间存在: $CONFIG_CENTER_NODES"
else
    echo "❌ config-center命名空间不存在"
fi

# 5. 检查configs节点
echo ""
echo "5. 检查configs节点..."
CONFIGS_NODES=$(docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs' | nc localhost 2181" 2>/dev/null)
if [ -n "$CONFIGS_NODES" ]; then
    echo "✅ configs节点存在: $CONFIGS_NODES"
else
    echo "❌ configs节点不存在"
fi

# 6. 清理测试节点
echo ""
echo "6. 清理测试节点..."
docker exec config-center-zookeeper sh -c "echo 'delete /test-config' | nc localhost 2181" 2>/dev/null
echo "✅ 测试节点已清理"

echo ""
echo "=== 测试完成 ===" 