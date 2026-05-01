package com.zhku.agriwarningplatform.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhku.agriwarningplatform.common.enums.DeleteFlagEnum;
import com.zhku.agriwarningplatform.common.enums.RoleEnum;
import com.zhku.agriwarningplatform.common.errorcode.AuthErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.common.util.JwtUtils;
import com.zhku.agriwarningplatform.common.util.PasswordUtils;
import com.zhku.agriwarningplatform.module.auth.controller.param.AuthBatchDeleteReqParam;
import com.zhku.agriwarningplatform.module.auth.controller.param.AuthPageParam;
import com.zhku.agriwarningplatform.module.auth.mapper.AuthMapper;
import com.zhku.agriwarningplatform.module.auth.mapper.dataobject.AdminRegisterDO;
import com.zhku.agriwarningplatform.module.auth.mapper.dataobject.UserDO;
import com.zhku.agriwarningplatform.module.auth.param.*;
import com.zhku.agriwarningplatform.module.auth.service.AuthService;
import com.zhku.agriwarningplatform.module.auth.service.dto.AuthDetailDTO;
import com.zhku.agriwarningplatform.module.auth.service.dto.AuthPageDTO;
import com.zhku.agriwarningplatform.module.auth.vo.CreateUserRespVO;
import com.zhku.agriwarningplatform.module.auth.vo.LoginRespVO;
import com.zhku.agriwarningplatform.module.auth.vo.RegisterRespVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthMapper authMapper;
    private final JwtUtils jwtUtils;
    private final PasswordUtils passwordUtils;

    @Transactional(rollbackFor = Exception.class)
    public LoginRespVO login(LoginReqParam loginReqParam) {

        if (!StringUtils.hasText(loginReqParam.getUsername())) {
            throw new ServiceException(AuthErrorCode.USERNAME_EMPTY);
        }

        if (!StringUtils.hasText(loginReqParam.getPassword())) {
            throw new ServiceException(AuthErrorCode.PASSWORD_EMPTY);
        }

        UserDO userInfo = authMapper.selectByUsername(loginReqParam.getUsername());

        if (userInfo == null) {
            throw new ServiceException(AuthErrorCode.USER_NOT_EXIST);
        }

        if (!passwordUtils.matches(loginReqParam.getPassword(), userInfo.getPassword())) {
            throw new ServiceException(AuthErrorCode.PASSWORD_ERROR);
        }

        String token = jwtUtils.generateToken(
                userInfo.getId(),
                userInfo.getUsername(),
                userInfo.getRole()
        );

        LoginRespVO loginRespVO = new LoginRespVO();
        loginRespVO.setToken(token);
        loginRespVO.setUserInfo(new LoginRespVO.UserInfoVO(
                userInfo.getId(),
                userInfo.getUsername(),
                userInfo.getRole()
        ));

        return loginRespVO;
    }

    @Override
    public LoginRespVO GetCurrentUser(String token) {

        Long userId = jwtUtils.getUserIdFromToken(token);
        String username = jwtUtils.getUsernameFromToken(token);
        String role = jwtUtils.getRoleFromToken(token);

        LoginRespVO loginRespVO = new LoginRespVO();
        loginRespVO.setUserInfo(new LoginRespVO.UserInfoVO(userId, username, role));

        return loginRespVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UpdatePasswordReqParam param, String token) {

        if (!StringUtils.hasText(param.getNewPassword())) {
            throw new ServiceException(AuthErrorCode.NEW_PASSWORD_EMPTY);
        }

        if (!StringUtils.hasText(param.getOldPassword())) {
            throw new ServiceException(AuthErrorCode.OLD_PASSWORD_EMPTY);
        }

        String username = jwtUtils.getUsernameFromToken(token);

        if (username == null) {
            throw new ServiceException(AuthErrorCode.TOKEN_INVALID);
        }

        UserDO userInfo = authMapper.selectByUsername(username);

        if (userInfo == null) {
            throw new ServiceException(AuthErrorCode.USER_NOT_EXIST);
        }

        if (!passwordUtils.matches(param.getOldPassword(), userInfo.getPassword())) {
            throw new ServiceException(AuthErrorCode.PASSWORD_ERROR);
        }

        int rows = authMapper.updatePassword(
                username,
                passwordUtils.encode(param.getNewPassword())
        );

        if (rows != 1) {
            throw new ServiceException(AuthErrorCode.UPDATE_PASSWORD_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterRespVO register(RegisterReqParam param) {

        if (!StringUtils.hasText(param.getUsername())) {
            throw new ServiceException(AuthErrorCode.USERNAME_EMPTY);
        }

        if (!StringUtils.hasText(param.getPassword())) {
            throw new ServiceException(AuthErrorCode.PASSWORD_EMPTY);
        }

        if (!StringUtils.hasText(param.getConfirmPassword())) {
            throw new ServiceException(AuthErrorCode.CONFIRM_PASSWORD_EMPTY);
        }

        if (!param.getPassword().equals(param.getConfirmPassword())) {
            throw new ServiceException(AuthErrorCode.PASSWORD_NOT_MATCH);
        }

        if (authMapper.selectByUsername(param.getUsername()) != null) {
            throw new ServiceException(AuthErrorCode.USERNAME_EXISTS);
        }

        authMapper.addUser(
                param.getUsername(),
                passwordUtils.encode(param.getPassword()),
                "USER"
        );

        UserDO user = authMapper.selectByUsername(param.getUsername());

        return new RegisterRespVO(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateUserRespVO adminRegister(CreateUserReqParam param) {

        if (!StringUtils.hasText(param.getUsername())) {
            throw new ServiceException(AuthErrorCode.USERNAME_EMPTY);
        }

        if (!StringUtils.hasText(param.getPassword())) {
            throw new ServiceException(AuthErrorCode.PASSWORD_EMPTY);
        }

        if (!StringUtils.hasText(param.getRole())) {
            throw new ServiceException(AuthErrorCode.ROle_EMPTY);
        }

        if (authMapper.selectByUsername(param.getUsername()) != null) {
            throw new ServiceException(AuthErrorCode.USERNAME_EXISTS);
        }

        if (!"USER".equals(param.getRole()) && !"ADMIN".equals(param.getRole())) {
            throw new ServiceException(AuthErrorCode.ROLE_NOT_EXIST);
        }

        authMapper.addUser(
                param.getUsername(),
                passwordUtils.encode(param.getPassword()),
                param.getRole()
        );

        AdminRegisterDO user = authMapper.adminselectByUsername(param.getUsername());

        return new CreateUserRespVO(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public PageResult<AuthPageDTO> page(AuthPageParam param) {

        checkPageListParam(param);

        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(param.getUsername())) {
            queryWrapper.like(UserDO::getUsername, param.getUsername());
        }

        if (StringUtils.hasText(param.getRole())) {
            queryWrapper.eq(UserDO::getRole, param.getRole());
        }

        queryWrapper.eq(UserDO::getDeleteFlag, DeleteFlagEnum.NOT_DELETED.getCode());
        queryWrapper.orderByDesc(UserDO::getGmtCreate).orderByDesc(UserDO::getId);

        Page<UserDO> page = new Page<>(param.getPageNum(), param.getPageSize());

        Page<UserDO> userPage = authMapper.selectPage(page, queryWrapper);

        List<AuthPageDTO> listDTO = convertToPageListDTO(userPage.getRecords());

        PageResult<AuthPageDTO> result = new PageResult<>();
        result.setTotal((int) userPage.getTotal());
        result.setRecords(listDTO);

        return result;
    }

    @Override
    public AuthDetailDTO detail(Long id) {

        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(UserDO::getDeleteFlag, DeleteFlagEnum.NOT_DELETED.getCode())
                .eq(UserDO::getId, id);

        UserDO user = authMapper.selectOne(wrapper);

        if (user == null) {
            throw new ServiceException(AuthErrorCode.USER_NOT_EXIST);
        }

        return convertToDetailDTO(user);
    }

    @Override
    public Boolean update(AuthUpdateReqParam param) {

        if (!RoleEnum.isValid(param.getRole())) {
            throw new ServiceException(AuthErrorCode.ROLE_NOT_EXIST);
        }

        if (param.getId() <= 0) {
            throw new ServiceException(AuthErrorCode.USER_ID_INVALID);
        }

        int rows = authMapper.updateUsernameAndRoleById(param);

        if (rows != 1) {
            throw new ServiceException(AuthErrorCode.UPDATE_USER_FAILED);
        }

        return true;
    }

    @Override
    public Boolean delete(Long id) {

        int rows = authMapper.deleteUserById(id);

        if (rows != 1) {
            throw new ServiceException(AuthErrorCode.DELETE_USER_FAILED);
        }

        return true;
    }

    @Override
    public Boolean batchDelete(AuthBatchDeleteReqParam param) {

        int rows = authMapper.batchDelete(param.getIds());

        if (rows != param.getIds().size()) {
            throw new ServiceException(AuthErrorCode.DELETE_USER_FAILED);
        }

        return true;
    }

    private List<AuthPageDTO> convertToPageListDTO(List<UserDO> listDO) {

        return listDO.stream().map(user -> {

            AuthPageDTO dto = new AuthPageDTO();

            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setRole(user.getRole());
            dto.setGmtCreate(user.getGmtCreate());

            return dto;

        }).collect(Collectors.toList());
    }

    private AuthDetailDTO convertToDetailDTO(UserDO user) {

        AuthDetailDTO dto = new AuthDetailDTO();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setGmtCreate(user.getGmtCreate());
        dto.setGmtModified(user.getGmtModified());

        return dto;
    }

    private void checkPageListParam(AuthPageParam param) {

        if (StringUtils.hasText(param.getRole()) && !RoleEnum.isValid(param.getRole())) {
            throw new ServiceException(AuthErrorCode.ROLE_NOT_EXIST);
        }
    }
}