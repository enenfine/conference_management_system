package com.exam.model.enums;

/**
 * 会议状态枚举
 */
public enum MeetingStatusEnum {
    PENDING("待开始"),
    ONGOING("进行中"),
    FINISHED("已结束"),
    CANCELLED("已取消");

    private final String text;

    MeetingStatusEnum(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
} 