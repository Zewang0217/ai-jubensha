package org.jubensha.aijubenshabackend.core.exception.example;

import org.jubensha.aijubenshabackend.core.exception.AppException;
import org.jubensha.aijubenshabackend.core.exception.BusinessException;
import org.jubensha.aijubenshabackend.core.exception.enums.ErrorCodeEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * 异常使用示例
 * 展示如何使用BusinessException和AppException
 * 注意：本示例基于实际的BusinessException.java和AppException.java实现
 */
public class ExceptionUsageExample {

    /**
     * 示例1：使用错误码枚举创建业务异常
     * BusinessException提供了接受ErrorCodeEnum的构造方法
     */
    public void example1() {
        // 方式1：直接使用ErrorCodeEnum创建业务异常
        throw new BusinessException(ErrorCodeEnum.GAME_NOT_FOUND);
    }

    /**
     * 示例2：使用简单消息创建业务异常
     * BusinessException提供了接受String消息的构造方法
     */
    public void example2() {
        // 方式2：使用简单消息创建业务异常
        throw new BusinessException("游戏创建失败");
    }

    /**
     * 示例3：使用Builder模式创建业务异常
     * BusinessException提供了Builder模式，可以设置status、code、message和data
     */
    public void example3() {
        // 方式3：使用Builder模式创建业务异常
        throw BusinessException.builder()
                .status(404)
                .code("20001")
                .message("剧本ID: 789 不存在")
                .datum("scriptId", 789)
                .datum("searchTime", System.currentTimeMillis())
                .build();
    }

