#!/bin/bash

# 🚀 AI Prompt Manager - 快速启动脚本
# 适用于 macOS (M3 Pro)

set -e  # 遇到错误立即退出

echo "=========================================="
echo "🚀 AI Prompt Manager - 环境检查"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查 Java
echo -n "检查 Java 21... "
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 21 ]; then
        echo -e "${GREEN}✅ Java $JAVA_VERSION 已安装${NC}"
    else
        echo -e "${RED}❌ Java 版本过低 (需要 21+)${NC}"
        echo -e "${YELLOW}请运行: brew install openjdk@21${NC}"
        exit 1
    fi
else
    echo -e "${RED}❌ Java 未安装${NC}"
    echo -e "${YELLOW}请运行: brew install openjdk@21${NC}"
    exit 1
fi

# 检查 Maven
echo -n "检查 Maven... "
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | awk '{print $3}')
    echo -e "${GREEN}✅ Maven $MVN_VERSION 已安装${NC}"
else
    echo -e "${RED}❌ Maven 未安装${NC}"
    echo -e "${YELLOW}请运行: brew install maven${NC}"
    exit 1
fi

# 检查端口占用
echo -n "检查端口 8080... "
if lsof -Pi :8080 -sTCP:LISTEN -t &> /dev/null; then
    echo -e "${YELLOW}⚠️  端口 8080 被占用${NC}"
    echo -e "${YELLOW}将自动使用端口 8081${NC}"
    # 临时修改配置
    if [ -f "src/main/resources/application.properties" ]; then
        sed -i.bak 's/server.port=8080/server.port=8081/' src/main/resources/application.properties
        PORT=8081
    fi
else
    echo -e "${GREEN}✅ 端口可用${NC}"
    PORT=8080
fi

echo ""
echo "=========================================="
echo "📦 正在编译项目..."
echo "=========================================="

# 首次编译（下载依赖）
if [ ! -d "target" ]; then
    echo "首次编译，下载依赖需要 2-5 分钟..."
    mvn clean install -DskipTests
else
    mvn compile -DskipTests
fi

echo ""
echo "=========================================="
echo "🎉 启动应用..."
echo "=========================================="

# 启动应用（后台运行）
mvn spring-boot:run &
APP_PID=$!

# 等待启动
echo "等待应用启动（最多 30 秒）..."
for i in {1..30}; do
    if curl -s http://localhost:$PORT/actuator/health &> /dev/null || \
       curl -s http://localhost:$PORT/api/prompts &> /dev/null; then
        echo ""
        echo -e "${GREEN}=========================================="
        echo "✅ 应用启动成功！"
        echo "==========================================${NC}"
        echo ""
        echo "📖 Swagger UI:  http://localhost:$PORT/swagger-ui.html"
        echo "📊 H2 Console:  http://localhost:$PORT/h2-console"
        echo "   - JDBC URL:  jdbc:h2:mem:promptdb"
        echo "   - Username:  sa"
        echo "   - Password:  (留空)"
        echo ""
        echo "🧪 测试 API:"
        echo "   curl http://localhost:$PORT/api/prompts"
        echo ""
        echo -e "${YELLOW}按 Ctrl+C 停止应用${NC}"
        echo "==========================================="

        # 等待用户 Ctrl+C
        wait $APP_PID
        exit 0
    fi
    echo -n "."
    sleep 1
done

echo ""
echo -e "${RED}❌ 应用启动超时，请检查日志${NC}"
kill $APP_PID 2>/dev/null
exit 1
