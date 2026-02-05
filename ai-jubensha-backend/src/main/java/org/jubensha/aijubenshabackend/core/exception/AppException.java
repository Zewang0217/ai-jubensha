package org.jubensha.aijubenshabackend.core.exception;

import lombok.Getter;
import org.jubensha.aijubenshabackend.core.exception.enums.ErrorCodeEnum;

/**
 * 应用程序异常类
 * 用于处理应用程序级别的异常情况
 */
@Getter
public class AppException extends RuntimeException {

    /**
     * HTTP状态码
     */
    private final int status;

    /**
     * 错误码
     */
    private final String code;

    /**
     * 最简，只使用错误消息创建应用程序异常
     *
     * @param message 错误消息
     */
    public AppException(String message) {
        super(message);
        this.status = 500;
        this.code = "ERROR";
    }

    /**
     * 使用错误码枚举创建应用程序异常
     *
     * @param errorCode 错误码枚举
     */
    public AppException(ErrorCodeEnum errorCode) {
        super(errorCode.getMessage());
        this.status = errorCode.getHttpStatus();
        this.code = String.valueOf(errorCode.getCode());
    }

    /**
     * 使用错误码枚举和自定义消息创建应用程序异常
     *
     * @param errorCode     错误码枚举
     * @param customMessage 自定义消息
     */
    public AppException(ErrorCodeEnum errorCode, String customMessage) {
        super(customMessage);
        this.status = errorCode.getHttpStatus();
        this.code = String.valueOf(errorCode.getCode());
    }
}
