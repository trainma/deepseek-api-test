package com.example.deepseekapi.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "登录响应结果")
public class LoginVO {
    
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    
    @ApiModelProperty(value = "用户名")
    private String username;
    
    @ApiModelProperty(value = "JWT令牌")
    private String token;
    
    @ApiModelProperty(value = "令牌过期时间(秒)")
    private Long expireTime;
}
