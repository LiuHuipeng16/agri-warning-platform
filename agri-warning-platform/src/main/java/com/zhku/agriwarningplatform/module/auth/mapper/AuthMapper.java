package com.zhku.agriwarningplatform.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhku.agriwarningplatform.module.auth.mapper.dataobject.UserDO;
import com.zhku.agriwarningplatform.module.auth.mapper.dataobject.AdminRegisterDO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AuthMapper extends BaseMapper<UserDO> {
    @Select("select id, username, password, role, delete_flag as deleteFlag, gmt_create as gmtCreate, gmt_modified as gmtModified " +
            "from user where username = #{username} and delete_flag = 0")
    UserDO selectByUsername(@Param("username") String username);


    @Update("update user set password = #{password} where username = #{username} and delete_flag = 0")
    int updatePassword(@Param("username") String username, @Param("password") String encode);

    @Insert("insert into user (username, password, role) values (#{username}, #{password}, #{role})")
    void addUser(@Param("username") String username, @Param("password") String encode, @Param("role") String role);

    @Select("select count(*) from user where ")
    Integer count();
    @Select("""
        SELECT id, username, role
        FROM user
        WHERE username = #{username}
        AND delete_flag = 0
        LIMIT 1
        """)
    AdminRegisterDO adminselectByUsername(@Param("username") String username);
}
