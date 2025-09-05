#!/bin/bash

# 停止三台机器容器脚本

set -e

echo "🛑 停止三台机器容器..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行"
    exit 1
fi

# 检查docker-compose文件是否存在
if [ ! -f "docker-compose-machines.yml" ]; then
    echo "❌ docker-compose-machines.yml文件不存在"
    exit 1
fi

echo "⏹️ 停止所有机器容器..."
docker-compose -f docker-compose-machines.yml down

echo "🔍 检查容器状态..."
docker-compose -f docker-compose-machines.yml ps

echo ""
echo "✅ 三台机器容器已停止"
echo ""
echo "📝 其他操作："
echo "  完全清理(包括数据): docker-compose -f docker-compose-machines.yml down -v"
echo "  重新启动: ./start-three-machines.sh"
echo "  查看镜像: docker images alpine"
