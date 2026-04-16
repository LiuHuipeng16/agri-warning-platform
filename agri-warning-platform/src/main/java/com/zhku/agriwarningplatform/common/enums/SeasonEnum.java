package com.zhku.agriwarningplatform.common.enums;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 11:15
 */
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 高发季节枚举
 */
@Getter
@AllArgsConstructor
public enum SeasonEnum {

    SPRING("春"),
    SUMMER("夏"),
    AUTUMN("秋"),
    WINTER("冬"),
    ALL_YEAR("全年");

    private final String message;
    public static boolean isValid(String value) {
        return Arrays.stream(values()).anyMatch(item -> item.getMessage().equals(value));
    }
}
