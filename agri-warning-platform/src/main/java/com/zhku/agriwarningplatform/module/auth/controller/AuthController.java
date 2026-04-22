package com.zhku.agriwarningplatform.module.auth.controller;

import com.zhku.agriwarningplatform.common.errorcode.AuthErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.module.auth.param.CreateUserReqParam;
import com.zhku.agriwarningplatform.module.auth.param.LoginReqParam;
import com.zhku.agriwarningplatform.module.auth.param.RegisterReqParam;
import com.zhku.agriwarningplatform.module.auth.param.UpdatePasswordReqParam;
import com.zhku.agriwarningplatform.module.auth.service.AuthService;
import com.zhku.agriwarningplatform.module.auth.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/auth/login")
    public CommonResult<LoginRespVO> login(@Validated @RequestBody LoginReqParam loginReqParam){
        log.info("用户登录：{}", loginReqParam);
        LoginRespVO loginRespVO = authService.login(loginReqParam);
        if (loginRespVO == null){
            throw new ControllerException(AuthErrorCode.USERNAME_OR_PASSWORD_ERROR);
        }
        return CommonResult.success(loginRespVO);
    }
    @GetMapping("/auth/currentUser")
    public CommonResult<LoginRespVO.UserInfoVO> currentUser(@Validated @RequestHeader("token") String token){
        log.info("获取当前用户：{}", token);
        LoginRespVO loginRespVO = authService.GetCurrentUser(token);
        return CommonResult.success(loginRespVO.getUserInfo());
    }
    @PostMapping("/auth/logout")
    public CommonResult<Boolean> logout(){
        log.info("用户登出");
        return CommonResult.success(true);
        }
    @PutMapping("/auth/changePassword")
    public CommonResult<Boolean> updatePassword(@Validated @RequestBody UpdatePasswordReqParam updatePasswordReqParam, @RequestHeader("token") String token){
        log.info("修改密码：{}", updatePasswordReqParam);
        authService.updatePassword(updatePasswordReqParam, token);
        return CommonResult.success(true);
    }
    @PostMapping("/auth/register")
    public CommonResult<RegisterRespVO> register(@Validated @RequestBody RegisterReqParam registerReqParam){
        RegisterRespVO userInfoVO = authService.register(registerReqParam);
        return CommonResult.success(userInfoVO);
    }
    @PostMapping("/admin/users")
    public CommonResult<CreateUserRespVO> createUser(@Validated @RequestBody CreateUserReqParam request){
         CreateUserRespVO userInfoVO = authService.adminRegister(request);
        return CommonResult.success(userInfoVO);
    }
}
