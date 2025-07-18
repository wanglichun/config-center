#!/bin/bash

# 快速启动三台机器集群脚本

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== 快速启动三台机器集群 ===${NC}"

# 1. 启动单机ZooKeeper
echo -e "${YELLOW}1. 启动单机ZooKeeper...${NC}"
docker-compose -f docker-compose-zk-single.yml up -d

# 等待ZooKeeper启动
sleep 5

# 检查ZooKeeper状态
if docker ps | grep -q config-center-zookeeper; then
    echo -e "${GREEN}✓ ZooKeeper启动成功${NC}"
else
    echo -e "${RED}✗ ZooKeeper启动失败${NC}"
    exit 1
fi

# 2. 启动配置中心服务
echo -e "${YELLOW}2. 启动配置中心服务...${NC}"
echo -e "${BLUE}请在另一个终端运行: mvn spring-boot:run${NC}"
echo -e "${BLUE}等待服务启动后，按任意键继续...${NC}"
read -n 1 -s

# 3. 登录获取token
echo -e "${YELLOW}3. 登录获取token...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:9090/config-center/api/auth/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123")

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ 登录失败，请检查配置中心服务是否启动${NC}"
    echo "响应: $LOGIN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ 登录成功，获取到token${NC}"

# 4. 注册三台机器
echo -e "${YELLOW}4. 注册三台机器...${NC}"

for i in {1..3}; do
    MACHINE_ID="machine-00$i"
    INSTANCE_IP="192.168.1.$i"
    
    echo -e "${BLUE}注册机器: $MACHINE_ID (IP: $INSTANCE_IP)${NC}"
    
    REGISTER_RESPONSE=$(curl -s -X POST "http://localhost:9090/config-center/api/machine-config/register" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -H "Authorization: Bearer $TOKEN" \
      -d "appName=demo-app&environment=dev&groupName=redis&instanceId=$MACHINE_ID&instanceIp=$INSTANCE_IP&configKeys=host")
    
    if echo "$REGISTER_RESPONSE" | grep -q '"success":true'; then
        echo -e "${GREEN}✓ 机器 $MACHINE_ID 注册成功${NC}"
    else
        echo -e "${RED}✗ 机器 $MACHINE_ID 注册失败${NC}"
        echo "响应: $REGISTER_RESPONSE"
    fi
done

# 5. 创建和发布配置
echo -e "${YELLOW}5. 创建和发布配置...${NC}"

# 创建配置
CREATE_RESPONSE=$(curl -s -X POST "http://localhost:9090/config-center/api/config" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "appName": "demo-app",
    "environment": "dev",
    "groupName": "redis",
    "configKey": "host",
    "configValue": "redis-server-01",
    "dataType": "STRING",
    "description": "Redis主机地址"
  }')

if echo "$CREATE_RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}✓ 配置创建成功${NC}"
else
    echo -e "${RED}✗ 配置创建失败${NC}"
    echo "响应: $CREATE_RESPONSE"
fi

# 获取配置ID并发布
CONFIG_ID=$(curl -s -X GET "http://localhost:9090/config-center/api/config/page?appName=demo-app&environment=dev&pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$CONFIG_ID" ]; then
    echo -e "${BLUE}发布配置ID: $CONFIG_ID${NC}"
    
    PUBLISH_RESPONSE=$(curl -s -X POST "http://localhost:9090/config-center/api/config/$CONFIG_ID/publish" \
      -H "Authorization: Bearer $TOKEN")
    
    if echo "$PUBLISH_RESPONSE" | grep -q '"success":true'; then
        echo -e "${GREEN}✓ 配置发布成功${NC}"
    else
        echo -e "${RED}✗ 配置发布失败${NC}"
        echo "响应: $PUBLISH_RESPONSE"
    fi
else
    echo -e "${RED}✗ 未找到配置ID${NC}"
fi

# 6. 显示集群状态
echo -e "${YELLOW}6. 集群状态信息...${NC}"

# 查看订阅机器列表
echo -e "${BLUE}订阅机器列表:${NC}"
curl -s -X GET "http://localhost:9090/config-center/api/machine-config/subscribers?appName=demo-app&environment=dev&groupName=redis&configKey=host" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

# 查看ZooKeeper中的配置
echo -e "${BLUE}ZooKeeper中的配置:${NC}"
docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/configs/demo-app/dev/redis/host

# 查看机器实例
echo -e "${BLUE}机器实例信息:${NC}"
for i in {1..3}; do
    MACHINE_ID="machine-00$i"
    echo -e "${BLUE}机器 $MACHINE_ID 的配置:${NC}"
    curl -s -X GET "http://localhost:9090/config-center/api/machine-config/configs/$MACHINE_ID" \
      -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
done

echo -e "${GREEN}=== 集群启动完成 ===${NC}"
echo -e "${BLUE}现在您可以:${NC}"
echo -e "${BLUE}1. 通过前端界面管理配置: http://localhost:3003${NC}"
echo -e "${BLUE}2. 使用API查询配置: curl -H 'Authorization: Bearer $TOKEN' http://localhost:9090/config-center/api/machine-config/configs/machine-001${NC}"
echo -e "${BLUE}3. 查看ZooKeeper数据: docker exec config-center-zookeeper zookeeper-shell localhost:2181 ls /config-center${NC}"
echo -e "${BLUE}4. 运行测试脚本: ./test-three-machines.sh${NC}" 