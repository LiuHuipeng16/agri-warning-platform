package com.zhku.agriwarningplatform.module.auth.controller.param;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-23
 * Time: 20:12
 */
@Data
public class AuthBatchDeleteReqParam {
    @NotNull
    @Size(min = 1)
    private List<Long> ids;
}
