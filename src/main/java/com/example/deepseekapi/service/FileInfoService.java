package com.example.deepseekapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.deepseekapi.entity.FileInfo;
import com.example.deepseekapi.mapper.FileInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件信息服务实现类
 */
@Slf4j
@Service
public class FileInfoService extends ServiceImpl<FileInfoMapper, FileInfo> {

    @Autowired
    private MinioService minioService;

    /**
     * 上传文件并保存文件信息
     *
     * @param file   文件
     * @param userId 用户ID
     * @return 文件信息
     */
    @Transactional(rollbackFor = Exception.class)
    public FileInfo uploadFile(MultipartFile file, Long userId) {
        try {
            // 上传文件到MinIO
            String originalFilename = file.getOriginalFilename();
            String url = minioService.uploadFile(file, originalFilename);
            
            // 创建文件信息
            FileInfo fileInfo = new FileInfo();
            fileInfo.setUserId(userId);
            fileInfo.setOriginalName(originalFilename);
            fileInfo.setFileName(getFileNameFromUrl(url));
            fileInfo.setFileSize(file.getSize());
            fileInfo.setFileType(file.getContentType());
            fileInfo.setFileUrl(url);
            fileInfo.setBucketName(minioService.getBucketName());
            fileInfo.setStatus(1); // 永久文件
            
            // 保存文件信息
            this.save(fileInfo);
            
            return fileInfo;
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new RuntimeException("上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件及文件信息
     *
     * @param id     文件ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(Long id, Long userId) {
        // 查询文件信息
        FileInfo fileInfo = this.getById(id);
        if (fileInfo == null) {
            throw new RuntimeException("文件不存在");
        }
        
        // 检查权限
        if (!fileInfo.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该文件");
        }
        
        try {
            // 从MinIO删除文件
            minioService.deleteFile(fileInfo.getFileName());
            
            // 删除文件信息
            return this.removeById(id);
        } catch (Exception e) {
            log.error("删除文件失败", e);
            throw new RuntimeException("删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户文件列表
     *
     * @param userId 用户ID
     * @return 文件列表
     */
    public List<FileInfo> getUserFiles(Long userId) {
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getUserId, userId)
                .orderByDesc(FileInfo::getCreateTime);
        return this.list(queryWrapper);
    }

    /**
     * 分页获取用户文件列表
     *
     * @param userId   用户ID
     * @param current  当前页
     * @param pageSize 每页大小
     * @return 分页数据
     */
    public Page<FileInfo> getUserFilesByPage(Long userId, long current, long pageSize) {
        Page<FileInfo> page = new Page<>(current, pageSize);
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getUserId, userId)
                .orderByDesc(FileInfo::getCreateTime);
        return this.page(page, queryWrapper);
    }

    /**
     * 从URL中提取文件名
     *
     * @param url 文件URL
     * @return 文件名
     */
    private String getFileNameFromUrl(String url) {
        String fileName = url;
        
        // 移除查询参数
        int queryIndex = fileName.indexOf('?');
        if (queryIndex > 0) {
            fileName = fileName.substring(0, queryIndex);
        }
        
        // 提取MinIO中的对象路径
        // 例如从 http://localhost:9000/deepseek/2025/03/16/file.pdf 提取 2025/03/16/file.pdf
        int bucketEndIndex = fileName.indexOf('/', fileName.indexOf("://") + 3);
        if (bucketEndIndex > 0) {
            int pathStartIndex = fileName.indexOf('/', bucketEndIndex + 1);
            if (pathStartIndex > 0) {
                fileName = fileName.substring(pathStartIndex + 1);
            }
        }
        
        return fileName;
    }
}
