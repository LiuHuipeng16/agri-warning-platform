package com.zhku.agriwarningplatform.common.result;

import com.zhku.agriwarningplatform.common.errorcode.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResult<T> {

    private Integer code;

    private String msg;

    private T data;

    public static <T> CommonResult<T> success(T data){
        return new CommonResult<>(200,"",data);
    }

    public static CommonResult<Void> success(){
        return new CommonResult<>(200,"",null);
    }

    public static CommonResult<Void> error(Integer code, String msg){
        return new CommonResult<>(code,msg,null);
    }
    public static CommonResult<Void> error(ErrorCode errorCode){
        return new CommonResult<>(errorCode.getCode(),errorCode.getMessage(),null);
    }
}