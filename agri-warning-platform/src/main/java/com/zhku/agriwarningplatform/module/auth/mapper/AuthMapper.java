package com.zhku.agriwarningplatform.module.auth.mapper;

import com.zhku.agriwarningplatform.module.auth.vo.LoginReqVO;
import com.zhku.agriwarningplatform.module.auth.vo.LoginRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthMapper {
    @Select("select  * from user where username = #{username}")
    LoginRespVO selectByUsername(@Param("username") String username);
}
