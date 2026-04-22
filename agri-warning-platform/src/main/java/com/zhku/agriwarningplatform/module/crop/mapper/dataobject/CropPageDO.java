package com.zhku.agriwarningplatform.module.crop.mapper.dataobject;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class CropPageDO {
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
     * 作物简介
     */
    private String intro;
    /**
     * 作物详细描述
     */
    private String imageUrl;
    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;
}
