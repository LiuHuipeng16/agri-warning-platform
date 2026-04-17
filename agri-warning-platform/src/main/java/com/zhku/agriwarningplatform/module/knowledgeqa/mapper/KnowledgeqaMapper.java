package com.zhku.agriwarningplatform.module.knowledgeqa.mapper;

import com.zhku.agriwarningplatform.module.knowledgeqa.vo.KnowledgeqaReqVO;
import com.zhku.agriwarningplatform.module.knowledgeqa.vo.KnowledgeqaRespVO;
import jakarta.validation.constraints.Min;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface KnowledgeqaMapper {
    List<KnowledgeqaRespVO> page(KnowledgeqaReqVO reqVO);

    int add(KnowledgeqaReqVO reqVO);

    int update(KnowledgeqaReqVO reqVO);

    @Update("update knowledgeqa set delete_flag = 1 where id = #{id}")
    int updatedDeleteFlag(@Min(value = 1, message = "ID必须大于0") Long id);
}
