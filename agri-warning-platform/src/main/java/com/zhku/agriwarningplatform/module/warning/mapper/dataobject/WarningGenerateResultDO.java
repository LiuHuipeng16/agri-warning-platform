package com.zhku.agriwarningplatform.module.warning.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 15:03
 */
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 预警生成结果 DO
 * 仅用于 service 内部组装，不直接映射数据库表
 */
@TableName("warning")
@Data
public class WarningGenerateResultDO {

    /**
     * 生成成功数量
     */
    private Integer generatedCount;

    /**
     * 跳过数量
     */
    private Integer skippedCount;

    /**
     * 预警日期（当天生成时使用）
     */
    private LocalDate warningDate;

    /**
     * 生成天数（多天生成时使用）
     */
    private Integer days;
}
