package com.example.deepseekapi.mq;

import com.example.deepseekapi.config.RabbitMQConfig;
import com.example.deepseekapi.entity.FileAnalysisTask;
import com.example.deepseekapi.entity.FileAnalysisTask.Status;
import com.example.deepseekapi.entity.FileInfo;
import com.example.deepseekapi.mapper.FileInfoMapper;
import com.example.deepseekapi.model.dto.FileAnalysisDTO;
import com.example.deepseekapi.model.vo.FileAnalysisVO;
import com.example.deepseekapi.service.DeepseekApiService;
import com.example.deepseekapi.service.FileAnalysisResultService;
import com.example.deepseekapi.service.FileAnalysisTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 文件分析消息消费者
 */
@Slf4j
@Component
public class FileAnalysisConsumer {

    @Autowired
    private FileAnalysisTaskService taskService;
    
    @Autowired
    private FileAnalysisResultService resultService;
    
    @Autowired
    private FileInfoMapper fileInfoMapper;
    
    @Autowired
    private DeepseekApiService deepseekApiService;
    
    /**
     * 处理文件分析任务
     * concurrency参数设置为3-10，表示最小3个消费者，最大10个消费者
     * 这样可以同时处理多个文件分析任务
     */
    @RabbitListener(queues = RabbitMQConfig.FILE_ANALYSIS_QUEUE, concurrency = "3-10")
    public void processFileAnalysis(FileAnalysisTask task) {
        log.info("收到文件分析任务: {}", task.getId());
        
        try {
            // 更新任务状态为处理中
            taskService.updateTaskStatus(task.getId(), Status.PROCESSING, 10, null);
            
            // 获取文件信息
            FileInfo fileInfo = fileInfoMapper.selectById(task.getFileId());
            if (fileInfo == null) {
                taskService.updateTaskStatus(task.getId(), Status.FAILED, 0, "文件不存在");
                return;
            }
            
            // 更新进度
            taskService.updateTaskStatus(task.getId(), Status.PROCESSING, 30, null);
            
            // 准备分析请求
            FileAnalysisDTO analysisDTO = new FileAnalysisDTO();
            analysisDTO.setFileId(task.getFileId());
            analysisDTO.setRequirement(task.getRequirement());
            analysisDTO.setModel(task.getModel());
            
            // 更新进度
            taskService.updateTaskStatus(task.getId(), Status.PROCESSING, 50, null);
            
            // 调用DeepSeek API分析文件
            FileAnalysisVO analysisVO = deepseekApiService.analyzeFile(analysisDTO);
            
            // 更新进度
            taskService.updateTaskStatus(task.getId(), Status.PROCESSING, 80, null);
            
            // 保存分析结果
            Long resultId = resultService.saveResult(
                task.getId(), 
                task.getFileId(), 
                task.getUserId(), 
                analysisVO.getAnalysis(), 
                analysisVO.getTotalTokens()
            );
            
            // 更新任务状态为已完成
            taskService.updateTaskStatus(task.getId(), Status.COMPLETED, 100, null);
            
            log.info("文件分析任务完成: {}, 结果ID: {}", task.getId(), resultId);
        } catch (Exception e) {
            log.error("处理文件分析任务失败: " + task.getId(), e);
            // 更新任务状态为失败
            taskService.updateTaskStatus(task.getId(), Status.FAILED, 0, e.getMessage());
        }
    }
}
