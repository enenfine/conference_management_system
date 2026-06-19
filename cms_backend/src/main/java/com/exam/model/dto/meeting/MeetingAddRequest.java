package com.exam.model.dto.meeting;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 创建会议请求
 */
@Data
public class MeetingAddRequest implements Serializable {

    /**
     * 会议主题
     */
    private String title;

    /**
     * 会议描述
     */
    private String description;

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
     * 参会人员id列表
     */
    private List<Long> participantIds;

    private static final long serialVersionUID = 1L;
} 