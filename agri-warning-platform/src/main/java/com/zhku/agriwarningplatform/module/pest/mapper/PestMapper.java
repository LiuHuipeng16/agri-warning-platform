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
}