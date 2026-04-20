package com.zhku.agriwarningplatform.module.pest.mapper;

import com.zhku.agriwarningplatform.module.pest.mapper.dataobject.PestDO;
import com.zhku.agriwarningplatform.module.pest.service.dto.PestOptionDTO;
import com.zhku.agriwarningplatform.module.pest.service.dto.PestPageItemDTO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:09
 */


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PestMapper {
    int insert(PestDO pestDO);

    int updateById(PestDO pestDO);

    PestDO selectById(@Param("id") Long id);

    PestDO selectByIdIncludingDeleted(@Param("id") Long id);

    PestDO selectByName(@Param("name") String name);

    PestDO selectByNameExcludeId(@Param("name") String name, @Param("excludeId") Long excludeId);

    Long countPage(@Param("name") String name,
                   @Param("type") String type,
                   @Param("riskLevel") String riskLevel,
                   @Param("season") String season,
                   @Param("cropId") Long cropId);

    List<PestPageItemDTO> selectPage(@Param("name") String name,
                                     @Param("type") String type,
                                     @Param("riskLevel") String riskLevel,
                                     @Param("season") String season,
                                     @Param("cropId") Long cropId,
                                     @Param("offset") Integer offset,
                                     @Param("pageSize") Integer pageSize);

    List<PestOptionDTO> selectOptions();

    int logicDeleteById(@Param("id") Long id);

    PestDO selectByNameIncludingDeleted(@Param("name") String name);

    PestDO selectByNameExcludeIdIncludingDeleted(@Param("name") String name,
                                                 @Param("excludeId") Long excludeId);

    @org.apache.ibatis.annotations.Select("select count(1) from prewarning_rules where pest_id = #{pestId} and delete_flag = 0")
    Long countRuleByPestId(@Param("pestId") Long pestId);

    @org.apache.ibatis.annotations.Select("select count(1) from lightweight_knowledge_base_enhanced_qa where pest_id = #{pestId} and delete_flag = 0")
    Long countKnowledgeByPestId(@Param("pestId") Long pestId);

    @org.apache.ibatis.annotations.Select("select count(1) from warning where pest_id = #{pestId} and delete_flag = 0")
    Long countWarningByPestId(@Param("pestId") Long pestId);
}