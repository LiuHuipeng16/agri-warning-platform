package com.zhku.agriwarningplatform.module.knowledgeqa.mapper;

import com.zhku.agriwarningplatform.module.knowledgeqa.vo.KnowledgeqaReqVO;
import com.zhku.agriwarningplatform.module.knowledgeqa.vo.KnowledgeqaRespVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface KnowledgeqaMapper {
    List<KnowledgeqaRespVO> page(KnowledgeqaReqVO reqVO);

    int add(KnowledgeqaReqVO reqVO);

    int update(KnowledgeqaReqVO reqVO);

    @Delete("delete from lightweight_knowledge_base_enhanced_qa where id = #{id}")
    int delete(Long id);
}
