package com.zhku.agriwarningplatform.module.knowledgeqa.service;

import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.knowledgeqa.vo.KnowledgeqaReqVO;
import com.zhku.agriwarningplatform.module.knowledgeqa.vo.KnowledgeqaRespVO;

public interface KnowledgeqaService {
    PageResult<KnowledgeqaRespVO> page(KnowledgeqaReqVO reqVO);

    CommonResult<Long> create(KnowledgeqaReqVO reqVO);

    CommonResult<Long> update(KnowledgeqaReqVO reqVO);

    CommonResult<Boolean> delete(Long id);
}
