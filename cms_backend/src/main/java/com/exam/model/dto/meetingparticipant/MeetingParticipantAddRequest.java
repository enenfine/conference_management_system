package com.exam.model.dto.meetingparticipant;

import java.io.Serializable;
import lombok.Data;

import java.util.List;

/**
 * 添加会议参与者请求
 */
@Data
public class MeetingParticipantAddRequest implements Serializable {

    /**
     * 会议ID
     */
    private Long meetingId;

    /**
     * 参与者用户ID列表
     */
    private List<Long> userIds;

    private static final long serialVersionUID = 1L;
} 