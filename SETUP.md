# 🛠️ 环境配置指南

## 1. 安装 Java 21

### 使用 Homebrew (推荐 - M3 Pro 优化)
```bash
# 安装 Homebrew (如果还没有)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 安装 OpenJDK 21
brew install openjdk@21

# 创建符号链接
sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk

# 配置环境变量 (添加到 ~/.zshrc 或 ~/.bash_profile)
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@21' >> ~/.zshrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# 验证安装
java -version
# 应该显示: openjdk version "21.x.x"
```

### 替代方案：使用 SDKMAN
```bash
# 安装 SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# 安装 Java 21
sdk install java 21.0.2-open

# 验证
java -version
```

## 2. 安装 Maven

```bash
# 使用 Homebrew
brew install maven

# 验证安装
mvn -version
# 应该显示: Apache Maven 3.9.x
```

## 3. 在 IntelliJ IDEA 中导入项目

### 方式一：直接导入 (推荐)
1. 打开 IntelliJ IDEA
2. File → Open
3. 选择项目根目录 (`/Users/kanyun/Downloads/javaproject`)
4. 等待 Maven 自动下载依赖（首次需要几分钟）

### 方式二：配置 Project Structure
1. File → Project Structure (⌘ + ;)
2. Project Settings → Project
   - SDK: 选择 Java 21
   - Language Level: 21
3. Apply → OK

## 4. 运行项目

### 在 IntelliJ 中运行
1. 找到 `PromptManagerApplication.java`
2. 右键 → Run 'PromptManagerApplication'
3. 或者点击类旁边的绿色三角形 ▶️

### 使用 Maven 命令行
```bash
cd /Users/kanyun/Downloads/javaproject

# 首次运行：下载依赖并编译
mvn clean install

# 启动应用
mvn spring-boot:run
```

## 5. 验证启动成功

启动后应该看到类似输出：
```
========================================
🚀 AI Prompt Manager 已启动！
========================================
📖 Swagger UI: http://localhost:8080/swagger-ui.html
📊 H2 Console:  http://localhost:8080/h2-console
========================================
```

然后访问：
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console

## 6. 测试 API

### 使用 IntelliJ HTTP Client (推荐)
1. 打开 `test-api.http` 文件
2. 点击每个请求旁边的 ▶️ 按钮执行

### 使用 curl
```bash
# 创建一个 Prompt
curl -X POST http://localhost:8080/api/prompts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "测试 Prompt",
    "content": "这是我的第一个 AI Prompt",
    "category": "Test"
  }'

# 查询所有
curl http://localhost:8080/api/prompts
```

### 使用 Postman
导入 `test-api.http` 或手动创建请求。

## 常见问题

### Q1: Maven 依赖下载很慢
**解决方案：配置阿里云镜像**

编辑 `~/.m2/settings.xml`（如果不存在则创建）:
```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
```

### Q2: Lombok 注解不生效
**解决方案：安装 Lombok 插件**
1. IntelliJ → Preferences → Plugins
2. 搜索 "Lombok"
3. Install → Restart IDE
4. Enable Annotation Processing:
   - Preferences → Build, Execution, Deployment → Compiler → Annotation Processors
   - ✅ Enable annotation processing

### Q3: 端口 8080 被占用
**临时解决：修改端口**

编辑 `src/main/resources/application.properties`:
```properties
server.port=8081
```

**永久解决：释放 8080 端口**
```bash
# 查找占用端口的进程
lsof -i :8080

# 杀死进程 (替换 PID)
kill -9 <PID>
```

## 下一步

✅ 项目已配置完成！现在你可以：
1. 在 Swagger UI 中测试所有接口
2. 在 H2 Console 中查看数据库表结构
3. 修改代码并观察自动重启（Spring DevTools）
4. 开始扩展功能（语义搜索、Redis 缓存等）

**Happy Coding! 🚀**
