package com.exam.model.dto.meetingparticipant;

import com.exam.common.PageRequest;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会议参与者查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MeetingParticipantQueryRequest extends PageRequest implements Serializable {

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