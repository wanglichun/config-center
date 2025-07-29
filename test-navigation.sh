#!/bin/bash

echo "=== 测试路由跳转 ==="

echo "1. 检查前端服务..."
if curl -s http://localhost:3002 > /dev/null 2>&1; then
  echo "✅ 前端服务运行正常 (端口: 3002)"
else
  echo "❌ 前端服务未运行"
  exit 1
fi

echo ""
echo "2. 测试路由..."
echo "请在浏览器中测试以下路由："
echo "- 配置列表: http://localhost:3002/config"
echo "- 配置详情: http://localhost:3002/config/detail/1"
echo "- 配置编辑: http://localhost:3002/config/edit/1"
echo "- Ticket列表: http://localhost:3002/ticket"
echo "- Ticket详情: http://localhost:3002/ticket/detail/12"

echo ""
echo "3. 调试步骤："
echo "1. 打开浏览器开发者工具 (F12)"
echo "2. 访问配置编辑页面"
echo "3. 修改配置值并提交"
echo "4. 查看控制台输出的调试信息"
echo "5. 检查是否有路由跳转"

echo ""
echo "4. 预期控制台输出："
echo "Submitting config edit for ID: 1"
echo "Form data: {...}"
echo "API response: {...}"
echo "Response success: true"
echo "Response data: {id: 12, ...}"
echo "Ticket received: {id: 12, ...}"
echo "Ticket ID: 12"
echo "Navigating to: /ticket/detail/12"
echo "Navigation completed"

echo ""
echo "=== 测试完成 ===" 