package com.zhku.agriwarningplatform.module.crop.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhku.agriwarningplatform.common.errorcode.CropErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.crop.controller.vo.CropOptionVO;
import com.zhku.agriwarningplatform.module.crop.controller.vo.CropQueryRespVO;
import com.zhku.agriwarningplatform.module.crop.controller.vo.DetailRespVO;
import com.zhku.agriwarningplatform.module.crop.mapper.CropMapper;
import com.zhku.agriwarningplatform.module.crop.mapper.dataobject.CropDO;
import com.zhku.agriwarningplatform.module.crop.mapper.dataobject.CropDetailDO;
import com.zhku.agriwarningplatform.module.crop.mapper.dataobject.CropPageDO;
import com.zhku.agriwarningplatform.module.crop.mapper.dataobject.CropUpdateDO;
import com.zhku.agriwarningplatform.module.crop.param.CropCreateParam;
import com.zhku.agriwarningplatform.module.crop.param.CropQueryReqParam;
import com.zhku.agriwarningplatform.module.crop.param.CropUpdateParam;
import com.zhku.agriwarningplatform.module.crop.service.CropService;
import com.zhku.agriwarningplatform.module.crop.param.CropQueryReqParam;
import com.zhku.agriwarningplatform.module.pest.mapper.CropPestRelMapper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CropServiceImpl implements CropService {
    private final CropMapper cropMapper;
    private final CropPestRelMapper cropPestRelMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PageResult<CropQueryRespVO> pageQuery(CropQueryReqParam cropQueryReqParam) {
        if (cropQueryReqParam.getPageNum() == null || cropQueryReqParam.getPageSize() == null){
            throw new ServiceException(CropErrorCode.PAGE_PARAM_ERROR);
        }

                Page<CropPageDO> page = PageHelper.startPage(
                        cropQueryReqParam.getPageNum(),
                        cropQueryReqParam.getPageSize());
        List<CropPageDO> doList =cropMapper.selectList(cropQueryReqParam);
        List<CropQueryRespVO> records = new ArrayList<>();
        if (doList != null) {
            for (CropPageDO doItem : doList) {
                CropQueryRespVO vo = new CropQueryRespVO();
                vo.setId(doItem.getId());
                vo.setName(doItem.getName());
                vo.setCategory(doItem.getCategory());
                vo.setIntro(doItem.getIntro());
                vo.setImageUrl(doItem.getImageUrl());
                vo.setGmtCreate(doItem.getGmtCreate());
                records.add(vo);
            }
        }
        PageResult<CropQueryRespVO> pageResult = new PageResult<>();
        pageResult.setTotal((int)page.getTotal());
        pageResult.setRecords(records);
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DetailRespVO detail(@PathVariable @NotNull(message = "作物ID不能为空") @Min(value = 1, message = "作物ID必须大于0") Long id) {
        if (id == null){
            throw new ServiceException(CropErrorCode.CROP_ID_EMPTY);
        }
        CropDetailDO cropDetailDO= cropMapper.detail(id);
        if (cropDetailDO == null){
            throw new ServiceException(CropErrorCode.CROP_NOT_EXIST);
        }
        if (id <= 0){
            throw new ServiceException(CropErrorCode.CROP_ID_INVALID);
        }
        DetailRespVO cropQueryRespVO = new DetailRespVO();
        cropQueryRespVO.setId(cropDetailDO.getId());
        cropQueryRespVO.setName(cropDetailDO.getName());
        cropQueryRespVO.setCategory(cropDetailDO.getCategory());
        cropQueryRespVO.setIntro(cropDetailDO.getIntro());
        cropQueryRespVO.setDescription(cropDetailDO.getDescription());
        cropQueryRespVO.setImageUrl(cropDetailDO.getImageUrl());
        return cropQueryRespVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Long> create(CropCreateParam cropQueryReqParam) {
        if (cropQueryReqParam.getName() == null || cropQueryReqParam.getName().isEmpty()){
            throw new ServiceException(CropErrorCode.CROP_NAME_EMPTY);
        }
        if (cropQueryReqParam.getCategory() == null || cropQueryReqParam.getCategory().isEmpty()){
            throw new ServiceException(CropErrorCode.CROP_CATEGORY_EMPTY);
        }
        CropQueryRespVO cropQueryRespVO = new CropQueryRespVO();
        BeanUtils.copyProperties(cropQueryReqParam,cropQueryRespVO);
        if (cropMapper.selectByName(cropQueryRespVO.getName()) != null){
            throw new ServiceException(CropErrorCode.CROP_NAME_EXISTS);
        }
        int rows =cropMapper.addcrop(cropQueryRespVO);
        if (rows != 1){
            throw new ServiceException(CropErrorCode.CREATE_CROP_FAILED);
        }

        return CommonResult.success(cropQueryRespVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(CropUpdateParam cropQueryReqParam) {
        if (cropQueryReqParam.getId() == null){
            throw new ServiceException(CropErrorCode.CROP_ID_EMPTY);
        }
        if (cropQueryReqParam.getId() <= 0){
            throw new ServiceException(CropErrorCode.CROP_ID_INVALID);
        }
        if (!StringUtils.hasText(cropQueryReqParam.getName())) {
            throw new ServiceException(CropErrorCode.CROP_NAME_EMPTY);
        }
        if (!StringUtils.hasText(cropQueryReqParam.getCategory())) {
            throw new ServiceException(CropErrorCode.CROP_CATEGORY_EMPTY);
        }
        CropDO cropDO= cropMapper.selectById(cropQueryReqParam.getId());
        if (cropDO == null){
            throw new ServiceException(CropErrorCode.CROP_NOT_EXIST);
        }
        if (cropMapper.selectByName(cropQueryReqParam.getName()) != null && !cropDO.getName().equals(cropQueryReqParam.getName())){
            throw new ServiceException(CropErrorCode.CROP_NAME_EXISTS);
        }
        int rows = cropMapper.update(cropQueryReqParam);
        if (rows != 1){
            throw new ServiceException(CropErrorCode.UPDATE_CROP_FAILED);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        log.info("删除作物：{}", id);

        if (id == null) {
            throw new ServiceException(CropErrorCode.CROP_ID_EMPTY);
        }
        if (id <= 0) {
            throw new ServiceException(CropErrorCode.CROP_ID_INVALID);
        }

        CropDO cropDO = cropMapper.selectByIdDO(id);
        if (cropDO == null) {
            throw new ServiceException(CropErrorCode.CROP_NOT_EXIST);
        }

        Long cropPestRelCount = cropPestRelMapper.countByCropId(id);
        if (cropPestRelCount != null && cropPestRelCount > 0) {
            throw new ServiceException(CropErrorCode.CROP_HAS_PEST_REL);
        }

        Long ruleCount = cropMapper.countRuleByCropId(id);
        if (ruleCount != null && ruleCount > 0) {
            throw new ServiceException(CropErrorCode.CROP_HAS_RULE);
        }

        Long knowledgeCount = cropMapper.countKnowledgeByCropId(id);
        if (knowledgeCount != null && knowledgeCount > 0) {
            throw new ServiceException(CropErrorCode.CROP_HAS_KNOWLEDGE);
        }

        Long warningCount = cropMapper.countWarningByCropId(id);
        if (warningCount != null && warningCount > 0) {
            throw new ServiceException(CropErrorCode.CROP_HAS_WARNING);
        }

        int rows = cropMapper.updateDeleteFlag(id);
        if (rows != 1) {
            throw new ServiceException(CropErrorCode.DELETE_CROP_FAILED);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CropOptionVO> getCropOptions() {
        List<CropOptionVO> cropOptions =cropMapper.selectCropOptions();
        if (cropOptions == null){
            throw new ServiceException(CropErrorCode.CROP_OPTIONS_EMPTY);
        }
        return cropOptions;
    }
}
