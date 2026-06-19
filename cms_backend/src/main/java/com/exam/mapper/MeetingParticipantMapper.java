package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.model.entity.MeetingParticipant;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 会议参与者 Mapper
 */
public interface MeetingParticipantMapper extends BaseMapper<MeetingParticipant> {

    /**
     * 物理删除某个会议的参会人员记录。
     * 这里不能使用 MyBatis-Plus 的 delete(wrapper)，因为实体里有 @TableLogic，
     * 逻辑删除后旧记录仍然占用 meetingId + userId 唯一索引。
     */
    @Delete("DELETE FROM meeting_participant WHERE meetingId = #{meetingId}")
    int physicalDeleteByMeetingId(@Param("meetingId") Long meetingId);
}
