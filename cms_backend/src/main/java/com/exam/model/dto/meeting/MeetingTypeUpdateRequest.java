package com.exam.model.dto.meeting;

import lombok.Data;

import java.io.Serializable;

@Data
public class MeetingTypeUpdateRequest implements Serializable {
    
    private Long id;
    
    private String typeName;
    
    private String typeCode;
    
    private String description;
    
    private static final long serialVersionUID = 1L;
}