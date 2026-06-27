package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test/ping")
    public String ping() {
        return "后端启动成功，AI Prompt管理平台连接正常";
    }
}
