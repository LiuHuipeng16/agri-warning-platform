package com.zhku.agriwarningplatform.module.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录响应结果（完全匹配接口文档返回结构）
 */
@Data
public class LoginRespVO {
    /**
     * 登录令牌
     */
    private String token;

    /**
     * 用户信息
     */
    private UserInfoVO userInfo;

    /**
     * 内部嵌套用户信息VO，对应data.userInfo结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoVO {
        /**
         * 用户ID
         */
        private Long id;

        /**
         * 用户名
         */
        private String username;

        /**
         * 用户角色（ADMIN / USER）
         */
        private String password;

        private String role;

        private Integer deleteFlag;

        private LocalDateTime gmtCreate;

        private LocalDateTime gmtModified;
    }
}