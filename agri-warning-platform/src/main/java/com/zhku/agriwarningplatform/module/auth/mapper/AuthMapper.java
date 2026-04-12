package com.zhku.agriwarningplatform.module.auth.mapper;

import com.zhku.agriwarningplatform.module.auth.vo.LoginReqVO;
import com.zhku.agriwarningplatform.module.auth.vo.LoginRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthMapper {
    @Select("select id, username, password, role, delete_flag as deleteFlag, gmt_create as gmtCreate, gmt_modified as gmtModified " +
            "from user where username = #{username} and delete_flag = 0")
    LoginRespVO.UserInfoVO selectByUsername(@Param("username") String username);
}
