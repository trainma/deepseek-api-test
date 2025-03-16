package com.example.deepseekapi.service;

import com.alibaba.fastjson.JSON;
import com.example.deepseekapi.config.DeepseekApiConfig;
import com.example.deepseekapi.entity.FileInfo;
import com.example.deepseekapi.mapper.FileInfoMapper;
import com.example.deepseekapi.model.deepseek.ChatMessage;
import com.example.deepseekapi.model.deepseek.ChatRequest;
import com.example.deepseekapi.model.deepseek.ChatResponse;
import com.example.deepseekapi.model.dto.ChatDTO;
import com.example.deepseekapi.model.dto.FileAnalysisDTO;
import com.example.deepseekapi.model.vo.ChatVO;
import com.example.deepseekapi.model.vo.FileAnalysisVO;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    private MinioService minioService;

    @Autowired
    private FileInfoMapper fileInfoMapper;

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

    /**
     * 分析文件内容并生成总结
     *
     * @param fileAnalysisDTO 文件分析请求DTO
     * @return 文件分析响应VO
     */
    public FileAnalysisVO analyzeFile(FileAnalysisDTO fileAnalysisDTO) {
        try {
            // 获取文件信息
            FileInfo fileInfo = fileInfoMapper.selectById(fileAnalysisDTO.getFileId());
            if (fileInfo == null) {
                throw new RuntimeException("文件不存在");
            }
            
            // 下载文件
            InputStream inputStream = minioService.downloadFile(fileInfo.getFileName());
            
            // 读取文件内容
            String fileContent = extractFileContent(inputStream, fileInfo.getFileType());
            
            // 构建分析提示词
            String prompt = buildAnalysisPrompt(fileContent, fileAnalysisDTO.getRequirement());
            
            // 调用DeepSeek API进行分析
            ChatDTO chatDTO = new ChatDTO();
            chatDTO.setMessage(prompt);
            chatDTO.setModel(fileAnalysisDTO.getModel());
            chatDTO.setTemperature(fileAnalysisDTO.getTemperature());
            chatDTO.setMaxTokens(fileAnalysisDTO.getMaxTokens());
            chatDTO.setSystemPrompt("你是一个专业的文档分析助手，擅长总结文档内容，提取关键信息。");
            
            ChatVO chatVO = chat(chatDTO);
            
            // 构建返回结果
            FileAnalysisVO analysisVO = new FileAnalysisVO();
            analysisVO.setFileId(fileInfo.getId());
            analysisVO.setFileName(fileInfo.getOriginalName());
            analysisVO.setAnalysis(chatVO.getReply());
            analysisVO.setModel(chatVO.getModel());
            analysisVO.setTotalTokens(chatVO.getTotalTokens());
            
            return analysisVO;
        } catch (Exception e) {
            logger.error("分析文件失败", e);
            FileAnalysisVO errorResponse = new FileAnalysisVO();
            errorResponse.setFileId(fileAnalysisDTO.getFileId());
            errorResponse.setAnalysis("抱歉，分析文件时出现错误：" + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * 提取文件内容
     *
     * @param inputStream 文件输入流
     * @param fileType    文件类型
     * @return 文件内容
     */
    private String extractFileContent(InputStream inputStream, String fileType) throws IOException {
        try {
            // 读取文件内容
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            
            // 根据文件类型处理
            if (fileType.contains("text") || fileType.contains("json") || fileType.contains("xml")) {
                // 文本文件直接返回
                return new String(bytes, "UTF-8");
            } else if (fileType.contains("pdf")) {
                // PDF文件需要使用PDF解析库
                return extractPdfContent(bytes);
            } else if (fileType.contains("word") || fileType.contains("document")) {
                // Word文件需要使用Word解析库
                return extractWordContent(bytes);
            } else {
                // 其他类型文件
                return "无法解析该类型文件: " + fileType;
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
    
    /**
     * 提取PDF文件内容
     */
    private String extractPdfContent(byte[] bytes) {
        try (PDDocument document = PDDocument.load(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // 如果文本太长，可能需要截断
            if (text.length() > 10000) {
                text = text.substring(0, 10000) + "\n\n[文档内容过长，已截断...]";
            }
            
            return text;
        } catch (Exception e) {
            logger.error("提取PDF内容失败", e);
            return "无法提取PDF内容: " + e.getMessage();
        }
    }
    
    /**
     * 提取Word文件内容
     */
    private String extractWordContent(byte[] bytes) {
        try {
            // 尝试使用XWPF（.docx格式）
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                 XWPFDocument document = new XWPFDocument(bis);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                
                String text = extractor.getText();
                
                // 如果文本太长，可能需要截断
                if (text.length() > 10000) {
                    text = text.substring(0, 10000) + "\n\n[文档内容过长，已截断...]";
                }
                
                return text;
            } catch (Exception e) {
                // 如果XWPF失败，尝试使用HWPF（.doc格式）
                try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                     HWPFDocument document = new HWPFDocument(bis);
                     WordExtractor extractor = new WordExtractor(document)) {
                    
                    String text = extractor.getText();
                    
                    // 如果文本太长，可能需要截断
                    if (text.length() > 10000) {
                        text = text.substring(0, 10000) + "\n\n[文档内容过长，已截断...]";
                    }
                    
                    return text;
                } catch (Exception ex) {
                    throw new IOException("无法解析Word文档", ex);
                }
            }
        } catch (Exception e) {
            logger.error("提取Word内容失败", e);
            return "无法提取Word内容: " + e.getMessage();
        }
    }
    
    /**
     * 构建分析提示词
     */
    private String buildAnalysisPrompt(String fileContent, String requirement) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("以下是文档内容：\n\n");
        prompt.append(fileContent);
        prompt.append("\n\n");
        prompt.append("请根据以下要求分析上述文档：\n");
        prompt.append(requirement);
        
        return prompt.toString();
    }

    /**
     * 获取文件内容
     *
     * @param fileInfo 文件信息
     * @return 文件内容
     */
    public String getFileContent(FileInfo fileInfo) throws Exception {
        // 从MinIO获取文件
        InputStream inputStream = minioService.getObject(fileInfo.getBucketName(), fileInfo.getFileName());
        
        // 提取文件内容
        return extractFileContent(inputStream, fileInfo.getFileType());
    }
    
    /**
     * 分析内容
     *
     * @param prompt 提示词
     * @param model 模型名称
     * @return 分析结果
     */
    public String analyzeContent(String prompt, String model) {
        // 调用DeepSeek API进行分析
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setMessage(prompt);
        chatDTO.setModel(model != null && !model.isEmpty() ? model : config.getDefaultModel());
        chatDTO.setTemperature(0.7);
        chatDTO.setMaxTokens(4000);
        chatDTO.setSystemPrompt("你是一个专业的文档分析助手，擅长总结文档内容，提取关键信息。");
        
        ChatVO chatVO = chat(chatDTO);
        return chatVO.getReply();
    }
}
