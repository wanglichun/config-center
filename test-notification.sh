#!/bin/bash

echo "=== 测试配置通知机制 ==="

# 1. 检查订阅机器
echo "1. 检查订阅机器列表:"
curl -s -X GET "http://localhost:9090/config-center/api/machine-config/subscribers?appName=demo-app&environment=dev&groupName=redis&configKey=port" -H "Authorization: Bearer $(cat /tmp/jwt_token)" | jq

# 2. 检查当前配置值
echo "2. 检查当前配置值:"
CONFIG_VALUE=$(curl -s -X GET "http://localhost:9090/config-center/api/config?appName=demo-app&environment=dev&groupName=redis&configKey=port" -H "Authorization: Bearer $(cat /tmp/jwt_token)" | jq -r '.data.configValue')
echo "当前配置值: $CONFIG_VALUE"

# 3. 手动创建通知节点测试
echo "3. 手动创建通知节点测试:"
NOTIFICATION_PATH="/config-center/notifications/demo-app/dev/redis/port/machine-001"
NOTIFICATION_DATA="{\"configKey\":\"port\",\"newValue\":\"$CONFIG_VALUE\",\"timestamp\":$(date +%s)000}"

echo "通知路径: $NOTIFICATION_PATH"
echo "通知数据: $NOTIFICATION_DATA"

# 4. 检查机器001的监听脚本
echo "4. 检查机器001监听脚本状态:"
docker exec machine-001 ps aux | grep listener

# 5. 检查机器001的配置
echo "5. 检查机器001当前配置:"
docker exec machine-001 cat /tmp/machine-001-config/port.properties 2>/dev/null || echo "配置文件不存在"

echo "=== 测试完成 ===" 