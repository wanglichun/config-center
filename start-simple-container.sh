#!/bin/bash

echo "=== 启动普通容器 ==="

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "错误: Docker未运行，请先启动Docker"
    exit 1
fi

# 停止并删除已存在的容器
echo "清理已存在的容器..."
docker-compose -f docker-compose-simple.yml down

# 构建并启动容器
echo "构建并启动普通容器..."
docker-compose -f docker-compose-simple.yml up -d --build

# 等待容器启动
echo "等待容器启动..."
sleep 15

# 检查容器状态
echo "检查容器状态..."
docker-compose -f docker-compose-simple.yml ps

# 检查容器日志
echo ""
echo "查看容器日志:"
docker logs --tail=10 simple-app-container

# 检查容器健康状态
echo ""
echo "检查容器健康状态..."
if docker ps | grep -q "simple-app-container.*Up"; then
    echo "✅ 普通容器启动成功！"
else
    echo "❌ 普通容器启动失败"
fi

echo ""
echo "=== 容器信息 ==="
echo "🔗 容器名称: simple-app-container"
echo "🌐 主机名: simple-app"
echo "📊 端口映射: 8081:8080"
echo "📁 数据卷: simple_data"
echo "📁 日志卷: simple_logs"
echo "📁 配置卷: simple_config"
echo ""
echo "=== 管理命令 ==="
echo "查看容器状态: docker-compose -f docker-compose-simple.yml ps"
echo "查看容器日志: docker-compose -f docker-compose-simple.yml logs -f simple-container"
echo "进入容器: docker exec -it simple-app-container bash"
echo "停止容器: docker-compose -f docker-compose-simple.yml down"
echo "重启容器: docker-compose -f docker-compose-simple.yml restart simple-container"
echo ""
echo "=== 容器内部信息 ==="
echo "查看系统信息: docker exec simple-app-container cat /etc/os-release"
echo "查看网络配置: docker exec simple-app-container ip addr"
echo "查看进程: docker exec simple-app-container ps aux"
echo "查看配置: docker exec simple-app-container cat /app/config/app.properties" 