package com.zhku.agriwarningplatform.module.pest.service;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:09
 */

import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.pest.controller.param.PestCreateParam;
import com.zhku.agriwarningplatform.module.pest.controller.param.PestPageQueryParam;
import com.zhku.agriwarningplatform.module.pest.controller.param.PestUpdateParam;
import com.zhku.agriwarningplatform.module.pest.service.dto.PestDetailDTO;
import com.zhku.agriwarningplatform.module.pest.service.dto.PestOptionDTO;
import com.zhku.agriwarningplatform.module.pest.service.dto.PestPageItemDTO;

import java.util.List;

public interface PestService {

    PageResult<PestPageItemDTO> page(PestPageQueryParam param);

    PestDetailDTO detail(Long id);

    Long create(PestCreateParam param);

    Boolean update(PestUpdateParam param);

    Boolean delete(Long id);

    List<PestOptionDTO> options();
}
