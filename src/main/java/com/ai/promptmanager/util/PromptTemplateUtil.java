package com.ai.promptmanager.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prompt 模板变量工具类
 *
 * 提供模板变量解析和渲染功能
 * 支持格式：{变量名}
 *
 * 示例：
 * - 模板："你是一个 {role}，请完成 {task}"
 * - 变量：{"role": "工程师", "task": "优化代码"}
 * - 渲染："你是一个 工程师，请完成 优化代码"
 */
public class PromptTemplateUtil {

    /**
     * 匹配 {变量名} 格式的正则表达式
     * 变量名支持：字母、数字、下划线、中文
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([\\w\\u4e00-\\u9fa5]+)\\}");

    /**
     * 从模板文本中提取所有变量名
     *
     * @param template 模板文本
     * @return 变量名列表（去重且保持出现顺序）
     */
    public static List<String> extractVariables(String template) {
        if (template == null || template.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> variableSet = new LinkedHashSet<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String variableName = matcher.group(1);
            variableSet.add(variableName);
        }

        return new ArrayList<>(variableSet);
    }

    /**
     * 渲染模板：将变量替换为实际值
     *
     * @param template 模板文本
     * @param variables 变量映射
     * @return 渲染后的文本
     * @throws IllegalArgumentException 如果缺少必需的变量
     */
    public static String render(String template, Map<String, String> variables) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        // 提取模板中的所有变量
        List<String> requiredVariables = extractVariables(template);

        // 检查是否缺少必需的变量
        List<String> missingVariables = new ArrayList<>();
        for (String varName : requiredVariables) {
            if (variables == null || !variables.containsKey(varName)) {
                missingVariables.add(varName);
            }
        }

        if (!missingVariables.isEmpty()) {
            throw new IllegalArgumentException(
                    "缺少必需的模板变量: " + String.join(", ", missingVariables));
        }

        // 渲染模板
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }

        return result;
    }

    /**
     * 校验变量映射是否满足模板需求
     *
     * @param template 模板文本
     * @param variables 变量映射
     * @return 缺少的变量列表（空列表表示通过校验）
     */
    public static List<String> validateVariables(String template, Map<String, String> variables) {
        List<String> requiredVariables = extractVariables(template);
        List<String> missingVariables = new ArrayList<>();

        for (String varName : requiredVariables) {
            if (variables == null || !variables.containsKey(varName)) {
                missingVariables.add(varName);
            }
        }

        return missingVariables;
    }
}
