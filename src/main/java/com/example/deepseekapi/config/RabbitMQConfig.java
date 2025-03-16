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
     */
    @Bean
    public Queue fileAnalysisQueue() {
        return new Queue(FILE_ANALYSIS_QUEUE, true);
    }

    /**
     * 创建直接交换机
     */
    @Bean
    public DirectExchange fileAnalysisExchange() {
        return new DirectExchange(FILE_ANALYSIS_EXCHANGE);
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
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jacksonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jacksonMessageConverter);
        return rabbitTemplate;
    }
}
