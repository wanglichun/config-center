#!/bin/bash

# 启动三台机器容器脚本

set -e

echo "🚀 启动三台机器容器..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker"
    exit 1
fi

# 检查docker-compose文件是否存在
if [ ! -f "docker-compose-machines.yml" ]; then
    echo "❌ docker-compose-machines.yml文件不存在"
    exit 1
fi

echo "🔥 启动三台机器容器..."
docker-compose -f docker-compose-machines.yml up -d

echo "⏳ 等待容器启动完成..."
sleep 5

echo "🔍 检查容器状态..."
docker-compose -f docker-compose-machines.yml ps

echo ""
echo "🔗 检查机器健康状态..."

# 检查机器001
echo "正在检查机器001..."
for i in {1..30}; do
    if docker exec config-center-machine-001 cat /app/config/machine.conf | grep -q "machine-001"; then
        echo "✅ 机器001就绪"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ 机器001启动超时"
    fi
    sleep 1
done

# 检查机器002
echo "正在检查机器002..."
for i in {1..30}; do
    if docker exec config-center-machine-002 cat /app/config/machine.conf | grep -q "machine-002"; then
        echo "✅ 机器002就绪"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ 机器002启动超时"
    fi
    sleep 1
done

# 检查机器003
echo "正在检查机器003..."
for i in {1..30}; do
    if docker exec config-center-machine-003 cat /app/config/machine.conf | grep -q "machine-003"; then
        echo "✅ 机器003就绪"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ 机器003启动超时"
    fi
    sleep 1
done

echo ""
echo "🎉 三台机器容器启动完成！"
echo ""
echo "📋 机器信息："
echo "  🖥️ 机器001: machine-001 (IP: 172.20.0.10)"
echo "     订阅配置: app.database.url, app.redis.host, app.log.level"
echo ""
echo "  🖥️ 机器002: machine-002 (IP: 172.20.0.11)"
echo "     订阅配置: app.database.url, app.cache.enabled, app.security.jwt"
echo ""
echo "  🖥️ 机器003: machine-003 (IP: 172.20.0.12)"
echo "     订阅配置: app.elasticsearch.host, app.kafka.brokers, app.monitoring.enabled"
echo ""
echo "📝 常用命令："
echo "  查看日志: docker-compose -f docker-compose-machines.yml logs -f [机器名]"
echo "  查看状态: docker-compose -f docker-compose-machines.yml ps"
echo "  停止机器: ./stop-three-machines.sh"
echo "  进入机器: docker exec -it config-center-machine-001 sh"
