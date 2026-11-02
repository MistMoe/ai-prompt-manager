package com.example.controller;

import com.example.common.Result;
import com.example.service.PublicPromptService;
import com.example.service.PromptService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public-prompts")
public class PublicPromptController {
    private final PublicPromptService shared;
    private final PromptService prompts;
    public PublicPromptController(PublicPromptService shared, PromptService prompts) { this.shared = shared; this.prompts = prompts; }
    @GetMapping public Result<PublicPromptService.Catalog> catalog(@RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="12") int size) { return Result.success(shared.catalog(page, size)); }
    @PostMapping("/{id}/copy") public Result<Void> copy(@PathVariable Long id) { shared.copy(id, prompts); return Result.success(); }
    @PutMapping("/{id}/visibility") public Result<Void> visibility(@PathVariable Long id, @RequestParam boolean published) {
        shared.visibility(id, published); return Result.success();
    }
}
