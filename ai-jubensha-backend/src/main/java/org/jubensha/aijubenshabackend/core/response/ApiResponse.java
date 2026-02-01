package org.jubensha.aijubenshabackend.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jubensha.aijubenshabackend.core.exception.enums.ErrorCodeEnum;

import java.time.LocalDateTime;

/**
 * 统一API响应对象
 *
 * @param <T> 数据类型
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误码
     */
    private String code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 路径
     */
    private String path;

    // ==================== 成功响应方法 ====================

    /**
     * 创建成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 创建成功响应（带数据）
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("0")
                .message("操作成功")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建成功响应（带自定义消息）
     *
     * @param message 自定义消息
     * @param data    数据
     * @param <T>     数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("0")
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== 失败响应方法 ====================

    /**
     * 创建失败响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败响应（带数据）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param data    数据
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(String code, String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败响应（使用错误码枚举）
     *
     * @param errorCode 错误码枚举
     * @param <T>       数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(ErrorCodeEnum errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(String.valueOf(errorCode.getCode()))
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败响应（使用错误码枚举和自定义消息）
     *
     * @param errorCode     错误码枚举
     * @param customMessage 自定义消息
     * @param <T>           数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(ErrorCodeEnum errorCode, String customMessage) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(String.valueOf(errorCode.getCode()))
                .message(customMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败响应（使用错误码枚举和自定义数据）
     *
     * @param errorCode 错误码枚举
     * @param data      自定义数据
     * @param <T>       数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(ErrorCodeEnum errorCode, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(String.valueOf(errorCode.getCode()))
                .message(errorCode.getMessage())
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败响应（使用HTTP状态码）
     *
     * @param status  HTTP状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(String.valueOf(status))
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败响应（使用HTTP状态码、错误码和错误消息）
     *
     * @param status  HTTP状态码
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(int status, String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== 工具方法 ====================

    /**
     * 设置路径
     *
     * @param path 路径
     * @return 当前响应实例
     */
    public ApiResponse<T> withPath(String path) {
        setPath(path);
        return this;
    }

    /**
     * 设置数据
     *
     * @param data 数据
     * @return 当前响应实例
     */
    public ApiResponse<T> withData(T data) {
        setData(data);
        return this;
    }

    /**
     * 使用Builder设置路径（替代方法）
     *
     * @param path 路径
     * @return 新的响应实例
     */
    public ApiResponse<T> withPathUsingBuilder(String path) {
        return this.toBuilder()
                .path(path)
                .build();
    }
}