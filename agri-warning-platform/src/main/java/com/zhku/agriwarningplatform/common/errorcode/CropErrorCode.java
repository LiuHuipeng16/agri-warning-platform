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
    ErrorCode FILE_NOT_EMPTY =
            new ErrorCode(400, "FILE_NOT_EMPTY", "文件不能为空");
    ErrorCode FILE_TYPE_NOT_SUPPORT =
            new ErrorCode(400, "FILE_TYPE_NOT_SUPPORT", "文件类型不支持，仅支持 jpg/png/jpeg/gif");
    ErrorCode FILE_SIZE_TOO_LARGE =
            new ErrorCode(400, "FILE_SIZE_TOO_LARGE", "文件大小超出限制");
    ErrorCode FILE_NAME_NOT_EMPTY =
            new ErrorCode(400, "FILE_NAME_NOT_EMPTY", "文件名称不能为空");
    ErrorCode FILE_UPLOAD_FAILED =
            new ErrorCode(500, "FILE_UPLOAD_FAILED", "文件上传失败");
    ErrorCode CROP_ID_INVALID =
            new ErrorCode(400, "CROP_ID_INVALID", "作物ID无效");
    ErrorCode CROP_OPTIONS_EMPTY =
            new ErrorCode(404, "CROP_OPTIONS_EMPTY", "作物选项为空");
}
