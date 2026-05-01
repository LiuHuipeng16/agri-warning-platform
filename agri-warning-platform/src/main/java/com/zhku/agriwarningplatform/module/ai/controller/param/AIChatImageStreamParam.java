package com.zhku.agriwarningplatform.module.ai.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-30
 * Time: 11:00
 */
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AIChatImageStreamParam {

    /**
     * 会话ID
     */
    @NotBlank(message = "会话ID不能为空")
    private String chatId;

    /**
     * 用户针对图片提出的问题
     */
    @NotBlank(message = "问题内容不能为空")
    private String prompt;

    /**
     * 图片文件列表，支持1~3张
     */
    @NotNull(message = "图片不能为空")
    @Size(min = 1, max = 3, message = "图片数量必须在1到3张之间")
    private MultipartFile[] images;
}