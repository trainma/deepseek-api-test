package com.example.deepseekapi.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 文件分析任务DTO
 */
@Data
@ApiModel(description = "文件分析任务请求DTO")
public class FileAnalysisTaskDTO {
    
    @ApiModelProperty(value = "文件ID", example = "1", required = true)
    private Long fileId;
    
    @ApiModelProperty(value = "分析要求", example = "总结文档内容并提取关键信息")
    private String requirement;
    
    @ApiModelProperty(value = "使用的模型", example = "deepseek-v2")
    private String model;
}
