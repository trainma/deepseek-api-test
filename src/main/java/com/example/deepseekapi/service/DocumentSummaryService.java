package com.example.deepseekapi.service;

import com.example.deepseekapi.model.deepseek.ChatMessage;
import com.example.deepseekapi.model.deepseek.ChatRequest;
import com.example.deepseekapi.model.deepseek.ChatResponse;
import com.example.deepseekapi.model.dto.DocumentSummaryDTO;
import com.example.deepseekapi.model.vo.DocumentSummaryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 文档总结服务
 */
@Slf4j
@Service
public class DocumentSummaryService {

    @Autowired
    private DocumentParserService documentParserService;

    @Autowired
    private DeepseekApiService deepseekApiService;
    
    @Autowired
    @Qualifier("apiTaskExecutor")
    private ThreadPoolTaskExecutor apiTaskExecutor;

    /**
     * 异步解析文档并生成总结
     *
     * @param file 上传的文件
     * @param dto 总结请求参数
     * @return 文档总结结果的CompletableFuture
     */
    @Async("documentTaskExecutor")
    public CompletableFuture<DocumentSummaryVO> summarizeDocumentAsync(MultipartFile file, DocumentSummaryDTO dto) {
        try {
            DocumentSummaryVO result = summarizeDocument(file, dto);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("异步总结文档失败", e);
            CompletableFuture<DocumentSummaryVO> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * 解析文档并生成总结
     *
     * @param file 上传的文件
     * @param dto 总结请求参数
     * @return 文档总结结果
     */
    @Cacheable(value = "apiResponseCache", key = "'summarize_' + #file.originalFilename + '_' + #file.size + '_' + #dto.model", unless = "#result == null")
    public DocumentSummaryVO summarizeDocument(MultipartFile file, DocumentSummaryDTO dto) {
        DocumentSummaryVO vo = new DocumentSummaryVO();
        vo.setFileName(file.getOriginalFilename());
        vo.setFileSize(file.getSize() / 1024); // 转换为KB
        
        try {
            // 异步解析文档
            CompletableFuture<String> contentFuture = documentParserService.parseDocumentAsync(file);
            
            // 设置超时，防止长时间阻塞
            String content = contentFuture.get(30, TimeUnit.SECONDS);
            log.info("文档解析完成，内容长度: {}", content.length());
            
            // 截断文本，防止超出模型最大token限制
            // 一般来说，1个中文字符约等于1.5个token，1个英文单词约等于1.3个token
            // 这里假设最大token为8000，保守估计截取4000个字符
            String truncatedContent = documentParserService.truncateText(content, 4000);
            vo.setOriginalContent(truncatedContent);
            
            // 构建提示词
            String prompt = buildPrompt(truncatedContent, dto);
            
            // 异步调用AI生成总结
            CompletableFuture<ChatResponse> responseFuture = callAiForSummaryAsync(prompt, dto.getModel());
            
            // 设置超时，防止长时间阻塞
            ChatResponse response = responseFuture.get(60, TimeUnit.SECONDS);
            
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                ChatResponse.Choice choice = response.getChoices().get(0);
                if (choice.getMessage() != null) {
                    vo.setSummary(choice.getMessage().getContent());
                }
                vo.setModel(response.getModel());
                if (response.getUsage() != null) {
                    vo.setTotalTokens(response.getUsage().getTotalTokens());
                }
            } else {
                vo.setSummary("无法生成总结，请检查文档内容或稍后重试。");
            }
            
            return vo;
        } catch (Exception e) {
            log.error("文档总结失败", e);
            vo.setSummary("文档处理失败: " + e.getMessage());
            return vo;
        }
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(String content, DocumentSummaryDTO dto) {
        StringBuilder promptBuilder = new StringBuilder();
        
        // 添加自定义提示词
        if (StringUtils.hasText(dto.getCustomPrompt())) {
            promptBuilder.append(dto.getCustomPrompt());
        } else {
            promptBuilder.append("请对以下文档内容进行总结，提取关键信息和主要观点。");
            
            // 添加语言要求
            if (StringUtils.hasText(dto.getLanguage())) {
                promptBuilder.append("请使用").append(dto.getLanguage()).append("回答。");
            }
            
            // 添加长度要求
            if (dto.getMaxLength() != null && dto.getMaxLength() > 0) {
                promptBuilder.append("总结字数控制在").append(dto.getMaxLength()).append("字以内。");
            }
        }
        
        promptBuilder.append("\n\n文档内容：\n").append(content);
        
        return promptBuilder.toString();
    }

    /**
     * 异步调用AI生成总结
     */
    private CompletableFuture<ChatResponse> callAiForSummaryAsync(String prompt, String model) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 构建请求
                ChatRequest request = new ChatRequest();
                request.setModel(model);
                request.setStream(false);
                
                // 设置消息
                List<ChatMessage> messages = new ArrayList<>();
                messages.add(new ChatMessage("system", "你是一个专业的文档分析助手，擅长总结文档要点和提取关键信息。"));
                messages.add(new ChatMessage("user", prompt));
                request.setMessages(messages);
                
                // 调用API
                return deepseekApiService.callDeepseekApi(request);
            } catch (Exception e) {
                log.error("调用AI生成总结失败", e);
                throw new RuntimeException("调用AI生成总结失败: " + e.getMessage(), e);
            }
        }, apiTaskExecutor);
    }
}
