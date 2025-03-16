package com.example.deepseekapi.service;

import com.example.deepseekapi.entity.FileInfo;
import com.example.deepseekapi.model.dto.FileAnalysisDTO;
import com.example.deepseekapi.model.dto.FileAnalysisTaskDTO;
import com.example.deepseekapi.model.vo.FileAnalysisTaskVO;
import com.example.deepseekapi.mq.FileAnalysisProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文件分析服务实现类
 */
@Slf4j
@Service
public class FileAnalysisService {

    @Autowired
    private FileInfoService fileInfoService;
    
    @Autowired
    private FileAnalysisProducer fileAnalysisProducer;

    /**
     * 异步分析文件
     *
     * @param fileAnalysisDTO 文件分析请求DTO
     * @param userId 用户ID
     * @return 文件分析任务VO
     */
    public FileAnalysisTaskVO analyzeAsync(FileAnalysisDTO fileAnalysisDTO, Long userId) {
        // 获取文件信息
        FileInfo fileInfo = fileInfoService.getById(fileAnalysisDTO.getFileId());
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }
        
        // 检查权限
        if (!fileInfo.getUserId().equals(userId)) {
            throw new RuntimeException("无权分析该文件");
        }
        
        // 转换为FileAnalysisTaskDTO
        FileAnalysisTaskDTO taskDTO = new FileAnalysisTaskDTO();
        taskDTO.setFileId(fileAnalysisDTO.getFileId());
        taskDTO.setRequirement(fileAnalysisDTO.getRequirement());
        taskDTO.setModel(fileAnalysisDTO.getModel());
        
        // 创建分析任务并发送到消息队列
        return fileAnalysisProducer.createAnalysisTask(taskDTO, userId);
    }
}
