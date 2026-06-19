package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.model.dto.meeting.MeetingQueryRequest;
import com.exam.model.entity.Meeting;
import com.exam.model.vo.MeetingVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * 会议服务
 */
public interface MeetingService extends IService<Meeting> {

    /**
     * 校验数据
     *
     * @param meeting
     * @param add     是否为创建校验
     */
    void validMeeting(Meeting meeting, boolean add);

    /**
     * 获取查询条件
     *
     * @param meetingQueryRequest
     * @return
     */
    QueryWrapper<Meeting> getQueryWrapper(MeetingQueryRequest meetingQueryRequest);

    /**
     * 获取会议封装
     *
     * @param meeting
     * @param request
     * @return
     */
    MeetingVO getMeetingVO(Meeting meeting, HttpServletRequest request);

    /**
     * 分页获取会议封装
     *
     * @param meetingPage
     * @param request
     * @return
     */
    Page<MeetingVO> getMeetingVOPage(Page<Meeting> meetingPage, HttpServletRequest request);

    /**
     * 更新会议参与者
     *
     * @param meetingId
     * @param participantIds
     * @return
     */
    boolean updateMeetingParticipants(Long meetingId, List<Long> participantIds);

    /**
     * 获取用户参与的所有会议ID列表
     * 包括：用户创建的会议和被邀请参加的会议
     *
     * @param userId 用户ID
     * @return 会议ID列表
     */
    List<Long> getUserMeetingIds(Long userId);

    /**
     * 取消会议
     *
     * @param meetingId
     * @param userId
     * @return
     */
    boolean cancelMeeting(Long meetingId, Long userId);

    /**
     * 获取用户创建的会议列表
     *
     * @param userId
     * @param current
     * @param size
     * @param request
     * @return
     */
    Page<MeetingVO> getCreatedMeetings(Long userId, long current, long size, HttpServletRequest request);

    /**
     * 获取用户参与的会议列表
     *
     * @param userId
     * @param current
     * @param size
     * @param request
     * @return
     */
    Page<MeetingVO> getParticipatedMeetings(Long userId, long current, long size, HttpServletRequest request);

    /**
     * 获取用户的所有会议列表（包括创建的和参与的）
     *
     * @param userId
     * @param current
     * @param size
     * @param request
     * @return
     */
    Page<MeetingVO> getUserMeetings(Long userId, long current, long size, HttpServletRequest request);

    /**
     * 检查会议室在指定时间段是否有冲突
     *
     * @param roomId 会议室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param excludeMeetingId 需要排除的会议ID（用于编辑会议时）
     * @return true表示有冲突，false表示无冲突
     */
    boolean checkRoomConflict(Long roomId, Date startTime, Date endTime, Long excludeMeetingId);

    /**
     * 获取指定日期的会议列表
     * @param date 指定日期
     * @return 会议列表
     */
    List<Meeting> listMeetingsByDate(Date date);

    /**
     * 获取用户创建和参与的所有会议ID列表
     *
     * @param userId 用户ID
     * @return 会议ID列表
     */
    List<Long> getUserAllMeetingIds(Long userId);
} 