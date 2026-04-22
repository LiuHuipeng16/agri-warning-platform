package com.zhku.agriwarningplatform.module.crop.service;

import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.crop.controller.vo.CropOptionVO;
import com.zhku.agriwarningplatform.module.crop.controller.vo.CropQueryReqVO;
import com.zhku.agriwarningplatform.module.crop.controller.vo.CropQueryRespVO;
import com.zhku.agriwarningplatform.module.crop.controller.vo.DetailRespVO;

import java.util.List;

public interface CropService {
    PageResult<CropQueryRespVO> pageQuery(CropQueryReqVO cropQueryReqVO);

    DetailRespVO detail(Long id);

    CommonResult<Long> create(CropQueryReqVO cropQueryReqVO);

    Boolean update(CropQueryReqVO cropQueryReqVO);

    Boolean delete(Long id);

    List<CropOptionVO> getCropOptions();
}
