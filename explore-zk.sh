#!/bin/bash

ZK_HOST="localhost"
ZK_PORT="2181"

echo "=== 探索ZooKeeper配置节点 ==="
echo ""

# 查看根目录
echo "1. 根目录节点:"
echo "ls /" | nc $ZK_HOST $ZK_PORT
echo ""

# 尝试查看config-center目录
echo "2. config-center目录:"
echo "ls /config-center" | nc $ZK_HOST $ZK_PORT
echo ""

# 尝试查看configs目录
echo "3. configs目录:"
echo "ls /config-center/configs" | nc $ZK_HOST $ZK_PORT
echo ""

# 尝试查看namespaces目录
echo "4. namespaces目录:"
echo "ls /config-center/namespaces" | nc $ZK_HOST $ZK_PORT
echo ""

# 尝试查看demo-app-dev配置
echo "5. demo-app-dev配置:"
echo "get /config-center/namespaces/demo-app-dev" | nc $ZK_HOST $ZK_PORT
echo ""

# 尝试查看具体的配置项
echo "6. debug.enabled配置:"
echo "get /config-center/configs/demo-app/dev/common/debug.enabled" | nc $ZK_HOST $ZK_PORT
echo ""

# 列出所有可能的路径
echo "7. 尝试其他可能的路径:"
for path in "/config-center" "/config-center/configs" "/config-center/namespaces" "/config-center/configs/demo-app" "/config-center/configs/demo-app/dev" "/config-center/configs/demo-app/dev/common"; do
    echo "检查路径: $path"
    echo "ls $path" | nc $ZK_HOST $ZK_PORT 2>/dev/null || echo "路径不存在或无法访问"
    echo ""
done 