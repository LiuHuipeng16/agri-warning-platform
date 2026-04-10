package com.zhku.agriwarningplatform.module.pestenvironment.controller;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 3:33
 */

import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.module.pest.convert.PestConvert;
import com.zhku.agriwarningplatform.module.pestenvironment.controller.param.PestEnvironmentSaveOrUpdateParam;
import com.zhku.agriwarningplatform.module.pestenvironment.controller.vo.PestEnvironmentDetailVO;
import com.zhku.agriwarningplatform.module.pestenvironment.service.PestEnvironmentService;
import com.zhku.agriwarningplatform.module.pestenvironment.service.dto.PestEnvironmentDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pestEnvironment")
@RequiredArgsConstructor
@Validated
public class PestEnvironmentController {
    @Autowired
    private final PestEnvironmentService pestEnvironmentService;

    @GetMapping("/detailByPestId/{pestId}")
    public CommonResult<PestEnvironmentDetailVO> detailByPestId(
            @PathVariable("pestId") @NotNull(message = "病虫害ID不能为空") Long pestId) {
        PestEnvironmentDTO dto = pestEnvironmentService.detailByPestId(pestId);
        return CommonResult.success(PestConvert.toEnvironmentDetailVO(dto));
    }

    @PostMapping("/saveOrUpdate")
    public CommonResult<Boolean> saveOrUpdate(@Valid @RequestBody PestEnvironmentSaveOrUpdateParam param) {
        return CommonResult.success(pestEnvironmentService.saveOrUpdate(param));
    }
}
