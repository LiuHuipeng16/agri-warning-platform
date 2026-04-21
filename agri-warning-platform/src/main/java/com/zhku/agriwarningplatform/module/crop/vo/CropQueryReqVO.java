package com.zhku.agriwarningplatform.module.crop.vo;
import com.zhku.agriwarningplatform.common.page.PageParam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * 作物查询请求参数实体
 * 对应接口的 Query 类型请求参数
 *
 * @author long-dek
 * @date 2026-04-12
 */
@Data // Lombok 注解，自动生成 Getter、Setter、toString、equals 等方法
public class CropQueryReqVO extends PageParam {
    /**
     * 作物ID（选填）
     * 示例：1
     */
    private Long id;
    /**
     * 作物名称关键词（选填）
     * 示例：小麦
     */
    @Size(max = 50, message = "作物名称长度不能超过50个字符")
    private String name;

    /**
     * 作物分类（选填）
     * 示例：粮食作物
     */
    @Size(max = 20, message = "作物分类长度不能超过20个字符")
    private String category;
    /**
     * 作物描述关键词（选填）
     * 示例：小麦
     */
    @Size(max = 1000, message = "作物详细描述长度不能超过1000个字符")
    private String description;
    /**
     * 作物描述关键词（选填）
     * 示例：小麦
     */
    @Size(max = 200, message = "作物简介长度不能超过200个字符")
    private String intro;
    /**
     * 作物图片关键词（选填）
     * 示例：小麦
     */
    @Size(max = 255, message = "图片地址长度不能超过255个字符")
    @URL(message = "图片地址格式不合法，请传入有效的URL")
    @Pattern(
            regexp = "^$|.*\\.(jpg|jpeg|png|gif)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "图片仅支持jpg、jpeg、png、gif格式"
    )
    private String imageUrl;
}