package com.exam.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.annotation.AuthCheck;
import com.exam.common.BaseResponse;
import com.exam.common.DeleteRequest;
import com.exam.common.ErrorCode;
import com.exam.common.ResultUtils;
import com.exam.constant.UserConstant;
import com.exam.exception.BusinessException;
import com.exam.exception.ThrowUtils;
import com.exam.model.dto.meetingroom.MeetingRoomAddRequest;
import com.exam.model.dto.meetingroom.MeetingRoomQueryRequest;
import com.exam.model.dto.meetingroom.MeetingRoomUpdateRequest;
import com.exam.model.entity.MeetingRoom;
import com.exam.model.vo.MeetingRoomVO;
import com.exam.service.MeetingRoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Api(tags = "会议室管理")
@RestController
@RequestMapping("/meeting/room")
@Slf4j
public class MeetingRoomController {

    @Resource
    private MeetingRoomService meetingRoomService;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addMeetingRoom(@RequestBody MeetingRoomAddRequest requestBody, HttpServletRequest request) {
        if (requestBody == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setName(requestBody.getRoomName());
        meetingRoom.setCode(requestBody.getRoomCode());
        meetingRoom.setCapacity(requestBody.getCapacity());
        meetingRoom.setLocation(requestBody.getLocation());
        meetingRoom.setStatus(requestBody.getStatus() == null ? 1 : requestBody.getStatus());
        if (requestBody.getFacilities() != null) {
            meetingRoom.setFacilities(JSONUtil.toJsonStr(requestBody.getFacilities()));
        }
        meetingRoomService.validMeetingRoom(meetingRoom, true);
        boolean result = meetingRoomService.save(meetingRoom);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(meetingRoom.getId());
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteMeetingRoom(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        MeetingRoom oldMeetingRoom = meetingRoomService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(oldMeetingRoom == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = meetingRoomService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateMeetingRoom(@RequestBody MeetingRoomUpdateRequest requestBody, HttpServletRequest request) {
        if (requestBody == null || requestBody.getId() == null || requestBody.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        MeetingRoom oldMeetingRoom = meetingRoomService.getById(requestBody.getId());
        ThrowUtils.throwIf(oldMeetingRoom == null, ErrorCode.NOT_FOUND_ERROR);
        MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setId(requestBody.getId());
        meetingRoom.setName(requestBody.getRoomName());
        meetingRoom.setCode(requestBody.getRoomCode());
        meetingRoom.setCapacity(requestBody.getCapacity());
        meetingRoom.setLocation(requestBody.getLocation());
        meetingRoom.setStatus(requestBody.getStatus());
        if (requestBody.getFacilities() != null) {
            meetingRoom.setFacilities(JSONUtil.toJsonStr(requestBody.getFacilities()));
        }
        meetingRoomService.validMeetingRoom(meetingRoom, false);
        boolean result = meetingRoomService.updateById(meetingRoom);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<MeetingRoom> getMeetingRoomById(@RequestParam("id") Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议室 id 不能为空");
        }
        MeetingRoom meetingRoom = meetingRoomService.getById(id);
        ThrowUtils.throwIf(meetingRoom == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(meetingRoom);
    }

    @GetMapping("/get/vo")
    public BaseResponse<MeetingRoomVO> getMeetingRoomVOById(@RequestParam("id") Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会议室 id 不能为空");
        }
        MeetingRoom meetingRoom = meetingRoomService.getById(id);
        ThrowUtils.throwIf(meetingRoom == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(meetingRoomService.getMeetingRoomVO(meetingRoom, request));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<MeetingRoom>> listMeetingRoomByPage(@RequestBody MeetingRoomQueryRequest queryRequest, HttpServletRequest request) {
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
        Page<MeetingRoom> page = meetingRoomService.page(new Page<>(current, size), meetingRoomService.getQueryWrapper(queryRequest));
        return ResultUtils.success(page);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<MeetingRoomVO>> listMeetingRoomVOByPage(@RequestBody MeetingRoomQueryRequest queryRequest, HttpServletRequest request) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<MeetingRoom> page = meetingRoomService.page(new Page<>(current, size), meetingRoomService.getQueryWrapper(queryRequest));
        return ResultUtils.success(meetingRoomService.getMeetingRoomVOPage(page, request));
    }

    @ApiOperation(value = "获取全部会议室")
    @GetMapping("/list/all")
    public BaseResponse<List<Map<String, String>>> listAll(HttpServletRequest request) {
        List<MeetingRoom> list = meetingRoomService.list();
        List<Map<String, String>> result = new ArrayList<>();
        for (MeetingRoom room : list) {
            Map<String, String> map = new HashMap<>();
            map.put("id", String.valueOf(room.getId()));
            map.put("roomName", room.getName());
            map.put("roomCode", room.getCode());
            result.add(map);
        }
        return ResultUtils.success(result);
    }

    @ApiOperation(value = "获取会议室某天的使用情况")
    @GetMapping("/{roomId}/schedule")
    public BaseResponse<List<Map<String, Object>>> getRoomSchedule(@PathVariable Long roomId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        if (roomId == null || roomId <= 0 || date == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(meetingRoomService.getRoomSchedule(roomId, date));
    }
}
