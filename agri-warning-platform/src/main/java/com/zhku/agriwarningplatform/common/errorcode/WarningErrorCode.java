package com.zhku.agriwarningplatform.common.errorcode;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 23:03
 */
/**
 * 预警模块错误码
 */
public interface WarningErrorCode {

    // ==================== controller层错误码 ====================

    ErrorCode PAGE_PARAM_INVALID = new ErrorCode(400, "WARNING_001", "分页参数不合法");

    ErrorCode WARNING_ID_INVALID = new ErrorCode(400, "WARNING_002", "预警ID不合法");

    ErrorCode WARNING_IDS_EMPTY = new ErrorCode(400, "WARNING_003", "预警ID列表不能为空");

    ErrorCode WARNING_IDS_TOO_MANY = new ErrorCode(400, "WARNING_004", "单次批量删除数量不能超过50");

    ErrorCode RISK_LEVEL_INVALID = new ErrorCode(400, "WARNING_005", "风险等级参数不合法");

    ErrorCode WARNING_TYPE_INVALID = new ErrorCode(400, "WARNING_006", "预警类型参数不合法");

    ErrorCode WARNING_DATE_RANGE_INVALID = new ErrorCode(400, "WARNING_007", "预警日期范围不合法");

    ErrorCode GENERATE_DAYS_INVALID = new ErrorCode(400, "WARNING_008", "生成天数不合法");

    // ==================== service层错误码 ====================

    ErrorCode WARNING_NOT_EXIST = new ErrorCode(404, "WARNING_009", "预警不存在");

    ErrorCode WARNING_DELETE_FAILED = new ErrorCode(500, "WARNING_010", "预警删除失败");

    ErrorCode WARNING_BATCH_DELETE_FAILED = new ErrorCode(500, "WARNING_011", "批量删除预警失败");

    ErrorCode WARNING_PAGE_QUERY_FAILED = new ErrorCode(500, "WARNING_012", "预警分页查询失败");

    ErrorCode WARNING_DETAIL_QUERY_FAILED = new ErrorCode(500, "WARNING_013", "预警详情查询失败");

    ErrorCode TODAY_WARNING_QUERY_FAILED = new ErrorCode(500, "WARNING_014", "当天预警查询失败");

    ErrorCode FORECAST_WARNING_QUERY_FAILED = new ErrorCode(500, "WARNING_015", "多天预警查询失败");

    ErrorCode PREWARNING_RULE_NOT_EXIST = new ErrorCode(404, "WARNING_016", "预警规则不存在");

    ErrorCode PREWARNING_RULE_QUERY_FAILED = new ErrorCode(500, "WARNING_017", "预警规则查询失败");

    ErrorCode WEATHER_QUERY_FAILED = new ErrorCode(502, "WARNING_018", "天气数据查询失败");

    ErrorCode WARNING_GENERATE_TODAY_FAILED = new ErrorCode(500, "WARNING_019", "当天预警生成失败");

    ErrorCode WARNING_GENERATE_FORECAST_FAILED = new ErrorCode(500, "WARNING_020", "多天预警生成失败");

    ErrorCode WARNING_CREATE_FAILED = new ErrorCode(500, "WARNING_021", "预警新增失败");

    ErrorCode WARNING_DATA_CONFLICT = new ErrorCode(409, "WARNING_022", "预警数据冲突");

    ErrorCode CROP_NOT_EXIST = new ErrorCode(404, "WARNING_023", "作物不存在");

    ErrorCode PEST_NOT_EXIST = new ErrorCode(404, "WARNING_024", "病虫害不存在");
}