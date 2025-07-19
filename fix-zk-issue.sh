#!/bin/bash

echo "=== ZooKeeper问题修复脚本 ==="

# 检查ZooKeeper容器状态
echo "1. 检查ZooKeeper容器状态..."
if docker ps | grep -q config-center-zookeeper; then
    echo "✅ ZooKeeper容器正在运行"
else
    echo "❌ ZooKeeper容器未运行"
    exit 1
fi

# 检查ZooKeeper服务状态
echo ""
echo "2. 检查ZooKeeper服务状态..."
ZK_STATUS=$(docker exec config-center-zookeeper zkServer.sh status 2>/dev/null)
echo "$ZK_STATUS"

# 检查是否有zxid错误
echo ""
echo "3. 检查ZooKeeper日志中的错误..."
ERROR_COUNT=$(docker logs config-center-zookeeper 2>&1 | grep -c "Refusing session request")
if [ $ERROR_COUNT -gt 0 ]; then
    echo "⚠️ 发现 $ERROR_COUNT 个zxid错误"
    echo "正在修复..."
    
    # 停止并删除容器
    echo "停止ZooKeeper容器..."
    docker stop config-center-zookeeper
    docker rm config-center-zookeeper
    
    # 清理数据卷
    echo "清理ZooKeeper数据..."
    docker volume rm config-center_zk_data config-center_zk_datalog 2>/dev/null
    
    # 重新启动
    echo "重新启动ZooKeeper..."
    docker-compose -f docker-compose-zk.yml up -d
    
    # 等待启动
    echo "等待ZooKeeper启动..."
    sleep 15
    
    # 检查状态
    echo "检查修复后的状态..."
    docker exec config-center-zookeeper zkServer.sh status
    
else
    echo "✅ 未发现zxid错误"
fi

# 测试连接
echo ""
echo "4. 测试ZooKeeper连接..."
if echo "stat" | nc localhost 2181 > /dev/null 2>&1; then
    echo "✅ ZooKeeper连接正常"
    echo "当前zxid: $(echo 'stat' | nc localhost 2181 | grep 'Zxid:' | cut -d' ' -f2)"
else
    echo "❌ ZooKeeper连接失败"
fi

echo ""
echo "=== 修复完成 ==="
echo "如果问题仍然存在，请检查："
echo "1. 客户端连接配置"
echo "2. 网络连接"
echo "3. 防火墙设置"
echo "4. 客户端重连逻辑" 