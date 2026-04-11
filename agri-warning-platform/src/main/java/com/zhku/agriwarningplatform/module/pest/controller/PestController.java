package com.zhku.agriwarningplatform.module.pest.controller;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 3:32
 */

import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.common.util.JacksonUtils;
import com.zhku.agriwarningplatform.module.pest.controller.param.PestCreateParam;
import com.zhku.agriwarningplatform.module.pest.controller.param.PestPageQueryParam;
import com.zhku.agriwarningplatform.module.pest.controller.param.PestUpdateParam;
import com.zhku.agriwarningplatform.module.pest.controller.vo.PestDetailVO;
import com.zhku.agriwarningplatform.module.pest.controller.vo.PestOptionVO;
import com.zhku.agriwarningplatform.module.pest.controller.vo.PestPageVO;
import com.zhku.agriwarningplatform.module.pest.convert.PestConvert;
import com.zhku.agriwarningplatform.module.pest.service.PestService;
import com.zhku.agriwarningplatform.module.pest.service.dto.PestDetailDTO;
import com.zhku.agriwarningplatform.module.pest.service.dto.PestOptionDTO;
import com.zhku.agriwarningplatform.module.pest.service.dto.PestPageItemDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pests")
@RequiredArgsConstructor
@Validated
public class PestController {

    private final PestService pestService;

    @GetMapping("/page")
    public CommonResult<PageResult<PestPageVO>> page(@Valid PestPageQueryParam param) {
        log.info("进入接口:PestController#page,param={}", JacksonUtils.writeValueAsString(param));
        PageResult<PestPageItemDTO> dtoPage = pestService.page(param);
        PageResult<PestPageVO> voPage = new PageResult<>();
        voPage.setTotal(dtoPage.getTotal());
        voPage.setRecords(PestConvert.toPageVOList(dtoPage.getRecords()));
        return CommonResult.success(voPage);
    }


    @GetMapping("/detail/{id}")
    public CommonResult<PestDetailVO> detail(@PathVariable("id") @NotNull(message = "病虫害ID不能为空") Long id) {
        log.info("进入接口:PestController#detail,id={}", id);
        PestDetailDTO dto = pestService.detail(id);
        return CommonResult.success(PestConvert.toDetailVO(dto));
    }

    @PostMapping("/create")
    public CommonResult<Long> create(@Valid @RequestBody PestCreateParam param) {
        log.info("进入接口:PestController#create,param={}", JacksonUtils.writeValueAsString(param));
        return CommonResult.success(pestService.create(param));
    }

    @PutMapping("/update")
    public CommonResult<Boolean> update(@Valid @RequestBody PestUpdateParam param) {
        log.info("进入接口:PestController#update,param={}", JacksonUtils.writeValueAsString(param));
        return CommonResult.success(pestService.update(param));
    }

    @DeleteMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable("id") @NotNull(message = "病虫害ID不能为空") Long id) {
        log.info("进入接口:PestController#delete,id={}", id);
        return CommonResult.success(pestService.delete(id));
    }

    @GetMapping("/options")
    public CommonResult<List<PestOptionVO>> options() {
        log.info("进入接口:PestController#options");
        List<PestOptionDTO> dtoList = pestService.options();
        return CommonResult.success(PestConvert.toOptionVOList(dtoList));
    }
}
