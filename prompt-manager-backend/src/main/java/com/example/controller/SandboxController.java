package com.example.controller;

import com.example.common.Result;
import com.example.sandbox.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sandbox")
public class SandboxController {
    private final SandboxService sandbox;
    public SandboxController(SandboxService sandbox) { this.sandbox = sandbox; }
    @GetMapping("/models") public Result<List<SandboxService.ModelOption>> models() { return Result.success(sandbox.models()); }
    @PostMapping("/run") public Result<List<ModelClient.RunResult>> run(@RequestBody SandboxService.RunRequest request) {
        return Result.success(sandbox.run(request));
    }
}
