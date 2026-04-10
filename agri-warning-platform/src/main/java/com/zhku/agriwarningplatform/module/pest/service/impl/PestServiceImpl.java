package com.zhku.agriwarningplatform.module.pest.service.impl;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:09
 */

import com.zhku.agriwarningplatform.common.enums.DeleteFlagEnum;
import com.zhku.agriwarningplatform.common.enums.PestTypeEnum;
import com.zhku.agriwarningplatform.common.enums.RiskLevelEnum;
import com.zhku.agriwarningplatform.common.enums.SeasonEnum;
import com.zhku.agriwarningplatform.common.errorcode.PestErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.result.PageResult;

import com.zhku.agriwarningplatform.module.pest.constant.PestLockManager;
import com.zhku.agriwarningplatform.module.pest.controller.param.PestCreateParam;
import com.zhku.agriwarningplatform.module.pest.controller.param.PestPageQueryParam;
import com.zhku.agriwarningplatform.module.pest.controller.param.PestUpdateParam;
import com.zhku.agriwarningplatform.module.pest.mapper.CropPestRelMapper;
import com.zhku.agriwarningplatform.module.pest.mapper.PestMapper;
import com.zhku.agriwarningplatform.module.pest.mapper.dataobject.CropPestRelDO;
import com.zhku.agriwarningplatform.module.pest.mapper.dataobject.PestDO;
import com.zhku.agriwarningplatform.module.pest.service.PestService;
import com.zhku.agriwarningplatform.module.pest.service.dto.CropSimpleDTO;
import com.zhku.agriwarningplatform.module.pest.service.dto.PestDetailDTO;
import com.zhku.agriwarningplatform.module.pest.service.dto.PestOptionDTO;
import com.zhku.agriwarningplatform.module.pest.service.dto.PestPageItemDTO;
import com.zhku.agriwarningplatform.module.pestenvironment.controller.param.PestEnvironmentParam;
import com.zhku.agriwarningplatform.module.pestenvironment.mapper.PestEnvironmentMapper;
import com.zhku.agriwarningplatform.module.pestenvironment.mapper.dataobject.PestEnvironmentDO;
import com.zhku.agriwarningplatform.module.pestenvironment.service.dto.PestEnvironmentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PestServiceImpl implements PestService {

    private final PestMapper pestMapper;
    private final CropPestRelMapper cropPestRelMapper;
    private final PestEnvironmentMapper pestEnvironmentMapper;

    @Override
    public PageResult<PestPageItemDTO> page(PestPageQueryParam param) {
        validateQueryEnums(param.getType(), param.getRiskLevel(), param.getSeason());

        int offset = (param.getPageNum() - 1) * param.getPageSize();
        Long total = pestMapper.countPage(
                trimToNull(param.getName()),
                trimToNull(param.getType()),
                trimToNull(param.getRiskLevel()),
                trimToNull(param.getSeason()),
                param.getCropId()
        );

        List<PestPageItemDTO> records = pestMapper.selectPage(
                trimToNull(param.getName()),
                trimToNull(param.getType()),
                trimToNull(param.getRiskLevel()),
                trimToNull(param.getSeason()),
                param.getCropId(),
                offset,
                param.getPageSize()
        );

        PageResult<PestPageItemDTO> result = new PageResult<>();
        result.setTotal((int)(total == null ? 0L : total));
        result.setRecords(records);
        return result;
    }

    @Override
    public PestDetailDTO detail(Long id) {
        if (id == null) {
            throw new ServiceException(PestErrorCode.PEST_ID_EMPTY);
        }

        PestDO pestDO = pestMapper.selectById(id);
        if (pestDO == null) {
            throw new ServiceException(PestErrorCode.PEST_NOT_EXIST);
        }

        PestDetailDTO dto = new PestDetailDTO();
        dto.setId(pestDO.getId());
        dto.setName(pestDO.getName());
        dto.setType(pestDO.getType());
        dto.setDescription(pestDO.getDescription());
        dto.setSymptoms(pestDO.getSymptoms());
        dto.setCause(pestDO.getCause());
        dto.setPrevention(pestDO.getPrevention());
        dto.setRiskLevel(pestDO.getRiskLevel());
        dto.setSeason(pestDO.getSeason());

        List<Long> cropIds = cropPestRelMapper.selectCropIdsByPestId(id);
        List<CropSimpleDTO> cropList = cropPestRelMapper.selectCropListByPestId(id);
        dto.setCropIds(cropIds);
        dto.setCropList(cropList);

        PestEnvironmentDO environmentDO = pestEnvironmentMapper.selectByPestId(id);
        if (environmentDO != null) {
            PestEnvironmentDTO environmentDTO = new PestEnvironmentDTO();
            environmentDTO.setPestId(environmentDO.getPestId());
            environmentDTO.setTemperatureRange(environmentDO.getTemperatureRange());
            environmentDTO.setHumidityRange(environmentDO.getHumidityRange());
            environmentDTO.setEnvironmentDescription(environmentDO.getEnvironmentDescription());
            dto.setEnvironment(environmentDTO);
        }

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PestCreateParam param) {
        validateCreateOrUpdateParam(
                param.getName(),
                param.getType(),
                param.getRiskLevel(),
                param.getSeason(),
                param.getCropIds()
        );

        String nameLockKey = PestLockManager.buildPestNameLockKey(param.getName().trim());
        ReentrantLock nameLock = PestLockManager.getLock(nameLockKey);
        nameLock.lock();
        try {
            PestDO exist = pestMapper.selectByName(param.getName().trim());
            if (exist != null) {
                throw new ServiceException(PestErrorCode.PEST_NAME_EXIST);
            }

            PestDO pestDO = new PestDO();
            pestDO.setId(IdGenerator.nextId());
            pestDO.setName(param.getName().trim());
            pestDO.setType(param.getType().trim());
            pestDO.setDescription(trimToNull(param.getDescription()));
            pestDO.setSymptoms(trimToNull(param.getSymptoms()));
            pestDO.setCause(trimToNull(param.getCause()));
            pestDO.setPrevention(trimToNull(param.getPrevention()));
            pestDO.setRiskLevel(defaultRiskLevel(param.getRiskLevel()));
            pestDO.setSeason(trimToNull(param.getSeason()));
            pestDO.setDeleteFlag(DeleteFlagEnum.NOT_DELETED.getCode());

            int insertCount = pestMapper.insert(pestDO);
            if (insertCount <= 0) {
                throw new ServiceException(PestErrorCode.PEST_CREATE_FAILED);
            }

            saveCropRelations(pestDO.getId(), param.getCropIds());
            saveEnvironment(pestDO.getId(), param.getEnvironment());

            return pestDO.getId();
        } finally {
            nameLock.unlock();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PestUpdateParam param) {
        validateCreateOrUpdateParam(
                param.getName(),
                param.getType(),
                param.getRiskLevel(),
                param.getSeason(),
                param.getCropIds()
        );
        if (param.getId() == null) {
            throw new ServiceException(PestErrorCode.PEST_ID_EMPTY);
        }

        String idLockKey = PestLockManager.buildPestIdLockKey(param.getId());
        String nameLockKey = PestLockManager.buildPestNameLockKey(param.getName().trim());

        ReentrantLock firstLock = PestLockManager.getLock(sortKey(idLockKey, nameLockKey));
        ReentrantLock secondLock = PestLockManager.getLock(sortKeyReverse(idLockKey, nameLockKey));

        firstLock.lock();
        secondLock.lock();
        try {
            PestDO oldPest = pestMapper.selectById(param.getId());
            if (oldPest == null) {
                throw new ServiceException(PestErrorCode.PEST_NOT_EXIST);
            }

            PestDO nameExist = pestMapper.selectByNameExcludeId(param.getName().trim(), param.getId());
            if (nameExist != null) {
                throw new ServiceException(PestErrorCode.PEST_NAME_EXIST);
            }

            PestDO updateDO = new PestDO();
            updateDO.setId(param.getId());
            updateDO.setName(param.getName().trim());
            updateDO.setType(param.getType().trim());
            updateDO.setDescription(trimToNull(param.getDescription()));
            updateDO.setSymptoms(trimToNull(param.getSymptoms()));
            updateDO.setCause(trimToNull(param.getCause()));
            updateDO.setPrevention(trimToNull(param.getPrevention()));
            updateDO.setRiskLevel(defaultRiskLevel(param.getRiskLevel()));
            updateDO.setSeason(trimToNull(param.getSeason()));

            int updateCount = pestMapper.updateById(updateDO);
            if (updateCount <= 0) {
                throw new ServiceException(PestErrorCode.PEST_UPDATE_FAILED);
            }

            replaceCropRelations(param.getId(), param.getCropIds());
            replaceEnvironment(param.getId(), param.getEnvironment());

            return Boolean.TRUE;
        } finally {
            secondLock.unlock();
            firstLock.unlock();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        if (id == null) {
            throw new ServiceException(PestErrorCode.PEST_ID_EMPTY);
        }

        String idLockKey = PestLockManager.buildPestIdLockKey(id);
        ReentrantLock lock = PestLockManager.getLock(idLockKey);
        lock.lock();
        try {
            PestDO pestDO = pestMapper.selectById(id);
            if (pestDO == null) {
                throw new ServiceException(PestErrorCode.PEST_NOT_EXIST);
            }

            int pestDelete = pestMapper.logicDeleteById(id);
            if (pestDelete <= 0) {
                throw new ServiceException(PestErrorCode.PEST_DELETE_FAILED);
            }

            cropPestRelMapper.logicDeleteByPestId(id);
            pestEnvironmentMapper.logicDeleteByPestId(id);

            return Boolean.TRUE;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<PestOptionDTO> options() {
        return pestMapper.selectOptions();
    }

    private void validateQueryEnums(String type, String riskLevel, String season) {
        if (StringUtils.hasText(type) && !PestTypeEnum.isValid(type.trim())) {
            throw new ServiceException(PestErrorCode.PEST_TYPE_INVALID);
        }
        if (StringUtils.hasText(riskLevel) && !RiskLevelEnum.isValid(riskLevel.trim())) {
            throw new ServiceException(PestErrorCode.PEST_RISK_LEVEL_INVALID);
        }
        if (StringUtils.hasText(season) && !SeasonEnum.isValid(season.trim())) {
            throw new ServiceException(PestErrorCode.PEST_SEASON_INVALID);
        }
    }

    private void validateCreateOrUpdateParam(String name,
                                             String type,
                                             String riskLevel,
                                             String season,
                                             List<Long> cropIds) {
        if (!StringUtils.hasText(name)) {
            throw new ServiceException(PestErrorCode.PEST_NAME_EMPTY);
        }
        if (!StringUtils.hasText(type) || !PestTypeEnum.isValid(type.trim())) {
            throw new ServiceException(PestErrorCode.PEST_TYPE_INVALID);
        }
        if (StringUtils.hasText(riskLevel) && !RiskLevelEnum.isValid(riskLevel.trim())) {
            throw new ServiceException(PestErrorCode.PEST_RISK_LEVEL_INVALID);
        }
        if (StringUtils.hasText(season) && !SeasonEnum.isValid(season.trim())) {
            throw new ServiceException(PestErrorCode.PEST_SEASON_INVALID);
        }

        List<Long> distinctCropIds = distinctCropIds(cropIds);
        if (!CollectionUtils.isEmpty(distinctCropIds)) {
            Long count = cropPestRelMapper.countValidCropIds(distinctCropIds);
            if (count == null || count.intValue() != distinctCropIds.size()) {
                throw new ServiceException(PestErrorCode.CROP_NOT_EXIST);
            }
        }
    }

    private void saveCropRelations(Long pestId, List<Long> cropIds) {
        List<Long> distinctCropIds = distinctCropIds(cropIds);
        if (CollectionUtils.isEmpty(distinctCropIds)) {
            return;
        }

        List<CropPestRelDO> relList = new ArrayList<>();
        for (Long cropId : distinctCropIds) {
            CropPestRelDO relDO = new CropPestRelDO();
            relDO.setCropId(cropId);
            relDO.setPestId(pestId);
            relDO.setDeleteFlag(DeleteFlagEnum.NOT_DELETED.getCode());
            relList.add(relDO);
        }

        int count = cropPestRelMapper.batchInsert(relList);
        if (count < relList.size()) {
            throw new ServiceException(PestErrorCode.PEST_REL_SAVE_FAILED);
        }
    }

    private void replaceCropRelations(Long pestId, List<Long> cropIds) {
        cropPestRelMapper.deletePhysicalByPestId(pestId);
        saveCropRelations(pestId, cropIds);
    }

    private void saveEnvironment(Long pestId, PestEnvironmentParam environmentParam) {
        if (environmentParam == null) {
            return;
        }
        if (!hasEnvironmentContent(environmentParam.getTemperatureRange(),
                environmentParam.getHumidityRange(),
                environmentParam.getEnvironmentDescription())) {
            return;
        }

        PestEnvironmentDO environmentDO = new PestEnvironmentDO();
        environmentDO.setPestId(pestId);
        environmentDO.setTemperatureRange(trimToNull(environmentParam.getTemperatureRange()));
        environmentDO.setHumidityRange(trimToNull(environmentParam.getHumidityRange()));
        environmentDO.setEnvironmentDescription(trimToNull(environmentParam.getEnvironmentDescription()));
        environmentDO.setDeleteFlag(DeleteFlagEnum.NOT_DELETED.getCode());

        int count = pestEnvironmentMapper.insert(environmentDO);
        if (count <= 0) {
            throw new ServiceException(PestErrorCode.PEST_ENV_SAVE_FAILED);
        }
    }

    private void replaceEnvironment(Long pestId, PestEnvironmentParam environmentParam) {
        pestEnvironmentMapper.deletePhysicalByPestId(pestId);
        saveEnvironment(pestId, environmentParam);
    }

    private boolean hasEnvironmentContent(String temperatureRange, String humidityRange, String environmentDescription) {
        return StringUtils.hasText(temperatureRange)
                || StringUtils.hasText(humidityRange)
                || StringUtils.hasText(environmentDescription);
    }

    private List<Long> distinctCropIds(List<Long> cropIds) {
        if (CollectionUtils.isEmpty(cropIds)) {
            return new ArrayList<>();
        }
        List<Long> filtered = cropIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toList());
        if (filtered.size() != cropIds.size()) {
            throw new ServiceException(PestErrorCode.CROP_ID_LIST_INVALID);
        }
        return new ArrayList<>(new LinkedHashSet<>(filtered));
    }

    private String defaultRiskLevel(String riskLevel) {
        return StringUtils.hasText(riskLevel) ? riskLevel.trim() : RiskLevelEnum.MEDIUM.getMessage();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String sortKey(String key1, String key2) {
        return key1.compareTo(key2) <= 0 ? key1 : key2;
    }

    private String sortKeyReverse(String key1, String key2) {
        return key1.compareTo(key2) <= 0 ? key2 : key1;
    }

    /**
     * 你项目里如果已经有雪花算法、UidGenerator、IdUtil，就替换这里。
     */
    private static final class IdGenerator {
        private static long sequence = System.currentTimeMillis();

        private IdGenerator() {
        }

        private static synchronized long nextId() {
            return ++sequence;
        }
    }
}
