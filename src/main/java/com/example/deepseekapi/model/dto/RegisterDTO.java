package com.example.deepseekapi.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 注册请求DTO
 */
@Data
@ApiModel(description = "注册请求参数")
public class RegisterDTO {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间")
    @ApiModelProperty(value = "用户名", required = true, example = "admin")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @ApiModelProperty(value = "密码", required = true, example = "123456")
    private String password;
    
    @Email(message = "邮箱格式不正确")
    @ApiModelProperty(value = "邮箱", example = "admin@example.com")
    private String email;
}
