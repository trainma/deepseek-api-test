package com.example.deepseekapi.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件信息实体类
 */
@Data
@TableName("file_info")
@ApiModel(value = "FileInfo对象", description = "文件信息")
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("文件ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("原始文件名")
    private String originalName;

    @ApiModelProperty("文件名(存储路径)")
    private String fileName;

    @ApiModelProperty("文件大小(字节)")
    private Long fileSize;

    @ApiModelProperty("文件类型")
    private String fileType;

    @ApiModelProperty("文件URL")
    private String fileUrl;

    @ApiModelProperty("存储位置(bucket)")
    private String bucketName;

    @ApiModelProperty("文件状态(0:临时 1:永久)")
    private Integer status;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty("是否删除(0:未删除 1:已删除)")
    @TableLogic
    private Integer deleted;
}
