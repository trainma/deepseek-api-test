-- 文件分析任务表
CREATE TABLE IF NOT EXISTS `file_analysis_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `file_id` bigint(20) NOT NULL COMMENT '文件ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `requirement` varchar(500) DEFAULT NULL COMMENT '分析要求',
  `model` varchar(100) DEFAULT NULL COMMENT '使用的模型',
  `status` varchar(20) NOT NULL COMMENT '任务状态：PENDING/PROCESSING/COMPLETED/FAILED',
  `progress` int(3) DEFAULT 0 COMMENT '进度百分比：0-100',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_file_id` (`file_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件分析任务表';

-- 文件分析结果表
CREATE TABLE IF NOT EXISTS `file_analysis_result` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  `task_id` bigint(20) NOT NULL COMMENT '关联的任务ID',
  `file_id` bigint(20) NOT NULL COMMENT '文件ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `analysis` text NOT NULL COMMENT '分析结果',
  `total_tokens` int(11) DEFAULT NULL COMMENT '使用的token数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_file_id` (`file_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件分析结果表';
