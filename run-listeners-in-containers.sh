#!/bin/bash

echo "=== 在三台容器中运行监听器 ==="

# 检查容器状态
echo "检查容器状态..."
docker ps | grep "machine-"

# 复制监听脚本到容器
echo ""
echo "复制监听脚本到容器..."

# 复制到machine-001
echo "复制到machine-001..."
docker cp ConfigCenterPushListenerEn-fixed-v2.java machine-001:/app/
docker cp apache-zookeeper-3.7.1-bin/ machine-001:/app/

# 复制到machine-002  
echo "复制到machine-002..."
docker cp ConfigCenterPushListenerEn-fixed-v2.java machine-002:/app/
docker cp apache-zookeeper-3.7.1-bin/ machine-002:/app/

# 复制到machine-003
echo "复制到machine-003..."
docker cp ConfigCenterPushListenerEn-fixed-v2.java machine-003:/app/
docker cp apache-zookeeper-3.7.1-bin/ machine-003:/app/

echo ""
echo "=== 在容器中编译和运行监听器 ==="

# 在machine-001中运行
echo "在machine-001中运行监听器..."
docker exec -d machine-001 bash -c 'cd /app && javac -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2.java && java -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2'

# 在machine-002中运行
echo "在machine-002中运行监听器..."
docker exec -d machine-002 bash -c 'cd /app && javac -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2.java && java -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2'

# 在machine-003中运行
echo "在machine-003中运行监听器..."
docker exec -d machine-003 bash -c 'cd /app && javac -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2.java && java -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2'

echo ""
echo "=== 检查监听器运行状态 ==="
sleep 3

echo "检查machine-001进程..."
docker exec machine-001 ps aux | grep java

echo "检查machine-002进程..."
docker exec machine-002 ps aux | grep java

echo "检查machine-003进程..."
docker exec machine-003 ps aux | grep java

echo ""
echo "=== 查看容器日志 ==="
echo "查看machine-001日志: docker logs machine-001"
echo "查看machine-002日志: docker logs machine-002"
echo "查看machine-003日志: docker logs machine-003"

echo ""
echo "=== 手动运行监听器命令 ==="
echo "在machine-001中手动运行:"
echo "docker exec -it machine-001 bash"
echo "cd /app"
echo "export INSTANCE_ID=machine-001"
echo "java -cp \".:apache-zookeeper-3.7.1-bin/lib/*\" ConfigCenterPushListenerEn-fixed-v2"

echo ""
echo "在machine-002中手动运行:"
echo "docker exec -it machine-002 bash"
echo "cd /app"
echo "export INSTANCE_ID=machine-002"
echo "java -cp \".:apache-zookeeper-3.7.1-bin/lib/*\" ConfigCenterPushListenerEn-fixed-v2"

echo ""
echo "在machine-003中手动运行:"
echo "docker exec -it machine-003 bash"
echo "cd /app"
echo "export INSTANCE_ID=machine-003"
echo "java -cp \".:apache-zookeeper-3.7.1-bin/lib/*\" ConfigCenterPushListenerEn-fixed-v2" 