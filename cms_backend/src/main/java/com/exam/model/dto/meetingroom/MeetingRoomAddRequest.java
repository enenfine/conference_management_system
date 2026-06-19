package com.exam.model.dto.meetingroom;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
@ApiModel(description = "会议室创建请求")
public class MeetingRoomAddRequest implements Serializable {

    @ApiModelProperty("会议室名称")
    private String roomName;

    @ApiModelProperty("会议室编号")
    private String roomCode;

    @ApiModelProperty("容纳人数")
    private Integer capacity;

    @ApiModelProperty("位置")
    private String location;

    @ApiModelProperty("设施配置")
    private List<String> facilities;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("描述")
    private String description;

    private static final long serialVersionUID = 1L;
}
