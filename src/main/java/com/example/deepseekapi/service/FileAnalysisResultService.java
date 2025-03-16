package com.example.deepseekapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.deepseekapi.entity.FileAnalysisResult;
import com.example.deepseekapi.mapper.FileAnalysisResultMapper;
import com.example.deepseekapi.model.vo.FileAnalysisVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 文件分析结果服务
 */
@Service
public class FileAnalysisResultService extends ServiceImpl<FileAnalysisResultMapper, FileAnalysisResult> {

    /**
     * 保存分析结果
     *
     * @param taskId 任务ID
     * @param fileId 文件ID
     * @param userId 用户ID
     * @param analysis 分析结果
     * @param totalTokens 使用的token数
     * @return 结果ID
     */
    public Long saveResult(Long taskId, Long fileId, Long userId, String analysis, Integer totalTokens) {
        FileAnalysisResult result = new FileAnalysisResult();
        result.setTaskId(taskId);
        result.setFileId(fileId);
        result.setUserId(userId);
        result.setAnalysis(analysis);
        result.setTotalTokens(totalTokens);
        
        this.save(result);
        return result.getId();
    }
    
    /**
     * 获取分析结果
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 分析结果VO
     */
    public FileAnalysisVO getResultByTaskId(Long taskId, Long userId) {
        FileAnalysisResult result = this.getOne(
            new LambdaQueryWrapper<FileAnalysisResult>()
                .eq(FileAnalysisResult::getTaskId, taskId)
                .eq(FileAnalysisResult::getUserId, userId)
        );
        
        if (result == null) {
            return null;
        }
        
        FileAnalysisVO vo = new FileAnalysisVO();
        BeanUtils.copyProperties(result, vo);
        vo.setAnalysisId(result.getId());
        return vo;
    }
    
    /**
     * 获取任务结果
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 分析结果
     */
    public FileAnalysisResult getTaskResult(Long taskId, Long userId) {
        return this.getOne(
            new LambdaQueryWrapper<FileAnalysisResult>()
                .eq(FileAnalysisResult::getTaskId, taskId)
                .eq(FileAnalysisResult::getUserId, userId)
        );
    }
    
    /**
     * 删除分析结果
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 是否成功
     */
    public boolean deleteResultByTaskId(Long taskId, Long userId) {
        return this.remove(
            new LambdaQueryWrapper<FileAnalysisResult>()
                .eq(FileAnalysisResult::getTaskId, taskId)
                .eq(FileAnalysisResult::getUserId, userId)
        );
    }
}
