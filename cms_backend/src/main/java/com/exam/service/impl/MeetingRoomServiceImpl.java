package com.exam.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.common.ErrorCode;
import com.exam.exception.BusinessException;
import com.exam.mapper.MeetingMapper;
import com.exam.mapper.MeetingRoomMapper;
import com.exam.mapper.MeetingTypeMapper;
import com.exam.mapper.UserMapper;
import com.exam.model.dto.meetingroom.MeetingRoomQueryRequest;
import com.exam.model.entity.*;
import com.exam.model.vo.MeetingRoomVO;
import com.exam.model.vo.MeetingVO;
import com.exam.service.MeetingRoomService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import cn.hutool.core.util.StrUtil;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会议室服务实现类
 */
@Service
public class MeetingRoomServiceImpl extends ServiceImpl<MeetingRoomMapper, MeetingRoom>
        implements MeetingRoomService {

    @Resource
    private MeetingMapper meetingMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private MeetingTypeMapper meetingTypeMapper;

    @Override
    public void validMeetingRoom(MeetingRoom meetingRoom, boolean add) {
        if (meetingRoom == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String name = meetingRoom.getName();
        String code = meetingRoom.getCode();
        Integer capacity = meetingRoom.getCapacity();

        // 创建时，参数不能为空
        if (add) {
            if (StringUtils.isAnyBlank(name, code)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议室名称和编号不能为空");
            }
            if (capacity == null || capacity <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议室容量必须大于0");
            }
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议室名称过长");
        }
        if (StringUtils.isNotBlank(code) && code.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议室编号过长");
        }
    }

    @Override
    public QueryWrapper<MeetingRoom> getQueryWrapper(MeetingRoomQueryRequest meetingRoomQueryRequest) {
        if (meetingRoomQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        String name = meetingRoomQueryRequest.getName();
        String code = meetingRoomQueryRequest.getCode();
        Integer minCapacity = meetingRoomQueryRequest.getMinCapacity();
        Integer maxCapacity = meetingRoomQueryRequest.getMaxCapacity();
        String location = meetingRoomQueryRequest.getLocation();
        Integer status = meetingRoomQueryRequest.getStatus();
        String sortField = meetingRoomQueryRequest.getSortField();
        String sortOrder = meetingRoomQueryRequest.getSortOrder();

        QueryWrapper<MeetingRoom> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(code), "code", code);
        queryWrapper.ge(minCapacity != null, "capacity", minCapacity);
        queryWrapper.le(maxCapacity != null, "capacity", maxCapacity);
        queryWrapper.like(StringUtils.isNotBlank(location), "location", location);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortOrder),
                "ascend".equals(sortOrder), sortField);

        return queryWrapper;
    }

    @Override
    public MeetingRoomVO getMeetingRoomVO(MeetingRoom meetingRoom, HttpServletRequest request) {
        if (meetingRoom == null) {
            return null;
        }
        MeetingRoomVO roomVO = new MeetingRoomVO();

        // 手动设置字段，因为字段名不同
        roomVO.setId(meetingRoom.getId());
        roomVO.setRoomName(meetingRoom.getName());
        roomVO.setRoomCode(meetingRoom.getCode());
        //将数据库的会议室图像JSON数据转换为数组
        // 会议室图片：数据库没有 roomImage 或为空时，返回空数组
        if (StringUtils.isNotBlank(meetingRoom.getRoomImage())) {
            roomVO.setRoomImage(JSONUtil.toList(JSONUtil.parseArray(meetingRoom.getRoomImage()), String.class));
        } else {
            roomVO.setRoomImage(new ArrayList<>());
        }

        roomVO.setCapacity(meetingRoom.getCapacity());
        roomVO.setLocation(meetingRoom.getLocation());
        roomVO.setStatus(meetingRoom.getStatus());
        roomVO.setCreateTime(meetingRoom.getCreateTime());
        roomVO.setUpdateTime(meetingRoom.getUpdateTime());

        // 1. 解析设施列表
        if (StringUtils.isNotBlank(meetingRoom.getFacilities())) {
            roomVO.setFacilities(JSONUtil.toList(JSONUtil.parseArray(meetingRoom.getFacilities()), String.class));
        }

        // 3. 查询当前会议状态
        Date now = new Date();
        QueryWrapper<Meeting> meetingQuery = new QueryWrapper<>();
        meetingQuery.eq("roomId", meetingRoom.getId())
                .eq("status", "ONGOING")
                .le("startTime", now)
                .ge("endTime", now)
                .last("LIMIT 1");
        Meeting currentMeeting = meetingMapper.selectOne(meetingQuery);

        if (currentMeeting != null) {
            roomVO.setIsOccupied(true);
            MeetingVO currentMeetingVO = new MeetingVO();
            BeanUtils.copyProperties(currentMeeting, currentMeetingVO);
            roomVO.setCurrentMeeting(currentMeetingVO);
        } else {
            roomVO.setIsOccupied(false);
        }

        // 4. 查询即将进行的会议
        QueryWrapper<Meeting> upcomingQuery = new QueryWrapper<>();
        upcomingQuery.eq("roomId", meetingRoom.getId())
                .eq("status", "PENDING")
                .gt("startTime", now)
                .orderByAsc("startTime")
                .last("LIMIT 5");
        List<Meeting> upcomingMeetings = meetingMapper.selectList(upcomingQuery);
        List<MeetingVO> upcomingMeetingVOs = upcomingMeetings.stream()
                .map(meeting -> {
                    MeetingVO meetingVO = new MeetingVO();
                    BeanUtils.copyProperties(meeting, meetingVO);
                    return meetingVO;
                })
                .collect(Collectors.toList());
        roomVO.setUpcomingMeetings(upcomingMeetingVOs);

        return roomVO;
    }

    @Override
    public Page<MeetingRoomVO> getMeetingRoomVOPage(Page<MeetingRoom> roomPage, HttpServletRequest request) {
        List<MeetingRoom> roomList = roomPage.getRecords();
        Page<MeetingRoomVO> roomVOPage = new Page<>(roomPage.getCurrent(), roomPage.getSize(), roomPage.getTotal());
        List<MeetingRoomVO> roomVOList = roomList.stream()
                .map(room -> getMeetingRoomVO(room, request))
                .collect(Collectors.toList());
        roomVOPage.setRecords(roomVOList);
        return roomVOPage;
    }

    @Override
    public List<Map<String, Object>> getRoomSchedule(Long roomId, Date date) {
        // 检查会议室是否存在
        MeetingRoom room = this.getById(roomId);
        if (room == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会议室不存在");
        }

        // 设置查询时间范围（当天的起止时间）
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startTime = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endTime = cal.getTime();

        // 查询该会议室在指定日期的所有会议
        QueryWrapper<Meeting> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("roomId", roomId)
                .between("startTime", startTime, endTime)
                .ne("status", "CANCELLED") // 排除已取消的会议
                .orderByAsc("startTime");

        List<Meeting> meetings = meetingMapper.selectList(queryWrapper);
        List<Map<String, Object>> scheduleList = new ArrayList<>();

        // 获取所有相关的会议类型
        Set<Long> typeIds = new HashSet<>();
        meetings.forEach(meeting -> typeIds.add(meeting.getTypeId()));
        Map<Long, MeetingType> typeMap = new HashMap<>();
        if (!typeIds.isEmpty()) {
            List<MeetingType> types = meetingTypeMapper.selectBatchIds(typeIds);
            types.forEach(type -> typeMap.put(type.getId(), type));
        }

        // 获取所有相关的用户信息
        Set<Long> userIds = new HashSet<>();
        meetings.forEach(meeting -> userIds.add(Long.parseLong(meeting.getOrganizerId())));
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            users.forEach(user -> userMap.put(user.getId(), user));
        }

        // 构建返回数据
        for (Meeting meeting : meetings) {
            Map<String, Object> scheduleItem = new HashMap<>();
            scheduleItem.put("meetingId", meeting.getId());
            scheduleItem.put("title", meeting.getTitle());
            scheduleItem.put("startTime", meeting.getStartTime());
            scheduleItem.put("endTime", meeting.getEndTime());
            
            // 添加会议类型信息
            MeetingType type = typeMap.get(meeting.getTypeId());
            if (type != null) {
                scheduleItem.put("typeName", type.getTypeName());
            }
            
            // 添加创建人信息
            User organizer = userMap.get(Long.parseLong(meeting.getOrganizerId()));
            if (organizer != null) {
                scheduleItem.put("organizerName", organizer.getUserName());
                scheduleItem.put("organizerAvatar", organizer.getUserAvatar());
            }

            scheduleList.add(scheduleItem);
        }

        return scheduleList;
    }
} 