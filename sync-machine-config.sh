#!/bin/bash

echo "=== 同步机器配置 ==="

# 获取ZooKeeper中的配置
echo "1. 获取ZooKeeper中的Redis端口配置..."

# 使用Docker容器内的zkCli.sh来获取配置
CONFIG_VALUE=$(docker exec machine-001 /usr/local/Cellar/zookeeper/3.9.3/libexec/bin/zkCli.sh -server localhost:2181 get "/config-center/configs/demo-app/dev/redis/port" 2>/dev/null | tail -1)

if [ -n "$CONFIG_VALUE" ] && [ "$CONFIG_VALUE" != "null" ]; then
    echo "✅ 获取到配置值: $CONFIG_VALUE"
    
    # 更新机器001的配置文件
    echo "2. 更新机器001的配置文件..."
    CONFIG_FILE="/tmp/machine-001-config/port.properties"
    docker exec machine-001 bash -c "mkdir -p /tmp/machine-001-config && echo 'port=$CONFIG_VALUE' > $CONFIG_FILE && echo '更新时间: $(date)' >> $CONFIG_FILE"
    
    echo "3. 显示更新后的配置:"
    docker exec machine-001 cat "$CONFIG_FILE"
    
    echo "4. 显示所有配置:"
    docker exec machine-001 ls -la /tmp/machine-001-config/
    echo "---"
    for file in $(docker exec machine-001 find /tmp/machine-001-config/ -name "*.properties"); do
        echo "文件: $file"
        docker exec machine-001 cat "$file"
        echo "---"
    done
    
else
    echo "❌ 无法获取配置值"
fi

echo "=== 同步完成 ===" 