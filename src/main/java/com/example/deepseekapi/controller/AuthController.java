package com.example.deepseekapi.controller;

import com.example.deepseekapi.model.dto.LoginDTO;
import com.example.deepseekapi.model.dto.RegisterDTO;
import com.example.deepseekapi.model.vo.LoginVO;
import com.example.deepseekapi.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Api(tags = "认证管理")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "注册新用户")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        try {
            userService.register(registerDTO);
            return ResponseEntity.ok("用户注册成功");
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "登录并获取JWT令牌")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            LoginVO loginVO = userService.login(loginDTO);
            return ResponseEntity.ok(loginVO);
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
