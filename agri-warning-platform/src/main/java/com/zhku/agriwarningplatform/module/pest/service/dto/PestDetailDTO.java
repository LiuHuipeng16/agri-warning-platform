package com.zhku.agriwarningplatform.module.pest.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:17
 */

import com.zhku.agriwarningplatform.module.pestenvironment.service.dto.PestEnvironmentDTO;


import lombok.Data;

import java.util.List;

@Data
public class PestDetailDTO {

    private Long id;

    private String name;

    private String type;

    private String description;

    private String symptoms;

    private String cause;

    private String prevention;

    private String riskLevel;

    private String season;

    private List<Long> cropIds;

    private List<CropSimpleDTO> cropList;

    private PestEnvironmentDTO environment;
}
