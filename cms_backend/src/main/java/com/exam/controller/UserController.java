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
import com.exam.model.dto.user.*;
import com.exam.model.entity.User;
import com.exam.model.vo.LoginUserVO;
import com.exam.model.vo.UserVO;
import com.exam.service.UserService;
import com.exam.utils.FileUploadUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.exam.utils.NetUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.exam.service.impl.UserServiceImpl.SALT;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户接口
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "exam";

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    @ApiOperation("获取当前登录用户IP")
    @GetMapping("/get/ip")
    public BaseResponse<Map<String,String>> getUserIp(HttpServletRequest request) {
        String ipAddress = NetUtils.getIpAddress(request);
        String localIpAddress = NetUtils.getLocalIpAddress();
        String publicIpAddress = NetUtils.getPublicIpAddress();
        Map<String, String> map = new HashMap<>();
        map.put("ipAddress", ipAddress);
        map.put("localIpAddress", localIpAddress);
        map.put("publicIpAddress", publicIpAddress);

        return ResultUtils.success(map);

    }

    @ApiOperation("用户注销")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @ApiOperation("获取当前登录用户信息")
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }


    @ApiOperation("创建用户（仅管理员）")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    @ApiOperation("删除用户（仅管理员）")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    @ApiOperation("更新用户信息（仅管理员）")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @ApiOperation("根据ID获取用户信息（仅管理员）")
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    @ApiOperation("根据ID获取用户信息（脱敏）")
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    @ApiOperation("分页获取用户列表（仅管理员）")
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    @ApiOperation("分页获取用户列表（脱敏）")
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                       HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }


    @ApiOperation("更新个人信息")
    @PostMapping("/update/my")
    public BaseResponse<LoginUserVO> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                                  HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        // 更新成功后，返回最新的用户信息
        User updatedUser = userService.getById(loginUser.getId());
        return ResultUtils.success(userService.getLoginUserVO(updatedUser));
    }

    @ApiOperation("更新密码")
    @PostMapping("/update/myPassword")
    public BaseResponse<String> updateMyPassword(@RequestBody UserPasswordRequest userPasswordRequest,
                                                 HttpServletRequest request) {
        if (userPasswordRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);

        String userName = userPasswordRequest.getUserName();
        String oldPassword = userPasswordRequest.getUserOldPassword();
        String newPassword = userPasswordRequest.getUserNewPassword();
        String checkPassword = userPasswordRequest.getCheckPassword();

        if (StringUtils.isAnyBlank(newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空");
        }

        if (newPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        String encryptNewPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());

        /*
         * 管理员重置用户密码：
         * 管理员在用户管理页面点击“重置密码”时，不应该校验旧密码。
         */
        if (userService.isAdmin(loginUser) && StringUtils.isNotBlank(userName)) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userName);
            User user = userService.getBaseMapper().selectOne(queryWrapper);

            if (user == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            }

            user.setUserPassword(encryptNewPassword);
            boolean result = userService.updateById(user);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "密码重置失败");

            return ResultUtils.success("密码重置成功！");
        }

        /*
         * 普通用户修改自己的密码：
         * 必须校验旧密码。
         */
        if (StringUtils.isBlank(oldPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码不能为空");
        }

        String encryptOldPassword = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());

        User currentUser = userService.getById(loginUser.getId());
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        if (!encryptOldPassword.equals(currentUser.getUserPassword())) {
            log.info("user update password failed, old password is incorrect");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码不正确");
        }

        currentUser.setUserPassword(encryptNewPassword);
        boolean result = userService.updateById(currentUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "密码修改失败");

        return ResultUtils.success("密码修改成功！");
    }


    @ApiOperation("上传用户头像")
    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 校验文件类型
        String fileName = file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        if (!Arrays.asList(".jpg", ".jpeg", ".png", ".gif").contains(fileExtension)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持jpg、jpeg、png、gif格式的图片");
        }

        // 校验文件大小（最大5MB）
        long fileSize = file.getSize();
        long maxSize = 5 * 1024 * 1024;
        if (fileSize > maxSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过5MB");
        }

        // 删除旧头像
        String oldAvatarUrl = loginUser.getUserAvatar();
        if (StringUtils.isNotBlank(oldAvatarUrl)) {
            FileUploadUtil.deleteFile(oldAvatarUrl);
        }

        // 上传新头像
        String avatarUrl = FileUploadUtil.uploadFile(file, "avatars/user");

        // 更新用户头像
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserAvatar(avatarUrl);
        boolean result = userService.updateById(user);

        if (!result) {
            // 更新失败，删除已上传的文件
            FileUploadUtil.deleteFile(avatarUrl);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "头像更新失败");
        }

        // 查询更新后的用户信息
        User updatedUser = userService.getById(loginUser.getId());
        return ResultUtils.success(updatedUser.getUserAvatar());
    }
}
