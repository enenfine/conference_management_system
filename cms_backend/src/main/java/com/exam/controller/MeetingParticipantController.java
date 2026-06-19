package com.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.BaseResponse;
import com.exam.common.ErrorCode;
import com.exam.common.ResultUtils;
import com.exam.exception.BusinessException;
import com.exam.model.dto.meetingparticipant.MeetingParticipantAddRequest;
import com.exam.model.dto.meetingparticipant.MeetingParticipantQueryRequest;
import com.exam.model.entity.Meeting;
import com.exam.model.entity.MeetingParticipant;
import com.exam.model.entity.User;
import com.exam.model.vo.MeetingParticipantVO;
import com.exam.model.vo.MeetingVO;
import com.exam.service.MeetingParticipantService;
import com.exam.service.MeetingService;
import com.exam.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 会议参与者接口
 */
@ApiIgnore
@RestController
@RequestMapping("/meeting-participant")
@Slf4j
@Api(tags = "会议参与管理", description = "包括参会人员的增删改查功能")
public class MeetingParticipantController {

    @Resource
    private MeetingParticipantService meetingParticipantService;

    @Resource
    private UserService userService;

    @Resource
    private MeetingService meetingService;

    /**
     * 添加会议参与者
     */
    @PostMapping("/add")
    public BaseResponse<Long> addMeetingParticipant(@RequestBody MeetingParticipantAddRequest participantAddRequest,
            HttpServletRequest request) {
        if (participantAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long participantId = meetingParticipantService.addMeetingParticipant(participantAddRequest, loginUser);
        return ResultUtils.success(participantId);
    }

    /**
     * 删除会议参与者
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteMeetingParticipant(@RequestParam Long meetingId, @RequestParam Long userId,
            HttpServletRequest request) {
        if (meetingId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = meetingParticipantService.removeMeetingParticipant(meetingId, userId, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取会议参与者列表
     */
    @GetMapping("/list")
    public BaseResponse<List<MeetingParticipantVO>> listMeetingParticipants(@RequestParam Long meetingId,
            HttpServletRequest request) {
        if (meetingId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<MeetingParticipantVO> participants = meetingParticipantService.listMeetingParticipants(meetingId);
        return ResultUtils.success(participants);
    }

    /**
     * 分页获取会议参与者列表
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<MeetingParticipantVO>> listMeetingParticipantsByPage(
            @RequestBody MeetingParticipantQueryRequest queryRequest,
            HttpServletRequest request) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
        // 限制爬虫
        if (size > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<MeetingParticipant> participantPage = meetingParticipantService.page(new Page<>(current, size),
                meetingParticipantService.getQueryWrapper(queryRequest));
        return ResultUtils.success(meetingParticipantService.getMeetingParticipantVOPage(participantPage, request));
    }

    /**
     * 获取用户参与的会议列表
     */
    @GetMapping("/user/meetings")
    public BaseResponse<List<Long>> getUserMeetings(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<Long> meetingIds = meetingParticipantService.getUserMeetings(loginUser.getId());
        return ResultUtils.success(meetingIds);
    }

    /**
     * 移除参会人员
     */
    @PostMapping("/remove")
    public BaseResponse<Boolean> removeMeetingParticipant(@RequestParam("meetingId") Long meetingId,
            @RequestParam("userId") Long userId,
            HttpServletRequest request) {
        if (meetingId == null || meetingId <= 0 || userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验会议是否存在
        Meeting meeting = meetingService.getById(meetingId);
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 只有会议发起人可以移除参会人员
        User loginUser = userService.getLoginUser(request);
        if (!meeting.getOrganizerId().equals(loginUser.getId().toString())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 移除参会人员
        QueryWrapper<MeetingParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meetingId", meetingId);
        queryWrapper.eq("userId", userId);
        boolean result = meetingParticipantService.remove(queryWrapper);
        return ResultUtils.success(result);
    }

    /**
     * 批量添加会议参与者
     */
    @PostMapping("/batch/add")
    public BaseResponse<Boolean> batchAddMeetingParticipant(
            @RequestParam("meetingId") Long meetingId,
            @RequestBody List<Long> userIds,
            HttpServletRequest request) {
        if (meetingId == null || meetingId <= 0 || userIds == null || userIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验会议是否存在
        Meeting meeting = meetingService.getById(meetingId);
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 校验权限
        User loginUser = userService.getLoginUser(request);
        if (!meeting.getOrganizerId().equals(loginUser.getId().toString())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 批量添加参会人员
        List<MeetingParticipant> participantList = new ArrayList<>();
        for (Long userId : userIds) {
            MeetingParticipant participant = new MeetingParticipant();
            participant.setMeetingId(meetingId);
            participant.setUserId(userId);
            participantList.add(participant);
        }
        boolean result = meetingParticipantService.saveBatch(participantList);
        return ResultUtils.success(result);
    }

    /**
     * 获取参会人员详情
     */
    @GetMapping("/get")
    public BaseResponse<MeetingParticipantVO> getMeetingParticipant(
            @RequestParam("meetingId") Long meetingId,
            @RequestParam("userId") Long userId,
            HttpServletRequest request) {
        if (meetingId == null || meetingId <= 0 || userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<MeetingParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meetingId", meetingId);
        queryWrapper.eq("userId", userId);
        MeetingParticipant participant = meetingParticipantService.getOne(queryWrapper);
        if (participant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(meetingParticipantService.getMeetingParticipantVO(participant, request));
    }

    /**
     * 更新参会人员信息
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateMeetingParticipant(
            @RequestBody MeetingParticipant participant,
            HttpServletRequest request) {
        if (participant == null || participant.getMeetingId() == null || participant.getUserId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验会议是否存在
        Meeting meeting = meetingService.getById(participant.getMeetingId());
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 校验权限
        User loginUser = userService.getLoginUser(request);
        if (!meeting.getOrganizerId().equals(loginUser.getId().toString())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 更新参会人员信息
        QueryWrapper<MeetingParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meetingId", participant.getMeetingId());
        queryWrapper.eq("userId", participant.getUserId());
        boolean result = meetingParticipantService.update(participant, queryWrapper);
        return ResultUtils.success(result);
    }

    /**
     * 确认参会
     */
    @PostMapping("/{meetingId}/confirm")
    @ApiOperation(value = "确认参会", notes = "确认参加指定会议")
    @ApiResponses({
        @ApiResponse(code = 200, message = "确认成功"),
        @ApiResponse(code = 400, message = "请求参数错误"),
        @ApiResponse(code = 401, message = "未登录"),
        @ApiResponse(code = 404, message = "会议不存在"),
        @ApiResponse(code = 500, message = "系统错误")
    })
    public BaseResponse<Boolean> confirmParticipation(
            @PathVariable Long meetingId,
            HttpServletRequest request) {
        if (meetingId == null || meetingId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 校验会议是否存在
        Meeting meeting = meetingService.getById(meetingId);
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会议不存在");
        }
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 查询参会记录
        QueryWrapper<MeetingParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meetingId", meetingId);
        queryWrapper.eq("userId", loginUser.getId());
        MeetingParticipant participant = meetingParticipantService.getOne(queryWrapper);
        
        if (participant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "您不是该会议的参会人员");
        }
        
        // 更新参会状态为已确认
        participant.setStatus("ACCEPTED");
        boolean result = meetingParticipantService.updateById(participant);
        
        return ResultUtils.success(result);
    }
}   
