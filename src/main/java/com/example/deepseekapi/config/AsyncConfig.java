package com.example.deepseekapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;

/**
 * 异步任务配置
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 配置异步任务执行器
     * 用于处理@Async注解的方法
     */
    @Bean(name = "fileAnalysisTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(20);
        // 队列容量
        executor.setQueueCapacity(100);
        // 线程名前缀
        executor.setThreadNamePrefix("async-task-");
        // 初始化
        executor.initialize();
        return executor;
    }
    
    /**
     * 实现AsyncConfigurer接口，指定默认的异步执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }
}
