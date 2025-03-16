package com.example.deepseekapi.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件分析任务VO
 */
@Data
@ApiModel(description = "文件分析任务响应VO")
public class FileAnalysisTaskVO {
    
    @ApiModelProperty(value = "任务ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "文件ID", example = "1")
    private Long fileId;
    
    @ApiModelProperty(value = "分析要求", example = "总结文档内容并提取关键信息")
    private String requirement;
    
    @ApiModelProperty(value = "使用的模型", example = "deepseek-v2")
    private String model;
    
    @ApiModelProperty(value = "任务状态", example = "PENDING")
    private String status;
    
    @ApiModelProperty(value = "进度百分比", example = "0")
    private Integer progress;
    
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
