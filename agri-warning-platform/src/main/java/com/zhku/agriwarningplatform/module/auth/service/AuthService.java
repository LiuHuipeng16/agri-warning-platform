package com.zhku.agriwarningplatform.module.auth.service;

import com.zhku.agriwarningplatform.module.auth.vo.LoginReqVO;
import com.zhku.agriwarningplatform.module.auth.vo.LoginRespVO;
import com.zhku.agriwarningplatform.module.auth.vo.RegisterReqVO;
import com.zhku.agriwarningplatform.module.auth.vo.UpdatePasswordReqVO;

public interface AuthService {
    LoginRespVO login(LoginReqVO loginReqVO);

    LoginRespVO GetCurrentUser(String token);

    void updatePassword(UpdatePasswordReqVO updatePasswordReqVO, String token);

    LoginRespVO.UserInfoVO register(RegisterReqVO registerReqVO);

    LoginRespVO.UserInfoVO adminRegister(RegisterReqVO registerReqVO);
}
