package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.common.ErrorCode;
import com.exam.exception.BusinessException;
import com.exam.mapper.MeetingMapper;
import com.exam.mapper.MeetingParticipantMapper;
import com.exam.model.dto.meetingparticipant.MeetingParticipantAddRequest;
import com.exam.model.dto.meetingparticipant.MeetingParticipantQueryRequest;
import com.exam.model.entity.Meeting;
import com.exam.model.entity.MeetingParticipant;
import com.exam.model.entity.User;
import com.exam.model.vo.MeetingParticipantVO;
import com.exam.service.MeetingParticipantService;
import com.exam.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 会议参与者服务实现类
 */
@Service
public class MeetingParticipantServiceImpl extends ServiceImpl<MeetingParticipantMapper, MeetingParticipant>
        implements MeetingParticipantService {

    @Resource
    private MeetingMapper meetingMapper;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addMeetingParticipant(MeetingParticipantAddRequest participantAddRequest, User loginUser) {
        if (participantAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long meetingId = participantAddRequest.getMeetingId();
        List<Long> userIds = participantAddRequest.getUserIds();
        if (meetingId == null || userIds == null || userIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 校验会议是否存在
        Meeting meeting = meetingMapper.selectById(meetingId);
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会议不存在");
        }
        // 校验用户权限
        if (!meeting.getOrganizerId().equals(loginUser.getId().toString())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 校验用户是否存在
        List<User> users = userService.listByIds(userIds);
        if (users.size() != userIds.size()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "存在无效的用户ID");
        }

        // 批量添加参与者
        List<MeetingParticipant> participantList = new ArrayList<>();
        for (Long userId : userIds) {
            MeetingParticipant participant = new MeetingParticipant();
            participant.setMeetingId(meetingId);
            participant.setUserId(userId);
            participantList.add(participant);
        }
        boolean result = this.saveBatch(participantList);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "添加参与者失败");
        }
        return participantList.get(0).getId();
    }

    @Override
    public boolean removeMeetingParticipant(Long meetingId, Long userId, User loginUser) {
        if (meetingId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 校验会议是否存在
        Meeting meeting = meetingMapper.selectById(meetingId);
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会议不存在");
        }
        // 校验用户权限
        if (!meeting.getOrganizerId().equals(loginUser.getId().toString())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 删除参与者
        QueryWrapper<MeetingParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meetingId", meetingId);
        queryWrapper.eq("userId", userId);
        return this.remove(queryWrapper);
    }

    @Override
    public MeetingParticipantVO getMeetingParticipantVO(MeetingParticipant participant, HttpServletRequest request) {
        if (participant == null) {
            return null;
        }

        MeetingParticipantVO participantVO = new MeetingParticipantVO();
        BeanUtils.copyProperties(participant, participantVO);

        // 获取会议信息
        Meeting meeting = meetingMapper.selectById(participant.getMeetingId());
        if (meeting != null) {
            participantVO.setMeetingTitle(meeting.getTitle());
            participantVO.setMeetingCode(meeting.getMeetingCode());
            participantVO.setMeetingStartTime(meeting.getStartTime());
            participantVO.setMeetingEndTime(meeting.getEndTime());
        }

        // 获取用户信息
        User user = userService.getById(participant.getUserId());
        if (user != null) {
            participantVO.setUserId(String.valueOf(user.getId()));
            participantVO.setUserName(user.getUserName());
            participantVO.setUserAvatar(user.getUserAvatar());
        }

        return participantVO;
    }

    @Override
    public List<MeetingParticipantVO> listMeetingParticipants(Long meetingId) {
        if (meetingId == null || meetingId <= 0) {
            return new ArrayList<>();
        }
        QueryWrapper<MeetingParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meetingId", meetingId);
        List<MeetingParticipant> participantList = list(queryWrapper);
        return participantList.stream()
                .map(participant -> getMeetingParticipantVO(participant, null))
                .collect(Collectors.toList());
    }

    @Override
    public Page<MeetingParticipantVO> pageMeetingParticipants(Long meetingId, long current, long size) {
        QueryWrapper<MeetingParticipant> queryWrapper = new QueryWrapper<>();
        if (meetingId != null && meetingId > 0) {
            queryWrapper.eq("meetingId", meetingId);
        }
        Page<MeetingParticipant> page = page(new Page<>(current, size), queryWrapper);
        
        Page<MeetingParticipantVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<MeetingParticipantVO> voList = page.getRecords().stream()
                .map(participant -> getMeetingParticipantVO(participant, null))
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public QueryWrapper<MeetingParticipant> getQueryWrapper(MeetingParticipantQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = queryRequest.getId();
        Long meetingId = queryRequest.getMeetingId();
        Long userId = queryRequest.getUserId();

        QueryWrapper<MeetingParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(meetingId != null && meetingId > 0, "meetingId", meetingId);
        queryWrapper.eq(userId != null && userId > 0, "userId", userId);
        return queryWrapper;
    }

    @Override
    public Page<MeetingParticipantVO> getMeetingParticipantVOPage(Page<MeetingParticipant> participantPage,
            HttpServletRequest request) {
        List<MeetingParticipant> participantList = participantPage.getRecords();
        Page<MeetingParticipantVO> participantVOPage = new Page<>(participantPage.getCurrent(), participantPage.getSize(),
                participantPage.getTotal());

        if (participantList.isEmpty()) {
            return participantVOPage;
        }

        // 获取用户信息
        Set<Long> userIds = participantList.stream()
                .map(MeetingParticipant::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 填充信息
        List<MeetingParticipantVO> participantVOList = participantList.stream().map(participant -> {
            MeetingParticipantVO participantVO = new MeetingParticipantVO();
            BeanUtils.copyProperties(participant, participantVO);
            User user = userMap.get(participant.getUserId());
            if (user != null) {
                participantVO.setUserName(user.getUserName());
                participantVO.setUserAccount(user.getUserAccount());
            }
            return participantVO;
        }).collect(Collectors.toList());

        participantVOPage.setRecords(participantVOList);
        return participantVOPage;
    }

    @Override
    public List<Long> getUserMeetings(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<MeetingParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        return this.list(queryWrapper).stream()
                .map(MeetingParticipant::getMeetingId)
                .collect(Collectors.toList());
    }

    @Override
    public void validMeetingParticipant(MeetingParticipant meetingParticipant, boolean add) {
        if (meetingParticipant == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long meetingId = meetingParticipant.getMeetingId();
        Long userId = meetingParticipant.getUserId();

        // 创建时，参数不能为空
        if (add) {
            if (meetingId == null || userId == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议ID和用户ID不能为空");
            }
        }
    }

    @Override
    public List<MeetingParticipant> getParticipantsByMeetingId(Long meetingId) {
        if (meetingId == null || meetingId <= 0) {
            return new ArrayList<>();
        }
        QueryWrapper<MeetingParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meetingId", meetingId);
        return this.list(queryWrapper);
    }

    @Override
    public boolean removeByMeetingId(Long meetingId) {
        if (meetingId == null || meetingId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<MeetingParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meetingId", meetingId);
        return this.remove(queryWrapper);
    }

    @Override
    public List<Long> getMeetingIdsByParticipantId(Long participantId) {
        if (participantId == null || participantId <= 0) {
            return new ArrayList<>();
        }
        QueryWrapper<MeetingParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", participantId);
        return this.list(queryWrapper).stream()
                .map(MeetingParticipant::getMeetingId)
                .collect(Collectors.toList());
    }

    @Override
    public boolean removeParticipant(Long participantId, String userId) {
        if (participantId == null || participantId <= 0 || StringUtils.isBlank(userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 查询参会记录
        MeetingParticipant participant = this.getById(participantId);
        if (participant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "参会记录不存在");
        }
        
        // 查询会议信息
        Meeting meeting = meetingMapper.selectById(participant.getMeetingId());
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会议不存在");
        }
        
        // 检查权限（只有会议组织者或参会者本人可以移除参会记录）
        if (!meeting.getOrganizerId().equals(userId) && !participant.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        
        // 如果是已结束或已取消的会议，不允许移除参会人员
        if ("FINISHED".equals(meeting.getStatus()) || "CANCELLED".equals(meeting.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "会议已结束或已取消，无法移除参会人员");
        }
        
        return this.removeById(participantId);
    }

    @Override
    public Page<MeetingParticipantVO> pageParticipants(Long meetingId, String role, String status, long current, long size) {
        if (meetingId == null || meetingId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 构建查询条件
        QueryWrapper<MeetingParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meetingId", meetingId)
                .eq(StringUtils.isNotBlank(role), "role", role)
                .eq(StringUtils.isNotBlank(status), "status", status)
                .eq("isDelete", 0)
                .orderByAsc("createTime");
        
        // 执行分页查询
        Page<MeetingParticipant> page = this.page(new Page<>(current, size), queryWrapper);
        
        // 转换为VO对象
        Page<MeetingParticipantVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<MeetingParticipantVO> voList = page.getRecords().stream()
                .map(participant -> getMeetingParticipantVO(participant, null))
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public boolean addParticipant(Long meetingId, String userId, String role) {
        if (meetingId == null || meetingId <= 0 || StringUtils.isBlank(userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 检查会议是否存在
        Meeting meeting = meetingMapper.selectById(meetingId);
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会议不存在");
        }
        
        // 检查用户是否已经是参会人员
        QueryWrapper<MeetingParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meetingId", meetingId)
                .eq("userId", userId)
                .eq("isDelete", 0);
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已是参会人员");
        }
        
        // 创建参会记录
        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeetingId(meetingId);
        participant.setUserId(Long.valueOf(userId));
        participant.setRole(role);
        participant.setStatus("PENDING"); // 默认待确认状态
        
        return this.save(participant);
    }

    @Override
    public boolean updateParticipantStatus(Long participantId, String status, String userId) {
        if (participantId == null || participantId <= 0 || StringUtils.isBlank(status) || StringUtils.isBlank(userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 查询参会记录
        MeetingParticipant participant = this.getById(participantId);
        if (participant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "参会记录不存在");
        }
        
        // 检查权限（只有参会者本人可以更新状态）
        if (!participant.getUserId().toString().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        
        // 检查状态是否合法
        if (!Arrays.asList("PENDING", "ACCEPTED", "DECLINED").contains(status)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态值不合法");
        }
        
        // 更新状态
        participant.setStatus(status);
        return this.updateById(participant);
    }
} 