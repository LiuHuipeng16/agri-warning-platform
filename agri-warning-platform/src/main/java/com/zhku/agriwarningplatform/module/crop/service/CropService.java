package com.zhku.agriwarningplatform.module.crop.service;

import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.crop.param.CropCreateParam;
import com.zhku.agriwarningplatform.module.crop.param.CropUpdateParam;
import com.zhku.agriwarningplatform.module.crop.vo.CropOptionVO;
import com.zhku.agriwarningplatform.module.crop.param.CropQueryReqParam;
import com.zhku.agriwarningplatform.module.crop.vo.CropQueryRespVO;
import com.zhku.agriwarningplatform.module.crop.vo.DetailRespVO;

import java.util.List;

public interface CropService {
    PageResult<CropQueryRespVO> pageQuery(CropQueryReqParam cropQueryReqParam);

    DetailRespVO detail(Long id);

    CommonResult<Long> create(CropCreateParam cropQueryReqParam);

    Boolean update(CropUpdateParam cropQueryReqParam);

    Boolean delete(Long id);

    List<CropOptionVO> getCropOptions();
}
