package com.exam.model.vo;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;

import lombok.Data;

/**
 * 会议文件视图对象
 */
@Data
public class MeetingFileVO implements Serializable {

    /**
     * ID
     */
    private Long id;

    /**
     * 会议ID
     */
    private Long meetingId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private FileTypeVO fileType;

    /**
     * 上传用户ID
     */
    private Long uploadUserId;

    /**
     * 上传用户名
     */
    private String uploaderName;

    /**
     * 上传者头像
     */
    private String uploaderAvatar;

    /**
     * 会议信息
     */
    private String meetingTitle;

    /**
     * 会议代码
     */
    private String meetingCode;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    // 文件大小的格式化显示
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(fileSize) / Math.log10(1024));
        return new DecimalFormat("#,##0.#")
                .format(fileSize / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
} 