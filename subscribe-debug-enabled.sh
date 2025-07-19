#!/bin/bash

# 容器订阅debug.enabled配置脚本
CONTAINER_NAME="simple-container"
ZK_CONTAINER="config-center-zookeeper"
CONFIG_PATH="/config-center/configs/demo-app/dev/common/debug.enabled"

echo "开始让容器订阅debug.enabled配置..."

# 1. 检查容器是否运行
if ! docker ps | grep -q $CONTAINER_NAME; then
    echo "错误: 容器 $CONTAINER_NAME 未运行"
    exit 1
fi

# 2. 检查ZooKeeper容器是否运行
if ! docker ps | grep -q $ZK_CONTAINER; then
    echo "错误: ZooKeeper容器 $ZK_CONTAINER 未运行"
    exit 1
fi

# 3. 创建订阅节点（使用容器名称）
echo "创建订阅节点..."
docker exec $ZK_CONTAINER zkCli.sh -server localhost:2181 create -e /config-center/instances/$CONTAINER_NAME/debug.enabled "" 2>/dev/null || {
    echo "创建订阅节点失败，尝试创建父目录..."
    docker exec $ZK_CONTAINER zkCli.sh -server localhost:2181 create -e /config-center/instances/$CONTAINER_NAME "" 2>/dev/null || true
    docker exec $ZK_CONTAINER zkCli.sh -server localhost:2181 create -e /config-center/instances/$CONTAINER_NAME/debug.enabled "" 2>/dev/null || true
}

# 4. 创建容器内的拉取脚本
cat > /tmp/pull-debug-config.sh << 'EOF'
#!/bin/sh

# 从ZooKeeper拉取debug.enabled配置
ZK_HOST="config-center-zookeeper"
CONFIG_PATH="/config-center/configs/demo-app/dev/common/debug.enabled"

echo "正在从ZooKeeper拉取debug.enabled配置..."

# 使用nc命令连接ZooKeeper并获取配置
echo "get $CONFIG_PATH" | nc $ZK_HOST 2181 | grep -A 1 "debug.enabled" | tail -n 1

echo "配置拉取完成"
EOF

# 5. 复制脚本到容器
echo "复制脚本到容器..."
docker cp /tmp/pull-debug-config.sh $CONTAINER_NAME:/tmp/
docker exec $CONTAINER_NAME chmod +x /tmp/pull-debug-config.sh

# 6. 在容器中执行脚本
echo "在容器中执行拉取脚本..."
docker exec $CONTAINER_NAME /tmp/pull-debug-config.sh

echo "订阅完成！容器已订阅debug.enabled配置"
echo "当前debug.enabled的值: $(docker exec $ZK_CONTAINER zkCli.sh -server localhost:2181 get $CONFIG_PATH | tail -n 1)" 