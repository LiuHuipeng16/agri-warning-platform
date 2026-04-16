package com.zhku.agriwarningplatform.module.pest.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:12
 */

import java.time.LocalDateTime;
import lombok.Data;
@Data
public class CropPestRelDO {

    /**
     * 作物ID
     */
    private Long cropId;

    /**
     * 病虫害ID
     */
    private Long pestId;

    /**
     * 删除标记：0未删除，1已删除
     */
    private Integer deleteFlag;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    private LocalDateTime gmtModified;
}
