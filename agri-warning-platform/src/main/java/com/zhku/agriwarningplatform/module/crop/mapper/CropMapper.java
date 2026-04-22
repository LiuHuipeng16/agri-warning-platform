package com.zhku.agriwarningplatform.module.crop.mapper;

import com.zhku.agriwarningplatform.module.crop.controller.vo.CropOptionVO;
import com.zhku.agriwarningplatform.module.crop.controller.vo.CropQueryRespVO;
import com.zhku.agriwarningplatform.module.crop.mapper.dataobject.CropDO;
import com.zhku.agriwarningplatform.module.crop.mapper.dataobject.CropDetailDO;
import com.zhku.agriwarningplatform.module.crop.mapper.dataobject.CropPageDO;
import com.zhku.agriwarningplatform.module.crop.param.CropQueryReqParam;
import com.zhku.agriwarningplatform.module.crop.param.CropUpdateParam;
import org.apache.ibatis.annotations.*;

import java.util.List;
@Mapper
public interface CropMapper {
    List<CropPageDO> selectList(CropQueryReqParam cropQueryReqParam);
    @Select("select * from crop where id = #{id} and delete_flag=0")
    CropDetailDO detail(Long id);

    int addcrop(CropQueryRespVO cropQueryRespVO);

    @Select("select * from crop where name = #{name} and delete_flag=0")
    CropDO selectByName(String name);

    @Select("select id from crop where name = #{name} and delete_flag=0")
    Long getIdByName(String name);

    int update(CropUpdateParam cropQueryReqParam);

    @Select("select * from crop where id = #{id} and delete_flag=0")
    CropDO selectById(Long id);

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
