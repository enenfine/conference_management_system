package com.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@TableName(value = "meeting_room")
@Data
public class MeetingRoom implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("roomName")
    private String name;

    @TableField("roomCode")
    private String code;

    private Integer capacity;
    private String location;
    private String facilities;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
