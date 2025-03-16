package com.example.deepseekapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.deepseekapi.entity.FileAnalysisTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件分析任务Mapper接口
 */
@Mapper
public interface FileAnalysisTaskMapper extends BaseMapper<FileAnalysisTask> {
}
