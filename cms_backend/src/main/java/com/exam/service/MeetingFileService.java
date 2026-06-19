package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.model.entity.MeetingFile;
import com.exam.model.entity.User;
import com.exam.model.vo.MeetingFileVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 会议文件服务
 */
public interface MeetingFileService extends IService<MeetingFile> {

    /**
     * 获取会议文件列表
     *
     * @param meetingId 会议ID
     * @return 文件列表
     */
    List<MeetingFileVO> listMeetingFiles(Long meetingId);

    /**
     * 获取会议文件分页信息
     *
     * @param meetingId 会议ID
     * @param current 当前页码
     * @param size 每页大小
     * @param request HTTP请求
     * @return 文件分页信息
     */
    Page<MeetingFileVO> getMeetingFileVOPage(Long meetingId, long current, long size, HttpServletRequest request);

    /**
     * 上传会议文件
     *
     * @param meetingId 会议ID
     * @param fileName 文件名
     * @param fileUrl 文件URL
     * @param fileSize 文件大小
     * @param userId 上传用户ID
     * @return 文件ID
     */
    Long uploadMeetingFile(Long meetingId, String fileName, String fileUrl, Long fileSize, Long userId);

    /**
     * 删除会议文件
     *
     * @param fileId 文件ID
     * @param userId 操作用户ID
     * @return 是否成功
     */
    boolean removeMeetingFile(Long fileId, Long userId);

    /**
     * 根据会议ID删除所有文件
     *
     * @param meetingId 会议ID
     * @return 是否成功
     */
    boolean removeByMeetingId(Long meetingId);

    /**
     * 上传会议文件
     *
     * @param file 文件
     * @param meetingId 会议ID
     * @param loginUser 当前登录用户
     * @return 会议文件信息
     */
    MeetingFile uploadMeetingFile(MultipartFile file, Long meetingId, User loginUser);

    MeetingFileVO getMeetingFileVO(MeetingFile file, HttpServletRequest request);

    Page<MeetingFileVO> pageMeetingFiles(Long meetingId, long current, long size);

    /**
     * 分页获取用户参与的所有会议的文件列表
     *
     * @param userId 用户ID
     * @param current 当前页
     * @param size 页面大小
     * @return 分页的会议文件列表
     */
    Page<MeetingFileVO> listUserMeetingFiles(Long userId, long current, long size);

    /**
     * 删除会议文件
     *
     * @param fileId 文件ID
     * @return 是否成功
     */
    boolean deleteMeetingFile(Long fileId);

    /**
     * 批量删除会议文件
     *
     * @param fileIds 文件ID列表
     * @return 是否全部成功
     */
    boolean batchDeleteMeetingFiles(List<Long> fileIds);
}