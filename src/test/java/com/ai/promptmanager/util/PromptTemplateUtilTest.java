package com.ai.promptmanager.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for PromptTemplateUtil
 */
class PromptTemplateUtilTest {

    @Test
    void testExtractVariables_NoVariables() {
        String template = "This is a plain prompt without variables.";
        List<String> variables = PromptTemplateUtil.extractVariables(template);
        assertThat(variables).isEmpty();
    }

    @Test
    void testExtractVariables_SingleVariable() {
        String template = "You are a {role}.";
        List<String> variables = PromptTemplateUtil.extractVariables(template);
        assertThat(variables).containsExactly("role");
    }

    @Test
    void testExtractVariables_MultipleVariables() {
        String template = "You are a {role}, please complete {task} in {format} format.";
        List<String> variables = PromptTemplateUtil.extractVariables(template);
        assertThat(variables).containsExactly("role", "task", "format");
    }

    @Test
    void testExtractVariables_DuplicateVariables() {
        String template = "Start with {role}, then {task}, and again {role}.";
        List<String> variables = PromptTemplateUtil.extractVariables(template);
        assertThat(variables).containsExactly("role", "task");
    }

    @Test
    void testExtractVariables_ChineseVariables() {
        String template = "你是一个 {角色}，请完成 {任务}。";
        List<String> variables = PromptTemplateUtil.extractVariables(template);
        assertThat(variables).containsExactly("角色", "任务");
    }

    @Test
    void testRender_AllVariablesProvided() {
        String template = "You are a {role}, please complete {task}.";
        Map<String, String> variables = Map.of(
                "role", "Java Engineer",
                "task", "optimize service layer"
        );

        String rendered = PromptTemplateUtil.render(template, variables);
        assertThat(rendered).isEqualTo("You are a Java Engineer, please complete optimize service layer.");
    }

    @Test
    void testRender_MissingVariable_ThrowsException() {
        String template = "You are a {role}, please complete {task}.";
        Map<String, String> variables = Map.of("role", "Java Engineer");

        assertThatThrownBy(() -> PromptTemplateUtil.render(template, variables))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("缺少必需的模板变量")
                .hasMessageContaining("task");
    }

    @Test
    void testRender_EmptyTemplate() {
        String template = "";
        Map<String, String> variables = Map.of();

        String rendered = PromptTemplateUtil.render(template, variables);
        assertThat(rendered).isEmpty();
    }

    @Test
    void testRender_NullVariablesMap() {
        String template = "You are a {role}.";

        assertThatThrownBy(() -> PromptTemplateUtil.render(template, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("缺少必需的模板变量");
    }

    @Test
    void testValidateVariables_AllPresent() {
        String template = "You are a {role}, please complete {task}.";
        Map<String, String> variables = Map.of(
                "role", "Engineer",
                "task", "code review"
        );

        List<String> missing = PromptTemplateUtil.validateVariables(template, variables);
        assertThat(missing).isEmpty();
    }

    @Test
    void testValidateVariables_SomeMissing() {
        String template = "You are a {role}, please complete {task} in {format}.";
        Map<String, String> variables = Map.of("role", "Engineer");

        List<String> missing = PromptTemplateUtil.validateVariables(template, variables);
        assertThat(missing).containsExactlyInAnyOrder("task", "format");
    }

    @Test
    void testValidateVariables_ExtraVariablesIgnored() {
        String template = "You are a {role}.";
        Map<String, String> variables = Map.of(
                "role", "Engineer",
                "extra", "ignored"
        );

        List<String> missing = PromptTemplateUtil.validateVariables(template, variables);
        assertThat(missing).isEmpty();
    }
}
