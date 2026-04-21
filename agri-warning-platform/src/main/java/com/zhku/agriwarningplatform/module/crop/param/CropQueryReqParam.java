package com.zhku.agriwarningplatform.module.crop.param;
import com.zhku.agriwarningplatform.common.page.PageParam;
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
public class CropQueryReqParam extends PageParam {
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
}