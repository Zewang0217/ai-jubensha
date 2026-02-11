package org.jubensha.aijubenshabackend.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.core.exception.enums.ErrorCodeEnum;
import org.jubensha.aijubenshabackend.core.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常处理 ====================

    /**
     * 处理业务异常
     *
     * @param ex      业务异常
     * @param request HTTP请求
     * @return 响应实体
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        log.warn("业务异常: code={}, message={}, status={}",
                ex.getCode(), ex.getMessage(), ex.getStatus(), ex);

        ApiResponse<Object> response = ApiResponse.error(
                ex.getCode(),
                ex.getMessage()
        );
        response.setPath(request.getRequestURI());

        if (ex.hasData()) {
            response.setData(ex.getDataCopy());
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getStatus()));
    }

    /**
     * 处理应用程序异常
     *
     * @param ex      应用程序异常
     * @param request HTTP请求
     * @return 响应实体
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(
            AppException ex, HttpServletRequest request) {
        log.warn("应用程序异常: code={}, message={}, status={}",
                ex.getCode(), ex.getMessage(), ex.getStatus(), ex);

        ApiResponse<Object> response = ApiResponse.error(
                ex.getCode(),
                ex.getMessage()
        );
        response.setPath(request.getRequestURI());

        return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getStatus()));
    }

    // ==================== 参数校验异常处理 ====================

    /**
     * 处理参数校验异常（@Valid注解）
     *
     * @param ex      参数校验异常
     * @param request HTTP请求
     * @return 响应实体
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("参数校验异常: {}", ex.getMessage());

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ?
                                fieldError.getDefaultMessage() : "参数校验失败"
                ));

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                ErrorCodeEnum.PARAM_VALIDATION_ERROR
        );
        response.setPath(request.getRequestURI());
        response.setData(errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理约束违反异常（@Validated注解）
     *
     * @param ex      约束违反异常
     * @param request HTTP请求
     * @return 响应实体
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("约束违反异常: {}", ex.getMessage());

        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                ErrorCodeEnum.PARAM_VALIDATION_ERROR
        );
        response.setPath(request.getRequestURI());
        response.setData(errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理缺少请求参数异常
     *
     * @param ex      缺少请求参数异常
     * @param request HTTP请求
     * @return 响应实体
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("缺少请求参数: {}", ex.getMessage());

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("parameterName", ex.getParameterName());
        errorDetail.put("parameterType", ex.getParameterType());

        ApiResponse<Object> response = ApiResponse.error(
                ErrorCodeEnum.PARAM_MISSING,
                String.format("缺少必要参数: %s", ex.getParameterName())
        );
        response.setPath(request.getRequestURI());
        response.setData(errorDetail);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理参数类型不匹配异常
     *
     * @param ex      参数类型不匹配异常
     * @param request HTTP请求
     * @return 响应实体
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("参数类型不匹配: {}", ex.getMessage());

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("parameterName", ex.getName());
        errorDetail.put("requiredType", ex.getRequiredType() != null ?
                ex.getRequiredType().getSimpleName() : "未知");
        errorDetail.put("actualValue", ex.getValue());

        ApiResponse<Object> response = ApiResponse.error(
                ErrorCodeEnum.PARAM_TYPE_ERROR,
                String.format("参数类型错误: %s", ex.getName())
        );
        response.setPath(request.getRequestURI());
        response.setData(errorDetail);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ==================== 资源未找到异常处理 ====================

    /**
     * 处理资源未找到异常
     *
     * @param ex      资源未找到异常
     * @param request HTTP请求
     * @return 响应实体
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("资源未找到: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("method", ex.getHttpMethod());
        errorDetail.put("url", ex.getRequestURL());

        ApiResponse<Object> response = ApiResponse.error(
                ErrorCodeEnum.fromCode(404),
                String.format("请求的资源不存在: %s %s", ex.getHttpMethod(), ex.getRequestURL())
        );
        response.setPath(request.getRequestURI());
        response.setData(errorDetail);

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // ==================== 运行时异常处理 ====================

    /**
     * 处理运行时异常
     *
     * @param ex      运行时异常
     * @param request HTTP请求
     * @return 响应实体
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        log.error("运行时异常: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.error(
                ErrorCodeEnum.SYSTEM_ERROR,
                "系统内部错误，请稍后重试"
        );
        response.setPath(request.getRequestURI());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ==================== 通用异常处理 ====================

    /**
     * 处理SSE端点异常
     *
     * @param ex      异常
     * @param request HTTP请求
     * @return 响应实体
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, HttpServletRequest request) {
        log.error("未处理的异常: {}", ex.getMessage(), ex);

        // 检查是否是SSE端点请求
        if (request.getRequestURI().contains("/generate/new")) {
            // 对于SSE端点，返回text/event-stream格式的响应
            String errorMessage = "系统内部错误，请稍后重试";
            StringBuilder sseResponse = new StringBuilder();
            sseResponse.append("event: error\n");
            sseResponse.append("data: ").append(errorMessage).append("\n\n");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(sseResponse.toString());
        } else {
            // 对于普通端点，返回ApiResponse格式的响应
            ApiResponse<Object> response = ApiResponse.error(
                    ErrorCodeEnum.SYSTEM_ERROR,
                    "系统内部错误，请稍后重试"
            );
            response.setPath(request.getRequestURI());

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
