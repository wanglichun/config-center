#!/bin/bash

echo "=== 启动三台机器的监听脚本 ==="

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "错误: Docker未运行，请先启动Docker"
    exit 1
fi

# 检查ZooKeeper是否运行
if ! docker ps | grep -q "config-center-zookeeper"; then
    echo "错误: ZooKeeper容器未运行，请先启动ZooKeeper"
    exit 1
fi

echo "✅ Docker和ZooKeeper运行正常"

# 编译监听器
echo "编译监听器..."
javac -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2.java

# 为每台机器创建启动脚本
create_machine_script() {
    local machine_id=$1
    local script_name="start-listener-${machine_id}.sh"
    
    cat > $script_name << EOF
#!/bin/bash

echo "=== 启动 ${machine_id} 监听器 ==="

# 设置环境变量
export INSTANCE_ID="${machine_id}"
export ZK_SERVER="config-center-zookeeper:2181"
export LOCAL_CONFIG_DIR="./configs"

# 编译监听器
javac -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2.java

# 运行监听器
echo "启动 ${machine_id} 监听器..."
java -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2
EOF

    chmod +x $script_name
    echo "✅ 创建启动脚本: $script_name"
}

# 创建各机器的启动脚本
create_machine_script "machine-001"
create_machine_script "machine-002" 
create_machine_script "machine-003"

echo ""
echo "=== 启动脚本已创建 ==="
echo "启动machine-001监听器: ./start-listener-machine-001.sh"
echo "启动machine-002监听器: ./start-listener-machine-002.sh"
echo "启动machine-003监听器: ./start-listener-machine-003.sh"
echo ""
echo "=== 在容器中运行监听器 ==="
echo "在machine-001容器中运行:"
echo "docker exec -it machine-001 bash -c 'cd /app && java -cp \".:apache-zookeeper-3.7.1-bin/lib/*\" ConfigCenterPushListenerEn-fixed-v2'"
echo ""
echo "在machine-002容器中运行:"
echo "docker exec -it machine-002 bash -c 'cd /app && java -cp \".:apache-zookeeper-3.7.1-bin/lib/*\" ConfigCenterPushListenerEn-fixed-v2'"
echo ""
echo "在machine-003容器中运行:"
echo "docker exec -it machine-003 bash -c 'cd /app && java -cp \".:apache-zookeeper-3.7.1-bin/lib/*\" ConfigCenterPushListenerEn-fixed-v2'"
echo ""
echo "=== 检查容器状态 ==="
docker ps | grep "machine-" 