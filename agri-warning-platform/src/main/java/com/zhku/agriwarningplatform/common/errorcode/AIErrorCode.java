package com.zhku.agriwarningplatform.common.errorcode;
import com.zhku.agriwarningplatform.common.errorcode.ErrorCode;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 23:01
 */
/**
 * AI模块错误码
 */
public interface AIErrorCode {

    // ==================== controller层 ====================

    ErrorCode CHAT_ID_EMPTY = new ErrorCode(400, "AI_001", "会话ID不能为空");
    ErrorCode PROMPT_EMPTY = new ErrorCode(400, "AI_002", "提问内容不能为空");
    ErrorCode CONTEXT_TYPE_INVALID = new ErrorCode(400, "AI_003", "上下文类型不合法");
    ErrorCode CONTEXT_ID_INVALID = new ErrorCode(400, "AI_004", "上下文ID不合法");
    ErrorCode TITLE_EMPTY = new ErrorCode(400, "AI_005", "会话标题不能为空");
    ErrorCode WARNING_ID_INVALID = new ErrorCode(400, "AI_006", "预警ID不合法");
    ErrorCode AUTH_HEADER_INVALID = new ErrorCode(401, "AI_007", "登录信息无效");

    // ==================== service层 ====================

    ErrorCode CHAT_SESSION_NOT_EXIST = new ErrorCode(404, "AI_101", "AI会话不存在");
    ErrorCode CHAT_MESSAGE_QUERY_FAILED = new ErrorCode(500, "AI_102", "聊天记录查询失败");
    ErrorCode CHAT_MESSAGE_SAVE_FAILED = new ErrorCode(500, "AI_103", "聊天记录保存失败");
    ErrorCode CHAT_SESSION_CREATE_FAILED = new ErrorCode(500, "AI_104", "AI会话创建失败");
    ErrorCode CHAT_SESSION_UPDATE_FAILED = new ErrorCode(500, "AI_105", "AI会话修改失败");
    ErrorCode CHAT_SESSION_DELETE_FAILED = new ErrorCode(500, "AI_106", "AI会话删除失败");
    ErrorCode AI_GENERATE_FAILED = new ErrorCode(500, "AI_107", "AI生成回答失败");
    ErrorCode WARNING_NOT_EXIST = new ErrorCode(404, "AI_108", "预警不存在");
    ErrorCode CROP_NOT_EXIST = new ErrorCode(404, "AI_109", "作物不存在");
    ErrorCode PEST_NOT_EXIST = new ErrorCode(404, "AI_110", "病虫害不存在");
    ErrorCode RULE_NOT_EXIST = new ErrorCode(404, "AI_111", "预警规则不存在");
    ErrorCode KNOWLEDGE_LOAD_FAILED = new ErrorCode(500, "AI_112", "知识库加载失败");
    ErrorCode CHAT_SESSION_ALREADY_EXIST = new ErrorCode(409, "AI_113", "AI会话已存在");
    ErrorCode CHAT_STREAM_STOP_FAILED = new ErrorCode(500, "AI_114", "停止AI输出失败");
    ErrorCode CHAT_SESSION_ACCESS_DENIED = new ErrorCode(403, "AI_115", "无权访问该AI会话");
    ErrorCode TOKEN_PARSE_FAILED = new ErrorCode(401, "AI_116", "Token解析失败");
    ErrorCode CONTEXT_BUILD_FAILED = new ErrorCode(500, "AI_117", "上下文信息构建失败");
}
