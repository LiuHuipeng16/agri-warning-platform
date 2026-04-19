package com.zhku.agriwarningplatform.module.auth.vo;

import lombok.Data;

/**
 * 修改密码请求VO
 */
@Data
public class UpdatePasswordReqVO {
    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}