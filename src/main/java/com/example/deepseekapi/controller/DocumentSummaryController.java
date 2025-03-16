package com.example.deepseekapi.controller;

import com.example.deepseekapi.model.dto.DocumentSummaryDTO;
import com.example.deepseekapi.model.vo.DocumentSummaryVO;
import com.example.deepseekapi.service.DocumentSummaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

/**
 * 文档总结控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/document")
@Api(tags = "文档总结接口")
public class DocumentSummaryController {

    @Autowired
    private DocumentSummaryService documentSummaryService;

    @PostMapping("/summarize")
    @ApiOperation(value = "文档总结", notes = "上传文档并生成AI总结")
    public DocumentSummaryVO summarizeDocument(
            @ApiParam(value = "文档文件", required = true)
            @RequestParam("file") MultipartFile file,
            @ApiParam(value = "总结参数")
            DocumentSummaryDTO dto) {
        
        log.info("接收到文档总结请求，文件名: {}, 文件大小: {} KB", 
                file.getOriginalFilename(), file.getSize() / 1024);
        
        if (dto == null) {
            dto = new DocumentSummaryDTO();
        }
        
        return documentSummaryService.summarizeDocument(file, dto);
    }
    
    @PostMapping("/summarize/async")
    @ApiOperation(value = "异步文档总结", notes = "上传文档并异步生成AI总结，适用于大文件")
    public DeferredResult<ResponseEntity<DocumentSummaryVO>> summarizeDocumentAsync(
            @ApiParam(value = "文档文件", required = true)
            @RequestParam("file") MultipartFile file,
            @ApiParam(value = "总结参数")
            DocumentSummaryDTO dto) {
        
        log.info("接收到异步文档总结请求，文件名: {}, 文件大小: {} KB", 
                file.getOriginalFilename(), file.getSize() / 1024);
        
        if (dto == null) {
            dto = new DocumentSummaryDTO();
        }
        
        // 创建DeferredResult，设置超时时间为3分钟
        DeferredResult<ResponseEntity<DocumentSummaryVO>> deferredResult = new DeferredResult<>(180000L);
        
        // 设置超时回调
        deferredResult.onTimeout(() -> {
            log.warn("文档总结请求超时，文件名: {}", file.getOriginalFilename());
            deferredResult.setErrorResult(
                ResponseEntity.status(503)
                    .body("请求处理超时，请稍后重试或尝试上传较小的文件")
            );
        });
        
        // 异步处理请求
        CompletableFuture<DocumentSummaryVO> future = documentSummaryService.summarizeDocumentAsync(file, dto);
        
        // 处理结果
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("异步文档总结失败", ex);
                DocumentSummaryVO errorVo = new DocumentSummaryVO();
                errorVo.setFileName(file.getOriginalFilename());
                errorVo.setFileSize(file.getSize() / 1024);
                errorVo.setSummary("处理失败: " + ex.getMessage());
                deferredResult.setResult(ResponseEntity.ok(errorVo));
            } else {
                deferredResult.setResult(ResponseEntity.ok(result));
            }
        });
        
        return deferredResult;
    }
}
