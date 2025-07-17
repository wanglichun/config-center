#!/bin/bash

echo "🔍 检查 ZooKeeper 中的配置..."

# 获取认证token
TOKEN=$(curl -s -X POST "http://localhost:9090/config-center/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.data.token')

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
    echo "❌ 登录失败"
    exit 1
fi

echo "✅ 登录成功"

# 获取已发布的配置
echo ""
echo "📋 已发布的配置列表："
curl -s -X GET "http://localhost:9090/config-center/api/config/page?page=1&size=50" \
  -H "Authorization: Bearer $TOKEN" | jq -r '.data.records[] | select(.lastPublishTime != null) | "\(.configKey) -> \(.zkPath) (发布时间: \(.lastPublishTime))"'

echo ""
echo "🔍 尝试通过 ZooKeeper CLI 查看配置..."

# 尝试查看 ZooKeeper 中的配置
CONFIG_PATHS=(
    "/configs/demo-app/dev/common/debug.enabled"
    "/configs/demo-app/dev/database/jdbc.url"
    "/configs/demo-app/dev/database/jdbc.password"
)

for path in "${CONFIG_PATHS[@]}"; do
    echo "检查路径: $path"
    docker exec zk1 sh -c "echo 'get $path' | nc -w 3 localhost 2181" 2>/dev/null || echo "无法访问该路径"
    echo "---"
done

echo ""
echo "📊 ZooKeeper 服务状态："
docker exec zk1 echo srvr | nc localhost 2181

echo ""
echo "✅ 检查完成！"
echo ""
echo "💡 提示："
echo "1. 如果 ZooKeeper CLI 无法访问，可以通过后端 API 验证配置发布状态"
echo "2. 配置发布成功会更新 lastPublishTime 和 publisher 字段"
echo "3. 可以通过 ZooKeeper 可视化工具（如 ZooInspector）来查看配置" 