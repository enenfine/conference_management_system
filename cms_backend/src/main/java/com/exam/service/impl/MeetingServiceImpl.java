package com.exam.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.common.ErrorCode;
import com.exam.exception.BusinessException;
import com.exam.mapper.MeetingMapper;
import com.exam.mapper.MeetingParticipantMapper;
import com.exam.model.dto.meeting.MeetingQueryRequest;
import com.exam.model.entity.*;
import com.exam.model.vo.MeetingRoomVO;
import com.exam.model.vo.MeetingTypeVO;
import com.exam.model.vo.MeetingVO;
import com.exam.model.vo.UserVO;
import com.exam.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会议服务实现
 */
@Service
@Slf4j
public class MeetingServiceImpl extends ServiceImpl<MeetingMapper, Meeting> implements MeetingService {

    @Resource
    private MeetingRoomService meetingRoomService;

    @Resource
    private UserService userService;

    @Resource
    private MeetingParticipantMapper meetingParticipantMapper;

    @Resource
    private MeetingTypeService meetingTypeService;

    @Resource
    private MeetingParticipantService meetingParticipantService;

    @Override
    public void validMeeting(Meeting meeting, boolean add) {
        if (meeting == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = meeting.getTitle();
        Long roomId = meeting.getRoomId();
        Date startTime = meeting.getStartTime();
        Date endTime = meeting.getEndTime();

        // 创建时，参数不能为空
        if (add) {
            if (StringUtils.isBlank(title)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议主题不能为空");
            }
            if (roomId == null || roomId <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议室不能为空");
            }
            if (startTime == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "开始时间不能为空");
            }
            if (endTime == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "结束时间不能为空");
            }
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议主题过长");
        }
        if (startTime != null && endTime != null && endTime.before(startTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "结束时间不能早于开始时间");
        }
        if (roomId != null && roomId > 0) {
            MeetingRoom meetingRoom = meetingRoomService.getById(roomId);
            if (meetingRoom == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会议室不存在");
            }
            // 检查会议室是否可用
            if (meetingRoom.getStatus() != 1) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议室不可用");
            }
            // TODO: 检查会议室在指定时间段是否已被预约
        }
    }

    @Override
    public QueryWrapper<Meeting> getQueryWrapper(MeetingQueryRequest meetingQueryRequest) {
        if (meetingQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = meetingQueryRequest.getId();
        String title = meetingQueryRequest.getTitle();
        Long roomId = meetingQueryRequest.getRoomId();
        Long typeId = meetingQueryRequest.getTypeId();
        Date startTime = meetingQueryRequest.getStartTime();
        Date endTime = meetingQueryRequest.getEndTime();
        String organizerId = meetingQueryRequest.getOrganizerId();
        Long participantId = meetingQueryRequest.getParticipantId();
        String status = meetingQueryRequest.getStatus();

        QueryWrapper<Meeting> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.eq(roomId != null && roomId > 0, "roomId", roomId);
        queryWrapper.eq(typeId != null && typeId > 0, "typeId", typeId);
        queryWrapper.ge(startTime != null, "startTime", startTime);
        queryWrapper.le(endTime != null, "endTime", endTime);
        queryWrapper.eq(StringUtils.isNotBlank(organizerId), "creatorId", organizerId);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.orderByAsc("startTime");
        if (participantId != null && participantId > 0) {
            // 查询用户参与的会议ID列表
            QueryWrapper<MeetingParticipant> participantQueryWrapper = new QueryWrapper<>();
            participantQueryWrapper.eq("userId", participantId);
            List<MeetingParticipant> participants = meetingParticipantMapper.selectList(participantQueryWrapper);
            List<Long> meetingIds = participants.stream()
                    .map(MeetingParticipant::getMeetingId)
                    .collect(Collectors.toList());
            if (meetingIds.isEmpty()) {
                // 如果没有找到相关会议，返回空结果
                queryWrapper.eq("id", -1);
            } else {
                queryWrapper.in("id", meetingIds);
            }
        }

        return queryWrapper;
    }

    @Override
    public MeetingVO getMeetingVO(Meeting meeting, HttpServletRequest request) {
        if (meeting == null) {
            return null;
        }
        MeetingVO meetingVO = new MeetingVO();
        BeanUtils.copyProperties(meeting, meetingVO);

        // 1. 查询会议类型信息
        MeetingType meetingType = meetingTypeService.getById(meeting.getTypeId());
        if (meetingType != null) {
            MeetingTypeVO meetingTypeVO = new MeetingTypeVO();
            BeanUtils.copyProperties(meetingType, meetingTypeVO);
            meetingVO.setMeetingType(meetingTypeVO);
        }

        // 2. 查询会议室信息
        MeetingRoom room = meetingRoomService.getById(meeting.getRoomId());
        if (room != null) {
            meetingVO.setRoomName(room.getName());
            meetingVO.setRoomLocation(room.getLocation());
            meetingVO.setRoomCapacity(room.getCapacity());
            if (StringUtils.isNotBlank(room.getFacilities())) {
                JSONArray facilities = JSONUtil.parseArray(room.getFacilities());
                meetingVO.setRoomFacilities(facilities.toList(String.class));
            }
        }

        // 3. 查询组织者信息
        User organizer = userService.getById(meeting.getOrganizerId());
        if (organizer != null) {
            meetingVO.setOrganizerName(organizer.getUserName());
            meetingVO.setOrganizerAvatar(organizer.getUserAvatar());
        }

        // 4. 统计参会人数
        QueryWrapper<MeetingParticipant> participantQuery = new QueryWrapper<>();
        participantQuery.eq("meetingId", meeting.getId());
        long participantCount = meetingParticipantService.count(participantQuery);
        meetingVO.setParticipantCount(Math.toIntExact(participantCount));

        return meetingVO;
    }

    @Override
    public Page<MeetingVO> getMeetingVOPage(Page<Meeting> meetingPage, HttpServletRequest request) {
        List<Meeting> meetingList = meetingPage.getRecords();
        Page<MeetingVO> meetingVOPage = new Page<>(meetingPage.getCurrent(), meetingPage.getSize(), meetingPage.getTotal());
        List<MeetingVO> meetingVOList = meetingList.stream()
                .map(meeting -> getMeetingVO(meeting, request))
                .collect(Collectors.toList());
        meetingVOPage.setRecords(meetingVOList);
        return meetingVOPage;
    }

    @Override
    public List<Long> getUserAllMeetingIds(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取用户创建的会议
        QueryWrapper<Meeting> createdMeetingsQuery = new QueryWrapper<>();
        createdMeetingsQuery.eq("creatorId", userId.toString())
                .select("id");
        List<Long> createdMeetingIds = this.list(createdMeetingsQuery)
                .stream()
                .map(Meeting::getId)
                .collect(Collectors.toList());

        // 获取用户参与的会议
        QueryWrapper<MeetingParticipant> participatedMeetingsQuery = new QueryWrapper<>();
        participatedMeetingsQuery.eq("userId", userId)
                .select("meetingId");
        List<Long> participatedMeetingIds = meetingParticipantMapper.selectList(participatedMeetingsQuery)
                .stream()
                .map(MeetingParticipant::getMeetingId)
                .collect(Collectors.toList());

        // 合并两个列表并去重
        Set<Long> allMeetingIds = new HashSet<>();
        allMeetingIds.addAll(createdMeetingIds);
        allMeetingIds.addAll(participatedMeetingIds);

        return new ArrayList<>(allMeetingIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMeetingParticipants(Long meetingId, List<Long> participantIds) {
        if (meetingId == null || meetingId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 检查会议是否存在
        Meeting meeting = this.getById(meetingId);
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 先物理删除旧参会人员。不要使用 meetingParticipantMapper.delete(wrapper)，
        // 因为 MeetingParticipant 实体配置了 @TableLogic，逻辑删除后旧数据仍然会占用
        // meetingId + userId 唯一索引，重新插入相同参会人会触发 Duplicate entry。
        meetingParticipantMapper.physicalDeleteByMeetingId(meetingId);

        // 如果没有新的参会人员，直接返回
        if (participantIds == null || participantIds.isEmpty()) {
            return true;
        }

        // 去重，避免前端重复传同一个用户 id 导致唯一索引冲突
        List<Long> userIds = participantIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<MeetingParticipant> participantList = new ArrayList<>();
        for (Long userId : userIds) {
            MeetingParticipant participant = new MeetingParticipant();
            participant.setMeetingId(meetingId);
            participant.setUserId(userId);
            boolean isOrganizer = Objects.equals(String.valueOf(userId), meeting.getOrganizerId());
            participant.setRole(isOrganizer ? "HOST" : "ATTENDEE");
            participant.setStatus(isOrganizer ? "ACCEPTED" : "PENDING");
            participant.setIsDelete(0);
            participantList.add(participant);
        }
        return saveBatchParticipants(participantList);
    }

    private boolean saveBatchParticipants(List<MeetingParticipant> participantList) {
        if (participantList == null || participantList.isEmpty()) {
            return true;
        }
        for (MeetingParticipant participant : participantList) {
            int result = meetingParticipantMapper.insert(participant);
            if (result <= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean cancelMeeting(Long meetingId, Long userId) {
        if (meetingId == null || meetingId <= 0 || userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Meeting meeting = this.getById(meetingId);
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 检查权限
        if (!meeting.getOrganizerId().equals(userId.toString())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 检查会议状态
        if ("FINISHED".equals(meeting.getStatus()) || "CANCELLED".equals(meeting.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "会议已结束或已取消");
        }
        // 更新会议状态为已取消
        meeting.setStatus("CANCELLED");
        return this.updateById(meeting);
    }

    @Override
    public Page<MeetingVO> getCreatedMeetings(Long userId, long current, long size, HttpServletRequest request) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Meeting> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("creatorId", userId.toString());
        queryWrapper.orderByDesc("createTime");
        Page<Meeting> meetingPage = this.page(new Page<>(current, size), queryWrapper);
        return this.getMeetingVOPage(meetingPage, request);
    }

    @Override
    public Page<MeetingVO> getParticipatedMeetings(Long userId, long current, long size, HttpServletRequest request) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取用户参与的会议ID列表
        QueryWrapper<MeetingParticipant> participantQuery = new QueryWrapper<>();
        participantQuery.eq("userId", userId.toString());
        participantQuery.eq("role", "ATTENDEE");
        List<Long> meetingIds = meetingParticipantMapper.selectList(participantQuery)
                .stream()
                .map(MeetingParticipant::getMeetingId)
                .collect(Collectors.toList());

        if (meetingIds.isEmpty()) {
            return new Page<>(current, size, 0);
        }
        QueryWrapper<Meeting> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", meetingIds);
        queryWrapper.orderByAsc("startTime");
        Page<Meeting> meetingPage = this.page(new Page<>(current, size), queryWrapper);
        return this.getMeetingVOPage(meetingPage, request);
    }

    @Override
    public Page<MeetingVO> getUserMeetings(Long userId, long current, long size, HttpServletRequest request) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取用户参与的会议ID列表
        QueryWrapper<MeetingParticipant> participantQuery = new QueryWrapper<>();
        participantQuery.eq("userId", userId.toString());
        List<Long> participatedMeetingIds = meetingParticipantMapper.selectList(participantQuery)
                .stream()
                .map(MeetingParticipant::getMeetingId)
                .collect(Collectors.toList());

        QueryWrapper<Meeting> queryWrapper = new QueryWrapper<>();
        if (!participatedMeetingIds.isEmpty()) {
            // 查询用户创建的或参与的会议
            queryWrapper.and(wrapper -> wrapper
                    .eq("creatorId", userId.toString())
                    .or()
                    .in("id", participatedMeetingIds));
        } else {
            // 只查询用户创建的会议
            queryWrapper.eq("creatorId", userId.toString());
        }
        queryWrapper.orderByDesc("createTime");
        Page<Meeting> meetingPage = this.page(new Page<>(current, size), queryWrapper);
        return this.getMeetingVOPage(meetingPage, request);
    }

    @Override
    public boolean checkRoomConflict(Long roomId, Date startTime, Date endTime, Long excludeMeetingId) {
        if (roomId == null || startTime == null || endTime == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (endTime.before(startTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "结束时间不能早于开始时间");
        }

        // 构建查询条件
        QueryWrapper<Meeting> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("roomId", roomId)
                .eq("isDelete", 0)
                .ne(excludeMeetingId != null, "id", excludeMeetingId)
                .and(wrapper -> wrapper
                        // 新会议开始时间在已有会议时间段内
                        .apply("? between startTime and endTime", startTime)
                        // 或新会议结束时间在已有会议时间段内
                        .or()
                        .apply("? between startTime and endTime", endTime)
                        // 或新会议时间段完全包含已有会议
                        .or()
                        .apply("startTime between ? and ?", startTime, endTime)
                );

        // 排除已取消和已结束的会议
        queryWrapper.notIn("status", "CANCELLED", "FINISHED");

        // 查询符合条件的会议数量
        return this.count(queryWrapper) > 0;
    }

    @Override
    public List<Meeting> listMeetingsByDate(Date date) {
        if (date == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期不能为空");
        }

        // 获取指定日期的开始时间和结束时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startTime = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endTime = calendar.getTime();

        // 构建查询条件
        QueryWrapper<Meeting> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("startTime", startTime)
                .le("startTime", endTime)
                .or()
                .ge("endTime", startTime)
                .le("endTime", endTime)
                .or()
                .le("startTime", startTime)
                .ge("endTime", endTime);

        queryWrapper.orderByAsc("startTime");

        return this.list(queryWrapper);
    }

    @Override
    public List<Long> getUserMeetingIds(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 1. 获取用户创建的会议
        QueryWrapper<Meeting> meetingQuery = new QueryWrapper<>();
        meetingQuery.eq("creatorId", userId.toString())
                .eq("isDelete", 0);
        List<Long> createdMeetingIds = this.list(meetingQuery)
                .stream()
                .map(Meeting::getId)
                .collect(Collectors.toList());

        // 2. 获取用户参与的会议
        QueryWrapper<MeetingParticipant> participantQuery = new QueryWrapper<>();
        participantQuery.eq("userId", userId)
                .eq("isDelete", 0);
        List<Long> participatedMeetingIds = meetingParticipantMapper.selectList(participantQuery)
                .stream()
                .map(MeetingParticipant::getMeetingId)
                .collect(Collectors.toList());

        // 3. 合并两个列表并去重
        Set<Long> allMeetingIds = new HashSet<>();
        allMeetingIds.addAll(createdMeetingIds);
        allMeetingIds.addAll(participatedMeetingIds);

        // 4. 过滤掉已删除的会议
        if (!allMeetingIds.isEmpty()) {
            QueryWrapper<Meeting> validMeetingQuery = new QueryWrapper<>();
            validMeetingQuery.in("id", allMeetingIds)
                    .eq("isDelete", 0);
            return this.list(validMeetingQuery)
                    .stream()
                    .map(Meeting::getId)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
} 