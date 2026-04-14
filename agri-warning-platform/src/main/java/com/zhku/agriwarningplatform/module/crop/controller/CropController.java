package com.zhku.agriwarningplatform.module.crop.controller;

import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.crop.service.CropService;
import com.zhku.agriwarningplatform.module.crop.vo.CropOptionVO;
import com.zhku.agriwarningplatform.module.crop.vo.CropQueryReqVO;
import com.zhku.agriwarningplatform.module.crop.vo.CropQueryRespVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 3:31
 */
@Slf4j
@RestController
@RequestMapping("/api/crops")
@RequiredArgsConstructor
public class CropController {
    private final CropService cropservice;
    @GetMapping("/page")
    public PageResult<CropQueryRespVO> page(CropQueryReqVO cropQueryReqVO){
        log.info("查询作物：{}", cropQueryReqVO);
        return cropservice.pageQuery(cropQueryReqVO);
    }
    @GetMapping("/detail/{id}")
    public CommonResult<CropQueryRespVO> detail(@PathVariable Long id){
        log.info("查询作物详情：{}", id);
        return CommonResult.success(cropservice.detail(id));
    }
    @PostMapping("/create")
    public CommonResult<Long> create(@RequestBody CropQueryReqVO cropQueryReqVO){
        log.info("创建作物：{}", cropQueryReqVO);
        return cropservice.create(cropQueryReqVO);
    }
    @PutMapping("/update")
    public CommonResult<Boolean> update(@RequestBody CropQueryReqVO cropQueryReqVO){
        log.info("更新作物：{}", cropQueryReqVO);
        return CommonResult.success(cropservice.update(cropQueryReqVO));
    }
    @DeleteMapping("/delete/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id){
        log.info("删除作物：{}", id);
        return CommonResult.success(cropservice.delete(id));
    }
    @GetMapping("/options")
    public CommonResult<List<CropOptionVO>> options(){
        log.info("获取作物选项");
        return CommonResult.success(cropservice.getCropOptions());
    }

}
