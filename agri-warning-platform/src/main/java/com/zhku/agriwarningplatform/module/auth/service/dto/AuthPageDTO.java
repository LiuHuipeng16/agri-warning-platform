package com.zhku.agriwarningplatform.module.auth.service.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-21
 * Time: 23:01
 */
@Data
public class AuthPageDTO {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户角色
     */
    private String role;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;
}
