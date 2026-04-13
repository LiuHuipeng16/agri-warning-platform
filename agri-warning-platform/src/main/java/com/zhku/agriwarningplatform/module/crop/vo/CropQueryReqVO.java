package com.zhku.agriwarningplatform.module.crop.vo;
import com.zhku.agriwarningplatform.common.page.PageParam;
import lombok.Data;

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
    private String name;

    /**
     * 作物分类（选填）
     * 示例：粮食作物
     */
    private String category;
    /**
     * 作物描述关键词（选填）
     * 示例：小麦
     */
    private String description;
    /**
     * 作物描述关键词（选填）
     * 示例：小麦
     */
    private String intro;
    /**
     * 作物图片关键词（选填）
     * 示例：小麦
     */
    private String imageUrl;
}