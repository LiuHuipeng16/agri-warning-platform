package com.zhku.agriwarningplatform.module.ai.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-30
 * Time: 11:01
 */
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AIChatImageStreamReqDTO {

    /**
     * 会话ID
     */
    private String chatId;

    /**
     * 用户问题
     */
    private String prompt;

    /**
     * 当前用户ID
     */
    private Long userId;

    /**
     * 图片列表
     */
    private MultipartFile[] images;
}