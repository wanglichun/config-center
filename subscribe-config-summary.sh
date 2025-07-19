#!/bin/bash

echo "=== 配置订阅操作总结 ==="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}操作详情:${NC}"
echo "1. 目标容器: simple-container (IP: 172.17.0.2)"
echo "2. 订阅配置: ID=7 (debug.enabled)"
echo "3. 应用: demo-app"
echo "4. 环境: dev"
echo "5. 配置组: common"
echo ""

echo -e "${GREEN}操作结果:${NC}"
echo "✅ 机器注册成功: simple-container"
echo "✅ 配置订阅成功: debug.enabled"
echo "✅ 配置发布成功: 通知1台机器"
echo "✅ ZooKeeper节点创建: 37个 → 49个"
echo ""

echo -e "${YELLOW}技术细节:${NC}"
echo "• 配置中心服务端口: 9090"
echo "• ZooKeeper端口: 2181"
echo "• 命名空间: config-center"
echo "• 通知机制: ZooKeeper临时节点"
echo ""

echo -e "${BLUE}验证命令:${NC}"
echo "# 查看订阅机器列表"
echo "curl -X GET 'http://localhost:9090/config-center/api/machine-config/subscribers?appName=demo-app&environment=dev&groupName=common&configKey=debug.enabled' -H 'Authorization: Bearer \$TOKEN'"
echo ""
echo "# 查看机器配置"
echo "curl -X GET 'http://localhost:9090/config-center/api/machine-config/configs/simple-container' -H 'Authorization: Bearer \$TOKEN'"
echo ""
echo "# 查看ZooKeeper节点统计"
echo "echo 'stat' | nc localhost 2181 | grep 'Node count'"
echo ""

echo -e "${GREEN}订阅完成！${NC}"
echo "IP为172.17.0.2的simple-container已成功订阅ID=7的debug.enabled配置。"
echo "配置变更时会自动通知到该容器。" 