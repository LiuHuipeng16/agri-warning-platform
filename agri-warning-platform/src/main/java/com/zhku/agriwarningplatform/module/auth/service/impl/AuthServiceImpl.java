package com.zhku.agriwarningplatform.module.auth.service.impl;
import com.zhku.agriwarningplatform.common.errorcode.AuthErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.util.JwtUtils;
import com.zhku.agriwarningplatform.common.util.PasswordUtils;
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
    private final PasswordUtils passwordUtils;
    public LoginRespVO login(LoginReqVO loginReqVO) {
        if (!StringUtils.hasText(loginReqVO.getUsername())){
            throw new ServiceException(AuthErrorCode.USERNAME_EMPTY);
        }
        if (!StringUtils.hasText(loginReqVO.getPassword())){
            throw new ServiceException(AuthErrorCode.PASSWORD_EMPTY);
        }
        LoginRespVO.UserInfoVO userInfo = authMapper.selectByUsername(loginReqVO.getUsername());
        if (userInfo == null){
            throw new ServiceException(AuthErrorCode.USER_NOT_EXIST);
        }
        if (!passwordUtils.matches(loginReqVO.getPassword(), userInfo.getPassword())){
            throw new ServiceException(AuthErrorCode.PASSWORD_ERROR);
        }

        String token = jwtUtils.generateToken(
                userInfo.getId(),
                userInfo.getUsername(),
                userInfo.getRole()
        );

        LoginRespVO loginRespVO = new LoginRespVO();
        loginRespVO.setToken(token);
        loginRespVO.setUserInfo(userInfo);
        loginRespVO.getUserInfo().setPassword(null);
        loginRespVO.getUserInfo().setGmtCreate( null);
        loginRespVO.getUserInfo().setGmtModified( null);
        loginRespVO.getUserInfo().setDeleteFlag( null);
        return loginRespVO;
    }
}
