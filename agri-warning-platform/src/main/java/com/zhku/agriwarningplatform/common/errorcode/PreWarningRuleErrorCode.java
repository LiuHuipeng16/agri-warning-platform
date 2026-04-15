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

    ErrorCode PAGE_NUM_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_001", "页码不能为空");
    ErrorCode PAGE_NUM_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_002", "页码不合法");
    ErrorCode PAGE_SIZE_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_003", "每页条数不能为空");
    ErrorCode PAGE_SIZE_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_004", "每页条数不合法");

    ErrorCode RULE_ID_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_005", "规则ID不能为空");
    ErrorCode RULE_ID_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_006", "规则ID不合法");

    ErrorCode RULE_NAME_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_007", "规则名称不能为空");
    ErrorCode RULE_NAME_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_008", "规则名称不合法");

    ErrorCode CROP_ID_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_009", "作物ID不能为空");
    ErrorCode CROP_ID_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_010", "作物ID不合法");

    ErrorCode PEST_ID_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_011", "病虫害ID不能为空");
    ErrorCode PEST_ID_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_012", "病虫害ID不合法");

    ErrorCode RISK_LEVEL_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_013", "风险等级不能为空");
    ErrorCode RISK_LEVEL_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_014", "风险等级不合法");

    ErrorCode RULE_STATUS_EMPTY = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_015", "规则状态不能为空");
    ErrorCode RULE_STATUS_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_016", "规则状态不合法");

    ErrorCode PARAM_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_CONTROLLER_017", "请求参数不合法");

    // =========================
    // Service 层错误码
    // =========================

    ErrorCode RULE_NOT_EXIST = new ErrorCode(404, "PRE_WARNING_RULE_SERVICE_001", "预警规则不存在");

    ErrorCode CROP_NOT_EXIST = new ErrorCode(404, "PRE_WARNING_RULE_SERVICE_002", "关联作物不存在");
    ErrorCode PEST_NOT_EXIST = new ErrorCode(404, "PRE_WARNING_RULE_SERVICE_003", "关联病虫害不存在");

    ErrorCode RISK_LEVEL_VALUE_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_SERVICE_004", "风险等级不合法");
    ErrorCode RULE_STATUS_VALUE_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_SERVICE_005", "规则状态不合法");

    ErrorCode TEMP_RANGE_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_SERVICE_006", "温度区间不合法");
    ErrorCode HUMIDITY_RANGE_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_SERVICE_007", "湿度区间不合法");
    ErrorCode PRECIPITATION_RANGE_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_SERVICE_008", "降雨量区间不合法");
    ErrorCode WIND_SPEED_RANGE_INVALID = new ErrorCode(400, "PRE_WARNING_RULE_SERVICE_009", "风速区间不合法");

    ErrorCode CREATE_FAILED = new ErrorCode(500, "PRE_WARNING_RULE_SERVICE_010", "新增预警规则失败");
    ErrorCode UPDATE_FAILED = new ErrorCode(500, "PRE_WARNING_RULE_SERVICE_011", "修改预警规则失败");
    ErrorCode DELETE_FAILED = new ErrorCode(500, "PRE_WARNING_RULE_SERVICE_012", "删除预警规则失败");
    ErrorCode CHANGE_STATUS_FAILED = new ErrorCode(500, "PRE_WARNING_RULE_SERVICE_013", "修改预警规则状态失败");

    ErrorCode DATA_CONFLICT = new ErrorCode(409, "PRE_WARNING_RULE_SERVICE_014", "预警规则数据冲突");
}
