package com.exam.model.dto.meeting;

import lombok.Data;

import java.io.Serializable;

@Data
public class MeetingTypeAddRequest implements Serializable {
    
    private String typeName;
    
    private String typeCode;
    
    private String description;
    
    private static final long serialVersionUID = 1L;
}