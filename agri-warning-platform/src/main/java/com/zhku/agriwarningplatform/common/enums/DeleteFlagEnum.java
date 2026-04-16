package com.zhku.agriwarningplatform.common.enums;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 11:16
 */
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 删除标记枚举
 */
@Getter
@AllArgsConstructor
public enum DeleteFlagEnum {

    NOT_DELETED(0, "未删除"),
    DELETED(1, "已删除");

    private final Integer code;
    private final String message;

}