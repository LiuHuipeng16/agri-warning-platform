package com.zhku.agriwarningplatform.common.errorcode;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 20:27
 */
/**
 * 天气模块错误码
 */
public interface WeatherErrorCode {

    // ==================== controller层错误码 ====================

    ErrorCode PARAM_INVALID = new ErrorCode(400, "WEATHER_001", "请求参数不合法");
    ErrorCode FORECAST_DAYS_INVALID = new ErrorCode(400, "WEATHER_004", "生成天数不合法");
    // ==================== service层错误码 ====================

    ErrorCode WEATHER_QUERY_FAILED = new ErrorCode(502, "WEATHER_002", "天气数据查询失败");

    ErrorCode WEATHER_DATA_EMPTY = new ErrorCode(404, "WEATHER_003", "天气数据不存在");
}
