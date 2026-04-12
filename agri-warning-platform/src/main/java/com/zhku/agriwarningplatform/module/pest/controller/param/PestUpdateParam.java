package com.zhku.agriwarningplatform.module.pest.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:30
 */
import com.zhku.agriwarningplatform.module.pestenvironment.controller.param.PestEnvironmentParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
public class PestUpdateParam {

    @NotNull(message = "病虫害ID不能为空")
    private Long id;

    @NotBlank(message = "病虫害名称不能为空")
    @Length(max = 100, message = "病虫害名称长度不能超过100")
    private String name;

    @NotBlank(message = "病虫害类型不能为空")
    @Length(max = 20, message = "病虫害类型长度不能超过20")
    private String type;

    @Length(max = 5000, message = "病虫害描述长度不能超过5000")
    private String description;

    @Length(max = 5000, message = "病虫害症状长度不能超过5000")
    private String symptoms;

    @Length(max = 5000, message = "病虫害成因长度不能超过5000")
    private String cause;

    @Length(max = 5000, message = "防治措施长度不能超过5000")
    private String prevention;

    @Length(max = 20, message = "风险等级长度不能超过20")
    private String riskLevel;

    @Length(max = 100, message = "高发季节长度不能超过100")
    private String season;

    private List<Long> cropIds;

    @Valid
    private PestEnvironmentParam environment;
}
