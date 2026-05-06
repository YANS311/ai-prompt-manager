#!/bin/bash

echo "=========================================="
echo "🧪 测试 AI Prompt Manager API"
echo "=========================================="
echo ""

# 1. 创建第一个 Prompt
echo "1️⃣ 创建 Prompt - Claude Code Agent"
curl -X POST http://localhost:8080/api/prompts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Claude Code Agent - 完整开发流程",
    "content": "You are an expert software engineer. When implementing features:\n1. Read existing code first\n2. Follow project conventions\n3. Write clean, tested code",
    "category": "Coding"
  }'
echo -e "\n"

# 2. 创建第二个 Prompt
echo "2️⃣ 创建 Prompt - SQL 优化专家"
curl -X POST http://localhost:8080/api/prompts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "PostgreSQL 性能调优助手",
    "content": "Analyze SQL queries and suggest optimizations",
    "category": "Database"
  }'
echo -e "\n"

# 3. 创建第三个 Prompt
echo "3️⃣ 创建 Prompt - API 文档生成器"
curl -X POST http://localhost:8080/api/prompts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring Boot REST API 文档生成",
    "content": "Generate comprehensive API documentation",
    "category": "Documentation"
  }'
echo -e "\n"

# 4. 查询所有 Prompts
echo "4️⃣ 查询所有 Prompts"
curl -s http://localhost:8080/api/prompts | python3 -m json.tool
echo ""

# 5. 按分类查询
echo "5️⃣ 按分类查询 - Coding"
curl -s "http://localhost:8080/api/prompts?category=Coding" | python3 -m json.tool
echo ""

# 6. 搜索
echo "6️⃣ 搜索关键词 'Claude'"
curl -s "http://localhost:8080/api/prompts/search?keyword=Claude" | python3 -m json.tool
echo ""

# 7. 更新 Prompt
echo "7️⃣ 更新 Prompt (ID=1)"
curl -X PUT http://localhost:8080/api/prompts/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Claude Code Agent - 增强版",
    "content": "Enhanced version with more features",
    "category": "Coding"
  }'
echo -e "\n"

# 8. 查询单个
echo "8️⃣ 查询单个 Prompt (ID=1)"
curl -s http://localhost:8080/api/prompts/1 | python3 -m json.tool
echo ""

echo "=========================================="
echo "✅ 测试完成！"
echo ""
echo "你也可以访问 Swagger UI 进行交互式测试："
echo "http://localhost:8080/swagger-ui.html"
echo "=========================================="
