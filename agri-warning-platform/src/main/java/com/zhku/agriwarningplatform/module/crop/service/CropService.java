package com.zhku.agriwarningplatform.module.crop.service;

import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.crop.vo.CropQueryReqVO;
import com.zhku.agriwarningplatform.module.crop.vo.CropQueryRespVO;

public interface CropService {
    PageResult<CropQueryRespVO> pageQuery(CropQueryReqVO cropQueryReqVO);

    CropQueryRespVO detail(Long id);

    CommonResult<Long> create(CropQueryReqVO cropQueryReqVO);

    Boolean update(CropQueryReqVO cropQueryReqVO);

    Boolean delete(Long id);
}
