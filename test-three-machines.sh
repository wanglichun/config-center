#!/bin/bash

# 测试三台机器配置订阅脚本
# 模拟三台机器订阅 /config-center/configs/demo-app/dev/redis/host 配置

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

echo -e "${BLUE}=== 三台机器配置订阅测试 ===${NC}"

# 1. 登录获取token
echo -e "${YELLOW}1. 登录获取token...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123")

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJlYWxOYW1lIjoi57O757uf566h55CG5ZGYIiwicm9sZSI6IkFETUlOIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTc1Mjc0NTgwMiwiZXhwIjoxNzUyODMyMjAyLCJpc3MiOiJjb25maWctY2VudGVyIn0.bUYVLD_Rj4QOTwh9Iy6E_IcttNqbsS7sfLXiJw_uB0fNeYyZRPK4_F0yExCG2MNQePUaRP_nXISdYP75Jf0mu" ]; then
    echo -e "${RED}登录失败，无法获取token${NC}"
    echo "响应: $LOGIN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}登录成功，获取到token${NC}"

# 2. 创建Redis主机配置
echo -e "${YELLOW}2. 创建Redis主机配置...${NC}"
CREATE_CONFIG_RESPONSE=$(curl -s -X POST "$BASE_URL/api/config" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJlYWxOYW1lIjoi57O757uf566h55CG5ZGYIiwicm9sZSI6IkFETUlOIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTc1Mjc0NTgwMiwiZXhwIjoxNzUyODMyMjAyLCJpc3MiOiJjb25maWctY2VudGVyIn0.bUYVLD_Rj4QOTwh9Iy6E_IcttNqbsS7sfLXiJw_uB0fNeYyZRPK4_F0yExCG2MNQePUaRP_nXISdYP75Jf0mug" \
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

# 3. 注册三台机器
echo -e "${YELLOW}3. 注册三台机器...${NC}"

for i in {1..3}; do
    MACHINE_ID="machine-00$i"
    INSTANCE_IP="192.168.1.$i"
    
    echo -e "${BLUE}注册机器: $MACHINE_ID (IP: $INSTANCE_IP)${NC}"
    
    REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/machine-config/register" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -H "Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJlYWxOYW1lIjoi57O757uf566h55CG5ZGYIiwicm9sZSI6IkFETUlOIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTc1Mjc0NTgwMiwiZXhwIjoxNzUyODMyMjAyLCJpc3MiOiJjb25maWctY2VudGVyIn0.bUYVLD_Rj4QOTwh9Iy6E_IcttNqbsS7sfLXiJw_uB0fNeYyZRPK4_F0yExCG2MNQePUaRP_nXISdYP75Jf0mug
  " \
      -d "appName=$APP_NAME&environment=$ENVIRONMENT&groupName=$GROUP_NAME&instanceId=$MACHINE_ID&instanceIp=$INSTANCE_IP&configKeys=$CONFIG_KEY")
    
    echo "注册响应: $REGISTER_RESPONSE"
done

# 4. 查看订阅机器列表
echo -e "${YELLOW}4. 查看订阅机器列表...${NC}"
SUBSCRIBERS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/machine-config/subscribers?appName=$APP_NAME&environment=$ENVIRONMENT&groupName=$GROUP_NAME&configKey=$CONFIG_KEY" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJlYWxOYW1lIjoi57O757uf566h55CG5ZGYIiwicm9sZSI6IkFETUlOIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTc1Mjc0NTgwMiwiZXhwIjoxNzUyODMyMjAyLCJpc3MiOiJjb25maWctY2VudGVyIn0.bUYVLD_Rj4QOTwh9Iy6E_IcttNqbsS7sfLXiJw_uB0fNeYyZRPK4_F0yExCG2MNQePUaRP_nXISdYP75Jf0mug")

echo "订阅机器列表: $SUBSCRIBERS_RESPONSE"

# 5. 发布配置
echo -e "${YELLOW}5. 发布配置...${NC}"

# 获取配置ID
CONFIG_LIST_RESPONSE=$(curl -s -X GET "$BASE_URL/api/config/page?appName=$APP_NAME&environment=$ENVIRONMENT&pageNum=1&pageSize=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJlYWxOYW1lIjoi57O757uf566h55CG5ZGYIiwicm9sZSI6IkFETUlOIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTc1Mjc0NTgwMiwiZXhwIjoxNzUyODMyMjAyLCJpc3MiOiJjb25maWctY2VudGVyIn0.bUYVLD_Rj4QOTwh9Iy6E_IcttNqbsS7sfLXiJw_uB0fNeYyZRPK4_F0yExCG2MNQePUaRP_nXISdYP75Jf0mug")

