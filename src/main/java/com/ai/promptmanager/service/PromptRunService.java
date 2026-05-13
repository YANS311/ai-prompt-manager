package com.ai.promptmanager.service;

import com.ai.promptmanager.entity.Prompt;
import com.ai.promptmanager.entity.PromptRun;
import com.ai.promptmanager.repository.PromptRepository;
import com.ai.promptmanager.repository.PromptRunRepository;
import com.ai.promptmanager.util.PromptTemplateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * PromptRun 业务逻辑层
 *
 * 提供 Mock LLM 调用功能，记录每次 Prompt 运行的输入、输出和耗时
 * 为后续接入真实 LLM (OpenAI/Claude/DeepSeek) 做准备
 */
@Service
public class PromptRunService {

    private static final Logger log = LoggerFactory.getLogger(PromptRunService.class);

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private PromptRunRepository promptRunRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 执行一次 Prompt Run (Mock LLM) - 支持模板变量
     *
     * @param promptId 要运行的 Prompt ID
     * @param inputText 用户输入（可选）
     * @param variables 模板变量
     * @param modelName 模型名称
     * @return 运行记录
     */
    @Transactional
    public PromptRun executeRun(Long promptId, String inputText, Map<String, String> variables, String modelName) {
        long startTime = System.currentTimeMillis();

        // 1. 查询 Prompt，不存在则抛出异常
        Optional<Prompt> promptOpt = promptRepository.findById(promptId);
        if (promptOpt.isEmpty()) {
            throw new RuntimeException("Prompt not found with id: " + promptId);
        }
        Prompt prompt = promptOpt.get();

        // 2. 渲染模板（替换变量）
        String renderedPrompt;
        try {
            renderedPrompt = PromptTemplateUtil.render(prompt.getContent(), variables != null ? variables : Map.of());
        } catch (IllegalArgumentException e) {
            // 缺少必需的模板变量
            throw e;
        }

        // 3. 可选：追加用户输入文本
        if (inputText != null && !inputText.isEmpty()) {
            renderedPrompt = renderedPrompt + "\n\n" + inputText;
        }

        log.info("Executing Prompt Run - promptId: {}, model: {}, hasVariables: {}",
                promptId, modelName, variables != null && !variables.isEmpty());

        // 4. Mock 模型响应
        String responseText = generateMockResponse(modelName, renderedPrompt);

        // 5. 计算耗时
        long latencyMs = System.currentTimeMillis() - startTime;

        // 6. 创建运行记录
        PromptRun run = new PromptRun();
        run.setPromptId(promptId);
        run.setInputText(inputText);
        run.setVariablesJson(serializeVariables(variables));
        run.setRenderedPrompt(renderedPrompt);
        run.setModelName(modelName);
        run.setResponseText(responseText);
        run.setStatus(PromptRun.RunStatus.SUCCESS);
        run.setLatencyMs(latencyMs);

        PromptRun saved = promptRunRepository.save(run);
        log.info("Prompt Run completed - id: {}, latency: {}ms", saved.getId(), latencyMs);

        return saved;
    }

    /**
     * 查询某个 Prompt 的运行历史
     *
     * @param promptId Prompt ID
     * @return 运行记录列表，按时间倒序
     */
    public List<PromptRun> getRunHistory(Long promptId) {
        return promptRunRepository.findByPromptIdOrderByCreatedAtDesc(promptId);
    }

    /**
     * 提取 Prompt 中的模板变量
     *
     * @param promptId Prompt ID
     * @return 变量名列表
     */
    public List<String> extractVariables(Long promptId) {
        Optional<Prompt> promptOpt = promptRepository.findById(promptId);
        if (promptOpt.isEmpty()) {
            throw new RuntimeException("Prompt not found with id: " + promptId);
        }
        return PromptTemplateUtil.extractVariables(promptOpt.get().getContent());
    }

    /**
     * 预览渲染后的 Prompt（不创建 PromptRun）
     *
     * @param promptId Prompt ID
     * @param variables 模板变量
     * @return 渲染后的 Prompt
     */
    public String previewPrompt(Long promptId, Map<String, String> variables) {
        Optional<Prompt> promptOpt = promptRepository.findById(promptId);
        if (promptOpt.isEmpty()) {
            throw new RuntimeException("Prompt not found with id: " + promptId);
        }
        Prompt prompt = promptOpt.get();
        return PromptTemplateUtil.render(prompt.getContent(), variables != null ? variables : Map.of());
    }

    /**
     * 序列化变量 Map 为 JSON 字符串
     */
    private String serializeVariables(Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize variables: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 生成 Mock 响应
     *
     * 未来可替换为真实 LLM 调用
     */
    private String generateMockResponse(String modelName, String renderedPrompt) {
        // Mock 响应逻辑：返回简单的模拟文本
        return String.format("Mock response from model [%s]:\n\n" +
                        "This is a simulated response for your prompt. " +
                        "The rendered prompt has %d characters. " +
                        "In production, this would be replaced with actual LLM API calls to OpenAI, Claude, or DeepSeek.",
                modelName, renderedPrompt.length());
    }
}
