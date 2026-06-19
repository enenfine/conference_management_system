package com.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会议参与者
 * @TableName meeting_participant
 */
@TableName(value = "meeting_participant")
@Data
public class MeetingParticipant implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会议id
     */
    private Long meetingId;

    /**
     * 用户id
     */
    private Long userId;

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

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
} 