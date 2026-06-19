package com.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.annotation.AuthCheck;
import com.exam.common.BaseResponse;
import com.exam.common.DeleteRequest;
import com.exam.common.ErrorCode;
import com.exam.common.ResultUtils;
import com.exam.constant.UserConstant;
import com.exam.exception.BusinessException;
import com.exam.exception.ThrowUtils;
import com.exam.mapper.MeetingParticipantMapper;
import com.exam.model.dto.meeting.MeetingAddRequest;
import com.exam.model.dto.meeting.MeetingQueryRequest;
import com.exam.model.dto.meeting.MeetingUpdateRequest;
import com.exam.model.entity.Meeting;
import com.exam.model.entity.MeetingParticipant;
import com.exam.model.entity.User;
import com.exam.model.entity.MeetingRoom;
import com.exam.model.vo.MeetingVO;
import com.exam.service.MeetingService;
import com.exam.service.UserService;
import com.exam.service.MeetingRoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会议接口
 */
@Api(tags = "会议管理")
@RestController
@RequestMapping("/meeting")
@Slf4j
public class MeetingController {

    @Resource
    private MeetingService meetingService;

    @Resource
    private UserService userService;

    @Resource
    private MeetingParticipantMapper meetingParticipantMapper;

    @Resource
    private MeetingRoomService meetingRoomService;

    // region 增删改查

    @ApiOperation("创建会议")
    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Long> addMeeting(@RequestBody MeetingAddRequest meetingAddRequest,
                                         HttpServletRequest request) {
        if (meetingAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 检查会议室状态
        Long roomId = meetingAddRequest.getRoomId();
        MeetingRoom meetingRoom = meetingRoomService.getById(roomId);
        if (meetingRoom == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会议室不存在");
        }
        if (meetingRoom.getStatus() == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "会议室维护中，暂不可用");
        }
        if (meetingRoom.getStatus() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "会议室当前不可用");
        }

        // 检查时间冲突
        Date startTime = meetingAddRequest.getStartTime();
        Date endTime = meetingAddRequest.getEndTime();
        QueryWrapper<Meeting> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("roomId", roomId)
                .ne("status", "CANCELLED")
                .and(wrapper -> wrapper
                        .between("startTime", startTime, endTime)
                        .or()
                        .between("endTime", startTime, endTime)
                        .or()
                        .le("startTime", startTime)
                        .ge("endTime", endTime)
                );

