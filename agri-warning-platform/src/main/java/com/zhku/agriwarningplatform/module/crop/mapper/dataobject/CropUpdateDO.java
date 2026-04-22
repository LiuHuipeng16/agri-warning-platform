package com.zhku.agriwarningplatform.module.crop.mapper.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CropUpdateDO {
    private Long id;
    private String name;
    private String category;
    private String imageUrl;
    private String intro;
    private String description;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private Integer deleteFlag;
}
