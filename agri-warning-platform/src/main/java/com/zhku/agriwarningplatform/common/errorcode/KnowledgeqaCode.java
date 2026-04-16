package com.zhku.agriwarningplatform.common.errorcode;

public interface KnowledgeqaCode {
    ErrorCode PAGE_PARAM_ERROR =
            new ErrorCode(400, "PAGE_PARAM_ERROR", "分页参数错误");
    ErrorCode QUESTION_NOT_NULL =
            new ErrorCode(400, "QUESTION_NOT_NULL", "问题不能为空");
    ErrorCode ANSWER_NOT_NULL =
            new ErrorCode(400, "ANSWER_NOT_NULL", "答案不能为空");
    ErrorCode CREATE_KNOWLEDGEQA_FAILED =
            new ErrorCode(500, "CREATE_KNOWLEDGEQA_FAILED", "创建知识问答失败");
    ErrorCode ID_NOT_NULL =
            new ErrorCode(400, "ID_NOT_NULL", "id不能为空");
    ErrorCode UPDATE_KNOWLEDGEQA_FAILED =
            new ErrorCode(500, "UPDATE_KNOWLEDGEQA_FAILED", "更新知识问答失败");
    ErrorCode DELETE_KNOWLEDGEQA_FAILED =
            new ErrorCode(500, "DELETE_KNOWLEDGEQA_FAILED", "删除知识问答失败");
    ErrorCode PAGE_NOT_FOUND =
            new ErrorCode(404, "PAGE_NOT_FOUND", "页面不存在");
}
