package com.backend.intelligent_scheduling_login.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Pilot
 * @param <T> 数据类型
 */
@Data
public class BaseResponse<T> implements Serializable {

    /**
     * code
     * 0 ok
     */
    private int code;
    private T data;
    private String message;
    private String description;

    public BaseResponse(){

    }

    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(int code, T data){
        this(code, data,"" , "");
    }

    public BaseResponse(int code, T data, String message){
        this(code, data, message ,"");
    }

    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }
}
