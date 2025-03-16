package com.example.deepseekapi.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {

    // 队列名称
    public static final String FILE_ANALYSIS_QUEUE = "file.analysis.queue";
    
    // 交换机名称
    public static final String FILE_ANALYSIS_EXCHANGE = "file.analysis.exchange";
    
    // 路由键
    public static final String FILE_ANALYSIS_ROUTING_KEY = "file.analysis";

    /**
     * 创建文件分析队列
     * durable=true 表示队列持久化，RabbitMQ重启后队列仍然存在
     */
    @Bean
    public Queue fileAnalysisQueue() {
        return QueueBuilder.durable(FILE_ANALYSIS_QUEUE)
                .build();
    }

    /**
     * 创建直接交换机
     * durable=true 表示交换机持久化
     */
    @Bean
    public DirectExchange fileAnalysisExchange() {
        return ExchangeBuilder.directExchange(FILE_ANALYSIS_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding bindingFileAnalysisQueue(Queue fileAnalysisQueue, DirectExchange fileAnalysisExchange) {
        return BindingBuilder.bind(fileAnalysisQueue)
                .to(fileAnalysisExchange)
                .with(FILE_ANALYSIS_ROUTING_KEY);
    }

    /**
     * 配置RabbitTemplate
     * 1. 开启发送者确认模式
     * 2. 设置消息持久化
     * 3. 配置重试机制
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jacksonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 设置消息转换器
        rabbitTemplate.setMessageConverter(jacksonMessageConverter);
        
        // 开启发送者确认模式
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData != null) {
                if (ack) {
                    System.out.println("消息发送成功，ID: " + correlationData.getId());
                } else {
                    System.out.println("消息发送失败，ID: " + correlationData.getId() + ", 原因: " + cause);
                }
            }
        });
        
        // 设置消息持久化
        rabbitTemplate.setMandatory(true);
        
        // 设置消息返回回调
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            System.out.println("消息被退回，交换机: " + exchange + 
                             ", 路由键: " + routingKey +
                             ", 回应码: " + replyCode +
                             ", 回应信息: " + replyText +
                             ", 消息: " + message);
        });
        
        return rabbitTemplate;
    }
}
