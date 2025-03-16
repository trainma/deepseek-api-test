package com.example.deepseekapi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.deepseekapi.entity.FileAnalysisResult;
import com.example.deepseekapi.model.dto.FileAnalysisDTO;
import com.example.deepseekapi.model.vo.FileAnalysisTaskVO;
import com.example.deepseekapi.service.FileAnalysisResultService;
import com.example.deepseekapi.service.FileAnalysisService;
import com.example.deepseekapi.service.FileAnalysisTaskService;
import com.example.deepseekapi.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文件分析任务控制器
 */
@RestController
@RequestMapping("/api/file-analysis-tasks")
@Api(tags = "文件分析任务接口")
public class FileAnalysisTaskController {

    @Autowired
    private FileAnalysisTaskService taskService;
    
    @Autowired
    private FileAnalysisResultService resultService;
    
    @Autowired
    private FileAnalysisService fileAnalysisService;
    
    @PostMapping("/create")
    @ApiOperation(value = "创建分析任务", notes = "提交文件分析任务到队列中异步处理")
    public FileAnalysisTaskVO createAnalysisTask(@RequestBody FileAnalysisDTO fileAnalysisDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        return fileAnalysisService.analyzeAsync(fileAnalysisDTO, userId);
    }
    
    @GetMapping("/{taskId}")
    @ApiOperation(value = "获取任务详情", notes = "获取文件分析任务的详细信息")
    public FileAnalysisTaskVO getTaskDetail(
            @ApiParam(value = "任务ID", required = true) @PathVariable Long taskId) {
        Long userId = SecurityUtil.getCurrentUserId();
        return taskService.getTaskDetail(taskId, userId);
    }
    
    @GetMapping("/list")
    @ApiOperation(value = "获取任务列表", notes = "分页获取当前用户的所有分析任务")
    public Page<FileAnalysisTaskVO> getUserTasksByPage(
            @ApiParam(value = "当前页", required = true) @RequestParam(defaultValue = "1") long current,
            @ApiParam(value = "每页大小", required = true) @RequestParam(defaultValue = "10") long pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        return taskService.getUserTasksByPage(userId, current, pageSize);
    }
    
    @GetMapping("/{taskId}/result")
    @ApiOperation(value = "获取任务结果", notes = "获取文件分析任务的结果")
    public FileAnalysisResult getTaskResult(
            @ApiParam(value = "任务ID", required = true) @PathVariable Long taskId) {
        Long userId = SecurityUtil.getCurrentUserId();
        return resultService.getTaskResult(taskId, userId);
    }
    
    @DeleteMapping("/{taskId}")
    @ApiOperation(value = "删除任务", notes = "删除文件分析任务及其结果")
    public boolean deleteTask(
            @ApiParam(value = "任务ID", required = true) @PathVariable Long taskId) {
        Long userId = SecurityUtil.getCurrentUserId();
        return taskService.deleteTask(taskId, userId);
    }
}
