package com.example.deepseekapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.deepseekapi.entity.FileAnalysisResult;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件分析结果Mapper接口
 */
@Mapper
public interface FileAnalysisResultMapper extends BaseMapper<FileAnalysisResult> {
}
