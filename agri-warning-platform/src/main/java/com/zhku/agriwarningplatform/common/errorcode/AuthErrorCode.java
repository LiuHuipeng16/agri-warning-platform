package com.zhku.agriwarningplatform.common.errorcode;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 1:41
 */
public interface AuthErrorCode {

        // ================= AUTH模块 AUTH_001 =================

        ErrorCode USERNAME_EMPTY =
                new ErrorCode(400, "AUTH_001", "用户名不能为空");

        ErrorCode USER_NOT_EXIST =
                new ErrorCode(404, "AUTH_002", "用户不存在");

        ErrorCode PASSWORD_ERROR =
                new ErrorCode(400, "AUTH_003", "密码错误");

        ErrorCode USERNAME_EXISTS =
                new ErrorCode(409, "AUTH_004", "用户名已存在");

        ErrorCode TOKEN_INVALID =
                new ErrorCode(401, "AUTH_005", "登录状态已失效");
        //用户或密码错误
        ErrorCode USERNAME_OR_PASSWORD_ERROR =
                new ErrorCode(400, "AUTH_006", "用户或密码错误");

        ErrorCode PASSWORD_EMPTY =
                new ErrorCode(400, "AUTH_007", "密码不能为空");

        ErrorCode NEW_PASSWORD_EMPTY =
                new ErrorCode(400, "AUTH_008", "新密码不能为空");

        ErrorCode OLD_PASSWORD_EMPTY =
                new ErrorCode(400, "AUTH_009", "旧密码不能为空");

        ErrorCode UPDATE_PASSWORD_FAILED =
                new ErrorCode(400, "AUTH_010", "修改密码操作失败");

        ErrorCode CONFIRM_PASSWORD_EMPTY =
                new ErrorCode(400, "AUTH_011", "确认密码不能为空");

        ErrorCode PASSWORD_NOT_MATCH =
            new ErrorCode(400, "AUTH_012", "密码不匹配");

        ErrorCode ROLE_NOT_EXIST =
                new ErrorCode(400, "AUTH_013", "角色参数不合法");
        // ================= CROP模块 CROP_001 =================

        ErrorCode CROP_NOT_EXIST =
                new ErrorCode(404, "CROP_001", "作物不存在");

        ErrorCode CROP_NAME_EMPTY =
                new ErrorCode(400, "CROP_002", "作物名称不能为空");

        ErrorCode CROP_CATEGORY_EMPTY =
                new ErrorCode(400, "CROP_003", "作物分类不能为空");

        ErrorCode CROP_NAME_DUPLICATE =
                new ErrorCode(409, "CROP_004", "作物名称已存在");

        ErrorCode ROle_EMPTY =
                new ErrorCode(400, "ROle_EMPTY", "角色不能为空");


        // ================= PEST模块 PEST_001 =================

        ErrorCode PEST_NOT_EXIST =
                new ErrorCode(404, "PEST_001", "病虫害不存在");

        ErrorCode PEST_NAME_EMPTY =
                new ErrorCode(400, "PEST_002", "病虫害名称不能为空");

        ErrorCode PEST_TYPE_ERROR =
                new ErrorCode(400, "PEST_003", "病虫害类型错误");

        ErrorCode PEST_RISK_LEVEL_ERROR =
                new ErrorCode(400, "PEST_004", "风险等级错误");


        // ================= WARNING模块 WARNING_001 =================

        ErrorCode WARNING_NOT_EXIST =
                new ErrorCode(404, "WARNING_001", "预警不存在");

        ErrorCode WARNING_TITLE_EMPTY =
                new ErrorCode(400, "WARNING_002", "预警标题不能为空");

        ErrorCode WARNING_CONTENT_EMPTY =
                new ErrorCode(400, "WARNING_003", "预警内容不能为空");


        // ================= UPLOAD模块 UPLOAD_001 =================

        ErrorCode FILE_EMPTY =
                new ErrorCode(400, "UPLOAD_001", "文件不能为空");

        ErrorCode FILE_TYPE_NOT_SUPPORTED =
                new ErrorCode(400, "UPLOAD_002", "文件类型不支持");

        ErrorCode FILE_SIZE_EXCEEDED =
                new ErrorCode(400, "UPLOAD_003", "文件大小超过限制");

        ErrorCode FILE_UPLOAD_ERROR =
                new ErrorCode(502, "UPLOAD_004", "文件上传失败");


        // ================= AI模块 AI_001 =================

        ErrorCode AI_QUESTION_EMPTY =
                new ErrorCode(400, "AI_001", "提问内容不能为空");

        ErrorCode AI_RESPONSE_EMPTY =
                new ErrorCode(502, "AI_002", "AI返回结果为空");

        ErrorCode AI_SERVICE_ERROR =
                new ErrorCode(502, "AI_003", "AI服务暂不可用");


        // ================= STATS模块 STATS_001 =================

        ErrorCode STATS_QUERY_ERROR =
                new ErrorCode(500, "STATS_001", "统计数据查询失败");


}

