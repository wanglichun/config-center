#!/bin/bash

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

echo -e "${BLUE}=== 查看三台机器配置信息 ===${NC}"

# 1. 登录获取token
echo -e "${YELLOW}1. 登录获取token...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}登录失败${NC}"
    exit 1
fi

echo -e "${GREEN}登录成功，获取到token${NC}"

# 2. 查看订阅机器列表
echo -e "${YELLOW}2. 查看订阅机器列表...${NC}"
SUBSCRIBERS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/machine-config/subscribers?appName=$APP_NAME&environment=$ENVIRONMENT&groupName=$GROUP_NAME&configKey=$CONFIG_KEY" \
  -H "Authorization: Bearer $TOKEN")

echo "订阅机器列表: $SUBSCRIBERS_RESPONSE"

# 3. 查看每台机器的配置
echo -e "${YELLOW}3. 查看每台机器的配置...${NC}"

for i in {1..3}; do
    MACHINE_ID="machine-00$i"
    echo -e "${BLUE}--- 机器 $MACHINE_ID ---${NC}"
    
    # 查看机器配置
    CONFIG_RESPONSE=$(curl -s -X GET "$BASE_URL/api/machine-config/configs/$MACHINE_ID" \
      -H "Authorization: Bearer $TOKEN")
    
    echo "配置信息: $CONFIG_RESPONSE"
    echo
done

# 4. 查看ZooKeeper中的机器实例
echo -e "${YELLOW}4. 查看ZooKeeper中的机器实例...${NC}"
echo -e "${BLUE}机器实例路径: /config-center/instances/$APP_NAME/$ENVIRONMENT/$GROUP_NAME${NC}"

# 使用zookeeper-shell查看
docker exec config-center-zookeeper zookeeper-shell localhost:2181 ls /config-center/instances/$APP_NAME/$ENVIRONMENT/$GROUP_NAME

# 5. 查看每台机器的详细信息
echo -e "${YELLOW}5. 查看每台机器的详细信息...${NC}"

for i in {1..3}; do
    MACHINE_ID="machine-00$i"
    echo -e "${BLUE}--- 机器 $MACHINE_ID 详细信息 ---${NC}"
    
    # 查看机器实例数据
    docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/instances/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$MACHINE_ID
    echo
done

# 6. 查看配置内容
echo -e "${YELLOW}6. 查看配置内容...${NC}"
echo -e "${BLUE}配置路径: /config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY${NC}"

docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY

echo -e "${GREEN}=== 查看完成 ===${NC}" 