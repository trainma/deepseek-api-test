package com.example.deepseekapi.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 文件分析请求DTO
 */
@Data
@ApiModel(description = "文件分析请求DTO")
public class FileAnalysisDTO {

    @ApiModelProperty(value = "文件ID", required = true, example = "1")
    private Long fileId;

    @ApiModelProperty(value = "分析要求", required = false, example = "请总结这个文档的主要内容")
    private String requirement = "请总结这个文档的主要内容，并提取关键信息点";

    @ApiModelProperty(value = "模型名称", required = false, example = "deepseek-chat")
    private String model = "deepseek-chat";

    @ApiModelProperty(value = "温度", required = false, example = "0.7")
    private Double temperature = 0.7;

    @ApiModelProperty(value = "最大生成token数", required = false, example = "2048")
    private Integer maxTokens = 2048;
}
