package com.zhku.agriwarningplatform.module.auth.controller.param;

import com.zhku.agriwarningplatform.common.page.PageParam;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-21
 * Time: 22:35
 */
@Data
public class AuthPageParam extends PageParam {
    /**
     * 用户名
     */
    @Size(max=20,message = "用户名长度不能超过20")
    private String username;

    /**
     * 角色
     */
    @Size(max=20,message = "角色长度不能超过20")
    private String role;
}
