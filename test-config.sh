#!/bin/bash

echo "=== 测试配置获取 ==="

# 方法1：直接获取
echo "方法1 - 直接获取:"
docker exec zk1 zookeeper-shell localhost:2181 get /config-center/configs/demo-app/dev/redis/host

echo -e "\n方法2 - 过滤输出:"
result=$(docker exec zk1 zookeeper-shell localhost:2181 get /config-center/configs/demo-app/dev/redis/host 2>/dev/null)
echo "原始结果:"
echo "$result"

echo -e "\n过滤后的结果:"
config_value=$(echo "$result" | grep -v "Connecting to localhost" | grep -v "WATCHER" | grep -v "WatchedEvent" | tail -1)
echo "配置值: '$config_value'"

echo -e "\n方法3 - 使用sed:"
config_value2=$(docker exec zk1 zookeeper-shell localhost:2181 get /config-center/configs/demo-app/dev/redis/host 2>/dev/null | sed -n '$p')
echo "配置值: '$config_value2'" 