    /**
     * 示例4：使用链式调用添加自定义数据
     * BusinessException提供了withData方法用于链式添加数据
     */
    public void example4() {
        // 方式4：创建异常后使用链式调用添加数据
        throw new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE)
                .withData("service", "DeepSeek API")
                .withData("endpoint", "https://api.deepseek.com/chat/completions")
                .withData("retryCount", 3)
                .withData("lastRetryTime", System.currentTimeMillis());
    }

    /**
     * 示例5：使用Builder模式创建带多个自定义数据的异常
     */
    public void example5() {
        // 方式5：使用Builder模式创建带多个自定义数据的异常
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("missingFields", new String[]{"name", "email"});
        errorDetails.put("requestId", "req_123456");

        throw BusinessException.builder()
                .status(ErrorCodeEnum.PARAM_MISSING.getHttpStatus())
                .code(String.valueOf(ErrorCodeEnum.PARAM_MISSING.getCode()))
                .message("缺少必要参数")
                .datum("details", errorDetails)
                .datum("timestamp", System.currentTimeMillis())
                .build();
    }

    /**
     * 示例6：使用AppException创建应用程序异常
     * AppException提供了接受ErrorCodeEnum的构造方法
     */
    public void example6() {
        // 方式6：使用AppException创建应用程序异常
        throw new AppException(ErrorCodeEnum.SYSTEM_ERROR, "数据库连接失败");
    }

    /**
     * 示例7：使用AppException的简单构造方法
     */
    public void example7() {
        // 方式7：使用AppException的简单构造方法
        throw new AppException("系统配置错误");
    }

    /**
     * 示例8：使用AppException的错误码枚举构造方法
     */
    public void example8() {
        // 方式8：使用AppException的错误码枚举构造方法
        throw new AppException(ErrorCodeEnum.DATABASE_ERROR);
    }

    /**
     * 示例9：错误码枚举的使用方法
     */
    public void example9() {
        ErrorCodeEnum errorCode = ErrorCodeEnum.GAME_NOT_FOUND;

        // 检查错误类型
        if (errorCode.isBusinessError()) {
            System.out.println("这是一个业务错误");
        }

        if (errorCode.isSystemError()) {
            System.out.println("这是一个系统错误");
        }

        if (errorCode.isParamError()) {
            System.out.println("这是一个参数错误");
        }

        if (errorCode.isAuthError()) {
            System.out.println("这是一个认证授权错误");
        }

        // 根据错误码获取枚举实例
        ErrorCodeEnum foundErrorCode = ErrorCodeEnum.fromCode(20001);
        System.out.println("找到的错误码: " + foundErrorCode.getMessage());
    }

    /**
     * 示例10：在实际业务方法中使用业务异常
     */
    public void findGameById(Long gameId) {
        if (gameId == null) {
            throw new BusinessException("游戏ID不能为空");
        }

        if (gameId <= 0) {
            throw new BusinessException(ErrorCodeEnum.PARAM_VALIDATION_ERROR)
                    .withData("gameId", gameId)
                    .withData("validationRule", "gameId > 0");
        }

        // 模拟游戏不存在的情况
        boolean gameExists = false; // 实际应从数据库查询

        if (!gameExists) {
            throw BusinessException.builder()
                    .status(ErrorCodeEnum.GAME_NOT_FOUND.getHttpStatus())
                    .code(String.valueOf(ErrorCodeEnum.GAME_NOT_FOUND.getCode()))
                    .message(String.format("游戏ID: %d 不存在", gameId))
                    .datum("gameId", gameId)
                    .datum("searchTime", System.currentTimeMillis())
                    .build();
        }

        // 游戏存在，继续处理...
    }

    /**
     * 示例11：处理业务异常
     */
    public void handleException() {
        try {
            findGameById(999L);
        } catch (BusinessException ex) {
            // 获取异常信息
            System.out.println("错误码: " + ex.getCode());
            System.out.println("错误消息: " + ex.getMessage());
            System.out.println("HTTP状态码: " + ex.getStatus());

            // 检查是否有自定义数据
            if (ex.hasData()) {
                System.out.println("自定义数据: " + ex.getDataCopy());
            }

            // 获取错误码整数形式
            try {
                int codeInt = Integer.parseInt(ex.getCode());
                System.out.println("错误码(整数): " + codeInt);
            } catch (NumberFormatException e) {
                System.out.println("错误码不是数字: " + ex.getCode());
            }
        }
    }

    /**
     * 示例12：完整的异常处理流程
     */
    public void completeExample() {
        try {
            // 业务逻辑
            validateUserInput("", "weakpassword");

        } catch (BusinessException ex) {
            // 业务异常处理
            System.err.println("业务异常: " + ex.getMessage());
            System.err.println("错误码: " + ex.getCode());
            System.err.println("HTTP状态码: " + ex.getStatus());
            if (ex.hasData()) {
                System.err.println("自定义数据: " + ex.getDataCopy());
            }
            // 可以记录日志、发送通知等

        } catch (AppException ex) {
            // 应用程序异常处理
            System.err.println("应用程序异常: " + ex.getMessage());
            System.err.println("错误码: " + ex.getCode());
            System.err.println("HTTP状态码: " + ex.getStatus());

        } catch (Exception ex) {
            // 其他异常处理
            System.err.println("系统异常: " + ex.getMessage());
            // 转换为业务异常重新抛出
            throw new BusinessException(ErrorCodeEnum.SYSTEM_ERROR)
                    .withData("originalError", ex.getMessage())
                    .withData("exceptionType", ex.getClass().getName());
        }
    }

    private void validateUserInput(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }

        if (password == null || password.length() < 6) {
            Map<String, Object> validationData = new HashMap<>();
            validationData.put("minLength", 6);
            validationData.put("actualLength", password != null ? password.length() : 0);
            validationData.put("validationRule", "PASSWORD_MIN_LENGTH");

            throw BusinessException.builder()
                    .status(ErrorCodeEnum.PARAM_VALIDATION_ERROR.getHttpStatus())
                    .code(String.valueOf(ErrorCodeEnum.PARAM_VALIDATION_ERROR.getCode()))
                    .message("密码长度至少6位")
                    .datum("validationDetails", validationData)
                    .build();
        }

        // 验证通过，继续处理...
    }

    /**
     * 示例13：使用Builder模式创建复杂异常
     */
    public void example13() {
        // 使用Builder模式创建复杂的异常实例
        throw BusinessException.builder()
                .status(400)
                .code("30001")
                .message("请求参数验证失败")
                .datum("fieldErrors", new HashMap<String, String>() {{
                    put("username", "用户名不能为空");
                    put("email", "邮箱格式不正确");
                    put("password", "密码强度不足");
                }})
                .datum("requestId", "req_" + System.currentTimeMillis())
                .datum("timestamp", System.currentTimeMillis())
                .datum("validationRules", new String[]{
                        "username: required, min:3, max:50",
                        "email: required, email format",
                        "password: required, min:6, contains: uppercase, lowercase, digit"
                })
                .build();
    }

    /**
     * 示例14：比较BusinessException和AppException的使用场景
     */
    public void example14() {
        // BusinessException 通常用于业务逻辑错误
        boolean userExists = false;
        if (!userExists) {
            throw new BusinessException(ErrorCodeEnum.PLAYER_NOT_FOUND);
        }

        // AppException 通常用于应用程序级别的错误
        boolean configValid = false;
        if (!configValid) {
            throw new AppException(ErrorCodeEnum.SYSTEM_ERROR, "配置文件无效");
        }
    }
}