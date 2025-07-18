#!/bin/bash

# 查询集群配置脚本

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:9090/config-center"
APP_NAME="demo-app"
ENVIRONMENT="dev"
GROUP_NAME="redis"
CONFIG_KEY="host"

echo -e "${BLUE}=== 查询集群配置 ===${NC}"

# 1. 登录获取token
echo -e "${YELLOW}1. 登录获取token...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123")

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}登录失败，请检查配置中心服务是否启动${NC}"
    echo "响应: $LOGIN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ 登录成功${NC}"

# 2. 查询订阅机器列表
echo -e "${YELLOW}2. 订阅机器列表:${NC}"
SUBSCRIBERS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/machine-config/subscribers?appName=$APP_NAME&environment=$ENVIRONMENT&groupName=$GROUP_NAME&configKey=$CONFIG_KEY" \
  -H "Authorization: Bearer $TOKEN")

echo "$SUBSCRIBERS_RESPONSE" | python3 -m json.tool

# 3. 查询每台机器的配置
echo -e "${YELLOW}3. 各机器配置信息:${NC}"
for i in {1..3}; do
    MACHINE_ID="machine-00$i"
    echo -e "${BLUE}--- 机器 $MACHINE_ID ---${NC}"
    
    CONFIG_RESPONSE=$(curl -s -X GET "$BASE_URL/api/machine-config/configs/$MACHINE_ID" \
      -H "Authorization: Bearer $TOKEN")
    
    echo "$CONFIG_RESPONSE" | python3 -m json.tool
    echo ""
done

# 4. 查询ZooKeeper中的配置
echo -e "${YELLOW}4. ZooKeeper中的配置:${NC}"
echo -e "${BLUE}配置路径: /config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY${NC}"
docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY

# 5. 查询机器实例信息
echo -e "${YELLOW}5. 机器实例信息:${NC}"
for i in {1..3}; do
    MACHINE_ID="machine-00$i"
    echo -e "${BLUE}--- 机器 $MACHINE_ID 实例信息 ---${NC}"
    docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/instances/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$MACHINE_ID
    echo ""
done

# 6. 查询通知节点
echo -e "${YELLOW}6. 通知节点:${NC}"
echo -e "${BLUE}通知路径: /config-center/notifications/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY${NC}"
NOTIFICATIONS=$(docker exec config-center-zookeeper zookeeper-shell localhost:2181 ls /config-center/notifications/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY 2>/dev/null)

if [ -n "$NOTIFICATIONS" ]; then
    echo "通知节点: $NOTIFICATIONS"
else
    echo "无通知节点（已处理完毕）"
fi

echo -e "${GREEN}=== 查询完成 ===${NC}" 