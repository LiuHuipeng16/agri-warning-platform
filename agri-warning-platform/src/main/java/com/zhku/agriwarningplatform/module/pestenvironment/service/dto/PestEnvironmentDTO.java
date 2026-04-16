package com.zhku.agriwarningplatform.module.pestenvironment.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:17
 */

import lombok.Data;

@Data
public class PestEnvironmentDTO {

    private Long pestId;

    private String temperatureRange;

    private String humidityRange;

    private String environmentDescription;
}
