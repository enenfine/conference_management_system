package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.common.ErrorCode;
import com.exam.config.FileUploadConfig;
import com.exam.exception.BusinessException;
import com.exam.mapper.MeetingFileMapper;
import com.exam.mapper.MeetingMapper;
import com.exam.mapper.MeetingParticipantMapper;
import com.exam.model.entity.Meeting;
import com.exam.model.entity.MeetingFile;
import com.exam.model.entity.FileType;
import com.exam.model.entity.User;
import com.exam.model.vo.MeetingFileVO;
import com.exam.model.vo.FileTypeVO;
import com.exam.service.MeetingFileService;
import com.exam.service.FileTypeService;
import com.exam.service.UserService;
import com.exam.service.MeetingService;
import com.exam.service.MeetingParticipantService;
import com.exam.utils.FileUploadUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会议文件服务实现类
 */
@Service
public class MeetingFileServiceImpl extends ServiceImpl<MeetingFileMapper, MeetingFile>
        implements MeetingFileService {

    @Resource
    private MeetingMapper meetingMapper;

    @Resource
    private UserService userService;

    @Resource
    private FileTypeService fileTypeService;

    @Resource
    private MeetingParticipantMapper meetingParticipantMapper;

    @Resource
    private MeetingService meetingService;

    @Override
    public List<MeetingFileVO> listMeetingFiles(Long meetingId) {
        if (meetingId == null || meetingId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取文件列表
        QueryWrapper<MeetingFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meetingId", meetingId);
        queryWrapper.orderByDesc("createTime");
        List<MeetingFile> fileList = this.list(queryWrapper);

        if (fileList.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取文件类型信息
        Set<Long> typeIds = fileList.stream()
                .map(MeetingFile::getTypeId)
                .collect(Collectors.toSet());
        final Map<Long, FileTypeVO> typeMap = new HashMap<>();
        if (!typeIds.isEmpty()) {
            List<FileType> fileTypes = fileTypeService.listByIds(typeIds);
            typeMap.putAll(fileTypes.stream()
                    .map(type -> {
                        FileTypeVO typeVO = new FileTypeVO();
                        BeanUtils.copyProperties(type, typeVO);
                        return typeVO;
                    })
                    .collect(Collectors.toMap(FileTypeVO::getId, type -> type)));
        }

        // 获取上传用户信息
        Set<Long> userIds = fileList.stream()
                .map(MeetingFile::getUploadUserId)
                .collect(Collectors.toSet());
        final Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 封装返回结果
        return fileList.stream().map(file -> {
            MeetingFileVO fileVO = new MeetingFileVO();
            BeanUtils.copyProperties(file, fileVO);

            // 设置文件类型信息
            FileTypeVO fileType = typeMap.get(file.getTypeId());
            if (fileType != null) {
                fileVO.setFileType(fileType);
            }

            // 设置上传用户信息
            User user = userMap.get(file.getUploadUserId());
            if (user != null) {
                fileVO.setUploaderName(user.getUserName());
            }
            return fileVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<MeetingFileVO> getMeetingFileVOPage(Long meetingId, long current, long size, HttpServletRequest request) {
        if (meetingId == null || meetingId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 分页查询文件列表
        QueryWrapper<MeetingFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meetingId", meetingId);
        queryWrapper.orderByDesc("createTime");
        Page<MeetingFile> filePage = this.page(new Page<>(current, size), queryWrapper);
        List<MeetingFile> fileList = filePage.getRecords();

        if (fileList.isEmpty()) {
            return new Page<>(current, size, filePage.getTotal());
        }

        // 获取文件类型信息
        Set<Long> typeIds = fileList.stream()
                .map(MeetingFile::getTypeId)
                .collect(Collectors.toSet());
        final Map<Long, FileTypeVO> typeMap = new HashMap<>();
        if (!typeIds.isEmpty()) {
            List<FileType> fileTypes = fileTypeService.listByIds(typeIds);
            typeMap.putAll(fileTypes.stream()
                    .map(type -> {
                        FileTypeVO typeVO = new FileTypeVO();
                        BeanUtils.copyProperties(type, typeVO);
                        return typeVO;
                    })
                    .collect(Collectors.toMap(FileTypeVO::getId, type -> type)));
        }

        // 获取上传用户信息
        Set<Long> userIds = fileList.stream()
                .map(MeetingFile::getUploadUserId)
                .collect(Collectors.toSet());
        final Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 封装返回结果
        List<MeetingFileVO> fileVOList = fileList.stream().map(file -> {
            MeetingFileVO fileVO = new MeetingFileVO();
            BeanUtils.copyProperties(file, fileVO);

            // 设置文件类型信息
            FileTypeVO fileType = typeMap.get(file.getTypeId());
            if (fileType != null) {
                fileVO.setFileType(fileType);
            }

            // 设置上传用户信息
            User user = userMap.get(file.getUploadUserId());
            if (user != null) {
                fileVO.setUploaderName(user.getUserName());
            }
            return fileVO;
        }).collect(Collectors.toList());

        Page<MeetingFileVO> fileVOPage = new Page<>(current, size, filePage.getTotal());
        fileVOPage.setRecords(fileVOList);
        return fileVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadMeetingFile(Long meetingId, String fileName, String fileUrl, Long fileSize, Long userId) {
        if (meetingId == null || meetingId <= 0 || fileName == null || fileUrl == null || userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 校验会议是否存在
        Meeting meeting = meetingMapper.selectById(meetingId);
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会议不存在");
        }

        // 创建文件记录
        MeetingFile meetingFile = new MeetingFile();
        meetingFile.setMeetingId(meetingId);
        meetingFile.setFileName(fileName);
        meetingFile.setFileUrl(fileUrl);
        meetingFile.setFileSize(fileSize);
        meetingFile.setUploadUserId(userId);

        boolean result = this.save(meetingFile);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败");
        }

        return meetingFile.getId();
    }

    @Override
    public boolean removeMeetingFile(Long fileId, Long userId) {
        if (fileId == null || fileId <= 0 || userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 校验文件是否存在
        MeetingFile meetingFile = this.getById(fileId);
        if (meetingFile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }

        // 校验权限
        Meeting meeting = meetingMapper.selectById(meetingFile.getMeetingId());
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会议不存在");
        }
        if (!meeting.getOrganizerId().equals(userId.toString()) && !meetingFile.getUploadUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        return this.removeById(fileId);
    }

    @Override
    public boolean removeByMeetingId(Long meetingId) {
        if (meetingId == null || meetingId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<MeetingFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meetingId", meetingId);
        return this.remove(queryWrapper);
    }

    @Override
    public MeetingFile uploadMeetingFile(MultipartFile file, Long meetingId, User loginUser) {
        if (file == null || meetingId == null || meetingId <= 0 || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 校验会议是否存在
        Meeting meeting = meetingMapper.selectById(meetingId);
        if (meeting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会议不存在");
        }

        // 获取文件信息
        String originalFilename = file.getOriginalFilename();
        long fileSize = file.getSize();

        // 获取文件扩展名
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 根据文件扩展名获取文件类型ID
        Long typeId = getFileTypeIdByExtension(fileExtension.toLowerCase());
        if (typeId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的文件类型");
        }

        // 上传文件
        String fileUrl = FileUploadUtil.uploadFile(file, "meetings/files");

        // 创建文件记录
        MeetingFile meetingFile = new MeetingFile();
        meetingFile.setMeetingId(meetingId);
        meetingFile.setFileName(originalFilename);
        meetingFile.setFileUrl(fileUrl);
        meetingFile.setFileSize(fileSize);
        meetingFile.setUploadUserId(loginUser.getId());
        meetingFile.setTypeId(typeId);

        boolean result = this.save(meetingFile);
        if (!result) {
            // 如果数据库保存失败，删除已上传的文件
            FileUploadUtil.deleteFile(fileUrl);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败");
        }

        return meetingFile;
    }

    /**
     * 根据文件扩展名获取文件类型ID
     *
     * @param extension 文件扩展名（包含点号）
     * @return 文件类型ID
     */
    private Long getFileTypeIdByExtension(String extension) {
        // 从数据库或缓存中获取文件类型信息
        QueryWrapper<FileType> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("typeCode", extension.substring(1).toUpperCase());
        FileType fileType = fileTypeService.getOne(queryWrapper);

        if (fileType != null) {
            return fileType.getId();
        }

        // 如果找不到对应的文件类型，创建一个新的
        FileType newFileType = new FileType();
        newFileType.setTypeName(extension.substring(1).toUpperCase() + "文件");
        newFileType.setTypeCode(extension.substring(1).toUpperCase());
        newFileType.setDescription(extension.substring(1).toUpperCase() + "格式文件");

        boolean result = fileTypeService.save(newFileType);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建文件类型失败");
        }

        return newFileType.getId();
    }

    @Override
    public MeetingFileVO getMeetingFileVO(MeetingFile file, HttpServletRequest request) {
        if (file == null) {
            return null;
        }

        MeetingFileVO fileVO = new MeetingFileVO();
        BeanUtils.copyProperties(file, fileVO);

        // 获取文件类型信息
        FileType fileType = fileTypeService.getById(file.getTypeId());
        if (fileType != null) {
            FileTypeVO fileTypeVO = new FileTypeVO();
            BeanUtils.copyProperties(fileType, fileTypeVO);
            fileVO.setFileType(fileTypeVO);
        }

        // 获取会议信息
        Meeting meeting = meetingMapper.selectById(file.getMeetingId());
        if (meeting != null) {
            fileVO.setMeetingTitle(meeting.getTitle());
            fileVO.setMeetingCode(meeting.getMeetingCode());
        }

        // 获取上传用户信息
        User user = userService.getById(file.getUploadUserId());
        if (user != null) {
            fileVO.setUploaderName(user.getUserName());
            fileVO.setUploaderAvatar(user.getUserAvatar());
        }

        return fileVO;
    }

    @Override
    public Page<MeetingFileVO> pageMeetingFiles(Long meetingId, long current, long size) {
        if (meetingId == null || meetingId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 分页查询文件列表
        QueryWrapper<MeetingFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meetingId", meetingId);
        queryWrapper.orderByDesc("createTime");
        Page<MeetingFile> filePage = this.page(new Page<>(current, size), queryWrapper);
        List<MeetingFile> fileList = filePage.getRecords();

        if (fileList.isEmpty()) {
            return new Page<>(current, size, filePage.getTotal());
        }

        // 获取文件类型信息
        Set<Long> typeIds = fileList.stream()
                .map(MeetingFile::getTypeId)
                .collect(Collectors.toSet());
        final Map<Long, FileTypeVO> typeMap = new HashMap<>();
        if (!typeIds.isEmpty()) {
            List<FileType> fileTypes = fileTypeService.listByIds(typeIds);
            typeMap.putAll(fileTypes.stream()
                    .map(type -> {
                        FileTypeVO typeVO = new FileTypeVO();
                        BeanUtils.copyProperties(type, typeVO);
                        return typeVO;
                    })
                    .collect(Collectors.toMap(FileTypeVO::getId, type -> type)));
        }

        // 获取上传用户信息
        Set<Long> userIds = fileList.stream()
                .map(MeetingFile::getUploadUserId)
                .collect(Collectors.toSet());
        final Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 封装返回结果
        List<MeetingFileVO> fileVOList = fileList.stream().map(file -> {
            MeetingFileVO fileVO = new MeetingFileVO();
            BeanUtils.copyProperties(file, fileVO);

            // 设置文件类型信息
            FileTypeVO fileType = typeMap.get(file.getTypeId());
            if (fileType != null) {
                fileVO.setFileType(fileType);
            }

            // 设置上传用户信息
            User user = userMap.get(file.getUploadUserId());
            if (user != null) {
                fileVO.setUploaderName(user.getUserName());
            }
            return fileVO;
        }).collect(Collectors.toList());

        Page<MeetingFileVO> fileVOPage = new Page<>(current, size, filePage.getTotal());
        fileVOPage.setRecords(fileVOList);
        return fileVOPage;
    }

    @Override
    public Page<MeetingFileVO> listUserMeetingFiles(Long userId, long current, long size) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取用户参与的所有会议ID
        List<Long> meetingIds = meetingService.getUserAllMeetingIds(userId);

        if (meetingIds.isEmpty()) {
            return new Page<>(current, size, 0);
        }

        // 构建查询条件
        QueryWrapper<MeetingFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("meetingId", meetingIds)
                .orderByDesc("createTime");

        // 执行分页查询
        Page<MeetingFile> filePage = this.page(new Page<>(current, size), queryWrapper);

        // 转换为VO对象
        Page<MeetingFileVO> fileVOPage = new Page<>(filePage.getCurrent(), filePage.getSize(), filePage.getTotal());
        List<MeetingFileVO> fileVOList = filePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        fileVOPage.setRecords(fileVOList);

        return fileVOPage;
    }

    /**
     * 将MeetingFile转换为MeetingFileVO
     */
    private MeetingFileVO convertToVO(MeetingFile meetingFile) {
        if (meetingFile == null) {
            return null;
        }
        MeetingFileVO vo = new MeetingFileVO();
        BeanUtils.copyProperties(meetingFile, vo);

        // 获取会议信息
        Meeting meeting = meetingService.getById(meetingFile.getMeetingId());
        if (meeting != null) {
            vo.setMeetingTitle(meeting.getTitle());
        }

        // 获取上传用户信息
        User user = userService.getById(meetingFile.getUploadUserId());
        if (user != null) {
            vo.setUploaderName(user.getUserName());
            vo.setUploaderAvatar(user.getUserAvatar());
        }

        return vo;
    }

    @Override
    public boolean deleteMeetingFile(Long fileId) {
        // 获取文件信息
        MeetingFile meetingFile = this.getById(fileId);
        if (meetingFile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }

        // 删除物理文件
        String fileUrl = meetingFile.getFileUrl();
        boolean deleteResult = FileUploadUtil.deleteFile(fileUrl);
        if (!deleteResult) {
            log.error("物理文件删除失败: {}"+fileUrl);
        }

        // 删除数据库记录
        return this.removeById(fileId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteMeetingFiles(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return false;
        }

        // 获取所有文件信息
        List<MeetingFile> meetingFiles = this.listByIds(fileIds);
        if (meetingFiles.isEmpty()) {
            return false;
        }

        // 删除物理文件
        for (MeetingFile file : meetingFiles) {
            String fileUrl = file.getFileUrl();
            boolean deleteResult = FileUploadUtil.deleteFile(fileUrl);
            if (!deleteResult) {
                log.error("物理文件删除失败: {}"+fileUrl);
            }
        }

        // 批量删除数据库记录
        return this.removeByIds(fileIds);
    }
} 