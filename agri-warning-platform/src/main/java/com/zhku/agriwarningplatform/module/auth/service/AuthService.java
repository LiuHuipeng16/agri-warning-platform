package com.zhku.agriwarningplatform.module.auth.service;

import com.zhku.agriwarningplatform.module.auth.param.CreateUserReqParam;
import com.zhku.agriwarningplatform.module.auth.param.LoginReqParam;
import com.zhku.agriwarningplatform.module.auth.param.RegisterReqParam;
import com.zhku.agriwarningplatform.module.auth.param.UpdatePasswordReqParam;
import com.zhku.agriwarningplatform.module.auth.vo.*;

public interface AuthService {
    LoginRespVO login(LoginReqParam loginReqParam);

    LoginRespVO GetCurrentUser(String token);

    void updatePassword(UpdatePasswordReqParam updatePasswordReqParam, String token);

    RegisterRespVO register(RegisterReqParam registerReqParam);

    CreateUserResp adminRegister(CreateUserReqParam registerReqVO);
}
