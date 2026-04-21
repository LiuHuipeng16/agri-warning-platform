package com.zhku.agriwarningplatform.module.auth.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求VO
 */
@Data
public class UpdatePasswordReqParam {
    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}