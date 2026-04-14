package com.zhku.agriwarningplatform.module.crop.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhku.agriwarningplatform.common.errorcode.CropErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.crop.mapper.CropMapper;
import com.zhku.agriwarningplatform.module.crop.service.CropService;
import com.zhku.agriwarningplatform.module.crop.vo.CropOptionVO;
import com.zhku.agriwarningplatform.module.crop.vo.CropQueryReqVO;
import com.zhku.agriwarningplatform.module.crop.vo.CropQueryRespVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CropServiceImpl implements CropService {
    private final CropMapper cropMapper;
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
    public CropQueryRespVO detail(Long id) {
        if (id == null){
            throw new ServiceException(CropErrorCode.CROP_ID_EMPTY);
        }
        CropQueryRespVO cropQueryRespVO = cropMapper.detail(id);
        if (cropQueryRespVO == null){
            throw new ServiceException(CropErrorCode.CROP_NOT_EXIST);
        }
        cropQueryRespVO.setGmtCreate(null);
        cropQueryRespVO.setGmtModified(null);
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

        return CommonResult.success(cropQueryReqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(CropQueryReqVO cropQueryReqVO) {
        if (cropQueryReqVO.getId() == null){
            throw new ServiceException(CropErrorCode.CROP_ID_EMPTY);
        }
        if (cropQueryReqVO.getName() == null || cropQueryReqVO.getName().isEmpty()){
            throw new ServiceException(CropErrorCode.CROP_NAME_EMPTY);
        }
        if (cropQueryReqVO.getCategory() == null || cropQueryReqVO.getCategory().isEmpty()){
            throw new ServiceException(CropErrorCode.CROP_CATEGORY_EMPTY);
        }
        CropQueryRespVO cropQueryRespVO = cropMapper.selectById(cropQueryReqVO.getId());
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
        if (id == null){
            throw new ServiceException(CropErrorCode.CROP_ID_EMPTY);
        }
        int rows = cropMapper.delete(id);
        if (rows != 1){
            throw new ServiceException(CropErrorCode.DELETE_CROP_FAILED);
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CropOptionVO> getCropOptions() {
        return cropMapper.selectCropOptions();
    }
}
