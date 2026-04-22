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
import com.zhku.agriwarningplatform.module.auth.controller.param.AuthPageParam;
import com.zhku.agriwarningplatform.module.auth.mapper.dataobject.UserDO;
import com.zhku.agriwarningplatform.module.auth.mapper.AuthMapper;
import com.zhku.agriwarningplatform.module.auth.mapper.dataobject.AdminRegisterDO;
import com.zhku.agriwarningplatform.module.auth.param.CreateUserReqParam;
import com.zhku.agriwarningplatform.module.auth.param.LoginReqParam;
import com.zhku.agriwarningplatform.module.auth.param.RegisterReqParam;
import com.zhku.agriwarningplatform.module.auth.param.UpdatePasswordReqParam;
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
        if (!StringUtils.hasText(loginReqParam.getUsername())){
            throw new ServiceException(AuthErrorCode.USERNAME_EMPTY);
        }
        if (!StringUtils.hasText(loginReqParam.getPassword())){
            throw new ServiceException(AuthErrorCode.PASSWORD_EMPTY);
        }
        UserDO userInfo= authMapper.selectByUsername(loginReqParam.getUsername());
        if (userInfo == null){
            throw new ServiceException(AuthErrorCode.USER_NOT_EXIST);
        }
        if (!passwordUtils.matches(loginReqParam.getPassword(), userInfo.getPassword())){
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
    @Transactional(rollbackFor = Exception.class)
    public LoginRespVO GetCurrentUser(String token) {
        Long userId = JwtUtils.getUserIdFromToken(token);
        String username = JwtUtils.getUsernameFromToken(token);
        String role = JwtUtils.getRoleFromToken(token);
        LoginRespVO loginRespVO= new LoginRespVO();
        LoginRespVO.UserInfoVO userInfo = new LoginRespVO.UserInfoVO(userId, username, role);
        loginRespVO.setUserInfo(userInfo);
        return loginRespVO;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UpdatePasswordReqParam updatePasswordReqParam, String token) {
        if (!StringUtils.hasText(updatePasswordReqParam.getNewPassword())){
            throw new ServiceException(AuthErrorCode.NEW_PASSWORD_EMPTY);
        }
        if (!StringUtils.hasText(updatePasswordReqParam.getOldPassword())){
            throw new ServiceException(AuthErrorCode.OLD_PASSWORD_EMPTY);
        }
        String username = JwtUtils.getUsernameFromToken(token);
        if (username== null){
            throw new ServiceException(AuthErrorCode.TOKEN_INVALID);
        }
        UserDO userInfo = authMapper.selectByUsername(username);
        if (userInfo== null){
            throw new ServiceException(AuthErrorCode.USER_NOT_EXIST);
        }
        if (!passwordUtils.matches(updatePasswordReqParam.getOldPassword(), userInfo.getPassword())){
            throw new ServiceException(AuthErrorCode.PASSWORD_ERROR);
        }
        int rows = authMapper.updatePassword(username, passwordUtils.encode(updatePasswordReqParam.getNewPassword()));
        if (rows != 1){
            throw new ServiceException(AuthErrorCode.UPDATE_PASSWORD_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterRespVO register(RegisterReqParam registerReqParam) {
        if (!StringUtils.hasText(registerReqParam.getUsername())){
            throw new ServiceException(AuthErrorCode.USERNAME_EMPTY);
        }
        if (!StringUtils.hasText(registerReqParam.getPassword())){
            throw new ServiceException(AuthErrorCode.PASSWORD_EMPTY);
        }
        if (!StringUtils.hasText(registerReqParam.getConfirmPassword())){
            throw new ServiceException(AuthErrorCode.CONFIRM_PASSWORD_EMPTY);
        }
        if (!registerReqParam.getPassword().equals(registerReqParam.getConfirmPassword())){
            throw new ServiceException(AuthErrorCode.PASSWORD_NOT_MATCH);
        }
        if (authMapper.selectByUsername(registerReqParam.getUsername()) != null){
            throw new ServiceException(AuthErrorCode.USERNAME_EXISTS);
        }
        authMapper.addUser(registerReqParam.getUsername(), passwordUtils.encode(registerReqParam.getPassword()), "USER");
        UserDO userdo = authMapper.selectByUsername(registerReqParam.getUsername());

        return new RegisterRespVO(userdo.getId(), userdo.getUsername(), userdo.getRole());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateUserRespVO adminRegister(CreateUserReqParam registerReqVO) {
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
        String role = registerReqVO.getRole();
        if (!"USER".equals(role) && !"ADMIN".equals(role)){
            throw new ServiceException(AuthErrorCode.ROLE_NOT_EXIST);
        }
        authMapper.addUser(registerReqVO.getUsername(), passwordUtils.encode(registerReqVO.getPassword()), registerReqVO.getRole());

        AdminRegisterDO adminRegisterDO = authMapper.adminselectByUsername(registerReqVO.getUsername());
        return new CreateUserRespVO(adminRegisterDO.getId(), adminRegisterDO.getUsername(), adminRegisterDO.getRole());
    }

    @Override
    public PageResult<AuthPageDTO> page(AuthPageParam param) {
        //1.校验参数是否正常
        checkPageListParam(param);
        //2.调用mapper层
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<UserDO>();
        if (StringUtils.hasText(param.getUsername())) {
            queryWrapper.like(UserDO::getUsername,param.getUsername());
        }
        if(StringUtils.hasText(param.getRole())){
            queryWrapper.eq(UserDO::getRole,param.getRole());
        }
        queryWrapper.orderByDesc(UserDO::getGmtCreate).orderByDesc(UserDO::getId);;
        Page<UserDO> page=new Page(param.getPageNum(),param.getPageSize());
        queryWrapper.eq(UserDO::getDeleteFlag, DeleteFlagEnum.NOT_DELETED.getCode());
        Page<UserDO> userDOPage=authMapper.selectPage(page,queryWrapper);
        List<UserDO> listDO=userDOPage.getRecords();
        List<AuthPageDTO> listDTO=convertToPageListDTO(listDO);
        Long count=authMapper.selectCount(queryWrapper);
        PageResult<AuthPageDTO> pageDTOPageResult=new PageResult<>();
        pageDTOPageResult.setTotal(count.intValue());
        pageDTOPageResult.setRecords(listDTO);
        return pageDTOPageResult;
    }

    @Override
    public AuthDetailDTO detail(Long id) {
        LambdaQueryWrapper<UserDO> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.eq(UserDO::getDeleteFlag,
                DeleteFlagEnum.NOT_DELETED.getCode())
                .eq(UserDO::getId,id);
        UserDO userDO=authMapper.selectOne(queryWrapper);
        if(userDO==null){
            throw new ServiceException(AuthErrorCode.USER_NOT_EXIST);
        }
        AuthDetailDTO authDetailDTO=convertToDetailDTO(userDO);
        return authDetailDTO;
    }



    /**
     * 私有方法-DO转DTO转换方法
     */

    private List<AuthPageDTO> convertToPageListDTO(List<UserDO> listDO) {
        List<AuthPageDTO> listDTO=listDO.stream().map(userDO ->{
            AuthPageDTO userDTO=new AuthPageDTO();
            userDTO.setId(userDO.getId());
            userDTO.setUsername(userDO.getUsername());
            userDTO.setRole(userDO.getRole());
            userDTO.setGmtCreate(userDO.getGmtCreate());
            return userDTO;
        }).collect(Collectors.toList());
        return listDTO;
    }

    private AuthDetailDTO convertToDetailDTO(UserDO userDO) {
        AuthDetailDTO authDetailDTO=new AuthDetailDTO();
        authDetailDTO.setId(userDO.getId());
        authDetailDTO.setUsername(userDO.getUsername());
        authDetailDTO.setRole(userDO.getRole());
        authDetailDTO.setGmtCreate(userDO.getGmtCreate());
        authDetailDTO.setGmtModified(userDO.getGmtModified());
        return authDetailDTO;
    }

    /**
     * 私有方法-service层校验业务数据
     */

    private void checkPageListParam(AuthPageParam param) {
        if(StringUtils.hasText(param.getRole())&&!RoleEnum.isValid(param.getRole())){
            throw  new ServiceException(AuthErrorCode.ROLE_NOT_EXIST);
        }
    }
}
