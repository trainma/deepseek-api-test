package com.example.deepseekapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.deepseekapi.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件信息Mapper接口
 */
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {
}
