package com.zhku.agriwarningplatform.module.auth.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuthUpdateReqParam {
    @NotNull(message = "id不能为空")
    private Long id;
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "角色不能为空")
    private String role;
}
