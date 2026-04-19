package com.zhku.agriwarningplatform.module.auth.vo;

import lombok.Data;

/**
 * 注册请求VO
 */
@Data
public class RegisterReqVO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 确认密码
     */
    private String confirmPassword;
    /**
     * 角色
     * (ADMIN / USER)
     *  */
    private String role;
}

