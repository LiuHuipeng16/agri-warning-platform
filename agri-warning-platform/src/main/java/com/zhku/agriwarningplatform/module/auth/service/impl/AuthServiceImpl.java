package com.zhku.agriwarningplatform.module.auth.service.impl;
import com.zhku.agriwarningplatform.common.errorcode.AuthErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.util.JwtUtils;
import com.zhku.agriwarningplatform.module.auth.mapper.AuthMapper;
import com.zhku.agriwarningplatform.module.auth.service.AuthService;
import com.zhku.agriwarningplatform.module.auth.vo.LoginReqVO;
import com.zhku.agriwarningplatform.module.auth.vo.LoginRespVO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthMapper authMapper;
    private final JwtUtils jwtUtils;
    public LoginRespVO login(LoginReqVO loginReqVO) {
        if (!StringUtils.hasText(loginReqVO.getUsername())){
            throw new ServiceException(AuthErrorCode.USERNAME_EMPTY);
        }
        if (!StringUtils.hasText(loginReqVO.getPassword())){
            throw new ServiceException(AuthErrorCode.PASSWORD_EMPTY);
        }
        LoginRespVO loginRespVO = authMapper.selectByUsername(loginReqVO.getUsername());
        if (loginRespVO == null){
            throw new ServiceException(AuthErrorCode.USER_NOT_EXIST);
        }
        if (!loginRespVO.getUserInfo().getPassword().equals(loginReqVO.getPassword())){
            throw new ServiceException(AuthErrorCode.PASSWORD_ERROR);
        }
        String token = jwtUtils.generateToken(
                loginRespVO.getUserInfo().getId(),
                loginRespVO.getUserInfo().getUsername(),
                loginRespVO.getUserInfo().getRole()
        );

        loginRespVO.setToken(token);
        loginRespVO.getUserInfo().setPassword(null);
        return loginRespVO;

    }
}
