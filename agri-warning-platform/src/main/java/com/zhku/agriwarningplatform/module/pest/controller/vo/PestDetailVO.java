package com.zhku.agriwarningplatform.module.pest.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:33
 */

import com.zhku.agriwarningplatform.module.pestenvironment.controller.vo.PestEnvironmentVO;
import lombok.Data;

import java.util.List;

@Data
public class PestDetailVO {

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

    private List<CropSimpleVO> cropList;

    private PestEnvironmentVO environment;
}
