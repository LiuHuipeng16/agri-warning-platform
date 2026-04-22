package com.zhku.agriwarningplatform.module.auth.service;

import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.auth.controller.param.AuthPageParam;
import com.zhku.agriwarningplatform.module.auth.service.dto.AuthDetailDTO;
import com.zhku.agriwarningplatform.module.auth.service.dto.AuthPageDTO;
import com.zhku.agriwarningplatform.module.auth.param.CreateUserReqParam;
import com.zhku.agriwarningplatform.module.auth.param.LoginReqParam;
import com.zhku.agriwarningplatform.module.auth.param.RegisterReqParam;
import com.zhku.agriwarningplatform.module.auth.param.UpdatePasswordReqParam;
import com.zhku.agriwarningplatform.module.auth.vo.CreateUserRespVO;
import com.zhku.agriwarningplatform.module.auth.vo.LoginRespVO;
import com.zhku.agriwarningplatform.module.auth.vo.RegisterRespVO;

public interface AuthService {
    LoginRespVO login(LoginReqParam loginReqParam);

    LoginRespVO GetCurrentUser(String token);

    void updatePassword(UpdatePasswordReqParam updatePasswordReqParam, String token);

    RegisterRespVO register(RegisterReqParam registerReqParam);

    PageResult<AuthPageDTO> page(AuthPageParam param);

    AuthDetailDTO detail(Long id);
    CreateUserRespVO adminRegister(CreateUserReqParam registerReqVO);
}
