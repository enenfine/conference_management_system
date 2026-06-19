package com.exam.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {
    
    // 文件上传根路径
    public static final String UPLOAD_ROOT_PATH =
            System.getProperty("user.dir") + File.separator + "uploads";
    
    // 文件访问路径前缀
    public static final String FILE_ACCESS_PATH = "/uploads/**";
    
    // 文件上传物理路径
    public static final String FILE_UPLOAD_PATH = "file:" + UPLOAD_ROOT_PATH + File.separator;
    
    @PostConstruct
    public void init() {
        File uploadDir = new File(UPLOAD_ROOT_PATH);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源访问
        registry.addResourceHandler(FILE_ACCESS_PATH)
                .addResourceLocations(FILE_UPLOAD_PATH);
    }
} 