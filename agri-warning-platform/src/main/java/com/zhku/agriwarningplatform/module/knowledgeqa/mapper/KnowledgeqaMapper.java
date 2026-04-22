package com.zhku.agriwarningplatform.module.knowledgeqa.mapper;

import com.zhku.agriwarningplatform.module.ai.mapper.dataobject.LightweightKnowledgeBaseEnhancedQaDO;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.param.KnowledgeqaCreateParam;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.param.KnowledgeqaUpdateParam;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.param.KnowledgeqaReqParam;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.vo.KnowledgeqaRespVO;
import com.zhku.agriwarningplatform.module.knowledgeqa.mapper.dataobject.KnowledgeqaCreateDO;
import com.zhku.agriwarningplatform.module.knowledgeqa.mapper.dataobject.KnowledgeqaPageDO;
import com.zhku.agriwarningplatform.module.knowledgeqa.mapper.dataobject.KnowledgeqaUptateDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface KnowledgeqaMapper {

    List<KnowledgeqaPageDO> page(KnowledgeqaReqParam reqVO);

    int add(KnowledgeqaCreateDO reqVO);

    int update(KnowledgeqaUptateDO reqVO);

    LightweightKnowledgeBaseEnhancedQaDO selectById(Long id);

    @Update("update lightweight_knowledge_base_enhanced_qa set delete_flag = 1 where id = #{id}")
    int updatedDeleteFlag(Long id);
}