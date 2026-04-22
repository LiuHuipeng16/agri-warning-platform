package com.zhku.agriwarningplatform.module.crop.controller.vo;

import lombok.Data;

@Data
public class FileRespVO {
    private String fileUrl;
    private String originalName;
    private String fileSize;
    private String fileType;
}
