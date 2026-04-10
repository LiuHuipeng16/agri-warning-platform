package com.zhku.agriwarningplatform.module.pest.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:19
 */

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PestPageQueryParam {

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于等于1")
    private Integer pageNum;

    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数必须大于等于1")
    private Integer pageSize;

    @Length(max = 100, message = "病虫害名称关键词长度不能超过100")
    private String name;

    @Length(max = 20, message = "病虫害类型长度不能超过20")
    private String type;

    @Length(max = 20, message = "风险等级长度不能超过20")
    private String riskLevel;

    @Length(max = 100, message = "高发季节长度不能超过100")
    private String season;

    private Long cropId;
}
