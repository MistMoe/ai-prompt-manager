package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.Result;
import com.example.dto.PromptCreateDTO;
import com.example.dto.PromptUpdateDTO;
import com.example.entity.Prompt;
import com.example.service.PromptService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prompts")
public class PromptController {

    @Resource
    private PromptService promptService;

    @PostMapping
    public Result<Void> createPrompt(@Valid @RequestBody PromptCreateDTO createDTO) {
        promptService.createPrompt(createDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<IPage<Prompt>> pagePrompts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String modelType,
            @RequestParam(required = false) Integer favorite) {

        IPage<Prompt> page = promptService.pagePrompts(
                pageNum,
                pageSize,
                keyword,
                category,
                modelType,
                favorite
        );

        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<Prompt> getPromptDetail(@PathVariable Long id) {
        Prompt prompt = promptService.getPromptDetail(id);
        return Result.success(prompt);
    }

    @PutMapping("/{id}")
    public Result<Void> updatePrompt(@PathVariable Long id,
                                     @Valid @RequestBody PromptUpdateDTO updateDTO) {
        promptService.updatePrompt(id, updateDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePrompt(@PathVariable Long id) {
        promptService.deletePrompt(id);
        return Result.success();
    }

    @PutMapping("/{id}/favorite")
    public Result<Void> updateFavorite(@PathVariable Long id,
                                       @RequestParam Integer isFavorite) {
        promptService.updateFavorite(id, isFavorite);
        return Result.success();
    }

    @PutMapping("/{id}/use")
    public Result<Void> increaseUseCount(@PathVariable Long id) {
        promptService.increaseUseCount(id);
        return Result.success();
    }
}