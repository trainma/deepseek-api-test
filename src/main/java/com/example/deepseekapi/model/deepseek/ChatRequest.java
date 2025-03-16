package com.example.deepseekapi.model.deepseek;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

/**
 * DeepSeek聊天请求
 */
@Data
@ApiModel(description = "DeepSeek聊天请求")
public class ChatRequest {

    @ApiModelProperty(value = "模型名称", required = true, example = "deepseek-chat")
    private String model;

    @ApiModelProperty(value = "消息列表", required = true)
    private List<ChatMessage> messages;

    @ApiModelProperty(value = "是否流式输出", required = false, example = "false")
    private Boolean stream = false;

    @ApiModelProperty(value = "温度", required = false, example = "0.7")
    private Double temperature;

    @ApiModelProperty(value = "最大生成token数", required = false, example = "2048")
    private Integer maxTokens;
}
