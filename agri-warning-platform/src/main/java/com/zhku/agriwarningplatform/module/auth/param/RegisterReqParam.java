<<<<<<<< HEAD:agri-warning-platform/src/main/java/com/zhku/agriwarningplatform/module/auth/controller/vo/RegisterReqVO.java
package com.zhku.agriwarningplatform.module.auth.controller.vo;
========
package com.zhku.agriwarningplatform.module.auth.param;
>>>>>>>> 4d46d3c30a88af68f3ea519d8b89fa9ac069c19c:agri-warning-platform/src/main/java/com/zhku/agriwarningplatform/module/auth/param/RegisterReqParam.java

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求VO
 */
@Data
public class RegisterReqParam {
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在 4~20 位之间")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6~20 位之间")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}

