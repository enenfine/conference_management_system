package com.exam.model.dto.user;

import lombok.Data;

@Data
public class UserPasswordRequest {
    /**
     * 用户名
     */
    private String userName;

    /**
     * 旧密码
     */
    private String userNewPassword;

    /**
     * 新密码
     */
    private String userOldPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

}
