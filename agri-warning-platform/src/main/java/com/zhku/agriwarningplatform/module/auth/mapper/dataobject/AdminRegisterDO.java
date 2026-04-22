package com.zhku.agriwarningplatform.module.auth.mapper.dataobject;

import lombok.Data;

@Data
public class AdminRegisterDO {
    private Long id;
    private String username;
    private String role;
}
