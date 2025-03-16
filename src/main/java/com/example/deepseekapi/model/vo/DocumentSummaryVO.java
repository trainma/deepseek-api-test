package com.example.deepseekapi.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 文档总结响应VO
 */
@Data
@ApiModel(description = "文档总结响应VO")
public class DocumentSummaryVO {

    @ApiModelProperty(value = "文档总结内容", example = "这篇文档主要讨论了人工智能在医疗领域的应用...")
    private String summary;

    @ApiModelProperty(value = "文档原始内容", example = "人工智能（AI）在医疗领域的应用正在迅速发展...")
    private String originalContent;

    @ApiModelProperty(value = "文档名称", example = "AI医疗应用白皮书.pdf")
    private String fileName;

    @ApiModelProperty(value = "文档大小(KB)", example = "1024")
    private Long fileSize;

    @ApiModelProperty(value = "使用的模型", example = "deepseek-chat")
    private String model;

    @ApiModelProperty(value = "使用的token数", example = "1500")
    private Integer totalTokens;
}
