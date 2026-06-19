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
 * 会议
 */
@TableName(value = "meeting")
@Data
public class Meeting implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会议标题
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
     * 会议类型id
     */
    private Long typeId;

    /**
     * 会议室id
     */
    private Long roomId;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 发起人id，对应数据库 creatorId 字段
     */
    @TableField("creatorId")
    private String organizerId;

    /**
     * 状态：PENDING-待开始，ONGOING-进行中，FINISHED-已结束，CANCELLED-已取消
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
