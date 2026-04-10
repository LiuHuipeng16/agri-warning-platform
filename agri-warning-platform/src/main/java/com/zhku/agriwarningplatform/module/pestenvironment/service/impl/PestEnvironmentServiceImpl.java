package com.zhku.agriwarningplatform.module.pestenvironment.service.impl;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 23:24
 */

import com.zhku.agriwarningplatform.common.enums.DeleteFlagEnum;
import com.zhku.agriwarningplatform.common.errorcode.PestErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.module.pest.constant.PestLockManager;
import com.zhku.agriwarningplatform.module.pest.mapper.PestMapper;
import com.zhku.agriwarningplatform.module.pest.mapper.dataobject.PestDO;
import com.zhku.agriwarningplatform.module.pestenvironment.controller.param.PestEnvironmentSaveOrUpdateParam;
import com.zhku.agriwarningplatform.module.pestenvironment.mapper.PestEnvironmentMapper;
import com.zhku.agriwarningplatform.module.pestenvironment.mapper.dataobject.PestEnvironmentDO;
import com.zhku.agriwarningplatform.module.pestenvironment.service.PestEnvironmentService;
import com.zhku.agriwarningplatform.module.pestenvironment.service.dto.PestEnvironmentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PestEnvironmentServiceImpl implements PestEnvironmentService {

    private final PestEnvironmentMapper pestEnvironmentMapper;
    private final PestMapper pestMapper;

    @Override
    public PestEnvironmentDTO detailByPestId(Long pestId) {
        if (pestId == null) {
            throw new ServiceException(PestErrorCode.PEST_ENVIRONMENT_PEST_ID_EMPTY);
        }

        PestDO pestDO = pestMapper.selectById(pestId);
        if (pestDO == null) {
            throw new ServiceException(PestErrorCode.PEST_NOT_EXIST);
        }

        PestEnvironmentDO environmentDO = pestEnvironmentMapper.selectByPestId(pestId);
        if (environmentDO == null) {
            throw new ServiceException(PestErrorCode.PEST_ENVIRONMENT_NOT_EXIST);
        }

        PestEnvironmentDTO dto = new PestEnvironmentDTO();
        dto.setPestId(environmentDO.getPestId());
        dto.setTemperatureRange(environmentDO.getTemperatureRange());
        dto.setHumidityRange(environmentDO.getHumidityRange());
        dto.setEnvironmentDescription(environmentDO.getEnvironmentDescription());
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdate(PestEnvironmentSaveOrUpdateParam param) {
        if (param.getPestId() == null) {
            throw new ServiceException(PestErrorCode.PEST_ENVIRONMENT_PEST_ID_EMPTY);
        }

        String lockKey = PestLockManager.buildPestEnvironmentLockKey(param.getPestId());
        ReentrantLock lock = PestLockManager.getLock(lockKey);
        lock.lock();
        try {
            PestDO pestDO = pestMapper.selectById(param.getPestId());
            if (pestDO == null) {
                throw new ServiceException(PestErrorCode.PEST_NOT_EXIST);
            }

            PestEnvironmentDO oldDO = pestEnvironmentMapper.selectByPestIdIncludingDeleted(param.getPestId());

            if (oldDO == null) {
                PestEnvironmentDO insertDO = new PestEnvironmentDO();
                insertDO.setPestId(param.getPestId());
                insertDO.setTemperatureRange(trimToNull(param.getTemperatureRange()));
                insertDO.setHumidityRange(trimToNull(param.getHumidityRange()));
                insertDO.setEnvironmentDescription(trimToNull(param.getEnvironmentDescription()));
                insertDO.setDeleteFlag(DeleteFlagEnum.NOT_DELETED.getCode());

                int count = pestEnvironmentMapper.insert(insertDO);
                if (count <= 0) {
                    throw new ServiceException(PestErrorCode.PEST_ENV_SAVE_FAILED);
                }
            } else {
                PestEnvironmentDO updateDO = new PestEnvironmentDO();
                updateDO.setPestId(param.getPestId());
                updateDO.setTemperatureRange(trimToNull(param.getTemperatureRange()));
                updateDO.setHumidityRange(trimToNull(param.getHumidityRange()));
                updateDO.setEnvironmentDescription(trimToNull(param.getEnvironmentDescription()));
                updateDO.setDeleteFlag(DeleteFlagEnum.NOT_DELETED.getCode());

                int count = pestEnvironmentMapper.updateByPestId(updateDO);
                if (count <= 0) {
                    throw new ServiceException(PestErrorCode.PEST_ENV_SAVE_FAILED);
                }
            }

            return Boolean.TRUE;
        } finally {
            lock.unlock();
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
