package com.zhku.agriwarningplatform.module.auth.service;

import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.auth.controller.param.AuthPageParam;
import com.zhku.agriwarningplatform.module.auth.controller.vo.*;
import com.zhku.agriwarningplatform.module.auth.service.dto.AuthDetailDTO;
import com.zhku.agriwarningplatform.module.auth.service.dto.AuthPageDTO;

public interface AuthService {
    LoginRespVO login(LoginReqVO loginReqVO);

    LoginRespVO GetCurrentUser(String token);

    void updatePassword(UpdatePasswordReqVO updatePasswordReqVO, String token);

    RegisterRespVO register(RegisterReqVO registerReqVO);

    CreateUserResp adminRegister(CreateUserReq registerReqVO);

    PageResult<AuthPageDTO> page(AuthPageParam param);

    AuthDetailDTO detail(Long id);
}
