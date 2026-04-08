package com.zhku.agriwarningplatform.common.errorcode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 1:37
 */
@Data
@NoArgsConstructor
@Getter
public class ErrorCode {

    /**
     * 对前端返回的code
     */
    private Integer code;

    /**
     * 内部错误码
     */
    private String internalCode;

    /**
     * 错误信息
     */
    private String message;

    public ErrorCode(Integer code, String internalCode, String message) {
        this.code = code;
        this.internalCode = internalCode;
        this.message = message;
    }

}
