package com.zhku.agriwarningplatform.module.auth.controller;

import com.zhku.agriwarningplatform.common.errorcode.AuthErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.common.util.JacksonUtils;
import com.zhku.agriwarningplatform.module.auth.controller.param.AuthPageParam;
import com.zhku.agriwarningplatform.module.auth.controller.vo.*;
import com.zhku.agriwarningplatform.module.auth.param.CreateUserReqParam;
import com.zhku.agriwarningplatform.module.auth.param.LoginReqParam;
import com.zhku.agriwarningplatform.module.auth.param.RegisterReqParam;
import com.zhku.agriwarningplatform.module.auth.param.UpdatePasswordReqParam;
import com.zhku.agriwarningplatform.module.auth.service.AuthService;
import com.zhku.agriwarningplatform.module.auth.service.dto.AuthDetailDTO;
import com.zhku.agriwarningplatform.module.auth.service.dto.AuthPageDTO;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 3:31
 */
@Validated
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

    @GetMapping("/admin/users/page")
    public CommonResult<PageResult<AuthPageVO>> page(@Validated AuthPageParam param){
        log.info("进入接口:AuthController#page,param={}", JacksonUtils.writeValueAsString(param));
        PageResult<AuthPageDTO> authPageDTOPageResult=authService.page(param);
        PageResult<AuthPageVO> authPageVOPageResult=new PageResult<>();
        authPageVOPageResult.setTotal(authPageDTOPageResult.getTotal());
        List<AuthPageVO> list=convertPageVOList(authPageDTOPageResult.getRecords());
        authPageVOPageResult.setRecords(list);
        return CommonResult.success(authPageVOPageResult);
    }

    @GetMapping("/admin/users/detail/{id}")
    public CommonResult<AuthDetailVO> detail(
            @Min (value = 1,message ="id必须大于等于1")@PathVariable Long id){
        log.info("进入接口:AuthController#detail,id={}", id);
        AuthDetailDTO authDetailDTO=authService.detail(id);
        AuthDetailVO authDetailVO=convertDetailVO(authDetailDTO);
        return CommonResult.success(authDetailVO);
    }

    /**
     * 私有方法-DTO转VO转换方法
     */

    private List<AuthPageVO> convertPageVOList(List<AuthPageDTO> records) {
        List<AuthPageVO> list=records.stream().map(
                authPageDTO ->{
                    AuthPageVO authPageVO=new AuthPageVO();
                    authPageVO.setId(authPageDTO.getId());
                    authPageVO.setUsername(authPageDTO.getUsername());
                    authPageVO.setRole(authPageDTO.getRole());
                    authPageVO.setGmtCreate(authPageDTO.getGmtCreate());
                    return authPageVO;
                }
        ).collect(Collectors.toList());
        return list;
    }
    private AuthDetailVO convertDetailVO(AuthDetailDTO authDetailDTO) {
        AuthDetailVO authDetailVO=new AuthDetailVO();
        authDetailVO.setId(authDetailDTO.getId());
        authDetailVO.setUsername(authDetailDTO.getUsername());
        authDetailVO.setRole(authDetailDTO.getRole());
        authDetailVO.setGmtCreate(authDetailDTO.getGmtCreate());
        authDetailVO.setGmtModified(authDetailDTO.getGmtModified());
        return authDetailVO;
    }
}
