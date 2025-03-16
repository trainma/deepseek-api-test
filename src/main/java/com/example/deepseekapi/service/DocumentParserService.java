package com.example.deepseekapi.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

/**
 * 文档解析服务
 */
@Slf4j
@Service
public class DocumentParserService {

    private final Tika tika = new Tika();
    private final AutoDetectParser parser = new AutoDetectParser();
    
    @Autowired
    @Qualifier("documentTaskExecutor")
    private ThreadPoolTaskExecutor documentTaskExecutor;

    /**
     * 异步解析文档内容
     *
     * @param file 上传的文件
     * @return 解析出的文本内容的CompletableFuture
     */
    @Async("documentTaskExecutor")
    public CompletableFuture<String> parseDocumentAsync(MultipartFile file) {
        try {
            String content = parseDocument(file);
            return CompletableFuture.completedFuture(content);
        } catch (IOException e) {
            log.error("异步解析文档失败", e);
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * 解析文档内容
     *
     * @param file 上传的文件
     * @return 解析出的文本内容
     */
    @Cacheable(value = "documentCache", key = "#root.methodName + '_' + #file.originalFilename + '_' + #file.size", unless = "#result == null")
    public String parseDocument(MultipartFile file) throws IOException {
        // 计算文件MD5，用于缓存和去重
        String fileHash = calculateFileHash(file);
        log.info("开始解析文档，文件名: {}, 大小: {} KB, Hash: {}", 
                file.getOriginalFilename(), file.getSize() / 1024, fileHash);
        
        try (InputStream inputStream = file.getInputStream()) {
            // 检测文件类型
            String mimeType = tika.detect(file.getOriginalFilename());
            log.info("文件类型: {}", mimeType);

            // 解析文档
            BodyContentHandler handler = new BodyContentHandler(-1); // -1表示不限制文本长度
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.getOriginalFilename());
            
            try {
                parser.parse(inputStream, handler, metadata, new ParseContext());
                
                // 打印元数据信息
                if (log.isDebugEnabled()) {
                    log.debug("文档元数据:");
                    for (String name : metadata.names()) {
                        log.debug("{}: {}", name, metadata.get(name));
                    }
                }
                
                return handler.toString();
            } catch (TikaException | SAXException e) {
                log.error("解析文档失败", e);
                throw new IOException("解析文档失败: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 计算文件MD5哈希值，用于缓存和去重
     */
    private String calculateFileHash(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                md.update(buffer, 0, read);
            }
            byte[] md5sum = md.digest();
            
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : md5sum) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            log.warn("计算文件哈希值失败", e);
            // 如果计算失败，使用文件名和大小作为替代
            return file.getOriginalFilename() + "_" + file.getSize();
        }
    }

    /**
     * 截断文本到指定长度
     *
     * @param text 原始文本
     * @param maxLength 最大长度
     * @return 截断后的文本
     */
    public String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...（内容过长已截断）";
    }
}
