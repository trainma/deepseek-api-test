package com.example.deepseekapi.service;

import com.alibaba.fastjson.JSON;
import com.example.deepseekapi.config.DeepseekApiConfig;
import com.example.deepseekapi.model.deepseek.ChatMessage;
import com.example.deepseekapi.model.deepseek.ChatRequest;
import com.example.deepseekapi.model.deepseek.ChatResponse;
import com.example.deepseekapi.model.dto.ChatDTO;
import com.example.deepseekapi.model.vo.ChatVO;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek API服务
 */
@Service
public class DeepseekApiService {

    private static final Logger logger = LoggerFactory.getLogger(DeepseekApiService.class);
    private static final String CHAT_COMPLETIONS_ENDPOINT = "/chat/completions";

    @Autowired
    private DeepseekApiConfig config;

    /**
     * 调用DeepSeek聊天API
     *
     * @param chatDTO 聊天请求DTO
     * @return 聊天响应VO
     */
    public ChatVO chat(ChatDTO chatDTO) {
        try {
            // 构建请求
            ChatRequest request = buildChatRequest(chatDTO);
            
            // 调用API
            ChatResponse response = callDeepseekApi(request);
            
            // 处理响应
            return buildChatResponse(response);
        } catch (Exception e) {
            logger.error("调用DeepSeek API失败", e);
            ChatVO errorResponse = new ChatVO();
            errorResponse.setReply("抱歉，调用AI服务出现错误：" + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 构建聊天请求
     */
    private ChatRequest buildChatRequest(ChatDTO chatDTO) {
        ChatRequest request = new ChatRequest();
        
        // 设置模型
        request.setModel(StringUtils.hasText(chatDTO.getModel()) ? chatDTO.getModel() : config.getDefaultModel());
        
        // 设置温度
        if (chatDTO.getTemperature() != null) {
            request.setTemperature(chatDTO.getTemperature());
        }
        
        // 设置最大token数
        if (chatDTO.getMaxTokens() != null) {
            request.setMaxTokens(chatDTO.getMaxTokens());
        }
        
        // 设置流式输出为false
        request.setStream(false);
        
        // 构建消息列表
        List<ChatMessage> messages = new ArrayList<>();
        
        // 添加系统消息（如果有）
        if (StringUtils.hasText(chatDTO.getSystemPrompt())) {
            messages.add(new ChatMessage("system", chatDTO.getSystemPrompt()));
        } else {
            messages.add(new ChatMessage("system", "You are a helpful assistant."));
        }
        
        // 添加用户消息
        messages.add(new ChatMessage("user", chatDTO.getMessage()));
        
        request.setMessages(messages);
        
        return request;
    }

    /**
     * 调用DeepSeek API
     */
    public ChatResponse callDeepseekApi(ChatRequest request) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String apiUrl = config.getApiUrl() + CHAT_COMPLETIONS_ENDPOINT;
        
        HttpPost httpPost = new HttpPost(apiUrl);
        
        // 设置请求头
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + config.getApiKey());
        
        // 设置请求体
        String jsonBody = JSON.toJSONString(request);
        StringEntity entity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        
        logger.debug("发送请求到DeepSeek API: {}", apiUrl);
        
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity responseEntity = response.getEntity();
            String responseBody = EntityUtils.toString(responseEntity);
            
            logger.debug("DeepSeek API响应: {}", responseBody);
            
            // 检查响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new IOException("DeepSeek API返回错误状态码: " + statusCode + ", 响应: " + responseBody);
            }
            
            // 解析响应
            return JSON.parseObject(responseBody, ChatResponse.class);
        }
    }

    /**
     * 构建聊天响应
     */
    private ChatVO buildChatResponse(ChatResponse response) {
        ChatVO chatVO = new ChatVO();
        
        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            ChatResponse.Choice choice = response.getChoices().get(0);
            if (choice.getMessage() != null) {
                chatVO.setReply(choice.getMessage().getContent());
            }
        }
        
        if (response != null) {
            chatVO.setModel(response.getModel());
            if (response.getUsage() != null) {
                chatVO.setTotalTokens(response.getUsage().getTotalTokens());
            }
        }
        
        return chatVO;
    }
}
