package com.zhku.agriwarningplatform.module.knowledgeqa.mapper;

import com.zhku.agriwarningplatform.module.ai.mapper.dataobject.LightweightKnowledgeBaseEnhancedQaDO;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.param.KnowledgeqaCreateParam;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.param.KnowledgeqaUpdateParam;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.vo.KnowledgeqaReqVO;
import com.zhku.agriwarningplatform.module.knowledgeqa.controller.vo.KnowledgeqaRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface KnowledgeqaMapper {

    List<KnowledgeqaRespVO> page(KnowledgeqaReqVO reqVO);

    int add(KnowledgeqaCreateParam reqVO);

    int update(KnowledgeqaUpdateParam reqVO);

    LightweightKnowledgeBaseEnhancedQaDO selectById(Long id);

    @Update("update lightweight_knowledge_base_enhanced_qa set delete_flag = 1 where id = #{id}")
    int updatedDeleteFlag(Long id);
}