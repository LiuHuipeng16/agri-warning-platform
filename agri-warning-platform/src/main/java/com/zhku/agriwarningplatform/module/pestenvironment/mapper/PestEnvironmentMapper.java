package com.zhku.agriwarningplatform.module.pestenvironment.mapper;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 23:23
 */

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhku.agriwarningplatform.module.pestenvironment.mapper.dataobject.PestEnvironmentDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@TableName("pest_environment_conditions")
@Mapper
public interface PestEnvironmentMapper {

    PestEnvironmentDO selectByPestId(@Param("pestId") Long pestId);

    PestEnvironmentDO selectByPestIdIncludingDeleted(@Param("pestId") Long pestId);

    int insert(PestEnvironmentDO environmentDO);

    int updateByPestId(PestEnvironmentDO environmentDO);

    int logicDeleteByPestId(@Param("pestId") Long pestId);

    int deletePhysicalByPestId(@Param("pestId") Long pestId);
}
