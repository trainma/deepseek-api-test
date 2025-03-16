package com.example.deepseekapi.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 文件分析响应VO
 */
@Data
@ApiModel(description = "文件分析响应VO")
public class FileAnalysisVO {

    @ApiModelProperty(value = "分析记录ID", example = "1")
    private Long analysisId;

    @ApiModelProperty(value = "文件ID", example = "1")
    private Long fileId;

    @ApiModelProperty(value = "文件名称", example = "report.pdf")
    private String fileName;

    @ApiModelProperty(value = "分析结果", example = "这份文档主要讨论了...")
    private String analysis;

    @ApiModelProperty(value = "使用的模型", example = "deepseek-chat")
    private String model;

    @ApiModelProperty(value = "使用的token数", example = "1024")
    private Integer totalTokens;
}
