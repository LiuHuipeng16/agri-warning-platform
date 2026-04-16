package com.zhku.agriwarningplatform.module.auth.service.impl;
import com.zhku.agriwarningplatform.common.errorcode.AuthErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.util.JwtUtils;
import com.zhku.agriwarningplatform.common.util.PasswordUtils;
import com.zhku.agriwarningplatform.module.auth.mapper.AuthMapper;
import com.zhku.agriwarningplatform.module.auth.service.AuthService;
import com.zhku.agriwarningplatform.module.auth.vo.LoginReqVO;
import com.zhku.agriwarningplatform.module.auth.vo.LoginRespVO;
import com.zhku.agriwarningplatform.module.auth.vo.RegisterReqVO;
import com.zhku.agriwarningplatform.module.auth.vo.UpdatePasswordReqVO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthMapper authMapper;
    private final JwtUtils jwtUtils;
    private final PasswordUtils passwordUtils;

    @Transactional(rollbackFor = Exception.class)
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginRespVO GetCurrentUser(String token) {
        Long userId = JwtUtils.getUserIdFromToken(token);
        String username = JwtUtils.getUsernameFromToken(token);
        String role = JwtUtils.getRoleFromToken(token);
        LoginRespVO loginRespVO= new LoginRespVO();
        LoginRespVO.UserInfoVO userInfo = new LoginRespVO.UserInfoVO(userId, username, null,role, null,null, null);
        loginRespVO.setUserInfo(userInfo);
        loginRespVO.setToken(token);
        return loginRespVO;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UpdatePasswordReqVO updatePasswordReqVO, String token) {
        if (!StringUtils.hasText(updatePasswordReqVO.getNewPassword())){
            throw new ServiceException(AuthErrorCode.NEW_PASSWORD_EMPTY);
        }
        if (!StringUtils.hasText(updatePasswordReqVO.getOldPassword())){
            throw new ServiceException(AuthErrorCode.OLD_PASSWORD_EMPTY);
        }
        String username = JwtUtils.getUsernameFromToken(token);
        if (username== null){
            throw new ServiceException(AuthErrorCode.TOKEN_INVALID);
        }
        LoginRespVO.UserInfoVO userInfo = authMapper.selectByUsername(username);
        if (userInfo== null){
            throw new ServiceException(AuthErrorCode.USER_NOT_EXIST);
        }
        if (!passwordUtils.matches(updatePasswordReqVO.getOldPassword(), userInfo.getPassword())){
            throw new ServiceException(AuthErrorCode.PASSWORD_ERROR);
        }
        int rows = authMapper.updatePassword(username, passwordUtils.encode(updatePasswordReqVO.getNewPassword()));
        if (rows != 1){
            throw new ServiceException(AuthErrorCode.UPDATE_PASSWORD_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginRespVO.UserInfoVO register(RegisterReqVO registerReqVO) {
        if (!StringUtils.hasText(registerReqVO.getUsername())){
            throw new ServiceException(AuthErrorCode.USERNAME_EMPTY);
        }
        if (!StringUtils.hasText(registerReqVO.getPassword())){
            throw new ServiceException(AuthErrorCode.PASSWORD_EMPTY);
        }
        if (!StringUtils.hasText(registerReqVO.getConfirmPassword())){
            throw new ServiceException(AuthErrorCode.CONFIRM_PASSWORD_EMPTY);
        }
        if (!registerReqVO.getPassword().equals(registerReqVO.getConfirmPassword())){
            throw new ServiceException(AuthErrorCode.PASSWORD_NOT_MATCH);
        }
        if (authMapper.selectByUsername(registerReqVO.getUsername()) != null){
            throw new ServiceException(AuthErrorCode.USERNAME_EXISTS);
        }
        authMapper.addUser(registerReqVO.getUsername(), passwordUtils.encode(registerReqVO.getPassword()), "USER");
        LoginRespVO.UserInfoVO userInfo = authMapper.selectByUsername(registerReqVO.getUsername());
        userInfo.setPassword(null);
        userInfo.setGmtCreate( null);
        userInfo.setGmtModified( null);
        userInfo.setDeleteFlag( null);
        return userInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginRespVO.UserInfoVO adminRegister(RegisterReqVO registerReqVO) {
        if (!StringUtils.hasText(registerReqVO.getUsername())){
            throw new ServiceException(AuthErrorCode.USERNAME_EMPTY);
        }
        if (!StringUtils.hasText(registerReqVO.getPassword())){
            throw new ServiceException(AuthErrorCode.PASSWORD_EMPTY);
        }
        if (!StringUtils.hasText(registerReqVO.getRole())){
            throw new ServiceException(AuthErrorCode.ROle_EMPTY);
        }
        if (authMapper.selectByUsername(registerReqVO.getUsername()) != null){
            throw new ServiceException(AuthErrorCode.USERNAME_EXISTS);
        }
        if (!registerReqVO.getRole().equals("USER") && !registerReqVO.getRole().equals("ADMIN")){
            throw new ServiceException(AuthErrorCode.ROLE_NOT_EXIST);
        }
        authMapper.addUser(registerReqVO.getUsername(), passwordUtils.encode(registerReqVO.getPassword()), registerReqVO.getRole());
        LoginRespVO.UserInfoVO userInfo = authMapper.selectByUsername(registerReqVO.getUsername());
        userInfo.setPassword(null);
        userInfo.setGmtCreate( null);
        userInfo.setGmtModified( null);
        userInfo.setDeleteFlag( null);
        return userInfo;
    }
}
