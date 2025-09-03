#!/bin/bash

# 配置中心服务启动脚本
echo "正在启动配置中心相关服务..."

# 创建Docker网络
echo "创建Docker网络..."
docker network create config-center-network 2>/dev/null || echo "网络已存在"

# 启动MySQL
echo "启动MySQL..."
docker run -d \
  --name config-center-mysql \
  --network config-center-network \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=config_center \
  -e MYSQL_USER=config_user \
  -e MYSQL_PASSWORD=config123 \
  -v mysql_data:/var/lib/mysql \
  -v $(pwd)/src/main/resources/sql/init.sql:/docker-entrypoint-initdb.d/init.sql:ro \
  --restart unless-stopped \
  mysql:8.0 \
  --default-authentication-plugin=mysql_native_password

# 启动Redis
echo "启动Redis..."
docker run -d \
  --name config-center-redis \
  --network config-center-network \
  -p 6379:6379 \
  -v redis_data:/data \
  --restart unless-stopped \
  redis:7-alpine \
  redis-server --appendonly yes

# 启动ZooKeeper
echo "启动ZooKeeper..."
docker run -d \
  --name config-center-zookeeper \
  --network config-center-network \
  -p 2181:2181 \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  -e ZOOKEEPER_TICK_TIME=2000 \
  -v zookeeper_data:/var/lib/zookeeper/data \
  -v zookeeper_logs:/var/lib/zookeeper/log \
  --restart unless-stopped \
  confluentinc/cp-zookeeper:7.4.0

# 等待ZooKeeper启动
echo "等待ZooKeeper启动..."
sleep 15

# 启动Kafka
echo "启动Kafka..."
docker run -d \
  --name config-center-kafka \
  --network config-center-network \
  -p 9092:9092 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=config-center-zookeeper:2181 \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://config-center-kafka:29092,PLAINTEXT_HOST://localhost:9092 \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
  -e KAFKA_AUTO_CREATE_TOPICS_ENABLE=true \
  -v kafka_data:/var/lib/kafka/data \
  --restart unless-stopped \
  confluentinc/cp-kafka:7.4.0

# 启动Elasticsearch
echo "启动Elasticsearch..."
docker run -d \
  --name config-center-elasticsearch \
  --network config-center-network \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -v elasticsearch_data:/usr/share/elasticsearch/data \
  --restart unless-stopped \
  docker.elastic.co/elasticsearch/elasticsearch:7.17.4

# 等待Elasticsearch启动
echo "等待Elasticsearch启动..."
sleep 30

# 启动Kibana（可选）
echo "启动Kibana..."
docker run -d \
  --name config-center-kibana \
  --network config-center-network \
  -p 5601:5601 \
  -e ELASTICSEARCH_HOSTS=http://config-center-elasticsearch:9200 \
  --restart unless-stopped \
  docker.elastic.co/kibana/kibana:7.17.4

echo "所有服务启动完成！"
echo ""
echo "服务端口："
echo "MySQL: localhost:3306"
echo "Redis: localhost:6379"
echo "ZooKeeper: localhost:2181"
echo "Kafka: localhost:9092"
echo "Elasticsearch: localhost:9200"
echo "Kibana: localhost:5601"
echo ""
echo "使用 './stop-services.sh' 停止所有服务"
