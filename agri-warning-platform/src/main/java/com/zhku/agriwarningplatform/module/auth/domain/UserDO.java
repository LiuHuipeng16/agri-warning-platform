package com.zhku.agriwarningplatform.module.auth.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDO {
    private Long id;
    private String username;
    private String password; // 关键：包含密码
    private String role;
    private Integer deleteFlag;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
}
