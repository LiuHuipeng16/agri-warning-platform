package com.zhku.agriwarningplatform.module.stats.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 16:40
 */
import lombok.Data;

/**
 * 病害/虫害比例统计
 */
@Data
public class PestTypeDistributionStatsDO {

    /**
     * 类型名称：病害 / 虫害
     */
    private String name;

    /**
     * 数量
     */
    private Long value;
}
