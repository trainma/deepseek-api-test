package com.example.deepseekapi.model.deepseek;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

/**
 * DeepSeek聊天响应
 */
@Data
@ApiModel(description = "DeepSeek聊天响应")
public class ChatResponse {

    @ApiModelProperty(value = "响应ID", example = "chatcmpl-123")
    private String id;

    @ApiModelProperty(value = "对象类型", example = "chat.completion")
    private String object;

    @ApiModelProperty(value = "创建时间", example = "1677652288")
    private Long created;

    @ApiModelProperty(value = "模型名称", example = "deepseek-chat")
    private String model;

    @ApiModelProperty(value = "系统指纹", example = "fp_44709d6fcb")
    private String systemFingerprint;

    @ApiModelProperty(value = "选择列表")
    private List<Choice> choices;

    @ApiModelProperty(value = "使用情况")
    private Usage usage;

    @Data
    public static class Choice {
        @ApiModelProperty(value = "索引", example = "0")
        private Integer index;

        @ApiModelProperty(value = "消息")
        private ChatMessage message;

        @ApiModelProperty(value = "结束原因", example = "stop")
        private String finishReason;
    }

    @Data
    public static class Usage {
        @ApiModelProperty(value = "提示token数", example = "9")
        private Integer promptTokens;

        @ApiModelProperty(value = "完成token数", example = "12")
        private Integer completionTokens;

        @ApiModelProperty(value = "总token数", example = "21")
        private Integer totalTokens;
    }
}
