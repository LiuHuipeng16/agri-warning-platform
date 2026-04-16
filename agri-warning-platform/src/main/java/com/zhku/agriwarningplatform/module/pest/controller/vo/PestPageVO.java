package com.zhku.agriwarningplatform.module.pest.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:32
 */
import lombok.Data;

@Data
public class PestPageVO {

    private Long id;

    private String name;

    private String type;

    private String riskLevel;

    private String season;

    private String description;
}