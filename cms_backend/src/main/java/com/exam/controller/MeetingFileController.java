package com.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.BaseResponse;
import com.exam.common.ErrorCode;
import com.exam.common.ResultUtils;
import com.exam.exception.BusinessException;
import com.exam.exception.ThrowUtils;
import com.exam.model.entity.MeetingFile;
import com.exam.model.entity.User;
import com.exam.model.vo.MeetingFileVO;
import com.exam.service.MeetingFileService;
import com.exam.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import io.swagger.annotations.Api;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.stream.Collectors;

/**
 * 会议文件接口
 */
@Api(tags = "会议文件上传与下载")
@RestController
@RequestMapping("/meeting-file")
@Slf4j
public class MeetingFileController {

    @Resource
    private MeetingFileService meetingFileService;

    @Resource
    private UserService userService;

    /**
     * 上传会议文件
     */
    @PostMapping("/upload")
    public BaseResponse<MeetingFile> uploadMeetingFile(@RequestParam("file") MultipartFile file,
            @RequestParam("meetingId") Long meetingId,
            HttpServletRequest request) {
        if (file == null || meetingId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        MeetingFile meetingFile = meetingFileService.uploadMeetingFile(file, meetingId, loginUser);
        return ResultUtils.success(meetingFile);
    }

    /**
     * 获取会议文件列表
     */
    @GetMapping("/list")
    public BaseResponse<List<MeetingFileVO>> listMeetingFiles(@RequestParam("meetingId") Long meetingId,
            HttpServletRequest request) {
        if (meetingId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<MeetingFileVO> meetingFiles = meetingFileService.listMeetingFiles(meetingId);
        return ResultUtils.success(meetingFiles);
    }

    /**
     * 分页获取用户参与的所有会议的文件列表
     */
    /**
     * 分页获取会议文件列表
     *
     * 管理员：查看全部会议文件
     * 普通用户：只查看自己创建或参与会议的文件
     */
    @GetMapping("/my/list/page")
    public BaseResponse<Page<MeetingFileVO>> listMyMeetingFiles(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            HttpServletRequest request) {
        if (current <= 0 || size <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的分页参数");
        }

        // 限制爬虫
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR, "页面大小超出限制");

        try {
            // 获取当前登录用户
            User loginUser = userService.getLoginUser(request);

            // 管理员查看全部会议文件
            if (userService.isAdmin(loginUser)) {
                QueryWrapper<MeetingFile> queryWrapper = new QueryWrapper<>();
                queryWrapper.orderByDesc("createTime");

                Page<MeetingFile> filePage = meetingFileService.page(new Page<>(current, size), queryWrapper);

                Page<MeetingFileVO> fileVOPage = new Page<>(
                        filePage.getCurrent(),
                        filePage.getSize(),
                        filePage.getTotal()
                );

                fileVOPage.setRecords(filePage.getRecords().stream()
                        .map(file -> meetingFileService.getMeetingFileVO(file, request))
                        .collect(Collectors.toList()));

                return ResultUtils.success(fileVOPage);
            }

            // 普通用户获取自己创建/参与会议的文件列表
            Page<MeetingFileVO> meetingFilePage =
                    meetingFileService.listUserMeetingFiles(loginUser.getId(), current, size);

            return ResultUtils.success(meetingFilePage);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("分页获取会议文件列表失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取会议文件列表失败");
        }
    }

    /**
     * 删除会议文件
     */
    @PostMapping("/delete/{fileId}")
    public BaseResponse<Boolean> deleteMeetingFile(@PathVariable Long fileId, HttpServletRequest request) {
        if (fileId == null || fileId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的文件ID");
        }

        try {
            // 获取当前登录用户
            User loginUser = userService.getLoginUser(request);
            
            // 获取文件信息
            MeetingFile meetingFile = meetingFileService.getById(fileId);
            if (meetingFile == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
            }

            // 检查权限（只有文件上传者和管理员可以删除）
            boolean isAdmin = userService.isAdmin(loginUser);
            boolean isUploader = meetingFile.getUploadUserId().equals(loginUser.getId());
            if (!isAdmin && !isUploader) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除此文件");
            }

            // 删除文件
            boolean result = meetingFileService.deleteMeetingFile(fileId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除文件失败");
            
            return ResultUtils.success(true);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除会议文件失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除文件失败");
        }
    }

    /**
     * 批量删除会议文件
     */
    @PostMapping("/delete/batch")
    public BaseResponse<Boolean> batchDeleteMeetingFiles(@RequestBody List<Long> fileIds, HttpServletRequest request) {
        if (fileIds == null || fileIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件ID列表不能为空");
        }

        try {
            // 获取当前登录用户
            User loginUser = userService.getLoginUser(request);
            boolean isAdmin = userService.isAdmin(loginUser);

            // 获取所有文件信息
            List<MeetingFile> meetingFiles = meetingFileService.listByIds(fileIds);
            if (meetingFiles.isEmpty()) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到任何文件");
            }

            // 检查权限
            if (!isAdmin) {
                // 非管理员只能删除自己上传的文件
                for (MeetingFile file : meetingFiles) {
                    if (!file.getUploadUserId().equals(loginUser.getId())) {
                        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除其他用户上传的文件");
                    }
                }
            }

            // 批量删除文件
            boolean result = meetingFileService.batchDeleteMeetingFiles(fileIds);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "批量删除文件失败");
            
            return ResultUtils.success(true);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除会议文件失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "批量删除文件失败");
        }
    }

} 
