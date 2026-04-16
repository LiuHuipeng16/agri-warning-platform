package com.zhku.agriwarningplatform.common.enums;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 11:14
 */
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 病虫害类型枚举
 */
@Getter
@AllArgsConstructor
public enum PestTypeEnum {

    DISEASE("病害"),
    INSECT("虫害");

    private final String message;
    public static boolean isValid(String value) {
        return Arrays.stream(values()).anyMatch(item -> item.getMessage().equals(value));
    }
}
