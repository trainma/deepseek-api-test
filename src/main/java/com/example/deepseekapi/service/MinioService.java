package com.example.deepseekapi.service;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * MinIO服务类
 */
@Slf4j
@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * 获取默认存储桶名称
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * 初始化默认存储桶
     */
    public void initBucket() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("创建存储桶: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("初始化存储桶失败: {}", e.getMessage(), e);
            throw new RuntimeException("初始化存储桶失败", e);
        }
    }

    /**
     * 获取所有存储桶
     */
    public List<Bucket> listBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            log.error("获取存储桶列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取存储桶列表失败", e);
        }
    }

    /**
     * 上传文件
     *
     * @param file     文件
     * @param fileName 文件名
     * @return 文件URL
     */
    public String uploadFile(MultipartFile file, String fileName) {
        try {
            initBucket();
            
            // 生成唯一文件名
            String objectName = generateUniqueFileName(fileName);
            
            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .contentType(file.getContentType())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build()
            );
            
            // 获取文件URL
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("上传文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("上传文件失败", e);
        }
    }

    /**
     * 下载文件
     *
     * @param fileName 文件名
     * @return 文件流
     */
    public InputStream downloadFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("下载文件失败", e);
        }
    }

    /**
     * 删除文件
     *
     * @param fileName 文件名
     */
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("删除文件失败", e);
        }
    }

    /**
     * 获取文件URL
     *
     * @param fileName 文件名
     * @return 文件URL
     */
    public String getFileUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .method(Method.GET)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取文件URL失败", e);
        }
    }

    /**
     * 获取对象
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 对象输入流
     */
    public InputStream getObject(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取对象失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取对象失败", e);
        }
    }

    /**
     * 列出存储桶中的所有文件
     *
     * @return 文件列表
     */
    public List<Map<String, Object>> listFiles() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            Iterable<Result<Item>> items = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .recursive(true)
                            .build()
            );
            
            for (Result<Item> item : items) {
                Item obj = item.get();
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("fileName", obj.objectName());
                fileInfo.put("size", obj.size());
                fileInfo.put("lastModified", obj.lastModified());
                fileInfo.put("url", getFileUrl(obj.objectName()));
                result.add(fileInfo);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取文件列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取文件列表失败", e);
        }
    }

    /**
     * 生成唯一文件名
     *
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    private String generateUniqueFileName(String originalFilename) {
        // 获取文件扩展名
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 生成UUID作为文件名
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        
        // 按日期分类存储
        String datePath = new java.text.SimpleDateFormat("yyyy/MM/dd").format(new Date());
        
        return datePath + "/" + uuid + extension;
    }
}
