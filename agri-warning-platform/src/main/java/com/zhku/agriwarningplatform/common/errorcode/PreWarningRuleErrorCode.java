package com.zhku.agriwarningplatform.common.errorcode;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 15:05
 */
public interface PreWarningRuleErrorCode {
    // =========================
// Controller 层错误码
// =========================

    ErrorCode PAGE_NUM_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_001", "页码不能为空");
    ErrorCode PAGE_NUM_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_002", "页码不合法");
    ErrorCode PAGE_SIZE_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_003", "每页条数不能为空");
    ErrorCode PAGE_SIZE_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_004", "每页条数不合法");

    ErrorCode RULE_ID_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_005", "规则ID不能为空");
    ErrorCode RULE_ID_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_006", "规则ID不合法");

    ErrorCode RULE_NAME_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_007", "规则名称不能为空");
    ErrorCode RULE_NAME_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_008", "规则名称不合法");

    ErrorCode CROP_ID_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_009", "作物ID不能为空");
    ErrorCode CROP_ID_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_010", "作物ID不合法");

    ErrorCode PEST_ID_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_011", "病虫害ID不能为空");
    ErrorCode PEST_ID_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_012", "病虫害ID不合法");

    ErrorCode RISK_LEVEL_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_013", "风险等级不能为空");
    ErrorCode RISK_LEVEL_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_014", "风险等级不合法");

    ErrorCode RULE_STATUS_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_015", "规则状态不能为空");
    ErrorCode RULE_STATUS_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_016", "规则状态不合法");

    ErrorCode PARAM_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_017", "请求参数不合法");

// =========================
// Service 层错误码
// =========================

    ErrorCode RULE_NOT_EXIST = new ErrorCode(404, "PRE_WARNING_RULE_101", "预警规则不存在");

    ErrorCode CROP_NOT_EXIST = new ErrorCode(404, "PRE_WARNING_RULE_102", "关联作物不存在");
    ErrorCode PEST_NOT_EXIST = new ErrorCode(404, "PRE_WARNING_RULE_103", "关联病虫害不存在");

    ErrorCode RISK_LEVEL_VALUE_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_104", "风险等级不合法");
    ErrorCode RULE_STATUS_VALUE_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_105", "规则状态不合法");

    ErrorCode TEMP_RANGE_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_106", "温度区间不合法");
    ErrorCode HUMIDITY_RANGE_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_107", "湿度区间不合法");
    ErrorCode PRECIPITATION_RANGE_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_108", "降雨量区间不合法");
    ErrorCode WIND_SPEED_RANGE_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_109", "风速区间不合法");

    ErrorCode CREATE_FAILED = new ErrorCode(500, "PRE_WARNING_RULE_110", "新增预警规则失败");
    ErrorCode UPDATE_FAILED = new ErrorCode(500, "PRE_WARNING_RULE_111", "修改预警规则失败");
    ErrorCode DELETE_FAILED = new ErrorCode(500, "PRE_WARNING_RULE_112", "删除预警规则失败");
    ErrorCode CHANGE_STATUS_FAILED = new ErrorCode(500, "PRE_WARNING_RULE_113", "修改预警规则状态失败");

    ErrorCode DATA_CONFLICT = new ErrorCode(409, "PRE_WARNING_RULE_114", "预警规则数据冲突");
}
