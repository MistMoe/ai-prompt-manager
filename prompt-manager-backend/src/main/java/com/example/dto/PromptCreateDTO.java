package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PromptCreateDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "提示词内容不能为空")
    private String content;

    private String category;

    private String modelType;

    private String tags;
}