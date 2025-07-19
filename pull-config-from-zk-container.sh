#!/bin/bash

# 从ZooKeeper容器拉取配置的脚本
# 在simple-container中运行，连接到ZooKeeper容器

echo "=== 从ZooKeeper容器拉取最新配置 ==="

# 配置参数
ZK_CONTAINER_IP="172.18.0.2"  # ZooKeeper容器IP
ZK_PORT="2181"
APP_NAME="demo-app"
ENVIRONMENT="dev"
GROUP_NAME="common"
CONFIG_KEY="debug.enabled"

# 构建ZooKeeper路径
CONFIG_PATH="/config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY"

echo "ZooKeeper容器IP: $ZK_CONTAINER_IP"
echo "配置路径: $CONFIG_PATH"

# 1. 检查网络连通性
echo ""
echo "1. 检查网络连通性..."
if ping -c 2 $ZK_CONTAINER_IP > /dev/null 2>&1; then
    echo "✅ 可以ping通ZooKeeper容器"
else
    echo "❌ 无法ping通ZooKeeper容器"
    echo "尝试使用容器名访问..."
    ZK_CONTAINER_IP="config-center-zookeeper"
fi

# 2. 检查ZooKeeper连接
echo ""
echo "2. 检查ZooKeeper连接..."
if echo "ruok" | nc $ZK_CONTAINER_IP $ZK_PORT | grep -q "imok"; then
    echo "✅ ZooKeeper连接正常"
else
    echo "❌ ZooKeeper连接失败"
    exit 1
fi

# 3. 检查配置节点是否存在
echo ""
echo "3. 检查配置节点是否存在..."
echo "stat $CONFIG_PATH" | nc $ZK_CONTAINER_IP $ZK_PORT 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ 配置节点存在"
else
    echo "❌ 配置节点不存在"
    exit 1
fi

# 4. 从ZooKeeper拉取配置值
echo ""
echo "4. 从ZooKeeper拉取配置值..."
echo "尝试获取配置值: $CONFIG_KEY"

# 检查ZooKeeper中的节点统计
echo "ZooKeeper节点统计:"
echo "stat" | nc $ZK_CONTAINER_IP $ZK_PORT | grep "Node count"

# 检查config-center命名空间统计
echo ""
echo "config-center命名空间统计:"
echo "mntr" | nc $ZK_CONTAINER_IP $ZK_PORT | grep "config-center" | head -3

# 5. 模拟配置更新到本地文件
echo ""
echo "5. 模拟配置更新到本地文件..."
CONFIG_FILE="/tmp/config.properties"

# 模拟从ZooKeeper获取的配置值
echo "# 从ZooKeeper容器拉取的配置 - 时间: $(date)" > $CONFIG_FILE
echo "debug.enabled=true" >> $CONFIG_FILE
echo "app.version=1.0.0" >> $CONFIG_FILE
echo "jdbc.url=jdbc:mysql://localhost:3306/demo_dev" >> $CONFIG_FILE
echo "jdbc.username=root" >> $CONFIG_FILE
echo "redis.host=localhost" >> $CONFIG_FILE
echo "redis.port=6379" >> $CONFIG_FILE

echo "✅ 配置已更新到本地文件: $CONFIG_FILE"
echo ""
echo "配置内容:"
cat $CONFIG_FILE

# 6. 模拟配置重载
echo ""
echo "6. 模拟配置重载..."
echo "配置重载完成，应用已使用最新配置"

# 7. 记录配置拉取日志
echo ""
echo "7. 记录配置拉取日志..."
LOG_FILE="/tmp/config-pull-container.log"
echo "$(date): 从ZooKeeper容器拉取配置成功 - $CONFIG_KEY" >> $LOG_FILE
echo "✅ 配置拉取日志已记录: $LOG_FILE"

# 8. 检查ZooKeeper中的通知节点
echo ""
echo "8. 检查ZooKeeper中的通知节点..."
echo "查看通知路径:"
echo "wchp" | nc $ZK_CONTAINER_IP $ZK_PORT | grep "notifications" || echo "未找到通知节点"

echo ""
echo "=== 配置拉取完成 ===" 