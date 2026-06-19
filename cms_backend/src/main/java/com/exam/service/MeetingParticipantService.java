package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.model.dto.meetingparticipant.MeetingParticipantAddRequest;
import com.exam.model.dto.meetingparticipant.MeetingParticipantQueryRequest;
import com.exam.model.entity.MeetingParticipant;
import com.exam.model.entity.User;
import com.exam.model.vo.MeetingParticipantVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 会议参与者服务
 */
public interface MeetingParticipantService extends IService<MeetingParticipant> {

    /**
     * 添加会议参与者
     *
     * @param participantAddRequest 添加请求
     * @param loginUser 当前登录用户
     * @return 参与者ID
     */
    Long addMeetingParticipant(MeetingParticipantAddRequest participantAddRequest, User loginUser);

    /**
     * 删除会议参与者
     *
     * @param meetingId 会议ID
     * @param userId 用户ID
     * @param loginUser 当前登录用户
     * @return 是否成功
     */
    boolean removeMeetingParticipant(Long meetingId, Long userId, User loginUser);

    /**
     * 获取会议参与者列表
     *
     * @param meetingId 会议ID
     * @return 参与者列表
     */
    List<MeetingParticipantVO> listMeetingParticipants(Long meetingId);

    Page<MeetingParticipantVO> pageMeetingParticipants(Long meetingId, long current, long size);

    /**
     * 获取查询条件
     *
     * @param queryRequest 查询参数
     * @return 查询条件
     */
    QueryWrapper<MeetingParticipant> getQueryWrapper(MeetingParticipantQueryRequest queryRequest);

    /**
     * 获取会议参与者分页信息
     *
     * @param participantPage 参与者分页
     * @param request HTTP请求
     * @return 参与者分页信息
     */
    Page<MeetingParticipantVO> getMeetingParticipantVOPage(Page<MeetingParticipant> participantPage,
            HttpServletRequest request);

    /**
     * 获取用户参与的会议列表
     *
     * @param userId 用户ID
     * @return 会议ID列表
     */
    List<Long> getUserMeetings(Long userId);

    /**
     * 校验会议参与者
     *
     * @param meetingParticipant 会议参与者
     * @param add 是否为创建校验
     */
    void validMeetingParticipant(MeetingParticipant meetingParticipant, boolean add);

    /**
     * 根据会议ID获取参与者列表
     *
     * @param meetingId 会议ID
     * @return 参与者列表
     */
    List<MeetingParticipant> getParticipantsByMeetingId(Long meetingId);

    /**
     * 根据会议ID删除所有参与者
     *
     * @param meetingId 会议ID
     * @return 是否成功
     */
    boolean removeByMeetingId(Long meetingId);

    /**
     * 根据参与者ID获取会议ID列表
     *
     * @param participantId 参与者ID
     * @return 会议ID列表
     */
    List<Long> getMeetingIdsByParticipantId(Long participantId);

    /**
     * 获取会议参与者VO
     *
     * @param participant 参与者实体
     * @param request 请求
     * @return 参与者VO
     */
    MeetingParticipantVO getMeetingParticipantVO(MeetingParticipant participant, HttpServletRequest request);

    /**
     * 分页获取会议参与者列表
     *
     * @param meetingId 会议ID
     * @param role 角色
     * @param status 状态
     * @param current 当前页
     * @param size 页大小
     * @return 分页参与者VO
     */
    Page<MeetingParticipantVO> pageParticipants(Long meetingId, String role, String status, long current, long size);

    /**
     * 添加会议参与者
     *
     * @param meetingId 会议ID
     * @param userId 用户ID
     * @param role 角色
     * @return 是否成功
     */
    boolean addParticipant(Long meetingId, String userId, String role);

    /**
     * 更新参与者状态
     *
     * @param participantId 参与者ID
     * @param status 状态
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updateParticipantStatus(Long participantId, String status, String userId);

    /**
     * 移除会议参与者
     *
     * @param participantId 参与者ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean removeParticipant(Long participantId, String userId);
} 