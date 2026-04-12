package com.zhku.agriwarningplatform.module.pestenvironment.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:33
 */
import lombok.Data;
@Data
public class PestEnvironmentVO {

    private String temperatureRange;

    private String humidityRange;

    private String environmentDescription;
}
