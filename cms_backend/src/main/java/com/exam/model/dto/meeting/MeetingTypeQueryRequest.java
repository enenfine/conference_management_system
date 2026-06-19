package com.exam.model.dto.meeting;

import com.exam.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class MeetingTypeQueryRequest extends PageRequest implements Serializable {
    
    private String typeName;
    
    private String typeCode;
    
    private static final long serialVersionUID = 1L;
}