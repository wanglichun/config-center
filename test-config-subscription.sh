#!/bin/bash

# 简单的配置订阅测试脚本

BASE_URL="http://localhost:9090/config-center"
APP_NAME="demo-app"
ENVIRONMENT="dev"
GROUP_NAME="redis"
CONFIG_KEY="host"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== 配置订阅测试 ===${NC}"

# 1. 登录获取token
echo -e "${YELLOW}1. 登录获取token...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123")

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}登录失败，无法获取token${NC}"
    echo "响应: $LOGIN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}登录成功，获取到token${NC}"

# 2. 创建Redis主机配置
echo -e "${YELLOW}2. 创建Redis主机配置...${NC}"
CREATE_CONFIG_RESPONSE=$(curl -s -X POST "$BASE_URL/api/config" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"appName\": \"$APP_NAME\",
    \"environment\": \"$ENVIRONMENT\",
    \"groupName\": \"$GROUP_NAME\",
    \"configKey\": \"$CONFIG_KEY\",
    \"configValue\": \"redis-server-01\",
    \"dataType\": \"STRING\",
    \"description\": \"Redis主机地址\"
  }")

echo "创建配置响应: $CREATE_CONFIG_RESPONSE"

# 3. 发布配置
echo -e "${YELLOW}3. 发布配置...${NC}"

# 获取配置ID
CONFIG_LIST_RESPONSE=$(curl -s -X GET "$BASE_URL/api/config/page?appName=$APP_NAME&environment=$ENVIRONMENT&pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN")

CONFIG_ID=$(echo $CONFIG_LIST_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$CONFIG_ID" ]; then
    echo -e "${BLUE}发布配置ID: $CONFIG_ID${NC}"
    
    PUBLISH_RESPONSE=$(curl -s -X POST "$BASE_URL/api/config/$CONFIG_ID/publish" \
      -H "Authorization: Bearer $TOKEN")
    
    echo "发布响应: $PUBLISH_RESPONSE"
else
    echo -e "${RED}未找到配置ID${NC}"
fi

# 4. 查看ZooKeeper中的配置
echo -e "${YELLOW}4. 查看ZooKeeper中的配置...${NC}"
echo -e "${BLUE}配置路径: /config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY${NC}"

# 使用zookeeper-shell查看配置
docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY

# 5. 模拟配置变更
echo -e "${YELLOW}5. 模拟配置变更...${NC}"
echo -e "${BLUE}更新配置值为: redis-server-02${NC}"

UPDATE_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/config/$CONFIG_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"appName\": \"$APP_NAME\",
    \"environment\": \"$ENVIRONMENT\",
    \"groupName\": \"$GROUP_NAME\",
    \"configKey\": \"$CONFIG_KEY\",
    \"configValue\": \"redis-server-02\",
    \"dataType\": \"STRING\",
    \"description\": \"Redis主机地址\"
  }")

echo "更新响应: $UPDATE_RESPONSE"

# 再次发布配置
PUBLISH_RESPONSE=$(curl -s -X POST "$BASE_URL/api/config/$CONFIG_ID/publish" \
  -H "Authorization: Bearer $TOKEN")

echo "发布响应: $PUBLISH_RESPONSE"

# 6. 再次查看ZooKeeper中的配置
echo -e "${YELLOW}6. 再次查看ZooKeeper中的配置...${NC}"
docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY

echo -e "${GREEN}=== 测试完成 ===${NC}"
echo -e "${BLUE}配置订阅客户端应该已经检测到配置变更${NC}"
echo -e "${BLUE}可以通过查看应用日志来确认配置变更是否被正确处理${NC}" 