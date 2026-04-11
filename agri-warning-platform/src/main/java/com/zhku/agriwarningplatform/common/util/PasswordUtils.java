package com.zhku.agriwarningplatform.common.util;

import cn.hutool.crypto.digest.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {

    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
