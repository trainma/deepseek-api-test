package com.example.deepseekapi.controller;

import com.example.deepseekapi.model.dto.ChatDTO;
import com.example.deepseekapi.model.vo.ChatVO;
import com.example.deepseekapi.service.DeepseekApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DeepSeek API控制器
 */
@RestController
@RequestMapping("/api/deepseek")
@Api(tags = "DeepSeek API接口")
public class DeepseekApiController {

    @Autowired
    private DeepseekApiService deepseekApiService;

    @PostMapping("/chat")
    @ApiOperation(value = "聊天接口", notes = "调用DeepSeek API进行聊天")
    public ChatVO chat(@RequestBody ChatDTO chatDTO) {
        return deepseekApiService.chat(chatDTO);
    }
}
