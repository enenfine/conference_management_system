package com.exam.utils;

import com.exam.common.ErrorCode;
import com.exam.config.FileUploadConfig;
import com.exam.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class FileUploadUtil {

    /**
     * 上传文件到指定目录
     * @param file 文件
     * @param subDir 子目录
     * @return 文件访问URL
     */
    public static String uploadFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        // 获取文件信息
        String originalFilename = file.getOriginalFilename();
        
        // 生成新的文件名（防止文件名冲突）
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        
        // 创建上传目录
        String uploadPath = FileUploadConfig.UPLOAD_ROOT_PATH + File.separator + subDir;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 保存文件
        File destFile = new File(uploadPath + File.separator + newFileName);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败");
        }
        
        // 返回文件访问URL
        return "/uploads/" + subDir + "/" + newFileName;
    }

    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    public static boolean deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return false;
        }
        
        String relativePath = fileUrl.substring("/uploads/".length());
        File file = new File(FileUploadConfig.UPLOAD_ROOT_PATH + File.separator + relativePath);
        return file.exists() && file.delete();
    }
} 