package com.zhku.agriwarningplatform.module.warning.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 15:07
 */
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除预警参数
 */
@Data
public class WarningBatchDeleteParam {

    /**
     * 预警ID列表
     */
    @NotEmpty(message = "预警ID列表不能为空")
    private List<Long> warningIds;
}
