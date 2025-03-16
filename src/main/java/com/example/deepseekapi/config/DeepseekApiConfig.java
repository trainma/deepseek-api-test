package com.example.deepseekapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek API配置
 */
@Configuration
public class DeepseekApiConfig {

    /**
     * DeepSeek API密钥
     */
    @Value("${deepseek.api.key:}")
    private String apiKey;

    /**
     * DeepSeek API地址
     */
    @Value("${deepseek.api.url}")
    private String apiUrl;

    /**
     * 默认模型
     */
    @Value("${deepseek.api.model}")
    private String defaultModel;

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getDefaultModel() {
        return defaultModel;
    }
}
