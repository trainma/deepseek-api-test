package com.example.deepseekapi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件分析任务实体类
 */
@Data
@TableName("file_analysis_task")
public class FileAnalysisTask {
    
    /**
     * 任务状态枚举
     */
    public enum Status {
        PENDING,    // 等待处理
        PROCESSING, // 处理中
        COMPLETED,  // 已完成
        FAILED      // 失败
    }
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 分析要求
     */
    private String requirement;
    
    /**
     * 使用的模型
     */
    private String model;
    
    /**
     * 任务状态
     */
    private String status;
    
    /**
     * 进度百分比：0-100
     */
    private Integer progress;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
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
