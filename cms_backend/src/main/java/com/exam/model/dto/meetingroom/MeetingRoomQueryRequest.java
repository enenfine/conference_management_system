package com.exam.model.dto.meetingroom;

import com.exam.common.PageRequest;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会议室查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MeetingRoomQueryRequest extends PageRequest implements Serializable {

    /**
     * 会议室名称
     */
    private String name;

    /**
     * 会议室编号
     */
    private String code;

    /**
     * 最小容纳人数
     */
    private Integer minCapacity;

    /**
     * 最大容纳人数
     */
    private Integer maxCapacity;

    /**
     * 位置
     */
    private String location;

    /**
     * 状态（0-空闲，1-使用中，2-维护中）
     */
    private Integer status;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（ascend/descend）
     */
    private String sortOrder;

    private static final long serialVersionUID = 1L;
} 