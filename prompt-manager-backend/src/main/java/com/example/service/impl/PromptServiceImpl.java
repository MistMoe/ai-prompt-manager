package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.UserContext;
import com.example.dto.PromptCreateDTO;
import com.example.dto.PromptUpdateDTO;
import com.example.entity.Prompt;
import com.example.mapper.PromptMapper;
import com.example.service.PromptService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class PromptServiceImpl extends ServiceImpl<PromptMapper, Prompt> implements PromptService {

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

        this.updateById(prompt);
    }

    @Override
    public void deletePrompt(Long id) {
        getOwnPrompt(id);
        this.removeById(id);
    }

    @Override
    public void updateFavorite(Long id, Integer isFavorite) {
        if (isFavorite == null || (isFavorite != 0 && isFavorite != 1)) {
            throw new RuntimeException("收藏状态参数错误");
        }

        Prompt prompt = getOwnPrompt(id);
        prompt.setIsFavorite(isFavorite);
        prompt.setUpdateTime(LocalDateTime.now());

        this.updateById(prompt);
    }

    @Override
    public void increaseUseCount(Long id) {
        Prompt prompt = getOwnPrompt(id);
        Integer useCount = prompt.getUseCount() == null ? 0 : prompt.getUseCount();
        prompt.setUseCount(useCount + 1);
        prompt.setUpdateTime(LocalDateTime.now());

        this.updateById(prompt);
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