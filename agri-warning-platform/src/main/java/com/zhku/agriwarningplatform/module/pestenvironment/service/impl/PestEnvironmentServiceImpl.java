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
            // 1. 校验病虫害是否存在
            PestDO pestDO = pestMapper.selectById(param.getPestId());
            if (pestDO == null) {
                throw new ServiceException(PestErrorCode.PEST_NOT_EXIST);
            }

            // 2. 查环境记录是否已存在（包含已删除）
            PestEnvironmentDO oldDO = pestEnvironmentMapper.selectByPestIdIncludingDeleted(param.getPestId());

            if (oldDO == null) {
                // 3. 不存在则新增
                PestEnvironmentDO insertDO = new PestEnvironmentDO();
                insertDO.setPestId(param.getPestId());
                insertDO.setTemperatureRange(trim(param.getTemperatureRange()));
                insertDO.setHumidityRange(trim(param.getHumidityRange()));
                insertDO.setEnvironmentDescription(trim(param.getEnvironmentDescription()));
                insertDO.setDeleteFlag(DeleteFlagEnum.NOT_DELETED.getCode());

                int count = pestEnvironmentMapper.insert(insertDO);
                if (count <= 0) {
                    throw new ServiceException(PestErrorCode.PEST_ENV_SAVE_FAILED);
                }
            } else {
                // 4. 存在则按“有传才更新”的语义更新
                PestEnvironmentDO updateDO = new PestEnvironmentDO();
                updateDO.setPestId(param.getPestId());

                if (param.getTemperatureRange() != null) {
                    updateDO.setTemperatureRange(trim(param.getTemperatureRange()));
                }
                if (param.getHumidityRange() != null) {
                    updateDO.setHumidityRange(trim(param.getHumidityRange()));
                }
                if (param.getEnvironmentDescription() != null) {
                    updateDO.setEnvironmentDescription(trim(param.getEnvironmentDescription()));
                }

                // 如果之前是逻辑删除状态，saveOrUpdate 应恢复为未删除
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

    private String trim(String value) {
        return value == null ? null : value.trim();
    }


}
