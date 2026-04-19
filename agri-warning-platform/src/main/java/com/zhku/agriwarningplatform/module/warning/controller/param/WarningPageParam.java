package com.zhku.agriwarningplatform.module.warning.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 15:07
 */
import com.zhku.agriwarningplatform.common.page.PageParam;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 预警分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WarningPageParam extends PageParam {

    /**
     * 预警标题关键词
     */
    private String title;

    /**
     * 作物ID
     */
    @Min(value = 1, message = "作物ID必须大于等于1")
    private Long cropId;

    /**
     * 病虫害ID
     */
    @Min(value = 1, message = "病虫害ID必须大于等于1")
    private Long pestId;

    /**
     * 风险等级：低 / 中 / 高
     */
    private String riskLevel;

    /**
     * 预警类型：TODAY / FORECAST
     */
    private String warningType;

    /**
     * 预警日期开始
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate warningDateStart;

    /**
     * 预警日期结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate warningDateEnd;
}
