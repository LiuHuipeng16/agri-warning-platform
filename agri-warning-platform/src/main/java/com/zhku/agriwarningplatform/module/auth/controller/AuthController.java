package com.zhku.agriwarningplatform.module.auth.controller;

import com.zhku.agriwarningplatform.common.errorcode.AuthErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.module.auth.service.AuthService;
import com.zhku.agriwarningplatform.module.auth.vo.LoginReqVO;
import com.zhku.agriwarningplatform.module.auth.vo.LoginRespVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 3:31
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/login")
    public CommonResult<LoginRespVO> login(@RequestBody LoginReqVO loginReqVO){
        log.info("用户登录：{}", loginReqVO);
        LoginRespVO loginRespVO = authService.login(loginReqVO);
        if (loginRespVO == null){
            throw new ControllerException(AuthErrorCode.USERNAME_OR_PASSWORD_ERROR);
        }
        return CommonResult.success(loginRespVO);
    }
    

}
