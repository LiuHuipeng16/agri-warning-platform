package com.zhku.agriwarningplatform.module.pestenvironment.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:20
 */

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PestEnvironmentParam {

    @Length(max = 100, message = "适宜温度范围长度不能超过100")
    private String temperatureRange;

    @Length(max = 100, message = "适宜湿度范围长度不能超过100")
    private String humidityRange;

    @Length(max = 2000, message = "环境条件描述长度不能超过2000")
    private String environmentDescription;
}