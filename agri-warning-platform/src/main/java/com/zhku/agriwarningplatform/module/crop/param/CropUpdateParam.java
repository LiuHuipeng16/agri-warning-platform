package com.zhku.agriwarningplatform.module.crop.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CropUpdateParam {
    /**
     * 作物id
     */
    @NotNull(message = "作物id不能为空")
    private Long id;
    /**
     * 作物名称
     */
    @NotBlank(message = "作物名称不能为空")
    private String name;
    /**
     * 作物描述
     */
    private String description;
    /**
     * 作物图片
     */
    private String imageUrl;
    /**
     * 作物分类
     */
    @NotBlank(message = "作物分类不能为空")
    private String category;
    /**
     * 作物简介
     */
    private String intro;
}
