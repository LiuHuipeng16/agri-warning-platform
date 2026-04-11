package com.zhku.agriwarningplatform.module.auth.service;

import com.zhku.agriwarningplatform.module.auth.vo.LoginReqVO;
import com.zhku.agriwarningplatform.module.auth.vo.LoginRespVO;

public interface AuthService {
    LoginRespVO login(LoginReqVO loginReqVO);
}
