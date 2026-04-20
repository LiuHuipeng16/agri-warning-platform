package com.zhku.agriwarningplatform.module.pest.mapper;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 23:22
 */

import com.zhku.agriwarningplatform.module.pest.mapper.dataobject.CropPestRelDO;
import com.zhku.agriwarningplatform.module.pest.service.dto.CropSimpleDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CropPestRelMapper {

    int batchInsert(@Param("list") List<CropPestRelDO> list);

    int logicDeleteByPestId(@Param("pestId") Long pestId);

    int deletePhysicalByPestId(@Param("pestId") Long pestId);

    List<Long> selectCropIdsByPestId(@Param("pestId") Long pestId);

    List<CropSimpleDTO> selectCropListByPestId(@Param("pestId") Long pestId);

    Long countValidCropIds(@Param("cropIds") List<Long> cropIds);
    @org.apache.ibatis.annotations.Select("select count(1) from crop_pest_rel where crop_id = #{cropId} and delete_flag = 0")
    Long countByCropId(@Param("cropId") Long cropId);

    @org.apache.ibatis.annotations.Select("select count(1) from crop_pest_rel where pest_id = #{pestId} and delete_flag = 0")
    Long countByPestId(@Param("pestId") Long pestId);
}