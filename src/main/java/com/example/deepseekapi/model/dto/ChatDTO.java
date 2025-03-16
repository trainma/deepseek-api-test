package com.example.deepseekapi.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 聊天请求DTO
 */
@Data
@ApiModel(description = "聊天请求DTO")
public class ChatDTO {

    @ApiModelProperty(value = "用户消息", required = true, example = "你好，请介绍一下自己")
    private String message;

    @ApiModelProperty(value = "系统提示词", required = false, example = "你是一个有用的助手")
    private String systemPrompt;

    @ApiModelProperty(value = "模型名称", required = false, example = "deepseek-chat")
    private String model = "deepseek-chat";

    @ApiModelProperty(value = "温度", required = false, example = "0.7")
    private Double temperature = 0.7;

    @ApiModelProperty(value = "最大生成token数", required = false, example = "2048")
    private Integer maxTokens = 2048;
}
