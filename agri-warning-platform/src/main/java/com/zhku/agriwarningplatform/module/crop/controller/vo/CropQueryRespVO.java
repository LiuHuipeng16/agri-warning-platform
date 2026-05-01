package com.zhku.agriwarningplatform.module.crop.controller.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CropQueryRespVO {
    /**
     * 作物ID
     */
    private Long id;

    /**
     * 作物名称
     */
    private String name;

    /**
     * 作物分类
     */
    private String category;

    /**
     * 作物图片
     */
    private String imageUrl;

    /**
     * 作物介绍
     */
    private String intro;

    /**
     *
     */
    private String description;
    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;
}
