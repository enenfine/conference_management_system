package com.exam.model.dto.meetingparticipant;

import java.io.Serializable;
import lombok.Data;

/**
 * 更新会议参与者请求
 */
@Data
public class MeetingParticipantUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 会议id
     */
    private Long meetingId;

    /**
     * 用户id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
} 