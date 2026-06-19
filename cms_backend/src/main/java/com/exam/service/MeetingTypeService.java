package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.model.dto.meeting.MeetingTypeQueryRequest;
import com.exam.model.entity.MeetingType;
import com.exam.model.vo.MeetingTypeVO;

import java.util.List;

public interface MeetingTypeService extends IService<MeetingType> {

    /**
     * 创建会议类型
     * @param typeName 类型名称
     * @param typeCode 类型编码
     * @param description 描述
     * @return 会议类型id
     */
    long createMeetingType(String typeName, String typeCode, String description);

    /**
     * 删除会议类型
     * @param id 会议类型id
     * @return 是否成功
     */
    boolean deleteMeetingType(long id);

    /**
     * 更新会议类型
     * @param meetingType 会议类型
     * @return 是否成功
     */
    boolean updateMeetingType(MeetingType meetingType);

    /**
     * 根据 id 获取会议类型
     * @param id 会议类型id
     * @return 会议类型
     */
    MeetingTypeVO getMeetingTypeById(long id);

    /**
     * 获取会议类型列表
     * @param meetingTypeQueryRequest 查询条件
     * @return 会议类型列表
     */
    Page<MeetingTypeVO> listMeetingTypeByPage(MeetingTypeQueryRequest meetingTypeQueryRequest);

    /**
     * 获取查询条件
     * @param meetingTypeQueryRequest 查询条件
     * @return QueryWrapper
     */
    QueryWrapper<MeetingType> getQueryWrapper(MeetingTypeQueryRequest meetingTypeQueryRequest);
}