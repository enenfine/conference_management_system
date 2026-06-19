package com.exam.model.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class MeetingRoomVO implements Serializable {

    private Long id;
    private String roomName;
    private String roomCode;
    private List<String> roomImage;
    private Integer capacity;
    private String location;
    private List<String> facilities;
    private Integer status;
    private Boolean isOccupied;
    private MeetingVO currentMeeting;
    private List<MeetingVO> upcomingMeetings;
    private Date createTime;
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
