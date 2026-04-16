package com.zhku.agriwarningplatform.module.pest.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:16
 */

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PestDTO {

    private Long id;

    private String name;

    private String type;

    private String description;

    private String symptoms;

    private String cause;

    private String prevention;

    private String riskLevel;

    private String season;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}
