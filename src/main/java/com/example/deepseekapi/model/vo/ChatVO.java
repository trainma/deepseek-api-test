package com.example.deepseekapi.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 聊天响应VO
 */
@Data
@ApiModel(description = "聊天响应VO")
public class ChatVO {

    @ApiModelProperty(value = "AI回复内容", example = "你好！我是DeepSeek AI助手，有什么可以帮助你的？")
    private String reply;

    @ApiModelProperty(value = "使用的模型", example = "deepseek-chat")
    private String model;

    @ApiModelProperty(value = "使用的token数", example = "42")
    private Integer totalTokens;
}
