#!/bin/bash

# 使用API的配置监听脚本
INSTANCE_ID="simple-container"
API_BASE="http://config-center:9090/config-center/api"
TOKEN="eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJlYWxOYW1lIjoi57O757uf566h55CG5ZGYIiwicm9sZSI6IkFETUlOIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTc1MjkzMjg3NiwiZXhwIjoxNzUzMDE5Mjc2LCJpc3MiOiJjb25maWctY2VudGVyIn0.5PxhOcPJPq_aMIReu-QA14r0Z_GAm7nCRdWsoV_qyiovLyboQZNxCY1tY5-4KMROtq3gU0uuUg1ZFuHq4I3LXQ"

echo "开始监听配置变更通知..."

while true; do
    # 检查是否有针对本实例的通知
    NOTIFICATIONS=$(curl -s -H "Authorization: Bearer $TOKEN" "$API_BASE/machine-config/notifications/$INSTANCE_ID" | jq -r '.data[]?.configKey // empty' 2>/dev/null)
    
    if [ -n "$NOTIFICATIONS" ]; then
        for config_key in $NOTIFICATIONS; do
            echo "收到配置变更通知: $config_key"
            
            # 从API获取最新配置
            CONFIG_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" "$API_BASE/config/query?configKey=$config_key&appName=demo-app&env=dev&namespace=common")
            CONFIG_VALUE=$(echo "$CONFIG_RESPONSE" | jq -r '.data[0].configValue // empty' 2>/dev/null)
            
            if [ -n "$CONFIG_VALUE" ]; then
                echo "获取到配置值: $CONFIG_VALUE"
                echo "$CONFIG_VALUE" > "/tmp/$config_key"
                echo "配置已保存到: /tmp/$config_key"
            fi
            
            # 删除通知
            curl -s -X DELETE -H "Authorization: Bearer $TOKEN" "$API_BASE/machine-config/notifications/$INSTANCE_ID/$config_key" > /dev/null
            echo "已处理通知并删除通知节点"
        done
    fi
    
    sleep 3
done 