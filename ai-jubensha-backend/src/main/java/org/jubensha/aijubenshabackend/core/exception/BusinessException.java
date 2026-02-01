package org.jubensha.aijubenshabackend.core.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.jubensha.aijubenshabackend.core.exception.enums.ErrorCodeEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * 业务异常类
 * 用于处理业务逻辑中的异常情况
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * HTTP状态码
     */
    private final int status;

    /**
     * 错误码
     */
    private final String code;

    /**
     * 自定义数据
     */
    private final Map<String, Object> data;

    /**
     * 最简，只使用错误消息创建业务异常
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.status = 500;
        this.code = "ERROR";
        this.data = new HashMap<>();
    }

    /**
     * 使用错误码枚举创建业务异常
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCodeEnum errorCode) {
        super(errorCode.getMessage());
        this.status = errorCode.getHttpStatus();
        this.code = String.valueOf(errorCode.getCode());
        this.data = new HashMap<>();
    }

    /**
     * 全参构造 Builder
     *
     * @param status  HTTP状态码
     * @param code    错误码
     * @param message 错误消息
     * @param data    自定义数据
     */
    @Builder
    public BusinessException(int status, String code, String message,
                             @Singular("datum") Map<String, Object> data) {
        super(message);
        this.status = status;
        this.code = code;
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
    }

    /**
     * 链式添加数据
     *
     * @param key   数据的键
     * @param value 数据的值
     */
    public BusinessException withData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    /**
     * 检查是否有自定义数据
     *
     * @return 如果有自定义数据返回true，否则返回false
     */
    public boolean hasData() {
        return !data.isEmpty();
    }

    /**
     * 获取自定义数据的副本
     *
     * @return 自定义数据的副本
     */
    public Map<String, Object> getDataCopy() {
        return new HashMap<>(data);
    }
}
