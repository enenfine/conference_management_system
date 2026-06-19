package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.model.dto.meetingroom.MeetingRoomAddRequest;
import com.exam.model.dto.meetingroom.MeetingRoomQueryRequest;
import com.exam.model.dto.meetingroom.MeetingRoomUpdateRequest;
import com.exam.model.entity.MeetingRoom;
import com.exam.model.vo.MeetingRoomVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 会议室服务
 */
public interface MeetingRoomService extends IService<MeetingRoom> {

    /**
     * 校验会议室
     *
     * @param meetingRoom 会议室信息
     * @param add 是否为创建校验
     */
    void validMeetingRoom(MeetingRoom meetingRoom, boolean add);

    /**
     * 获取查询条件
     *
     * @param meetingRoomQueryRequest 查询参数
     * @return 查询条件
     */
    QueryWrapper<MeetingRoom> getQueryWrapper(MeetingRoomQueryRequest meetingRoomQueryRequest);

    /**
     * 获取会议室封装
     *
     * @param meetingRoom 会议室信息
     * @param request HTTP请求
     * @return 会议室视图
     */
    MeetingRoomVO getMeetingRoomVO(MeetingRoom meetingRoom, HttpServletRequest request);

    /**
     * 分页获取会议室封装
     *
     * @param meetingRoomPage 会议室分页信息
     * @param request HTTP请求
     * @return 会议室分页视图
     */
    Page<MeetingRoomVO> getMeetingRoomVOPage(Page<MeetingRoom> meetingRoomPage, HttpServletRequest request);

    /**
     * 获取会议室某天的使用详情
     *
     * @param roomId 会议室ID
     * @param date 日期
     * @return 会议室使用详情列表，包含每个时间段的会议信息
     */
    List<Map<String, Object>> getRoomSchedule(Long roomId, Date date);
} 