        long count = meetingService.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该时间段会议室已被预订");
        }

        Meeting meeting = new Meeting();
        BeanUtils.copyProperties(meetingAddRequest, meeting);
        User loginUser = userService.getLoginUser(request);
        meeting.setOrganizerId(loginUser.getId().toString());
        meeting.setStatus("PENDING"); // 默认待开始状态
        meeting.setMeetingCode(loginUser.getId().toString() + meeting.getTypeId() + meeting.getRoomId() + "-" + generateRandomNumber());
        meetingService.validMeeting(meeting, true);
        boolean result = meetingService.save(meeting);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        // 添加参会人员。发起人单独作为 HOST 插入，先从普通参会人列表中过滤掉，避免唯一索引冲突。
        if (meetingAddRequest.getParticipantIds() != null) {
            List<Long> participantIds = meetingAddRequest.getParticipantIds().stream()
                    .filter(Objects::nonNull)
                    .filter(userId -> !Objects.equals(userId, loginUser.getId()))
                    .distinct()
                    .collect(Collectors.toList());
            meetingService.updateMeetingParticipants(meeting.getId(), participantIds);
        }

        long newMeetingId = meeting.getId();
        MeetingParticipant meetingParticipant = new MeetingParticipant();
        meetingParticipant.setMeetingId(newMeetingId);
        meetingParticipant.setUserId(loginUser.getId());
        meetingParticipant.setRole("HOST");
        meetingParticipant.setStatus("ACCEPTED");
        meetingParticipant.setIsDelete(0);
        meetingParticipantMapper.insert(meetingParticipant);


        return ResultUtils.success(newMeetingId);
    }

    public static String generateRandomNumber() {
        StringBuilder randomNumber = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            randomNumber.append(random.nextInt(10));
            if ((i + 1) % 3 == 0 && i < 6) {
                randomNumber.append("-");
            }
        }

        return randomNumber.toString();
    }

    /**
     * 更新会议状态
     */
    @PostMapping("/update/status")
    public BaseResponse<Boolean> updateMeetingStatus(@RequestParam("id") Long id,
                                                     @RequestParam("status") String status,
                                                     HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Meeting meeting = meetingService.getById(id);
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 只有会议发起人可以更新状态
        if (!meeting.getOrganizerId().equals(loginUser.getId().toString())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        meeting.setStatus(status);
        boolean result = meetingService.updateById(meeting);
        return ResultUtils.success(result);
    }

    /**
     * 取消会议
     */
    @PostMapping("/cancel")
    public BaseResponse<Boolean> cancelMeeting(@RequestParam("id") Long id,
                                               HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = meetingService.cancelMeeting(id, loginUser.getId());
        return ResultUtils.success(result);
    }

    @ApiOperation("删除会议")
    @PostMapping("/delete")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> deleteMeeting(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Meeting meeting = meetingService.getById(deleteRequest.getId());
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        User loginUser = userService.getLoginUser(request);

        // 管理员端允许管理员删除任意会议；普通用户仍然只能删除自己组织的会议
        if (!meeting.getOrganizerId().equals(loginUser.getId().toString()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除该会议");
        }

        // 删除会议前先物理删除参会关系，避免 meeting_participant 逻辑删除残留影响后续新增/编辑
        meetingParticipantMapper.physicalDeleteByMeetingId(deleteRequest.getId());

        boolean result = meetingService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    @ApiOperation("更新会议")
    @PostMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> updateMeeting(@RequestBody MeetingUpdateRequest meetingUpdateRequest, HttpServletRequest request) {
        if (meetingUpdateRequest == null || meetingUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Meeting meeting = new Meeting();
        BeanUtils.copyProperties(meetingUpdateRequest, meeting);
        User loginUser = userService.getLoginUser(request);
        long id = meetingUpdateRequest.getId();
        // 判断是否存在
        Meeting oldMeeting = meetingService.getById(id);
        ThrowUtils.throwIf(oldMeeting == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可更新
        if (!oldMeeting.getOrganizerId().equals(loginUser.getId().toString()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        meetingService.validMeeting(meeting, false);
        boolean result = meetingService.updateById(meeting);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 更新参会人员
        if (meetingUpdateRequest.getParticipantIds() != null) {
            meetingService.updateMeetingParticipants(meeting.getId(), meetingUpdateRequest.getParticipantIds());
        }
        return ResultUtils.success(true);
    }

    @ApiOperation("根据ID获取会议信息")
    @GetMapping("/get")
    public BaseResponse<MeetingVO> getMeetingById(@RequestParam("id") Long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Meeting meeting = meetingService.getById(id);
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(meetingService.getMeetingVO(meeting, request));
    }

    /**
     * 根据 id 获取会议（封装类）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<MeetingVO> getMeetingVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Meeting meeting = meetingService.getById(id);
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(meetingService.getMeetingVO(meeting, request));
    }

    @ApiOperation("分页获取会议列表")
    @PostMapping("/list/page")
    public BaseResponse<Page<MeetingVO>> listMeetingByPage(@RequestBody MeetingQueryRequest meetingQueryRequest,
                                                           HttpServletRequest request) {
        long current = meetingQueryRequest.getCurrent();
        long size = meetingQueryRequest.getPageSize();
        Page<Meeting> meetingPage = meetingService.page(new Page<>(current, size),
                meetingService.getQueryWrapper(meetingQueryRequest));
        return ResultUtils.success(meetingService.getMeetingVOPage(meetingPage, request));
    }

    /**
     * 分页获取会议列表（封装类）
     *
     * @param meetingQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<MeetingVO>> listMeetingVOByPage(@RequestBody MeetingQueryRequest meetingQueryRequest,
                                                             HttpServletRequest request) {
        if (meetingQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = meetingQueryRequest.getCurrent();
        long size = meetingQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        Page<Meeting> meetingPage = meetingService.page(new Page<>(current, size),
                meetingService.getQueryWrapper(meetingQueryRequest));
        return ResultUtils.success(meetingService.getMeetingVOPage(meetingPage, request));
    }

    /**
     * 获取我创建的会议列表
     *
     * @param current 当前页码
     * @param size    页面大小
     * @param request HTTP请求
     * @return 分页会议列表
     */
    @GetMapping("/my/created")
    public BaseResponse<Page<MeetingVO>> getMyCreatedMeetings(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<MeetingVO> meetingVOPage = meetingService.getCreatedMeetings(loginUser.getId(), current, size, request);
        return ResultUtils.success(meetingVOPage);
    }

    /**
     * 获取我参与的会议列表
     */
    @GetMapping("/my/participated")
    public BaseResponse<Page<MeetingVO>> getMyParticipatedMeetings(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,

            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<MeetingVO> meetingVOPage = meetingService.getParticipatedMeetings(loginUser.getId(), current, size, request);
        return ResultUtils.success(meetingVOPage);
    }

    /**
     * 搜索会议
     *
     * @param searchText 搜索关键词（会议标题）
     * @param status     会议状态
     * @param roomId     会议室ID
     * @param typeId     会议类型ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param current    当前页码
     * @param size       页面大小
     * @param request    HTTP请求
     * @return 分页会议列表
     */
    @GetMapping("/search")
    public BaseResponse<Page<MeetingVO>> searchMeetings(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        // 参数校验
        if (current <= 0 || size <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页参数错误");
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR, "页面大小超出限制");
        if (startTime != null && endTime != null && endTime.before(startTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "结束时间不能早于开始时间");
        }

        // 构建查询请求
        MeetingQueryRequest queryRequest = new MeetingQueryRequest();
        queryRequest.setTitle(searchText);
        queryRequest.setStatus(status);
        queryRequest.setRoomId(roomId);
        queryRequest.setTypeId(typeId);
        queryRequest.setStartTime(startTime);
        queryRequest.setEndTime(endTime);
        queryRequest.setCurrent(current);
        queryRequest.setPageSize(size);

        // 执行查询
        Page<Meeting> meetingPage = meetingService.page(
                new Page<>(current, size),
                meetingService.getQueryWrapper(queryRequest)
        );

        return ResultUtils.success(meetingService.getMeetingVOPage(meetingPage, request));
    }

    /**
     * 检查会议室是否有时间冲突
     */
    @GetMapping("/check/room/conflict")
    public BaseResponse<Boolean> checkRoomConflict(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(required = false) Long excludeMeetingId) {
        if (roomId == null || startTime == null || endTime == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean hasConflict = meetingService.checkRoomConflict(roomId, startTime, endTime, excludeMeetingId);
        return ResultUtils.success(hasConflict);
    }

    @ApiOperation("获取指定日期的会议列表")
    @GetMapping("/list/date")
    public BaseResponse<List<MeetingVO>> listMeetingsByDate(
            @ApiParam(value = "日期", required = true, example = "2024-03-20")
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
            HttpServletRequest request) {
        // 参数校验
        if (date == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期不能为空");
        }

        try {
            // 获取当前登录用户
            User loginUser = userService.getLoginUser(request);

            // 获取指定日期的会议列表
            List<Meeting> meetings = meetingService.listMeetingsByDate(date);

            // 转换为VO对象
            List<MeetingVO> meetingVOList = meetings.stream()
                    .map(meeting -> meetingService.getMeetingVO(meeting, request))
                    .collect(Collectors.toList());

            return ResultUtils.success(meetingVOList);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取指定日期的会议列表失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取会议列表失败");
        }
    }

    @ApiOperation("获取我的会议列表")
    @GetMapping("/my/list")
    public BaseResponse<List<MeetingVO>> listMyMeetings(HttpServletRequest request) {
        try {
            // 获取当前登录用户
            User loginUser = userService.getLoginUser(request);

            // 构建查询条件
            MeetingQueryRequest queryRequest = new MeetingQueryRequest();
            queryRequest.setOrganizerId(String.valueOf(loginUser.getId()));

            // 获取会议列表
            List<Meeting> meetings = meetingService.list(
                    meetingService.getQueryWrapper(queryRequest)
            );

            // 转换为VO对象
            List<MeetingVO> meetingVOList = meetings.stream()
                    .map(meeting -> meetingService.getMeetingVO(meeting, request))
                    .collect(Collectors.toList());

            return ResultUtils.success(meetingVOList);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取我的会议列表失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取会议列表失败");
        }
    }

    @ApiOperation("分页获取我的会议列表")
    @PostMapping("/my/list/page")
    public BaseResponse<Page<MeetingVO>> listMyMeetingByPage(
            @RequestBody MeetingQueryRequest meetingQueryRequest,
            HttpServletRequest request) {
        // 参数校验
        if (meetingQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        // 校验分页参数
        long current = meetingQueryRequest.getCurrent();
        long size = meetingQueryRequest.getPageSize();
        if (current <= 0 || size <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的分页参数");
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR, "页面大小超出限制");

        try {
            // 获取当前登录用户
            User loginUser = userService.getLoginUser(request);

            // 设置查询条件
            meetingQueryRequest.setOrganizerId(String.valueOf(loginUser.getId()));

            // 执行分页查询
            Page<Meeting> meetingPage = meetingService.page(
                    new Page<>(current, size),
                    meetingService.getQueryWrapper(meetingQueryRequest)
            );

            // 转换为VO分页对象
            Page<MeetingVO> meetingVOPage = meetingService.getMeetingVOPage(meetingPage, request);

            return ResultUtils.success(meetingVOPage);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("分页获取我的会议列表失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取会议列表失败");
        }
    }

    @ApiOperation("同步会议状态")
    @GetMapping("/sync/status")
    public BaseResponse<Boolean> syncMeetingStatus() {
        // 获取所有未取消的会议
        QueryWrapper<Meeting> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", "CANCELLED");
        List<Meeting> meetings = meetingService.list(queryWrapper);

        Date now = new Date();
        boolean result = true;

        for (Meeting meeting : meetings) {
            String newStatus = meeting.getStatus();
            // 只更新非取消状态的会议
            if (!"CANCELLED".equals(meeting.getStatus())) {
                if (now.before(meeting.getStartTime())) {
                    newStatus = "PENDING";
                } else if (now.after(meeting.getEndTime())) {
                    newStatus = "FINISHED";
                } else {
                    newStatus = "ONGOING";
                }

                if (!newStatus.equals(meeting.getStatus())) {
                    meeting.setStatus(newStatus);
                    result &= meetingService.updateById(meeting);
                }
            }
        }

        return ResultUtils.success(result);
    }

}