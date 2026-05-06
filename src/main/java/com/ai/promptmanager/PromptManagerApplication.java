package com.ai.promptmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
public class PromptManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromptManagerApplication.class, args);
        System.out.println("""

                ========================================
                🚀 AI Prompt Manager 已启动！
                ========================================
                🎨 Web 管理界面: http://localhost:8080/manager
                📖 API 文档:     http://localhost:8080
                📡 API 接口:     http://localhost:8080/api/prompts
                💾 数据库:       MySQL (localhost:3306/promptdb)
                ========================================
                """);
    }
}
