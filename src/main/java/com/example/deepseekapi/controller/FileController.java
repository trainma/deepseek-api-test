package com.example.deepseekapi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.deepseekapi.common.R;
import com.example.deepseekapi.entity.FileInfo;
import com.example.deepseekapi.model.entity.User;
import com.example.deepseekapi.repository.UserRepository;
import com.example.deepseekapi.service.FileInfoService;
import com.example.deepseekapi.service.MinioService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * 文件控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@Api(tags = "文件管理")
public class FileController {

    @Autowired
    private MinioService minioService;
    
    @Autowired
    private FileInfoService fileInfoService;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/upload")
    @ApiOperation(value = "上传文件", notes = "上传文件到MinIO服务器并保存文件信息")
    public R<FileInfo> upload(
            @ApiParam(value = "文件", required = true) @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return R.fail("上传文件不能为空");
            }
            
            // 获取当前用户ID
            Long userId = getCurrentUserId();
            
            // 上传文件并保存文件信息
            FileInfo fileInfo = fileInfoService.uploadFile(file, userId);
            
            return R.ok(fileInfo, "文件上传成功");
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @ApiOperation(value = "文件列表", notes = "获取当前用户的文件列表")
    public R<List<FileInfo>> list() {
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId();
            
            // 获取用户文件列表
            List<FileInfo> files = fileInfoService.getUserFiles(userId);
            
            return R.ok(files, "获取文件列表成功");
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            return R.fail("获取文件列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/page")
    @ApiOperation(value = "分页获取文件列表", notes = "分页获取当前用户的文件列表")
    public R<Page<FileInfo>> page(
            @ApiParam(value = "当前页", defaultValue = "1") @RequestParam(defaultValue = "1") long current,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") long size) {
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId();
            
            // 分页获取用户文件列表
            Page<FileInfo> page = fileInfoService.getUserFilesByPage(userId, current, size);
            
            return R.ok(page, "获取文件列表成功");
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            return R.fail("获取文件列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    @ApiOperation(value = "下载文件", notes = "根据文件ID下载文件")
    public void download(
            @ApiParam(value = "文件ID", required = true) @PathVariable("id") Long id,
            HttpServletResponse response) {
        try {
            // 获取文件信息
            FileInfo fileInfo = fileInfoService.getById(id);
            if (fileInfo == null) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":404,\"message\":\"文件不存在\",\"success\":false}");
                return;
            }
            
            // 检查文件是否存在于MinIO中
            try {
                // 从数据库中获取文件名（MinIO对象名）
                String objectName = fileInfo.getFileName();
                log.info("尝试下载文件: {}", objectName);
                
                // 检查对象名是否包含完整路径
                if (!objectName.contains("/")) {
                    log.warn("文件名不包含路径，可能导致下载失败: {}", objectName);
                }
                
                // 下载文件
                InputStream inputStream = minioService.downloadFile(objectName);
                
                // 设置响应头
                response.setContentType(fileInfo.getFileType());
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Content-Disposition", "attachment;filename=" + 
                        URLEncoder.encode(fileInfo.getOriginalName(), "UTF-8"));
                
                // 写入响应流
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    response.getOutputStream().write(buffer, 0, len);
                }
                
                inputStream.close();
            } catch (Exception e) {
                log.error("文件下载失败: {}", e.getMessage(), e);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"message\":\"文件下载失败: " + e.getMessage() + "\",\"success\":false}");
            }
        } catch (Exception e) {
            log.error("文件下载失败", e);
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"message\":\"文件下载失败: " + e.getMessage() + "\",\"success\":false}");
            } catch (Exception ex) {
                log.error("返回错误信息失败", ex);
            }
        }
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除文件", notes = "根据文件ID删除文件")
    public R<?> delete(
            @ApiParam(value = "文件ID", required = true) @PathVariable("id") Long id) {
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId();
            
            // 删除文件
            boolean result = fileInfoService.deleteFile(id, userId);
            
            if (result) {
                return R.ok("文件删除成功");
            } else {
                return R.fail("文件删除失败");
            }
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return R.fail("文件删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/url/{id}")
    @ApiOperation(value = "获取文件URL", notes = "根据文件ID获取文件的访问URL")
    public R<String> getUrl(
            @ApiParam(value = "文件ID", required = true) @PathVariable("id") Long id) {
        try {
            // 获取文件信息
            FileInfo fileInfo = fileInfoService.getById(id);
            if (fileInfo == null) {
                return R.fail("文件不存在");
            }
            
            return R.ok(fileInfo.getFileUrl(), "获取文件URL成功");
        } catch (Exception e) {
            log.error("获取文件URL失败", e);
            return R.fail("获取文件URL失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        
        Object principal = authentication.getPrincipal();
        String username = null;
        
        if (principal instanceof org.springframework.security.core.userdetails.User) {
            username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        }
        
        if (username == null) {
            throw new RuntimeException("获取用户名失败");
        }
        
        // 通过用户名查询用户ID
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        return user.getId();
    }
}
