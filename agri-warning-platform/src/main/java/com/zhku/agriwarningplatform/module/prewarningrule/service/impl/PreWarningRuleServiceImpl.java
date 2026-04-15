package com.zhku.agriwarningplatform.module.prewarningrule.service.impl;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 11:19
 */

import com.zhku.agriwarningplatform.common.errorcode.PreWarningRuleErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.module.crop.mapper.CropMapper;
import com.zhku.agriwarningplatform.module.crop.mapper.dataobject.CropDO;
import com.zhku.agriwarningplatform.module.pest.mapper.PestMapper;
import com.zhku.agriwarningplatform.module.pest.mapper.dataobject.PestDO;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRuleChangeStatusParam;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRuleCreateParam;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRuleOptionParam;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRulePageParam;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRuleUpdateParam;
import com.zhku.agriwarningplatform.module.prewarningrule.mapper.PreWarningRuleMapper;
import com.zhku.agriwarningplatform.module.prewarningrule.mapper.dataobject.PreWarningRuleDO;
import com.zhku.agriwarningplatform.module.prewarningrule.service.PreWarningRuleService;
import com.zhku.agriwarningplatform.module.prewarningrule.service.dto.PreWarningRuleDTO;
import com.zhku.agriwarningplatform.module.prewarningrule.service.dto.PreWarningRuleOptionDTO;
import com.zhku.agriwarningplatform.module.prewarningrule.service.dto.PreWarningRulePageDTO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
public class PreWarningRuleServiceImpl implements PreWarningRuleService {

    private static final String RISK_LEVEL_LOW = "低";
    private static final String RISK_LEVEL_MEDIUM = "中";
    private static final String RISK_LEVEL_HIGH = "高";

    private static final String RULE_STATUS_ENABLED = "ENABLED";
    private static final String RULE_STATUS_DISABLED = "DISABLED";

    @Resource
    private PreWarningRuleMapper preWarningRuleMapper;

    @Resource
    private CropMapper cropMapper;

    @Resource
    private PestMapper pestMapper;

    @Override
    public PreWarningRulePageDTO page(PreWarningRulePageParam param) {
        validateRiskLevel(param.getRiskLevel());
        validateRuleStatus(param.getRuleStatus());

        Integer total = preWarningRuleMapper.countPage(param);
        List<PreWarningRuleDTO> records = Collections.emptyList();
        if (total != null && total > 0) {
            records = preWarningRuleMapper.selectPage(param);
        }

        PreWarningRulePageDTO pageDTO = new PreWarningRulePageDTO();
        pageDTO.setTotal(total == null ? 0 : total);
        pageDTO.setRecords(records);
        return pageDTO;
    }

