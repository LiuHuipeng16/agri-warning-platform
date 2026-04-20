package com.zhku.agriwarningplatform.module.crop.mapper;

import com.zhku.agriwarningplatform.module.crop.mapper.dataobject.CropDO;
import com.zhku.agriwarningplatform.module.crop.vo.CropOptionVO;
import com.zhku.agriwarningplatform.module.crop.vo.CropQueryReqVO;
import com.zhku.agriwarningplatform.module.crop.vo.CropQueryRespVO;
import com.zhku.agriwarningplatform.module.crop.vo.DetailRespVO;
import org.apache.ibatis.annotations.*;

import java.util.List;
@Mapper
public interface CropMapper {
    List<CropQueryRespVO> selectList( CropQueryReqVO cropQueryReqVO);
    @Select("select * from crop where id = #{id} and delete_flag=0")
    DetailRespVO detail(Long id);

    int addcrop(CropQueryRespVO cropQueryRespVO);

    @Select("select * from crop where name = #{name} and delete_flag=0")
    CropQueryRespVO selectByName(String name);

    @Select("select id from crop where name = #{name} and delete_flag=0")
    Long getIdByName(String name);

    int update(CropQueryReqVO cropQueryReqVO);

    @Select("select * from crop where id = #{id} and delete_flag=0")
    CropQueryRespVO selectById(Long id);

    @Select("select id as value, name as label from crop where delete_flag=0")
    List<CropOptionVO> selectCropOptions();

    @Update("update crop set delete_flag = 1 where id = #{id}")
    int updateDeleteFlag(Long id);

    @Select("select * from crop where id = #{id} and delete_flag=0")
    CropDO selectByIdDO(Long id);
    @Select("select count(1) from prewarning_rules where crop_id = #{cropId} and delete_flag = 0")
    Long countRuleByCropId(Long cropId);

    @Select("select count(1) from lightweight_knowledge_base_enhanced_qa where crop_id = #{cropId} and delete_flag = 0")
    Long countKnowledgeByCropId(Long cropId);

    @Select("select count(1) from warning where crop_id = #{cropId} and delete_flag = 0")
    Long countWarningByCropId(Long cropId);
}
