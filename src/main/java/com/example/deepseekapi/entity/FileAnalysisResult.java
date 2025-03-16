package com.example.deepseekapi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件分析结果实体类
 */
@Data
@TableName("file_analysis_result")
public class FileAnalysisResult {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联的任务ID
     */
    private Long taskId;
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 分析结果
     */
    private String analysis;
    
    /**
     * 使用的token数
     */
    private Integer totalTokens;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