    @Override
    public PreWarningRuleDTO detail(Long ruleId) {
        PreWarningRuleDO preWarningRuleDO = preWarningRuleMapper.selectById(ruleId);
        if (preWarningRuleDO == null) {
            throw new ServiceException(PreWarningRuleErrorCode.RULE_NOT_EXIST);
        }

        PreWarningRuleDTO detail = preWarningRuleMapper.selectDetailById(ruleId);
        if (detail == null) {
            throw new ServiceException(PreWarningRuleErrorCode.RULE_NOT_EXIST);
        }
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PreWarningRuleCreateParam param) {
        validateRiskLevel(param.getRiskLevel());
        validateRuleStatus(param.getRuleStatus());

        validateRange(param.getMinTemp(), param.getMaxTemp(), PreWarningRuleErrorCode.TEMP_RANGE_INVALID);
        validateRange(param.getMinHumidity(), param.getMaxHumidity(), PreWarningRuleErrorCode.HUMIDITY_RANGE_INVALID);
        validateRange(param.getMinPrecipitation(), param.getMaxPrecipitation(), PreWarningRuleErrorCode.PRECIPITATION_RANGE_INVALID);
        validateRange(param.getMinWindSpeed(), param.getMaxWindSpeed(), PreWarningRuleErrorCode.WIND_SPEED_RANGE_INVALID);

        checkCropExists(param.getCropId());
        checkPestExists(param.getPestId());

        PreWarningRuleDO preWarningRuleDO = new PreWarningRuleDO();
        preWarningRuleDO.setRuleName(trimToNull(param.getRuleName()));
        preWarningRuleDO.setCropId(param.getCropId());
        preWarningRuleDO.setPestId(param.getPestId());
        preWarningRuleDO.setMinTemp(param.getMinTemp());
        preWarningRuleDO.setMaxTemp(param.getMaxTemp());
        preWarningRuleDO.setMinHumidity(param.getMinHumidity());
        preWarningRuleDO.setMaxHumidity(param.getMaxHumidity());
        preWarningRuleDO.setMinPrecipitation(param.getMinPrecipitation());
        preWarningRuleDO.setMaxPrecipitation(param.getMaxPrecipitation());
        preWarningRuleDO.setMinWindSpeed(param.getMinWindSpeed());
        preWarningRuleDO.setMaxWindSpeed(param.getMaxWindSpeed());
        preWarningRuleDO.setRiskLevel(param.getRiskLevel());
        preWarningRuleDO.setSuggestion(trimToNull(param.getSuggestion()));
        preWarningRuleDO.setRuleStatus(param.getRuleStatus());

        try {
            int rows = preWarningRuleMapper.insert(preWarningRuleDO);
            if (rows <= 0 || preWarningRuleDO.getId() == null) {
                throw new ServiceException(PreWarningRuleErrorCode.CREATE_FAILED);
            }
            return preWarningRuleDO.getId();
        } catch (DuplicateKeyException e) {
            throw new ServiceException(PreWarningRuleErrorCode.DATA_CONFLICT);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PreWarningRuleUpdateParam param) {
        PreWarningRuleDO existed = preWarningRuleMapper.selectById(param.getRuleId());
        if (existed == null) {
            throw new ServiceException(PreWarningRuleErrorCode.RULE_NOT_EXIST);
        }

        if (param.getRiskLevel() != null) {
            validateRiskLevel(param.getRiskLevel());
        }
        if (param.getRuleStatus() != null) {
            validateRuleStatus(param.getRuleStatus());
        }

        BigDecimal finalMinTemp = param.getMinTemp() != null ? param.getMinTemp() : existed.getMinTemp();
        BigDecimal finalMaxTemp = param.getMaxTemp() != null ? param.getMaxTemp() : existed.getMaxTemp();
        BigDecimal finalMinHumidity = param.getMinHumidity() != null ? param.getMinHumidity() : existed.getMinHumidity();
        BigDecimal finalMaxHumidity = param.getMaxHumidity() != null ? param.getMaxHumidity() : existed.getMaxHumidity();
        BigDecimal finalMinPrecipitation = param.getMinPrecipitation() != null ? param.getMinPrecipitation() : existed.getMinPrecipitation();
        BigDecimal finalMaxPrecipitation = param.getMaxPrecipitation() != null ? param.getMaxPrecipitation() : existed.getMaxPrecipitation();
        BigDecimal finalMinWindSpeed = param.getMinWindSpeed() != null ? param.getMinWindSpeed() : existed.getMinWindSpeed();
        BigDecimal finalMaxWindSpeed = param.getMaxWindSpeed() != null ? param.getMaxWindSpeed() : existed.getMaxWindSpeed();

        validateRange(finalMinTemp, finalMaxTemp, PreWarningRuleErrorCode.TEMP_RANGE_INVALID);
        validateRange(finalMinHumidity, finalMaxHumidity, PreWarningRuleErrorCode.HUMIDITY_RANGE_INVALID);
        validateRange(finalMinPrecipitation, finalMaxPrecipitation, PreWarningRuleErrorCode.PRECIPITATION_RANGE_INVALID);
        validateRange(finalMinWindSpeed, finalMaxWindSpeed, PreWarningRuleErrorCode.WIND_SPEED_RANGE_INVALID);

        if (param.getCropId() != null) {
            checkCropExists(param.getCropId());
        }
        if (param.getPestId() != null) {
            checkPestExists(param.getPestId());
        }

        PreWarningRuleDO updateDO = new PreWarningRuleDO();
        updateDO.setId(param.getRuleId());
        updateDO.setRuleName(param.getRuleName() == null ? null : trimToNull(param.getRuleName()));
        updateDO.setCropId(param.getCropId());
        updateDO.setPestId(param.getPestId());
        updateDO.setMinTemp(param.getMinTemp());
        updateDO.setMaxTemp(param.getMaxTemp());
        updateDO.setMinHumidity(param.getMinHumidity());
        updateDO.setMaxHumidity(param.getMaxHumidity());
        updateDO.setMinPrecipitation(param.getMinPrecipitation());
        updateDO.setMaxPrecipitation(param.getMaxPrecipitation());
        updateDO.setMinWindSpeed(param.getMinWindSpeed());
        updateDO.setMaxWindSpeed(param.getMaxWindSpeed());
        updateDO.setRiskLevel(param.getRiskLevel());
            if(param.getSuggestion()!=null){
                updateDO.setSuggestion(trimToNull(param.getSuggestion()));
                updateDO.setUpdateSuggestion(true);
            }
        updateDO.setRuleStatus(param.getRuleStatus());
        try {
            int rows = preWarningRuleMapper.updateByIdSelective(updateDO);
            if (rows <= 0) {
                throw new ServiceException(PreWarningRuleErrorCode.UPDATE_FAILED);
            }
            return Boolean.TRUE;
        } catch (DuplicateKeyException e) {
            throw new ServiceException(PreWarningRuleErrorCode.DATA_CONFLICT);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long ruleId) {
        PreWarningRuleDO existed = preWarningRuleMapper.selectById(ruleId);
        if (existed == null) {
            throw new ServiceException(PreWarningRuleErrorCode.RULE_NOT_EXIST);
        }

        int rows = preWarningRuleMapper.logicalDeleteById(ruleId);
        if (rows <= 0) {
            throw new ServiceException(PreWarningRuleErrorCode.DELETE_FAILED);
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changeStatus(PreWarningRuleChangeStatusParam param) {
        PreWarningRuleDO existed = preWarningRuleMapper.selectById(param.getRuleId());
        if (existed == null) {
            throw new ServiceException(PreWarningRuleErrorCode.RULE_NOT_EXIST);
        }

        validateRuleStatus(param.getRuleStatus());

        int rows = preWarningRuleMapper.updateRuleStatusById(param.getRuleId(), param.getRuleStatus());
        if (rows <= 0) {
            throw new ServiceException(PreWarningRuleErrorCode.CHANGE_STATUS_FAILED);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<PreWarningRuleOptionDTO> options(PreWarningRuleOptionParam param) {
        if (param != null && param.getRuleStatus() != null) {
            validateRuleStatus(param.getRuleStatus());
        }
        return preWarningRuleMapper.selectOptions(param);
    }

    private void checkCropExists(Long cropId) {
        CropDO cropDO = cropMapper.selectById(cropId);
        if (cropDO == null) {
            throw new ServiceException(PreWarningRuleErrorCode.CROP_NOT_EXIST);
        }
    }

    private void checkPestExists(Long pestId) {
        PestDO pestDO = pestMapper.selectById(pestId);
        if (pestDO == null) {
            throw new ServiceException(PreWarningRuleErrorCode.PEST_NOT_EXIST);
        }
    }

    private void validateRiskLevel(String riskLevel) {
        if (riskLevel == null) {
            return;
        }
        if (!RISK_LEVEL_LOW.equals(riskLevel)
                && !RISK_LEVEL_MEDIUM.equals(riskLevel)
                && !RISK_LEVEL_HIGH.equals(riskLevel)) {
            throw new ServiceException(PreWarningRuleErrorCode.RISK_LEVEL_INVALID);
        }
    }

    private void validateRuleStatus(String ruleStatus) {
        if (ruleStatus == null) {
            return;
        }
        if (!RULE_STATUS_ENABLED.equals(ruleStatus)
                && !RULE_STATUS_DISABLED.equals(ruleStatus)) {
            throw new ServiceException(PreWarningRuleErrorCode.RULE_STATUS_INVALID);
        }
    }

    private void validateRange(BigDecimal minValue, BigDecimal maxValue, com.zhku.agriwarningplatform.common.errorcode.ErrorCode errorCode) {
        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            throw new ServiceException(errorCode);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimValue = value.trim();
        return trimValue.isEmpty() ? null : trimValue;
    }
}