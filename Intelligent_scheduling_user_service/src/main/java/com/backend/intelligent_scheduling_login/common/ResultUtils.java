package com.backend.intelligent_scheduling_login.common;

/**
 * @author Pilot
 * 返回工具类
 */
public class ResultUtils {
    /**
     * 成功
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 类
     */
    public static <T> BaseResponse<T> success(T data) {
        //return new BaseResponse<>(0, data, "ok");
        return new BaseResponse<>(20000, data, "ok");
    }

    /**
     * 失败
     *
     * @param errorCode 错误信息
     * @return 类
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    public static BaseResponse error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse<>(errorCode.getCode(), null, message, description);
    }

    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse<>(code, null, message, description);
    }

    public static BaseResponse error(ErrorCode errorCode, String description) {
        return new BaseResponse<>(errorCode.getCode(), null, errorCode.getMessage(), description);
    }

}
