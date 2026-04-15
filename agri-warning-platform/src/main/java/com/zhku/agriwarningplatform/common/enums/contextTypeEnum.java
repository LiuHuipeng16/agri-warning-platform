package com.zhku.agriwarningplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-13
 * Time: 23:09
 */
@AllArgsConstructor
public enum contextTypeEnum {
    CROP("作物详情页"),PEST("病虫害详情页"),WARNING("预警详情页"),NONE("普通页面");
    private String message;
}
