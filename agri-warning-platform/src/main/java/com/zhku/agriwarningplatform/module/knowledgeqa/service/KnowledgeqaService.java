package com.zhku.agriwarningplatform.module.knowledgeqa.service;

import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.param.KnowledgeqaCreateParam;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.param.KnowledgeqaUpdateParam;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.param.KnowledgeqaReqParam;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.vo.KnowledgeqaRespVO;

public interface KnowledgeqaService {
    PageResult<KnowledgeqaRespVO> page(KnowledgeqaReqParam reqVO);

    CommonResult<Long> create(KnowledgeqaCreateParam param);

    CommonResult<Boolean> update(KnowledgeqaUpdateParam param);

    CommonResult<Boolean> delete(Long id);
}
