package com.exam.model.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 会议视图
 */
@Data
public class MeetingVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 会议主题
     */
    private String title;

    /**
     * 会议描述
     */
    private String description;

    /**
     * 会议编号
     */
    private String meetingCode;

    /**
     * 会议室id
     */
    private Long roomId;

    /**
     * 会议室名称
     */
    private String roomName;

    /**
     * 会议室位置
     */
    private String roomLocation;

    /**
     * 会议室容量
     */
    private Integer roomCapacity;

    /**
     * 会议室设施
     */
    private List<String> roomFacilities;

    /**
     * 会议类型信息
     */
    private MeetingTypeVO meetingType;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 发起人id
     */
    private String organizerId;

    /**
     * 发起人名称
     */
    private String organizerName;

    /**
     * 发起人头像
     */
    private String organizerAvatar;

    /**
     * 参会人数统计
     */
    private Integer participantCount;

    /**
     * 会议文件数量
     */
    private Integer fileCount;

    /**
     * 会议状态：PENDING-待开始，ONGOING-进行中，FINISHED-已结束，CANCELLED-已取消
     */
    private String status;

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
