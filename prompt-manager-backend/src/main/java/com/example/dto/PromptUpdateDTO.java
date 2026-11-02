package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PromptUpdateDTO {

    @NotBlank(message = "标题不能为空")
    @jakarta.validation.constraints.Size(max=100, message="标题最多 100 字")
    private String title;

    @NotBlank(message = "提示词内容不能为空")
    @jakarta.validation.constraints.Size(max=20000, message="模板最多 20000 字")
    private String content;

    private String category;

    private String modelType;

    private String tags;

    private Integer isFavorite;
}
