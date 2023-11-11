package com.weikey.liuguangapisdk.exception;

/**
 * 自定义异常类
 *
 * @author wei-key
 */
public class ApiException extends RuntimeException {

    /**
     * 错误码
     */
    private final String code;

    public ApiException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ApiException(ApiError apiError) {
        super(apiError.getMessage());
        this.code = apiError.getCode();
    }

    public ApiException(ApiError apiError, String message) {
        super(message);
        this.code = apiError.getCode();
    }

    public String getCode() {
        return code;
    }
}
