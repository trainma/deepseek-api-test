package com.example.deepseekapi.model.deepseek;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeepSeek聊天消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "DeepSeek聊天消息")
public class ChatMessage {

    @ApiModelProperty(value = "消息角色", required = true, example = "user")
    private String role;

    @ApiModelProperty(value = "消息内容", required = true, example = "你好！")
    private String content;
}
