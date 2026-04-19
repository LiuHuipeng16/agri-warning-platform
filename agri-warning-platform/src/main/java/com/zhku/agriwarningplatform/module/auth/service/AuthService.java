package com.zhku.agriwarningplatform.module.auth.service;

import com.zhku.agriwarningplatform.module.auth.vo.*;

public interface AuthService {
    LoginRespVO login(LoginReqVO loginReqVO);

    LoginRespVO GetCurrentUser(String token);

    void updatePassword(UpdatePasswordReqVO updatePasswordReqVO, String token);

    RegisterRespVO register(RegisterReqVO registerReqVO);

    CreateUserResp adminRegister(CreateUserReq registerReqVO);
}
