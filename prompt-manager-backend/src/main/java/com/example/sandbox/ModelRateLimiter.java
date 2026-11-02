package com.example.sandbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@Service
public class ModelRateLimiter {
    private final StringRedisTemplate redis;
    private final int limit;
    private final long windowMs;
    private final DefaultRedisScript<Long> script;
    public ModelRateLimiter(StringRedisTemplate redis, @Value("${sandbox.rate-limit:10}") int limit,
            @Value("${sandbox.window-ms:60000}") long windowMs) {
        if (limit < 1 || windowMs < 1) throw new IllegalArgumentException("限流配置必须为正数");
        this.redis = redis; this.limit = limit; this.windowMs = windowMs;
        script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("sliding-window.lua")); script.setResultType(Long.class);
    }
    public void acquire(long userId, int cost) {
        if (userId < 1 || cost < 1 || cost > 3) throw new IllegalArgumentException("无效的调试调用");
        Long admitted;
        try { admitted = redis.execute(script, List.of("prompt:sandbox:rate:" + userId),
                String.valueOf(windowMs), String.valueOf(limit), String.valueOf(cost), UUID.randomUUID().toString()); }
        catch (RuntimeException unavailable) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "限流服务不可用，模型调试暂时关闭");
        }
        if (admitted == null || admitted != 1L)
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "模型调用过于频繁，请稍后重试");
    }
}
