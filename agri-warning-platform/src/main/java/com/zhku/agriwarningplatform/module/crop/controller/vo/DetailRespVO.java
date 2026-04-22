package com.zhku.agriwarningplatform.module.crop.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 作物详情响应VO
 * 对应接口：获取作物详情
 */
@Data
public class DetailRespVO {
    /**
     * 作物ID (long)
     */
    private Long id;
    /**
     * 作物名称 (string)
     */
    private String name;

    /**
     * 作物分类 (string)
     */
    private String category;

    /**
     * 作物简介 (string)
     */
    private String intro;

    /**
     * 作物详细描述 (string)
     */
    private String description;

    /**
     * 作物图片地址 (string)
     */
    private String imageUrl;
}