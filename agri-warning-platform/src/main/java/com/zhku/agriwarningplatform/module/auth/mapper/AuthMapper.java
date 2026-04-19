package com.zhku.agriwarningplatform.module.auth.mapper;

import com.zhku.agriwarningplatform.module.auth.domain.UserDO;
import com.zhku.agriwarningplatform.module.auth.vo.CreateUserResp;
import com.zhku.agriwarningplatform.module.auth.vo.LoginReqVO;
import com.zhku.agriwarningplatform.module.auth.vo.LoginRespVO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AuthMapper {
    @Select("select id, username, password, role, delete_flag as deleteFlag, gmt_create as gmtCreate, gmt_modified as gmtModified " +
            "from user where username = #{username} and delete_flag = 0")
    UserDO selectByUsername(@Param("username") String username);


    @Update("update user set password = #{password} where username = #{username} and delete_flag = 0")
    int updatePassword(@Param("username") String username, @Param("password") String encode);

    @Insert("insert into user (username, password, role) values (#{username}, #{password}, #{role})")
    void addUser(@Param("username") String username, @Param("password") String encode, @Param("role") String role);

    @Select("select id, username, role from user where username = #{username} and delete_flag = 0")
    CreateUserResp adminselectByUsername(@Param("username") String username);
}
