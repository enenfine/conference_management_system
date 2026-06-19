package com.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.BaseResponse;
import com.exam.common.ErrorCode;
import com.exam.common.ResultUtils;
import com.exam.exception.BusinessException;
import com.exam.exception.ThrowUtils;
import com.exam.model.dto.meeting.MeetingTypeAddRequest;
import com.exam.model.dto.meeting.MeetingTypeQueryRequest;
import com.exam.model.dto.meeting.MeetingTypeUpdateRequest;
import com.exam.model.entity.MeetingType;
import com.exam.model.vo.MeetingTypeVO;
import com.exam.service.MeetingTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import springfox.documentation.annotations.ApiIgnore;

@Api(tags = "会议类型管理")
@ApiIgnore
@RestController
@RequestMapping("/meeting-type")
@Slf4j
public class MeetingTypeController {

    @Resource
    private MeetingTypeService meetingTypeService;

    @ApiOperation("创建会议类型")
    @PostMapping("/add")
    public BaseResponse<Long> addMeetingType(@RequestBody MeetingTypeAddRequest meetingTypeAddRequest) {
        // 参数校验
        if (meetingTypeAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String typeName = meetingTypeAddRequest.getTypeName();
        String typeCode = meetingTypeAddRequest.getTypeCode();
        String description = meetingTypeAddRequest.getDescription();
        
        // 校验必填参数
        if (StringUtils.isAnyBlank(typeName, typeCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "类型名称或编码不能为空");
        }
        // 校验长度限制
        if (typeName.length() > 50 || typeCode.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "类型名称或编码超出长度限制");
        }
        
        try {
            Long typeId = meetingTypeService.createMeetingType(typeName, typeCode, description);
            return ResultUtils.success(typeId);
        } catch (Exception e) {
            log.error("创建会议类型失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建会议类型失败");
        }
    }

    @ApiOperation("删除会议类型")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteMeetingType(
            @ApiParam(value = "会议类型ID", required = true) @RequestParam("id") long id) {
        // 参数校验
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的会议类型ID");
        }
        
        // 校验是否存在
        MeetingType meetingType = meetingTypeService.getById(id);
        ThrowUtils.throwIf(meetingType == null, ErrorCode.NOT_FOUND_ERROR, "会议类型不存在");
        
        try {
            boolean result = meetingTypeService.deleteMeetingType(id);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("删除会议类型失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除会议类型失败");
        }
    }

    @ApiOperation("更新会议类型")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateMeetingType(@RequestBody MeetingTypeUpdateRequest meetingTypeUpdateRequest) {
        // 参数校验
        if (meetingTypeUpdateRequest == null || meetingTypeUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        
        // 校验必填参数
        String typeName = meetingTypeUpdateRequest.getTypeName();
        String typeCode = meetingTypeUpdateRequest.getTypeCode();
        if (StringUtils.isAnyBlank(typeName, typeCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "类型名称或编码不能为空");
        }
        
        // 校验长度限制
        if (typeName.length() > 50 || typeCode.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "类型名称或编码超出长度限制");
        }
        
        // 校验是否存在
        Long id = meetingTypeUpdateRequest.getId();
        MeetingType oldMeetingType = meetingTypeService.getById(id);
        ThrowUtils.throwIf(oldMeetingType == null, ErrorCode.NOT_FOUND_ERROR, "会议类型不存在");
        
        try {
            MeetingType meetingType = new MeetingType();
            BeanUtils.copyProperties(meetingTypeUpdateRequest, meetingType);
            boolean result = meetingTypeService.updateMeetingType(meetingType);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("更新会议类型失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新会议类型失败");
        }
    }

    @ApiOperation("根据ID获取会议类型")
    @GetMapping("/get")
    public BaseResponse<MeetingTypeVO> getMeetingTypeById(
            @ApiParam(value = "会议类型ID", required = true) @RequestParam("id") long id) {
        // 参数校验
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的会议类型ID");
        }
        
        try {
            MeetingTypeVO meetingTypeVO = meetingTypeService.getMeetingTypeById(id);
            ThrowUtils.throwIf(meetingTypeVO == null, ErrorCode.NOT_FOUND_ERROR, "会议类型不存在");
            return ResultUtils.success(meetingTypeVO);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取会议类型失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取会议类型失败");
        }
    }

    @ApiOperation("获取会议类型列表")
    @GetMapping("/list")
    public BaseResponse<List<MeetingTypeVO>> listMeetingTypes() {
        try {
            // 获取所有会议类型
            List<MeetingType> meetingTypes = meetingTypeService.list();
            // 转换为VO对象
            List<MeetingTypeVO> meetingTypeVOList = meetingTypes.stream()
                    .map(type -> {
                        MeetingTypeVO vo = new MeetingTypeVO();
                        BeanUtils.copyProperties(type, vo);
                        return vo;
                    })
                    .collect(Collectors.toList());
            return ResultUtils.success(meetingTypeVOList);
        } catch (Exception e) {
            log.error("获取会议类型列表失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取会议类型列表失败");
        }
    }

    @ApiOperation("分页获取会议类型列表")
    @PostMapping("/list/page")
    public BaseResponse<Page<MeetingTypeVO>> listMeetingTypeByPage(@RequestBody MeetingTypeQueryRequest meetingTypeQueryRequest) {
        // 参数校验
        if (meetingTypeQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        
        // 校验分页参数
        long current = meetingTypeQueryRequest.getCurrent();
        long pageSize = meetingTypeQueryRequest.getPageSize();
        if (current <= 0 || pageSize <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的分页参数");
        }
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "页面大小超出限制");
        
        try {
            Page<MeetingTypeVO> page = meetingTypeService.listMeetingTypeByPage(meetingTypeQueryRequest);
            return ResultUtils.success(page);
        } catch (Exception e) {
            log.error("分页获取会议类型列表失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "分页获取会议类型列表失败");
        }
    }
}
