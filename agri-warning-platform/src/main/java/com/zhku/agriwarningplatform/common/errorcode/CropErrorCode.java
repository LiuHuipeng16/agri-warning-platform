package com.zhku.agriwarningplatform.common.errorcode;

public interface CropErrorCode {
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
    ErrorCode CROP_ID_EMPTY =
            new ErrorCode(400, "CROP_ID_EMPTY", "作物ID不能为空");
    ErrorCode PAGE_PARAM_ERROR =
            new ErrorCode(400, "PAGE_PARAM_ERROR", "分页参数错误");
    ErrorCode CROP_NAME_EXISTS =
            new ErrorCode(409, "CROP_NAME_EXISTS", "作物名称已存在");
    ErrorCode UPDATE_CROP_FAILED =
            new ErrorCode(500, "UPDATE_CROP_FAILED", "更新作物失败");
    ErrorCode CREATE_CROP_FAILED =
            new ErrorCode(500, "CREATE_CROP_FAILED", "创建作物失败");
    ErrorCode DELETE_CROP_FAILED =
            new ErrorCode(500, "DELETE_CROP_FAILED", "删除作物失败");
}
