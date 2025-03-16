-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` int(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 文件信息表
CREATE TABLE IF NOT EXISTS `file_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_name` varchar(255) NOT NULL COMMENT '文件名(存储路径)',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(100) DEFAULT NULL COMMENT '文件类型',
  `file_url` varchar(500) NOT NULL COMMENT '文件URL',
  `bucket_name` varchar(100) NOT NULL COMMENT '存储位置(bucket)',
  `status` int(1) NOT NULL DEFAULT 1 COMMENT '文件状态(0:临时 1:永久)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `deleted` int(1) NOT NULL DEFAULT 0 COMMENT '是否删除(0:未删除 1:已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';
