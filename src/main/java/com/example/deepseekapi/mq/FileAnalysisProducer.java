package com.example.deepseekapi.mq;

import com.example.deepseekapi.config.RabbitMQConfig;
import com.example.deepseekapi.entity.FileAnalysisTask;
import com.example.deepseekapi.model.dto.FileAnalysisTaskDTO;
import com.example.deepseekapi.model.vo.FileAnalysisTaskVO;
import com.example.deepseekapi.service.FileAnalysisTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 文件分析消息生产者
 */
@Slf4j
@Component
public class FileAnalysisProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private FileAnalysisTaskService taskService;
    
    /**
     * 创建分析任务并发送到消息队列
     *
     * @param taskDTO 任务DTO
     * @param userId 用户ID
     * @return 任务VO
     */
    public FileAnalysisTaskVO createAnalysisTask(FileAnalysisTaskDTO taskDTO, Long userId) {
        // 创建任务记录
        FileAnalysisTask task = new FileAnalysisTask();
        task.setFileId(taskDTO.getFileId());
        task.setUserId(userId);
        task.setRequirement(taskDTO.getRequirement());
        task.setModel(taskDTO.getModel());
        task.setStatus(FileAnalysisTask.Status.PENDING.name());
        task.setProgress(0);
        
        // 保存任务
        taskService.save(task);
        
        // 异步发送消息到队列
        sendMessageAsync(task);
        
        log.info("创建文件分析任务: {}", task.getId());
        
        // 返回任务信息
        FileAnalysisTaskVO taskVO = new FileAnalysisTaskVO();
        BeanUtils.copyProperties(task, taskVO);
        return taskVO;
    }
    
    /**
     * 异步发送消息到队列
     * 使用Spring的@Async注解实现异步发送
     * 这样可以提高API响应速度，不必等待消息发送完成
     * 明确指定使用fileAnalysisTaskExecutor线程池
     */
    @Async("fileAnalysisTaskExecutor")
    public void sendMessageAsync(FileAnalysisTask task) {
        // 创建消息关联数据，用于消息确认
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        
        // 设置消息确认回调
        correlationData.getFuture().addCallback(
            confirm -> {
                if (confirm.isAck()) {
                    log.info("消息发送成功，任务ID: {}, 关联ID: {}", task.getId(), correlationData.getId());
                } else {
                    log.error("消息发送失败，任务ID: {}, 关联ID: {}, 原因: {}", 
                              task.getId(), correlationData.getId(), confirm.getReason());
                    // 可以在这里添加重试逻辑
                    retryMessage(task, correlationData);
                }
            },
            ex -> {
                log.error("消息发送异常，任务ID: {}, 关联ID: {}, 异常: {}", 
                          task.getId(), correlationData.getId(), ex.getMessage());
                // 发生异常时重试
                retryMessage(task, correlationData);
            }
        );
        
        // 发送消息到队列，设置消息持久化
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.FILE_ANALYSIS_EXCHANGE,
            RabbitMQConfig.FILE_ANALYSIS_ROUTING_KEY,
            task,
            message -> {
                // 设置消息持久化
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return message;
            },
            correlationData
        );
        
        log.info("发送文件分析任务到队列: {}, 关联ID: {}", task.getId(), correlationData.getId());
    }
    
    /**
     * 消息发送失败时的重试逻辑
     * 
     * @param task 任务
     * @param correlationData 关联数据
     */
    private void retryMessage(FileAnalysisTask task, CorrelationData correlationData) {
        // 这里可以实现重试逻辑，例如最多重试3次
        // 为了简单起见，这里只是记录日志，实际应用中可以添加更复杂的重试机制
        log.info("准备重试发送消息，任务ID: {}, 关联ID: {}", task.getId(), correlationData.getId());
        
        // 实际项目中可以添加重试次数限制和延迟重试
        // 例如使用Spring的RetryTemplate或自定义重试逻辑
    }
}
