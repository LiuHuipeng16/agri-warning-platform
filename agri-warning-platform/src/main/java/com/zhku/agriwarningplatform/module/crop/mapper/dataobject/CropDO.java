package com.zhku.agriwarningplatform.module.crop.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 17:04
 */

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CropDO {

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
    private String description;

    /**
     * 作物图片地址
     */
    private String imageUrl;

    /**
     * 删除标记：0未删除，1已删除
     */
    private Integer deleteFlag;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    private LocalDateTime gmtModified;
}
