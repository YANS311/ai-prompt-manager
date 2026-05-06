package com.ai.promptmanager.controller;

import com.ai.promptmanager.entity.Prompt;
import com.ai.promptmanager.service.PromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ViewController {

    @Autowired
    private PromptService service;

    @GetMapping("/manager")
    public String index(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Prompt> promptPage;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (keyword != null && !keyword.isEmpty()) {
            // 搜索不使用分页，因为结果通常较少
            List<Prompt> prompts = service.searchByTitle(keyword);
            model.addAttribute("prompts", prompts);
            model.addAttribute("keyword", keyword);
            model.addAttribute("totalPages", 1);
            model.addAttribute("currentPage", 0);
        } else if (category != null && !category.isEmpty()) {
            // 分类筛选不使用分页（可根据需求调整）
            List<Prompt> prompts = service.findByCategory(category);
            model.addAttribute("prompts", prompts);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("totalPages", 1);
            model.addAttribute("currentPage", 0);
        } else {
            // 主列表使用分页
            promptPage = service.findAllPaged(pageable);
            model.addAttribute("prompts", promptPage.getContent());
            model.addAttribute("totalPages", promptPage.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalElements", promptPage.getTotalElements());
        }

        List<String> categories = service.findAll().stream()
                .map(Prompt::getCategory)
                .filter(cat -> cat != null && !cat.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        model.addAttribute("categories", categories);
        model.addAttribute("pageSize", size);
        return "manager";
    }

    @PostMapping("/add")
    public String addPrompt(@ModelAttribute Prompt prompt) {
        service.save(prompt);
        return "redirect:/manager";
    }

    @GetMapping("/delete/{id}")
    public String deletePrompt(@PathVariable Long id) {
        service.deleteById(id);
        return "redirect:/manager";
    }

    @GetMapping("/edit/{id}")
    public String editPrompt(@PathVariable Long id, Model model) {
        Prompt prompt = service.findById(id)
                .orElseThrow(() -> new RuntimeException("Prompt not found"));
        model.addAttribute("prompt", prompt);
        return "edit";
    }

    @PostMapping("/update/{id}")
    public String updatePrompt(@PathVariable Long id, @ModelAttribute Prompt newPrompt) {
        service.update(id, newPrompt);
        return "redirect:/manager";
    }
}
