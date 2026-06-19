package com.exam.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会议参与者视图对象
 */
@Data
public class MeetingParticipantVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 会议id
     */
    private Long meetingId;

    /**
     * 会议标题
     */
    private String meetingTitle;

    /**
     * 会议编号
     */
    private String meetingCode;

    /**
     * 会议开始时间
     */
    private Date meetingStartTime;

    /**
     * 会议结束时间
     */
    private Date meetingEndTime;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 角色：HOST-主持人，ATTENDEE-参会者
     */
    private String role;

    /**
     * 状态：PENDING-待确认，ACCEPTED-已接受，DECLINED-已拒绝
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