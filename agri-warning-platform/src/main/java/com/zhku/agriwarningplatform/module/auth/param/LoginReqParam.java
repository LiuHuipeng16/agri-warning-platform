package com.zhku.agriwarningplatform.module.auth.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求参数（完全匹配接口文档）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginReqParam {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}