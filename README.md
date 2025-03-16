# DeepSeek API 测试项目

## 项目简介

DeepSeek API 测试项目是一个基于Spring Boot的Web应用，用于处理文件分析、文档摘要等AI相关任务。项目集成了RabbitMQ消息队列，实现了高并发、异步处理的能力，提高了系统的吞吐量和响应速度。

## 技术栈

- **后端框架**：Spring Boot
- **消息队列**：RabbitMQ
- **数据库**：MySQL
- **API文档**：Swagger
- **日志框架**：Logback
- **工具库**：Lombok, Jackson

## 系统架构

系统主要由以下几个部分组成：

1. **控制器层（Controller）**：处理HTTP请求，提供RESTful API
2. **服务层（Service）**：实现业务逻辑，处理数据
3. **消息队列（MQ）**：
   - 生产者（Producer）：发送消息到队列
   - 消费者（Consumer）：从队列接收消息并处理
4. **配置层（Config）**：系统配置，包括RabbitMQ、异步任务等配置
5. **实体层（Entity）**：数据库实体类
6. **DTO/VO层**：数据传输对象和视图对象

## 核心功能

### 文件分析

系统支持异步文件分析功能，用户上传文件后，系统会创建分析任务并异步处理：

1. 用户提交文件分析请求
2. 系统创建分析任务记录并立即返回任务ID
3. 任务通过消息队列异步发送给消费者
4. 消费者并行处理多个分析任务
5. 用户可以通过任务ID查询分析进度和结果

### 文档摘要

系统支持文档摘要功能，可以对文档内容进行智能摘要：

1. 用户提交文档摘要请求
2. 系统调用DeepSeek API进行摘要生成
3. 返回摘要结果给用户

## 高并发处理

系统通过以下方式实现高并发处理：

1. **异步消息队列**：使用RabbitMQ实现请求的异步处理
2. **生产者异步发送**：使用 `@Async`注解实现消息异步发送
3. **消费者并发处理**：配置 `concurrency = "3-10"`实现多线程消费
4. **线程池配置**：自定义线程池参数，优化异步任务执行

## 消息可靠性保障

系统通过以下机制确保消息的可靠性：

1. **队列持久化**：确保RabbitMQ重启后队列不丢失
2. **消息持久化**：确保RabbitMQ重启后消息不丢失
3. **消息确认机制**：确保消息成功发送到RabbitMQ
4. **消息重试机制**：当消息发送失败时进行重试

## 快速开始

### 环境要求

- JDK 11+
- Maven 3.6+
- RabbitMQ 3.8+
- MySQL 5.7+

### 安装步骤

1. 克隆项目

```bash
git clone https://github.com/yourusername/deepseek-api-test.git
cd deepseek-api-test
```

2. 配置数据库

```
# 修改 application.properties 文件中的数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/deepseek_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. 配置RabbitMQ

```
# 修改 application.properties 文件中的RabbitMQ配置
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

4. 编译项目

```bash
mvn clean package
```

5. 运行项目

```bash
java -jar target/deepseek-api-test-0.0.1-SNAPSHOT.jar
```

### API文档

启动项目后，访问以下URL查看API文档：

```
http://localhost:8080/swagger-ui.html
```

## 开发指南

### 添加新的消息队列

1. 在 `RabbitMQConfig`类中定义新的队列、交换机和路由键
2. 创建新的生产者类，实现消息发送逻辑
3. 创建新的消费者类，实现消息处理逻辑

### 异步任务配置

系统使用 `@Async`注解实现异步任务处理，相关配置在 `AsyncConfig`类中：

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Bean(name = "fileAnalysisTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-task-");
        executor.initialize();
        return executor;
    }
  
    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }
}
```

## 许可证

本项目采用 MIT 许可证 - 详情请参阅 [LICENSE](LICENSE) 文件
