package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.PromptCreateDTO;
import com.example.dto.PromptUpdateDTO;
import com.example.entity.Prompt;

public interface PromptService extends IService<Prompt> {

    void createPrompt(PromptCreateDTO createDTO);

    IPage<Prompt> pagePrompts(Integer pageNum,
                              Integer pageSize,
                              String keyword,
                              String category,
                              String modelType,
                              Integer favorite);

    Prompt getPromptDetail(Long id);

    void updatePrompt(Long id, PromptUpdateDTO updateDTO);

    void deletePrompt(Long id);

    void updateFavorite(Long id, Integer isFavorite);

    void increaseUseCount(Long id);
}