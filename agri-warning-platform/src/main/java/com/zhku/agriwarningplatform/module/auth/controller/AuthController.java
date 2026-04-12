package com.zhku.agriwarningplatform.module.auth.controller;

import com.zhku.agriwarningplatform.common.errorcode.AuthErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.module.auth.service.AuthService;
import com.zhku.agriwarningplatform.module.auth.vo.LoginReqVO;
import com.zhku.agriwarningplatform.module.auth.vo.LoginRespVO;
import com.zhku.agriwarningplatform.module.auth.vo.RegisterReqVO;
import com.zhku.agriwarningplatform.module.auth.vo.UpdatePasswordReqVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
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
    public CommonResult<LoginRespVO> login(@RequestBody LoginReqVO loginReqVO){
        log.info("用户登录：{}", loginReqVO);
        LoginRespVO loginRespVO = authService.login(loginReqVO);
        if (loginRespVO == null){
            throw new ControllerException(AuthErrorCode.USERNAME_OR_PASSWORD_ERROR);
        }
        return CommonResult.success(loginRespVO);
    }
    @GetMapping("/auth/currentUser")
    public CommonResult<LoginRespVO> currentUser(@RequestHeader("token") String token){
        log.info("获取当前用户：{}", token);
        LoginRespVO loginRespVO = authService.GetCurrentUser(token);
        return CommonResult.success(loginRespVO);
    }
    @PostMapping("/auth/logout")
    public CommonResult<Boolean> logout(){
        log.info("用户登出");
        return CommonResult.success(true);
        }
    @PutMapping("/auth/changePassword")
    public CommonResult<Boolean> updatePassword(@RequestBody UpdatePasswordReqVO updatePasswordReqVO,@RequestHeader("token") String token){
        log.info("修改密码：{}", updatePasswordReqVO);
        authService.updatePassword(updatePasswordReqVO, token);
        return CommonResult.success(true);
    }
    @PostMapping("/auth/register")
    public CommonResult<LoginRespVO.UserInfoVO> register(@RequestBody RegisterReqVO registerReqVO){
        LoginRespVO.UserInfoVO userInfoVO = authService.register(registerReqVO);
        return CommonResult.success(userInfoVO);
    }
    @PostMapping("/admin/users")
    public CommonResult<LoginRespVO.UserInfoVO> createUser(@RequestBody RegisterReqVO registerReqVO){
        LoginRespVO.UserInfoVO userInfoVO = authService.adminRegister(registerReqVO);
        return CommonResult.success(userInfoVO);
    }
}
