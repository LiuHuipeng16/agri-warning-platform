package com.zhku.agriwarningplatform.module.pest.convert;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 23:12
 */

import com.zhku.agriwarningplatform.module.pest.controller.vo.CropSimpleVO;
import com.zhku.agriwarningplatform.module.pest.controller.vo.PestDetailVO;
import com.zhku.agriwarningplatform.module.pest.controller.vo.PestOptionVO;
import com.zhku.agriwarningplatform.module.pest.controller.vo.PestPageVO;
import com.zhku.agriwarningplatform.module.pest.service.dto.CropSimpleDTO;
import com.zhku.agriwarningplatform.module.pest.service.dto.PestDetailDTO;
import com.zhku.agriwarningplatform.module.pest.service.dto.PestOptionDTO;
import com.zhku.agriwarningplatform.module.pest.service.dto.PestPageItemDTO;
import com.zhku.agriwarningplatform.module.pestenvironment.controller.vo.PestEnvironmentDetailVO;
import com.zhku.agriwarningplatform.module.pestenvironment.controller.vo.PestEnvironmentVO;
import com.zhku.agriwarningplatform.module.pestenvironment.service.dto.PestEnvironmentDTO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class PestConvert {

    private PestConvert() {
    }

    public static PestPageVO toPageVO(PestPageItemDTO dto) {
        if (dto == null) {
            return null;
        }
        PestPageVO vo = new PestPageVO();
        vo.setId(dto.getId());
        vo.setName(dto.getName());
        vo.setType(dto.getType());
        vo.setRiskLevel(dto.getRiskLevel());
        vo.setSeason(dto.getSeason());
        vo.setDescription(dto.getDescription());
        return vo;
    }

    public static List<PestPageVO> toPageVOList(List<PestPageItemDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return Collections.emptyList();
        }
        return dtoList.stream().map(PestConvert::toPageVO).collect(Collectors.toList());
    }

    public static CropSimpleVO toCropSimpleVO(CropSimpleDTO dto) {
        if (dto == null) {
            return null;
        }
        CropSimpleVO vo = new CropSimpleVO();
        vo.setId(dto.getId());
        vo.setName(dto.getName());
        return vo;
    }

    public static List<CropSimpleVO> toCropSimpleVOList(List<CropSimpleDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return Collections.emptyList();
        }
        return dtoList.stream().map(PestConvert::toCropSimpleVO).collect(Collectors.toList());
    }

    public static PestEnvironmentVO toEnvironmentVO(PestEnvironmentDTO dto) {
        if (dto == null) {
            return null;
        }
        PestEnvironmentVO vo = new PestEnvironmentVO();
        vo.setTemperatureRange(dto.getTemperatureRange());
        vo.setHumidityRange(dto.getHumidityRange());
        vo.setEnvironmentDescription(dto.getEnvironmentDescription());
        return vo;
    }

    public static PestDetailVO toDetailVO(PestDetailDTO dto) {
        if (dto == null) {
            return null;
        }
        PestDetailVO vo = new PestDetailVO();
        vo.setId(dto.getId());
        vo.setName(dto.getName());
        vo.setType(dto.getType());
        vo.setDescription(dto.getDescription());
        vo.setSymptoms(dto.getSymptoms());
        vo.setCause(dto.getCause());
        vo.setPrevention(dto.getPrevention());
        vo.setRiskLevel(dto.getRiskLevel());
        vo.setSeason(dto.getSeason());
        vo.setCropIds(dto.getCropIds());
        vo.setCropList(toCropSimpleVOList(dto.getCropList()));
        vo.setEnvironment(toEnvironmentVO(dto.getEnvironment()));
        return vo;
    }

    public static PestOptionVO toOptionVO(PestOptionDTO dto) {
        if (dto == null) {
            return null;
        }
        PestOptionVO vo = new PestOptionVO();
        vo.setLabel(dto.getLabel());
        vo.setValue(dto.getValue());
        return vo;
    }

    public static List<PestOptionVO> toOptionVOList(List<PestOptionDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return Collections.emptyList();
        }
        return dtoList.stream().map(PestConvert::toOptionVO).collect(Collectors.toList());
    }

    public static PestEnvironmentDetailVO toEnvironmentDetailVO(PestEnvironmentDTO dto) {
        if (dto == null) {
            return null;
        }
        PestEnvironmentDetailVO vo = new PestEnvironmentDetailVO();
        vo.setPestId(dto.getPestId());
        vo.setTemperatureRange(dto.getTemperatureRange());
        vo.setHumidityRange(dto.getHumidityRange());
        vo.setEnvironmentDescription(dto.getEnvironmentDescription());
        return vo;
    }
}
