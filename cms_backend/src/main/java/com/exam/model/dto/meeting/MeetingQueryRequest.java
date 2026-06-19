package com.exam.model.dto.meeting;

import com.exam.common.PageRequest;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会议查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MeetingQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 会议主题
     */
    private String title;

    /**
     * 会议室id
     */
    private Long roomId;

    /**
     * 会议类型id
     */
    private Long typeId;

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
     * 参会人id
     */
    private Long participantId;

    /**
     * 会议状态：0-待开始，1-进行中，2-已结束，3-已取消
     */
    private String status;

    private static final long serialVersionUID = 1L;
} 