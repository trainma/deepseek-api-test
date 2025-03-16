package com.example.deepseekapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.deepseekapi.entity.FileAnalysisTask;
import com.example.deepseekapi.mapper.FileAnalysisTaskMapper;
import com.example.deepseekapi.model.dto.FileAnalysisTaskDTO;
import com.example.deepseekapi.model.vo.FileAnalysisTaskVO;
import com.example.deepseekapi.mq.FileAnalysisProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件分析任务服务
 */
@Service
public class FileAnalysisTaskService extends ServiceImpl<FileAnalysisTaskMapper, FileAnalysisTask> {

    @Autowired
    private FileAnalysisProducer fileAnalysisProducer;
    
    /**
     * 创建分析任务并发送到消息队列
     *
     * @param taskDTO 任务DTO
     * @param userId 用户ID
     * @return 任务VO
     */
    public FileAnalysisTaskVO createAnalysisTask(FileAnalysisTaskDTO taskDTO, Long userId) {
        return fileAnalysisProducer.createAnalysisTask(taskDTO, userId);
    }
    
    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 任务VO
     */
    public FileAnalysisTaskVO getTaskDetail(Long taskId, Long userId) {
        FileAnalysisTask task = this.getOne(
            new LambdaQueryWrapper<FileAnalysisTask>()
                .eq(FileAnalysisTask::getId, taskId)
                .eq(FileAnalysisTask::getUserId, userId)
        );
        
        if (task == null) {
            return null;
        }
        
        FileAnalysisTaskVO taskVO = new FileAnalysisTaskVO();
        BeanUtils.copyProperties(task, taskVO);
        return taskVO;
    }
    
    /**
     * 获取文件的所有分析任务
     *
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 任务列表
     */
    public List<FileAnalysisTaskVO> getFileAnalysisTasks(Long fileId, Long userId) {
        List<FileAnalysisTask> tasks = this.list(
            new LambdaQueryWrapper<FileAnalysisTask>()
                .eq(FileAnalysisTask::getFileId, fileId)
                .eq(FileAnalysisTask::getUserId, userId)
                .orderByDesc(FileAnalysisTask::getCreateTime)
        );
        
        return tasks.stream().map(task -> {
            FileAnalysisTaskVO taskVO = new FileAnalysisTaskVO();
            BeanUtils.copyProperties(task, taskVO);
            return taskVO;
        }).collect(Collectors.toList());
    }
    
    /**
     * 分页获取用户的所有分析任务
     *
     * @param userId 用户ID
     * @param current 当前页
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public Page<FileAnalysisTaskVO> getUserTasksByPage(Long userId, long current, long pageSize) {
        Page<FileAnalysisTask> page = this.page(
            new Page<>(current, pageSize),
            new LambdaQueryWrapper<FileAnalysisTask>()
                .eq(FileAnalysisTask::getUserId, userId)
                .orderByDesc(FileAnalysisTask::getCreateTime)
        );
        
        Page<FileAnalysisTaskVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<FileAnalysisTaskVO> records = page.getRecords().stream().map(task -> {
            FileAnalysisTaskVO taskVO = new FileAnalysisTaskVO();
            BeanUtils.copyProperties(task, taskVO);
            return taskVO;
        }).collect(Collectors.toList());
        
        resultPage.setRecords(records);
        return resultPage;
    }
    
    /**
     * 更新任务状态和进度
     *
     * @param taskId 任务ID
     * @param status 状态
     * @param progress 进度
     * @param errorMessage 错误信息
     */
    public void updateTaskStatus(Long taskId, FileAnalysisTask.Status status, Integer progress, String errorMessage) {
        FileAnalysisTask task = new FileAnalysisTask();
        task.setId(taskId);
        task.setStatus(status.name());
        task.setProgress(progress);
        task.setErrorMessage(errorMessage);
        
        this.updateById(task);
    }
    
    /**
     * 删除任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 是否成功
     */
    public boolean deleteTask(Long taskId, Long userId) {
        return this.remove(
            new LambdaQueryWrapper<FileAnalysisTask>()
                .eq(FileAnalysisTask::getId, taskId)
                .eq(FileAnalysisTask::getUserId, userId)
        );
    }
}
