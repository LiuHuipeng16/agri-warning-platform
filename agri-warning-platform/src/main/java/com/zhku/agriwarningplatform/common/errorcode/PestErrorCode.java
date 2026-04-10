package com.zhku.agriwarningplatform.common.errorcode;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:50
 */
public interface PestErrorCode {

     /* =========================================================
       Controller ErrorCode
       Controller层：参数校验 / 请求格式问题
       ========================================================= */

    ErrorCode PEST_ID_EMPTY = new ErrorCode(400, "PEST_001", "病虫害ID不能为空");

    ErrorCode PEST_NAME_EMPTY = new ErrorCode(400, "PEST_002", "病虫害名称不能为空");

    ErrorCode PEST_TYPE_INVALID = new ErrorCode(400, "PEST_003", "病虫害类型不合法");

    ErrorCode PEST_RISK_LEVEL_INVALID = new ErrorCode(400, "PEST_004", "风险等级不合法");

    ErrorCode PEST_SEASON_INVALID = new ErrorCode(400, "PEST_005", "高发季节不合法");

    ErrorCode CROP_ID_LIST_INVALID = new ErrorCode(400, "PEST_006", "关联作物ID列表不合法");

    ErrorCode PEST_ENVIRONMENT_PEST_ID_EMPTY =
            new ErrorCode(400, "PEST_007", "环境条件关联的病虫害ID不能为空");


    /* =========================================================
       Service ErrorCode
       Service层：业务逻辑错误
       ========================================================= */

    ErrorCode PEST_NOT_EXIST =
            new ErrorCode(404, "PEST_101", "病虫害不存在");

    ErrorCode PEST_NAME_EXIST =
            new ErrorCode(409, "PEST_102", "病虫害名称已存在");

    ErrorCode CROP_NOT_EXIST =
            new ErrorCode(404, "PEST_103", "存在无效的作物ID");

    ErrorCode PEST_ENVIRONMENT_NOT_EXIST =
            new ErrorCode(404, "PEST_104", "病虫害环境条件不存在");


    /* =========================================================
       System ErrorCode
       系统执行失败
       ========================================================= */

    ErrorCode PEST_CREATE_FAILED =
            new ErrorCode(500, "PEST_201", "病虫害新增失败");

    ErrorCode PEST_UPDATE_FAILED =
            new ErrorCode(500, "PEST_202", "病虫害编辑失败");

    ErrorCode PEST_DELETE_FAILED =
            new ErrorCode(500, "PEST_203", "病虫害删除失败");

    ErrorCode PEST_REL_SAVE_FAILED =
            new ErrorCode(500, "PEST_204", "病虫害关联作物保存失败");

    ErrorCode PEST_ENV_SAVE_FAILED =
            new ErrorCode(500, "PEST_205", "病虫害环境条件保存失败");

}
