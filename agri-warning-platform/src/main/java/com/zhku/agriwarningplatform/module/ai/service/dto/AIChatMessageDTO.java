package com.zhku.agriwarningplatform.module.ai.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class AIChatMessageDTO {

    private String role;

    private String content;

    /**
     * 消息类型：TEXT / IMAGE_TEXT
     */
    private String messageType;

    /**
     * 图片地址列表，普通文本消息为null
     */
    private List<String> imageUrls;

    /**
     * 图片识别结果，普通文本消息为null
     */
    private String imageAnalysis;
}