<<<<<<<< HEAD:agri-warning-platform/src/main/java/com/zhku/agriwarningplatform/module/auth/controller/vo/LoginReqVO.java
package com.zhku.agriwarningplatform.module.auth.controller.vo;
========
package com.zhku.agriwarningplatform.module.auth.param;
>>>>>>>> 4d46d3c30a88af68f3ea519d8b89fa9ac069c19c:agri-warning-platform/src/main/java/com/zhku/agriwarningplatform/module/auth/param/LoginReqParam.java

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