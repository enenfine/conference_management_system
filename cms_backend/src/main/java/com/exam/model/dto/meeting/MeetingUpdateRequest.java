package com.exam.model.dto.meeting;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class MeetingUpdateRequest implements Serializable {

    private Long id;
    private String title;
    private String description;
    private Long roomId;
    private Long typeId;
    private Date startTime;
    private Date endTime;
    private List<Long> participantIds;
    private String status;

    private static final long serialVersionUID = 1L;
}
