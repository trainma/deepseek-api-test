package com.example.deepseekapi.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "Hello接口")
public class HelloController {

    @GetMapping("/hello")
    @ApiOperation(value = "Hello接口", notes = "返回问候信息")
    public String hello() {
        return "Hello, Deepseek API!";
    }
}