CONFIG_ID=$(echo $CONFIG_LIST_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$CONFIG_ID" ]; then
    echo -e "${BLUE}发布配置ID: $CONFIG_ID${NC}"
    
    PUBLISH_RESPONSE=$(curl -s -X POST "$BASE_URL/api/config/$CONFIG_ID/publish" \
      -H "Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJlYWxOYW1lIjoi57O757uf566h55CG5ZGYIiwicm9sZSI6IkFETUlOIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTc1Mjc0NTgwMiwiZXhwIjoxNzUyODMyMjAyLCJpc3MiOiJjb25maWctY2VudGVyIn0.bUYVLD_Rj4QOTwh9Iy6E_IcttNqbsS7sfLXiJw_uB0fNeYyZRPK4_F0yExCG2MNQePUaRP_nXISdYP75Jf0mug")
    
    echo "发布响应: $PUBLISH_RESPONSE"
else
    echo -e "${RED}未找到配置ID${NC}"
fi

# 6. 查看ZooKeeper中的配置
echo -e "${YELLOW}6. 查看ZooKeeper中的配置...${NC}"
echo -e "${BLUE}配置路径: /config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY${NC}"

# 使用zookeeper-shell查看配置
docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/configs/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY

# 7. 查看机器实例
echo -e "${YELLOW}7. 查看机器实例...${NC}"
for i in {1..3}; do
    MACHINE_ID="machine-00$i"
    echo -e "${BLUE}机器实例: $MACHINE_ID${NC}"
    docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/instances/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$MACHINE_ID
done

# 8. 模拟配置变更
echo -e "${YELLOW}8. 模拟配置变更...${NC}"
echo -e "${BLUE}更新配置值为: redis-server-02${NC}"

UPDATE_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/config/$CONFIG_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJlYWxOYW1lIjoi57O757uf566h55CG5ZGYIiwicm9sZSI6IkFETUlOIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTc1Mjc0NTgwMiwiZXhwIjoxNzUyODMyMjAyLCJpc3MiOiJjb25maWctY2VudGVyIn0.bUYVLD_Rj4QOTwh9Iy6E_IcttNqbsS7sfLXiJw_uB0fNeYyZRPK4_F0yExCG2MNQePUaRP_nXISdYP75Jf0mug" \
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
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJlYWxOYW1lIjoi57O757uf566h55CG5ZGYIiwicm9sZSI6IkFETUlOIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTc1Mjc0NTgwMiwiZXhwIjoxNzUyODMyMjAyLCJpc3MiOiJjb25maWctY2VudGVyIn0.bUYVLD_Rj4QOTwh9Iy6E_IcttNqbsS7sfLXiJw_uB0fNeYyZRPK4_F0yExCG2MNQePUaRP_nXISdYP75Jf0mug")

echo "发布响应: $PUBLISH_RESPONSE"

# 9. 查看通知节点
echo -e "${YELLOW}9. 查看通知节点...${NC}"
for i in {1..3}; do
    MACHINE_ID="machine-00$i"
    echo -e "${BLUE}通知节点: $MACHINE_ID${NC}"
    docker exec config-center-zookeeper zookeeper-shell localhost:2181 ls /config-center/notifications/$APP_NAME/$ENVIRONMENT/$GROUP_NAME/$CONFIG_KEY
done

# 10. 查看每台机器的配置
echo -e "${YELLOW}10. 查看每台机器的配置...${NC}"
for i in {1..3}; do
    MACHINE_ID="machine-00$i"
    echo -e "${BLUE}机器 $MACHINE_ID 的配置:${NC}"
    curl -s -X GET "$BASE_URL/api/machine-config/configs/$MACHINE_ID" \
      -H "Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJlYWxOYW1lIjoi57O757uf566h55CG5ZGYIiwicm9sZSI6IkFETUlOIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTc1Mjc0NTgwMiwiZXhwIjoxNzUyODMyMjAyLCJpc3MiOiJjb25maWctY2VudGVyIn0.bUYVLD_Rj4QOTwh9Iy6E_IcttNqbsS7sfLXiJw_uB0fNeYyZRPK4_F0yExCG2MNQePUaRP_nXISdYP75Jf0mug
  "
    echo ""
done

echo -e "${GREEN}=== 测试完成 ===${NC}"
echo -e "${BLUE}现在三台机器应该已经收到了配置变更通知${NC}"
echo -e "${BLUE}可以通过查看应用日志来确认配置变更是否被正确处理${NC}" 