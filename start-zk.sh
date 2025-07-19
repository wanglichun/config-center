#!/bin/bash

echo "=== 启动ZooKeeper容器 ==="

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "错误: Docker未运行，请先启动Docker"
    exit 1
fi

# 停止并删除已存在的容器
echo "清理已存在的容器..."
docker-compose -f docker-compose-zk.yml down

# 启动ZooKeeper容器
echo "启动ZooKeeper容器..."
docker-compose -f docker-compose-zk.yml up -d

# 等待ZooKeeper启动
echo "等待ZooKeeper启动..."
sleep 10

# 检查ZooKeeper状态
echo "检查ZooKeeper状态..."
if docker exec config-center-zookeeper zkServer.sh status > /dev/null 2>&1; then
    echo "✅ ZooKeeper启动成功！"
    echo "📊 ZooKeeper管理界面: http://localhost:9090"
    echo "🔗 ZooKeeper连接地址: localhost:2181"
else
    echo "❌ ZooKeeper启动失败，请检查日志:"
    docker-compose -f docker-compose-zk.yml logs zookeeper
    exit 1
fi

echo ""
echo "=== 容器信息 ==="
docker-compose -f docker-compose-zk.yml ps

echo ""
echo "=== 查看日志 ==="
echo "查看ZooKeeper日志: docker-compose -f docker-compose-zk.yml logs -f zookeeper"
echo "查看ZKUI日志: docker-compose -f docker-compose-zk.yml logs -f zkui"
echo ""
echo "=== 停止容器 ==="
echo "停止所有容器: docker-compose -f docker-compose-zk.yml down"
echo "停止并删除数据: docker-compose -f docker-compose-zk.yml down -v" 