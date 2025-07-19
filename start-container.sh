#!/bin/bash

echo "=== 启动虚拟容器 ==="

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "错误: Docker未运行，请先启动Docker"
    exit 1
fi

# 创建必要的目录
echo "创建目录..."
mkdir -p data logs config

# 停止并删除已存在的容器
echo "清理已存在的容器..."
docker-compose -f docker-compose-virtual.yml down

# 构建并启动容器
echo "构建并启动虚拟容器..."
docker-compose -f docker-compose-virtual.yml up -d --build

# 等待容器启动
echo "等待容器启动..."
sleep 15

# 检查容器状态
echo "检查容器状态..."
docker-compose -f docker-compose-virtual.yml ps

# 检查容器日志
echo ""
echo "查看容器日志:"
docker logs --tail=20 virtual-app-container

echo ""
echo "=== 容器信息 ==="
echo "🔗 容器名称: virtual-app-container"
echo "🌐 主机名: virtual-app"
echo "📁 数据目录: ./data"
echo "📁 日志目录: ./logs"
echo "📁 配置目录: ./config"
echo ""
echo "=== 管理命令 ==="
echo "查看容器状态: docker-compose -f docker-compose-virtual.yml ps"
echo "查看容器日志: docker-compose -f docker-compose-virtual.yml logs -f virtual-container"
echo "进入容器: docker exec -it virtual-app-container bash"
echo "停止容器: docker-compose -f docker-compose-virtual.yml down"
echo "重启容器: docker-compose -f docker-compose-virtual.yml restart virtual-container" 