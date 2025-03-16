package com.example.deepseekapi.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 文档总结请求DTO
 */
@Data
@ApiModel(description = "文档总结请求DTO")
public class DocumentSummaryDTO {

    @ApiModelProperty(value = "自定义提示词", required = false, example = "请总结这篇文档的主要内容，包括关键点和结论")
    private String customPrompt;

    @ApiModelProperty(value = "总结语言", required = false, example = "中文")
    private String language = "中文";

    @ApiModelProperty(value = "总结长度", required = false, example = "500")
    private Integer maxLength = 500;

    @ApiModelProperty(value = "模型名称", required = false, example = "deepseek-chat")
    private String model = "deepseek-chat";
}
