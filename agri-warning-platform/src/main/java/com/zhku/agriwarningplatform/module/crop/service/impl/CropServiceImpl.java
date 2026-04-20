package com.zhku.agriwarningplatform.module.crop.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhku.agriwarningplatform.common.errorcode.CropErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.crop.mapper.CropMapper;
import com.zhku.agriwarningplatform.module.crop.mapper.dataobject.CropDO;
import com.zhku.agriwarningplatform.module.crop.service.CropService;
import com.zhku.agriwarningplatform.module.crop.vo.CropOptionVO;
import com.zhku.agriwarningplatform.module.crop.vo.CropQueryReqVO;
import com.zhku.agriwarningplatform.module.crop.vo.CropQueryRespVO;
import com.zhku.agriwarningplatform.module.crop.vo.DetailRespVO;
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

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CropServiceImpl implements CropService {
    private final CropMapper cropMapper;
    private final CropPestRelMapper cropPestRelMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PageResult<CropQueryRespVO> pageQuery(CropQueryReqVO cropQueryReqVO) {
        if (cropQueryReqVO.getPageNum() == null || cropQueryReqVO.getPageSize() == null){
            throw new ServiceException(CropErrorCode.PAGE_PARAM_ERROR);
        }
                Page<CropQueryRespVO> page = PageHelper.startPage(
                        cropQueryReqVO.getPageNum(),
                        cropQueryReqVO.getPageSize());
        cropMapper.selectList(cropQueryReqVO);
        PageResult<CropQueryRespVO> pageResult = new PageResult<>();
        pageResult.setTotal((int)page.getTotal());
        pageResult.setRecords(page.getResult());
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DetailRespVO detail(@PathVariable @NotNull(message = "作物ID不能为空") @Min(value = 1, message = "作物ID必须大于0") Long id) {
        if (id == null){
            throw new ServiceException(CropErrorCode.CROP_ID_EMPTY);
        }
        DetailRespVO cropQueryRespVO = cropMapper.detail(id);
        if (cropQueryRespVO == null){
            throw new ServiceException(CropErrorCode.CROP_NOT_EXIST);
        }
        if (id <= 0){
            throw new ServiceException(CropErrorCode.CROP_ID_INVALID);
        }
        return cropQueryRespVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Long> create(CropQueryReqVO cropQueryReqVO) {
        if (cropQueryReqVO.getName() == null || cropQueryReqVO.getName().isEmpty()){
            throw new ServiceException(CropErrorCode.CROP_NAME_EMPTY);
        }
        if (cropQueryReqVO.getCategory() == null || cropQueryReqVO.getCategory().isEmpty()){
            throw new ServiceException(CropErrorCode.CROP_CATEGORY_EMPTY);
        }
        CropQueryRespVO cropQueryRespVO = new CropQueryRespVO();
        BeanUtils.copyProperties(cropQueryReqVO,cropQueryRespVO);
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
    public Boolean update(CropQueryReqVO cropQueryReqVO) {
        if (cropQueryReqVO.getId() == null){
            throw new ServiceException(CropErrorCode.CROP_ID_EMPTY);
        }
        if (cropQueryReqVO.getId() <= 0){
            throw new ServiceException(CropErrorCode.CROP_ID_INVALID);
        }
        if (!StringUtils.hasText(cropQueryReqVO.getName())) {
            throw new ServiceException(CropErrorCode.CROP_NAME_EMPTY);
        }
        if (!StringUtils.hasText(cropQueryReqVO.getCategory())) {
            throw new ServiceException(CropErrorCode.CROP_CATEGORY_EMPTY);
        }
        CropQueryRespVO cropQueryRespVO = cropMapper.selectById(cropQueryReqVO.getId());
        if (cropQueryRespVO == null){
            throw new ServiceException(CropErrorCode.CROP_NOT_EXIST);
        }
        if (cropMapper.selectByName(cropQueryReqVO.getName()) != null && !cropQueryRespVO.getName().equals(cropQueryReqVO.getName())){
            throw new ServiceException(CropErrorCode.CROP_NAME_EXISTS);
        }
        int rows = cropMapper.update(cropQueryReqVO);
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
