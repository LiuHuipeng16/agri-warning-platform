package com.zhku.agriwarningplatform.common.errorcode;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 23:02
 */
public interface StatsErrorCode {
    //controller层错误码
    ErrorCode STATS_QUERY_ERROR =
            new ErrorCode(500, "STATS_001", "统计数据查询失败");

    //service层错误码

}
