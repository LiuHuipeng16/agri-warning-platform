package com.zhku.agriwarningplatform.module.crop.mapper;

import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.module.crop.vo.CropQueryReqVO;
import com.zhku.agriwarningplatform.module.crop.vo.CropQueryRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
@Mapper
public interface CropMapper {
    List<CropQueryRespVO> selectList( CropQueryReqVO cropQueryReqVO);
    @Select("select * from crop where id = #{id}")
    CropQueryRespVO detail(Long id);

    int addcrop(CropQueryRespVO cropQueryRespVO);

    @Select("select * from crop where name = #{name}")
    CropQueryRespVO selectByName(String name);

    @Select("select id from crop where name = #{name}")
    Long getIdByName(String name);

    int update(CropQueryReqVO cropQueryReqVO);

    @Select("delete from crop where id = #{id}")
    int delete(Long id);

    @Select("select * from crop where id = #{id}")
    CropQueryRespVO selectById(Long id);
}
