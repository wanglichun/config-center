#!/bin/bash

# 容器内部运行的ZooKeeper配置拉取脚本
# 直接使用ZooKeeper客户端工具获取真实配置

echo "=== 容器内部ZooKeeper配置拉取脚本 ==="

# 配置参数
ZK_HOST="172.18.0.2"
ZK_PORT="2181"
CONFIG_PATH="/config-center/configs/demo-app/dev/common/debug.enabled"
CONFIG_FILE="/tmp/config.properties"

echo "ZooKeeper地址: $ZK_HOST:$ZK_PORT"
echo "配置路径: $CONFIG_PATH"

# 检查是否有ZooKeeper客户端工具
if ! command -v zkCli.sh &> /dev/null; then
    echo "❌ 容器中没有ZooKeeper客户端工具"
    echo "尝试安装ZooKeeper客户端..."
    
    # 尝试安装ZooKeeper客户端
    apk add --no-cache wget openjdk11-jre
    
    # 下载ZooKeeper客户端
    wget -O /tmp/zookeeper-client.tar.gz https://archive.apache.org/dist/zookeeper/zookeeper-3.8.1/apache-zookeeper-3.8.1-bin.tar.gz
    tar -xzf /tmp/zookeeper-client.tar.gz -C /tmp/
    export PATH=$PATH:/tmp/apache-zookeeper-3.8.1-bin/bin
fi

# 获取配置值
echo "正在从ZooKeeper获取配置..."

# 使用ZooKeeper客户端获取配置值
CONFIG_VALUE=$(zkCli.sh -server $ZK_HOST:$ZK_PORT get $CONFIG_PATH 2>/dev/null | grep -v "INFO\|WARN\|ERROR\|Connecting\|Client environment\|Session establishment\|WATCHER\|Exiting JVM" | tail -1 | tr -d '\r')

echo "获取到的配置值: '$CONFIG_VALUE'"

# 检查是否成功获取配置
if [ -z "$CONFIG_VALUE" ] || [ "$CONFIG_VALUE" = "null" ] || [[ "$CONFIG_VALUE" == *"INFO"* ]] || [[ "$CONFIG_VALUE" == *"ERROR"* ]]; then
    echo "❌ 无法从ZooKeeper获取配置值"
    echo "尝试使用nc命令..."
    
    # 尝试使用nc命令
    CONFIG_VALUE=$(echo "get $CONFIG_PATH" | nc $ZK_HOST $ZK_PORT 2>/dev/null | tail -1)
    echo "使用nc获取到的配置值: '$CONFIG_VALUE'"
    
    if [ -z "$CONFIG_VALUE" ] || [ "$CONFIG_VALUE" = "null" ]; then
        echo "❌ 所有方法都无法获取配置值"
        exit 1
    fi
fi

# 更新配置文件
echo "# 从ZooKeeper拉取的配置 - 更新时间: $(date)" > $CONFIG_FILE
echo "# 配置路径: $CONFIG_PATH" >> $CONFIG_FILE
echo "debug.enabled=$CONFIG_VALUE" >> $CONFIG_FILE
echo "app.version=1.0.0" >> $CONFIG_FILE
echo "jdbc.url=jdbc:mysql://localhost:3306/demo_dev" >> $CONFIG_FILE
echo "jdbc.username=root" >> $CONFIG_FILE
echo "redis.host=localhost" >> $CONFIG_FILE
echo "redis.port=6379" >> $CONFIG_FILE

echo "✅ 配置已更新到: $CONFIG_FILE"
echo "配置内容:"
cat $CONFIG_FILE

echo "=== 配置拉取完成 ===" 