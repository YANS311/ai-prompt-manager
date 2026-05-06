#!/bin/bash

# 诊断脚本 - 检查应用状态和常见问题

echo "=========================================="
echo "🔍 AI Prompt Manager - 诊断工具"
echo "=========================================="
echo ""

# 1. 检查应用是否运行
echo "1️⃣ 检查应用进程..."
if pgrep -f "PromptManagerApplication" > /dev/null; then
    echo "✅ 应用正在运行"
    ps aux | grep "PromptManagerApplication" | grep -v grep
else
    echo "❌ 应用未运行"
fi
echo ""

# 2. 检查端口
echo "2️⃣ 检查端口占用..."
if lsof -Pi :8080 -sTCP:LISTEN -t &> /dev/null; then
    echo "✅ 端口 8080 正在监听"
    lsof -Pi :8080 -sTCP:LISTEN
else
    echo "❌ 端口 8080 未监听"
fi
echo ""

# 3. 测试主要接口
echo "3️⃣ 测试 API 接口..."
echo "测试 GET /api/prompts:"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:8080/api/prompts || echo "❌ 无法连接"
echo ""

echo "测试 Swagger UI:"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:8080/swagger-ui.html || echo "❌ 无法连接"
echo ""

# 4. 检查数据库连接
echo "4️⃣ 测试 H2 Console:"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:8080/h2-console || echo "❌ 无法连接"
echo ""

# 5. 查看最近的日志（如果存在）
echo "5️⃣ 最近的应用日志（如果有）:"
if [ -d "logs" ]; then
    tail -20 logs/*.log 2>/dev/null || echo "无日志文件"
else
    echo "无 logs 目录，检查控制台输出"
fi
echo ""

# 6. 尝试创建一条测试数据
echo "6️⃣ 尝试创建测试 Prompt:"
RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST http://localhost:8080/api/prompts \
  -H "Content-Type: application/json" \
  -d '{"title":"诊断测试","content":"这是一个测试","category":"Test"}')

HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE" | cut -d':' -f2)
BODY=$(echo "$RESPONSE" | grep -v "HTTP_CODE")

if [ "$HTTP_CODE" = "201" ] || [ "$HTTP_CODE" = "200" ]; then
    echo "✅ 创建成功 (HTTP $HTTP_CODE)"
    echo "$BODY"
else
    echo "❌ 创建失败 (HTTP $HTTP_CODE)"
    echo "$BODY"
fi
echo ""

# 7. 常见错误诊断
echo "7️⃣ 常见问题检查:"
echo "   - Java 版本: $(java -version 2>&1 | head -n 1)"
echo "   - Maven 版本: $(mvn -version 2>&1 | head -n 1)"
echo "   - 配置文件: $([ -f "src/main/resources/application.properties" ] && echo "✅ 存在" || echo "❌ 缺失")"
echo "   - pom.xml: $([ -f "pom.xml" ] && echo "✅ 存在" || echo "❌ 缺失")"
echo ""

echo "=========================================="
echo "诊断完成！"
echo ""
echo "如果看到错误，请将完整输出发送给开发者"
echo "=========================================="
