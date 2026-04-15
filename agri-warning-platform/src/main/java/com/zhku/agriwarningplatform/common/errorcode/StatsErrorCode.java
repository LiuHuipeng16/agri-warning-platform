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
    ErrorCode PARAM_IS_NULL = new ErrorCode(400, "STATS_CONTROLLER_001", "请求参数不能为空");

    ErrorCode PARAM_INVALID = new ErrorCode(400, "STATS_CONTROLLER_002", "请求参数不合法");
    //service层错误码
    ErrorCode DATA_NOT_EXIST = new ErrorCode(404, "STATS_SERVICE_001", "统计数据不存在");

    ErrorCode QUERY_FAILED = new ErrorCode(500, "STATS_SERVICE_002", "统计数据查询失败");

    ErrorCode DASHBOARD_QUERY_FAILED = new ErrorCode(500, "STATS_SERVICE_003", "后台仪表盘统计数据查询失败");

    ErrorCode CROP_PEST_COUNT_QUERY_FAILED = new ErrorCode(500, "STATS_SERVICE_004", "作物病虫害数量统计查询失败");

    ErrorCode PEST_TYPE_DISTRIBUTION_QUERY_FAILED = new ErrorCode(500, "STATS_SERVICE_005", "病害/虫害比例统计查询失败");

    ErrorCode HIGH_RISK_PESTS_QUERY_FAILED = new ErrorCode(500, "STATS_SERVICE_006", "高风险病虫害分布统计查询失败");

    ErrorCode SEASON_TREND_QUERY_FAILED = new ErrorCode(500, "STATS_SERVICE_007", "季节高发趋势统计查询失败");
}
