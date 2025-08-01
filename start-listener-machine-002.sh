#!/bin/bash

echo "=== 启动 machine-002 监听器 ==="

# 设置环境变量
export INSTANCE_ID="machine-002"
export ZK_SERVER="config-center-zookeeper:2181"
export LOCAL_CONFIG_DIR="./configs"

# 编译监听器
javac -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2.java

# 运行监听器
echo "启动 machine-002 监听器..."
java -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2
