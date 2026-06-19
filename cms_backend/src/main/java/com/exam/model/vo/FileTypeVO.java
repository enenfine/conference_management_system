package com.exam.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 文件类型视图对象
 */
@Data
public class FileTypeVO implements Serializable {
    
    /**
     * id
     */
    private Long id;
    
    /**
     * 类型名称
     */
    private String typeName;
    
    /**
     * 类型编码
     */
    private String typeCode;
    
    /**
     * MIME类型
     */
    private String mimeType;
    
    /**
     * 类型描述
     */
    private String description;
    
//    /**
//     * 最大文件大小
//     */
//    private Integer maxSize;
//
//    /**
//     * 允许的文件扩展名
//     */
//    private String allowedExtensions;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    private static final long serialVersionUID = 1L;
}