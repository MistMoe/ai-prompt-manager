package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.UserContext;
import com.example.dto.PromptCreateDTO;
import com.example.dto.PromptUpdateDTO;
import com.example.entity.Prompt;
import com.example.mapper.PromptMapper;
import com.example.service.PromptService;
import com.example.service.PublicPromptService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class PromptServiceImpl extends ServiceImpl<PromptMapper, Prompt> implements PromptService {
    @Resource private PublicPromptService publicPrompts;

    @Override
    public void createPrompt(PromptCreateDTO createDTO) {
        Long userId = UserContext.getUserId();

        Prompt prompt = new Prompt();
        prompt.setUserId(userId);
        prompt.setTitle(createDTO.getTitle());
        prompt.setContent(createDTO.getContent());
        prompt.setCategory(createDTO.getCategory());
        prompt.setModelType(createDTO.getModelType());
        prompt.setTags(createDTO.getTags());
        prompt.setIsFavorite(0);
        prompt.setUseCount(0);
        prompt.setIsPublic(0);
        prompt.setShareVersion(0L);
        prompt.setCreateTime(LocalDateTime.now());
        prompt.setUpdateTime(LocalDateTime.now());
        prompt.setDeleteStatus(0);

        this.save(prompt);
    }

    @Override
    public IPage<Prompt> pagePrompts(Integer pageNum,
                                     Integer pageSize,
                                     String keyword,
                                     String category,
                                     String modelType,
                                     Integer favorite) {
        Long userId = UserContext.getUserId();

        Page<Prompt> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Prompt> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_id", userId);

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("title", keyword)
                    .or()
                    .like("tags", keyword)
                    .or()
                    .like("content", keyword)
            );
        }

        if (StringUtils.hasText(category)) {
            queryWrapper.eq("category", category);
        }

        if (StringUtils.hasText(modelType)) {
            queryWrapper.eq("model_type", modelType);
        }

        if (favorite != null) {
            queryWrapper.eq("is_favorite", favorite);
        }

        queryWrapper.orderByDesc("update_time");

        return this.page(page, queryWrapper);
    }

    @Override
    public Prompt getPromptDetail(Long id) {
        return getOwnPrompt(id);
    }

    @Override
    public void updatePrompt(Long id, PromptUpdateDTO updateDTO) {
        if (updateDTO.getIsFavorite() != null && updateDTO.getIsFavorite() != 0 && updateDTO.getIsFavorite() != 1)
            throw new IllegalArgumentException("收藏状态参数错误");
        Prompt prompt = getOwnPrompt(id);

        prompt.setTitle(updateDTO.getTitle());
        prompt.setContent(updateDTO.getContent());
        prompt.setCategory(updateDTO.getCategory());
        prompt.setModelType(updateDTO.getModelType());
        prompt.setTags(updateDTO.getTags());

        if (updateDTO.getIsFavorite() != null) {
            prompt.setIsFavorite(updateDTO.getIsFavorite());
        }

        prompt.setUpdateTime(LocalDateTime.now());

        boolean changed = this.update(new UpdateWrapper<Prompt>().eq("id", id).eq("user_id", UserContext.getUserId())
                .set("title", prompt.getTitle()).set("content", prompt.getContent()).set("category", prompt.getCategory())
                .set("model_type", prompt.getModelType()).set("tags", prompt.getTags())
                .set(updateDTO.getIsFavorite() != null, "is_favorite", updateDTO.getIsFavorite())
                .set("update_time", prompt.getUpdateTime()).setSql("share_version = share_version + 1"));
        if (!changed) throw new RuntimeException("模板状态已变化，请刷新");
        publicPrompts.evict(id, prompt.getShareVersion());
    }

    @Override
    public void deletePrompt(Long id) {
        Prompt prompt = getOwnPrompt(id);
        this.removeById(id);
        publicPrompts.evict(id, prompt.getShareVersion());
    }

    @Override
    public void updateFavorite(Long id, Integer isFavorite) {
        if (isFavorite == null || (isFavorite != 0 && isFavorite != 1)) {
            throw new RuntimeException("收藏状态参数错误");
        }

        getOwnPrompt(id);
        this.update(new UpdateWrapper<Prompt>().eq("id", id).eq("user_id", UserContext.getUserId())
                .set("is_favorite", isFavorite).set("update_time", LocalDateTime.now()));
    }

    @Override
    public void increaseUseCount(Long id) {
        getOwnPrompt(id);
        this.update(new UpdateWrapper<Prompt>().eq("id", id).eq("user_id", UserContext.getUserId())
                .setSql("use_count = COALESCE(use_count, 0) + 1").set("update_time", LocalDateTime.now()));
    }

    private Prompt getOwnPrompt(Long id) {
        Long userId = UserContext.getUserId();

        Prompt prompt = this.getById(id);
        if (prompt == null) {
            throw new RuntimeException("提示词不存在");
        }

        if (!prompt.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该提示词");
        }

        return prompt;
    }
}
