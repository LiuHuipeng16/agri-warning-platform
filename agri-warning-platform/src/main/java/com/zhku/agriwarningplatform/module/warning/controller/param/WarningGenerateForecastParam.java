package com.zhku.agriwarningplatform.module.warning.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 15:08
 */
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 手动触发多天预警生成参数
 */
@Data
public class WarningGenerateForecastParam {

    /**
     * 生成未来几天预警
     */
    @NotNull(message = "生成天数不能为空")
    @Min(value = 1, message = "生成天数最小为1")
    @Max(value = 7, message = "生成天数最大为7")
    private Integer days;
}
