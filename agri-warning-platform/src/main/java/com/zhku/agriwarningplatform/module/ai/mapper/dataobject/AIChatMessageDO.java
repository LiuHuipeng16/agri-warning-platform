package com.zhku.agriwarningplatform.module.ai.mapper.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AIChatMessageDO {

    private Long id;

    private String chatId;

    private Long userId;

    private String role;

    private String content;

    /**
     * 消息类型：TEXT / IMAGE_TEXT
     */
    private String messageType;

    /**
     * 图片URL列表，JSON数组字符串
     */
    private String imageUrls;

    /**
     * 图片识别结果
     */
    private String imageAnalysis;

    private String messageStatus;

    private Integer deleteFlag;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}