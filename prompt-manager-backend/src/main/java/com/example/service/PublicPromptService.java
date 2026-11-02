package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.UserContext;
import com.example.dto.PromptCreateDTO;
import com.example.entity.Prompt;
import com.example.mapper.PromptMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PublicPromptService {
    public record PublicPrompt(Long id, String title, String content, String category, String modelType, String tags) {
        static PublicPrompt from(Prompt p) {
            return new PublicPrompt(p.getId(), p.getTitle(), p.getContent(), p.getCategory(), p.getModelType(), p.getTags());
        }
    }
    public record Catalog(List<PublicPrompt> records, long total) {}
    private final PromptMapper mapper;
    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private volatile long retryCacheAfter;
    private boolean cacheAvailable() { return retryCacheAfter == 0 || System.nanoTime() - retryCacheAfter >= 0; }
    private void cacheFailed() { retryCacheAfter = System.nanoTime() + Duration.ofSeconds(10).toNanos(); }
    public PublicPromptService(PromptMapper mapper, StringRedisTemplate redis, ObjectMapper json) {
        this.mapper = mapper; this.redis = redis; this.json = json;
    }
    public Catalog catalog(int page, int size) {
        if (page < 1 || page > 10000 || size < 1 || size > 50) throw new IllegalArgumentException("分页参数超出范围");
        var result = mapper.selectPage(new Page<Prompt>(page, size), metadata().orderByDesc("update_time").orderByDesc("id"));
        return new Catalog(result.getRecords().stream().map(this::cached).toList(), result.getTotal());
    }
    private QueryWrapper<Prompt> metadata() {
        return new QueryWrapper<Prompt>().select("id", "share_version").eq("is_public", 1);
    }
    public PublicPrompt get(Long id) {
        var meta = mapper.selectOne(metadata().eq("id", id));
        if (meta == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "公共模板不存在或已取消公开");
        return cached(meta);
    }
    private PublicPrompt cached(Prompt meta) {
        String key = key(meta.getId(), meta.getShareVersion());
        try {
            String value = cacheAvailable() ? redis.opsForValue().get(key) : null;
            if (value != null) return json.readValue(value, PublicPrompt.class);
        } catch (Exception unavailable) { cacheFailed(); }
        Prompt current = mapper.selectOne(new QueryWrapper<Prompt>().eq("id", meta.getId())
                .eq("is_public", 1).eq("share_version", meta.getShareVersion()));
        if (current == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "模板状态已变化，请刷新列表");
        PublicPrompt value = PublicPrompt.from(current);
        try { if (cacheAvailable()) redis.opsForValue().set(key, json.writeValueAsString(value), Duration.ofMinutes(5)); }
        catch (Exception unavailable) { cacheFailed(); }
        return value;
    }
    public void visibility(Long id, boolean published) {
        Prompt own = mapper.selectOne(new QueryWrapper<Prompt>().eq("id", id).eq("user_id", UserContext.getUserId()));
        if (own == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "个人模板不存在");
        int changed = mapper.update(null, new UpdateWrapper<Prompt>().eq("id", id).eq("user_id", UserContext.getUserId())
                .set("is_public", published ? 1 : 0).set("update_time", LocalDateTime.now()).setSql("share_version = share_version + 1"));
        if (changed != 1) throw new ResponseStatusException(HttpStatus.CONFLICT, "模板状态已变化");
        evict(id, own.getShareVersion());
    }
    public void copy(Long id, PromptService prompts) {
        PublicPrompt source = get(id);
        var dto = new PromptCreateDTO();
        dto.setTitle(source.title()); dto.setContent(source.content()); dto.setCategory(source.category());
        dto.setModelType(source.modelType()); dto.setTags(source.tags());
        prompts.createPrompt(dto);
    }
    public void evict(Long id, Long version) {
        try { if (cacheAvailable()) redis.delete(key(id, version)); }
        catch (RuntimeException unavailable) { cacheFailed(); }
    }
    private String key(Long id, Long version) { return "prompt:public:" + id + ":v" + version; }
}
