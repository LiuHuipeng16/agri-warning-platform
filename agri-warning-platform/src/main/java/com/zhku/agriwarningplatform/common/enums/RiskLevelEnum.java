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

/**
 * 风险等级枚举
 */
@Getter
@AllArgsConstructor
public enum RiskLevelEnum {

    LOW("低"),
    MEDIUM("中"),
    HIGH("高");

    private final String message;

}
