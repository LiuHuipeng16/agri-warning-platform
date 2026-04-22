package com.zhku.agriwarningplatform.common.enums;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 11:11
 */

import lombok.AllArgsConstructor;

/**
 * 用户角色枚举
 */
@AllArgsConstructor
public enum RoleEnum {
    ADMIN("管理员"),USER("普通用户");
    private String message;
    public static boolean isValid(String message){
        for(RoleEnum roleEnum:RoleEnum.values()){
            if(roleEnum.name().equals(message)){
                return true;
            }
        }
        return false;
    }
}
