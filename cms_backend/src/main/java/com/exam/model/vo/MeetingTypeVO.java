package com.exam.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会议类型视图对象
 */
@Data
public class MeetingTypeVO implements Serializable {
    
    private Long id;
    
    private String typeName;
    
    private String typeCode;
    
    private String description;
    
//    private Integer maxDuration;
//
//    private Integer minParticipants;
//
//    private Integer maxParticipants;
//
//    private Boolean needApproval;
//
//    private Boolean needMinutes;
    
    private Date createTime;
    
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}