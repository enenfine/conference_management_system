package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.common.ErrorCode;
import com.exam.exception.BusinessException;
import com.exam.mapper.MeetingTypeMapper;
import com.exam.model.dto.meeting.MeetingTypeQueryRequest;
import com.exam.model.entity.MeetingType;
import com.exam.model.vo.MeetingTypeVO;
import com.exam.service.MeetingTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MeetingTypeServiceImpl extends ServiceImpl<MeetingTypeMapper, MeetingType>
        implements MeetingTypeService {

    @Override
    public long createMeetingType(String typeName, String typeCode, String description) {
        // 校验参数
        if (StringUtils.isAnyBlank(typeName, typeCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (typeName.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "类型名称过长");
        }
        // 校验类型编码是否重复
        QueryWrapper<MeetingType> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type_code", typeCode);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "类型编码已存在");
        }
        // 创建会议类型
        MeetingType meetingType = new MeetingType();
        meetingType.setTypeName(typeName);
        meetingType.setTypeCode(typeCode);
        meetingType.setDescription(description);
        boolean result = this.save(meetingType);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建失败");
        }
        return meetingType.getId();
    }

    @Override
    public boolean deleteMeetingType(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return this.removeById(id);
    }

    @Override
    public boolean updateMeetingType(MeetingType meetingType) {
        if (meetingType == null || meetingType.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验类型编码是否重复
        MeetingType oldType = this.getById(meetingType.getId());
        if (oldType == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (!oldType.getTypeCode().equals(meetingType.getTypeCode())) {
            QueryWrapper<MeetingType> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("type_code", meetingType.getTypeCode());
            long count = this.count(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "类型编码已存在");
            }
        }
        return this.updateById(meetingType);
    }

    @Override
    public MeetingTypeVO getMeetingTypeById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        MeetingType meetingType = this.getById(id);
        if (meetingType == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return convertToVO(meetingType);
    }

    @Override
    public Page<MeetingTypeVO> listMeetingTypeByPage(MeetingTypeQueryRequest meetingTypeQueryRequest) {
        if (meetingTypeQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        long current = meetingTypeQueryRequest.getCurrent();
        long size = meetingTypeQueryRequest.getPageSize();
        
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        Page<MeetingType> meetingTypePage = this.page(new Page<>(current, size),
                getQueryWrapper(meetingTypeQueryRequest));
        
        return new Page<MeetingTypeVO>(meetingTypePage.getCurrent(), meetingTypePage.getSize(), meetingTypePage.getTotal())
                .setRecords(meetingTypePage.getRecords().stream()
                        .map(this::convertToVO)
                        .collect(Collectors.toList()));
    }

    @Override
    public QueryWrapper<MeetingType> getQueryWrapper(MeetingTypeQueryRequest meetingTypeQueryRequest) {
        if (meetingTypeQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        
        QueryWrapper<MeetingType> queryWrapper = new QueryWrapper<>();
        String typeName = meetingTypeQueryRequest.getTypeName();
        String typeCode = meetingTypeQueryRequest.getTypeCode();
        String sortField = meetingTypeQueryRequest.getSortField();
        String sortOrder = meetingTypeQueryRequest.getSortOrder();

        queryWrapper.eq("isDelete", 0);
        if (StringUtils.isNotBlank(typeName)) {
            queryWrapper.like("typeName", typeName);
        }
        if (StringUtils.isNotBlank(typeCode)) {
            queryWrapper.eq("typeCode", typeCode);
        }
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), 
                sortOrder.equals("ascend"), 
                sortField);
        return queryWrapper;
    }

    /**
     * 实体转VO
     * @param meetingType 会议类型
     * @return MeetingTypeVO
     */
    private MeetingTypeVO convertToVO(MeetingType meetingType) {
        if (meetingType == null) {
            return null;
        }
        MeetingTypeVO vo = new MeetingTypeVO();
        BeanUtils.copyProperties(meetingType, vo);
        return vo;
    }
}