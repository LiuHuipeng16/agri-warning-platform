package com.zhku.agriwarningplatform.module.crop.vo;

import lombok.Data;

@Data
public class CropOptionVO {
    /**
     * 前端显示的名称（对应你红框的 label）
     */
    private String label;

    /**
     * 后端真实值（对应你红框的 value）
     */
    private Long value;
}
