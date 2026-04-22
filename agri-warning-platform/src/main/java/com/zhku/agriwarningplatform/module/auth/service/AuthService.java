package com.zhku.agriwarningplatform.module.auth.service;

import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.auth.controller.param.AuthPageParam;
import com.zhku.agriwarningplatform.module.auth.param.*;
import com.zhku.agriwarningplatform.module.auth.service.dto.AuthDetailDTO;
import com.zhku.agriwarningplatform.module.auth.service.dto.AuthPageDTO;
import com.zhku.agriwarningplatform.module.auth.vo.CreateUserRespVO;
import com.zhku.agriwarningplatform.module.auth.vo.LoginRespVO;
import com.zhku.agriwarningplatform.module.auth.vo.RegisterRespVO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public interface AuthService {
    LoginRespVO login(LoginReqParam loginReqParam);

    LoginRespVO GetCurrentUser(String token);

    void updatePassword(UpdatePasswordReqParam updatePasswordReqParam, String token);

    RegisterRespVO register(RegisterReqParam registerReqParam);

    CreateUserRespVO adminRegister(CreateUserReqParam registerReqVO);

    PageResult<AuthPageDTO> page(AuthPageParam param);

    AuthDetailDTO detail(Long id);

    Boolean update(AuthUpdateReqParam authUpdateReqParam);

    Boolean delete(Long id);

    Boolean batchDelete(@NotNull @Size(min = 1) List<Long> ids);
}
