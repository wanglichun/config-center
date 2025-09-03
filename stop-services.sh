#!/bin/bash

# 配置中心服务停止脚本
echo "正在停止配置中心相关服务..."

# 停止所有服务
docker stop config-center-kibana config-center-elasticsearch config-center-kafka config-center-zookeeper config-center-redis config-center-mysql 2>/dev/null

# 删除所有容器
docker rm config-center-kibana config-center-elasticsearch config-center-kafka config-center-zookeeper config-center-redis config-center-mysql 2>/dev/null

# 删除网络
docker network rm config-center-network 2>/dev/null

echo "所有服务已停止并清理完成！"
