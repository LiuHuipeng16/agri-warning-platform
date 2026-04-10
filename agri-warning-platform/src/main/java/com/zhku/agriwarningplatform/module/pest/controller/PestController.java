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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/pests")
@RequiredArgsConstructor
@Validated
public class PestController {

    private final PestService pestService;

    @GetMapping("/page")
    public CommonResult<PageResult<PestPageVO>> page(@Valid PestPageQueryParam param) {
        PageResult<PestPageItemDTO> dtoPage = pestService.page(param);
        PageResult<PestPageVO> voPage = new PageResult<>();
        voPage.setTotal(dtoPage.getTotal());
        voPage.setRecords(PestConvert.toPageVOList(dtoPage.getRecords()));
        return CommonResult.success(voPage);
    }

    @GetMapping("/detail/{id}")
    public CommonResult<PestDetailVO> detail(@PathVariable("id") @NotNull(message = "病虫害ID不能为空") Long id) {
        PestDetailDTO dto = pestService.detail(id);
        return CommonResult.success(PestConvert.toDetailVO(dto));
    }git status

    @PostMapping("/create")
    public CommonResult<Long> create(@Valid @RequestBody PestCreateParam param) {
        return CommonResult.success(pestService.create(param));
    }

    @PutMapping("/update")
    public CommonResult<Boolean> update(@Valid @RequestBody PestUpdateParam param) {
        return CommonResult.success(pestService.update(param));
    }

    @DeleteMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable("id") @NotNull(message = "病虫害ID不能为空") Long id) {
        return CommonResult.success(pestService.delete(id));
    }

    @GetMapping("/options")
    public CommonResult<List<PestOptionVO>> options() {
        List<PestOptionDTO> dtoList = pestService.options();
        return CommonResult.success(PestConvert.toOptionVOList(dtoList));
    }
}
