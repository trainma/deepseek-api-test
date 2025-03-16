package com.example.deepseekapi.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密钥生成工具类
 */
public class SecretGenerator {
    
    public static void main(String[] args) {
        // 生成64字节的随机密钥
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        
        // 转换为Base64编码
        String secret = Base64.getEncoder().encodeToString(bytes);
        System.out.println("生成的JWT密钥: " + secret);
    }
}
