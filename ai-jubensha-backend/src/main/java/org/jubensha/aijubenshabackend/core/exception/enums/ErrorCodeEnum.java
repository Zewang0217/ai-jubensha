package org.jubensha.aijubenshabackend.core.exception.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举类
 * 定义系统所有错误码，包含错误码、错误消息和HTTP状态码
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {

    // ==================== 系统错误 (10000-19999) ====================
    SYSTEM_ERROR(10000, "系统内部错误", 500),
    DATABASE_ERROR(10001, "数据库操作失败", 500),
    REDIS_ERROR(10002, "缓存服务异常", 500),
    RABBITMQ_ERROR(10003, "消息队列异常", 500),
    EXTERNAL_SERVICE_ERROR(10004, "外部服务调用失败", 500),
    FILE_OPERATION_ERROR(10005, "文件操作失败", 500),
    NETWORK_ERROR(10006, "网络连接异常", 500),

    // ==================== 业务错误 (20000-29999) ====================
    // 游戏相关错误 (20001-20099)
    GAME_NOT_FOUND(20001, "游戏不存在", 404),
    GAME_ALREADY_STARTED(20002, "游戏已开始", 400),
    GAME_NOT_STARTED(20003, "游戏未开始", 400),
    GAME_ALREADY_ENDED(20004, "游戏已结束", 400),
    GAME_PLAYER_LIMIT(20005, "游戏玩家数量已达上限", 400),
    GAME_INVALID_STATE(20006, "游戏状态无效", 400),

    // 玩家相关错误 (20100-20199)
    PLAYER_NOT_FOUND(20101, "玩家不存在", 404),
    PLAYER_ALREADY_IN_GAME(20102, "玩家已在游戏中", 400),
    PLAYER_NOT_IN_GAME(20103, "玩家不在游戏中", 400),
    PLAYER_ROLE_CONFLICT(20104, "玩家角色冲突", 400),
    PLAYER_STATUS_INVALID(20105, "玩家状态无效", 400),

    // 剧本相关错误 (20200-20299)
    SCRIPT_NOT_FOUND(20201, "剧本不存在", 404),
    SCRIPT_GENERATION_FAILED(20202, "剧本生成失败", 500),
    SCRIPT_INVALID_FORMAT(20203, "剧本格式无效", 400),
    SCRIPT_TOO_LONG(20204, "剧本过长", 400),
    SCRIPT_TOO_SHORT(20205, "剧本过短", 400),

    // 角色相关错误 (20300-20399)
    CHARACTER_NOT_FOUND(20301, "角色不存在", 404),
    CHARACTER_ALREADY_ASSIGNED(20302, "角色已被分配", 400),
    CHARACTER_INVALID_ATTRIBUTES(20303, "角色属性无效", 400),

    // 场景相关错误 (20400-20499)
    SCENE_NOT_FOUND(20401, "场景不存在", 404),
    SCENE_INVALID_SEQUENCE(20402, "场景顺序无效", 400),

    // 线索相关错误 (20500-20599)
    CLUE_NOT_FOUND(20501, "线索不存在", 404),
    CLUE_ALREADY_DISCOVERED(20502, "线索已被发现", 400),
    CLUE_VISIBILITY_RESTRICTED(20503, "线索可见性受限", 403),

    // AI相关错误 (20600-20699)
    AI_SERVICE_UNAVAILABLE(20601, "AI服务不可用", 503),
    AI_RESPONSE_TIMEOUT(20602, "AI响应超时", 504),
    AI_CONTENT_FILTERED(20603, "AI内容被过滤", 400),
    AI_CONTEXT_TOO_LONG(20604, "AI上下文过长", 400),

    // ==================== 参数错误 (30000-39999) ====================
    PARAM_VALIDATION_ERROR(30001, "参数校验失败", 400),
    PARAM_MISSING(30002, "缺少必要参数", 400),
    PARAM_FORMAT_ERROR(30003, "参数格式错误", 400),
    PARAM_TYPE_ERROR(30004, "参数类型错误", 400),
    PARAM_RANGE_ERROR(30005, "参数范围错误", 400),
    PARAM_DUPLICATE(30006, "参数重复", 400),

    // ==================== 认证授权错误 (40000-49999) ====================
    UNAUTHORIZED(40001, "未授权访问", 401),
    FORBIDDEN(40002, "禁止访问", 403),
    TOKEN_EXPIRED(40003, "令牌已过期", 401),
    TOKEN_INVALID(40004, "令牌无效", 401),
    TOKEN_MISSING(40005, "令牌缺失", 401),
    CREDENTIALS_INVALID(40006, "凭证无效", 401),
    ACCOUNT_LOCKED(40007, "账户已被锁定", 403),
    ACCOUNT_DISABLED(40008, "账户已被禁用", 403),

    // ==================== 资源操作错误 (50000-59999) ====================
    RESOURCE_CONFLICT(50001, "资源冲突", 409),
    RESOURCE_LOCKED(50002, "资源已被锁定", 423),
    RESOURCE_QUOTA_EXCEEDED(50003, "资源配额已超限", 429),
    OPERATION_NOT_ALLOWED(50004, "操作不被允许", 405),
    OPERATION_TIMEOUT(50005, "操作超时", 408),

    // ==================== 数据错误 (60000-69999) ====================
    DATA_INTEGRITY_ERROR(60001, "数据完整性错误", 409),
    DATA_VERSION_CONFLICT(60002, "数据版本冲突", 409),
    DATA_NOT_CONSISTENT(60003, "数据不一致", 409),

    // ==================== 第三方服务错误 (70000-79999) ====================
    THIRD_PARTY_SERVICE_ERROR(70001, "第三方服务错误", 502),
    THIRD_PARTY_RATE_LIMIT(70002, "第三方服务限流", 429),
    THIRD_PARTY_AUTH_ERROR(70003, "第三方服务认证失败", 401);

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * HTTP状态码
     */
    private final int httpStatus;

    /**
     * 根据错误码获取枚举实例
     *
     * @param code 错误码
     * @return 对应的枚举实例，如果找不到则返回SYSTEM_ERROR
     */
    public static ErrorCodeEnum fromCode(int code) {
        for (ErrorCodeEnum errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }

    /**
     * 检查错误码是否属于系统错误
     *
     * @return 如果是系统错误返回true，否则返回false
     */
    public boolean isSystemError() {
        return code >= 10000 && code < 20000;
    }

    /**
     * 检查错误码是否属于业务错误
     *
     * @return 如果是业务错误返回true，否则返回false
     */
    public boolean isBusinessError() {
        return code >= 20000 && code < 30000;
    }

    /**
     * 检查错误码是否属于参数错误
     *
     * @return 如果是参数错误返回true，否则返回false
     */
    public boolean isParamError() {
        return code >= 30000 && code < 40000;
    }

    /**
     * 检查错误码是否属于认证授权错误
     *
     * @return 如果是认证授权错误返回true，否则返回false
     */
    public boolean isAuthError() {
        return code >= 40000 && code < 50000;
    }

    /**
     * 获取错误码字符串表示
     *
     * @return 错误码字符串
     */
    public String getCodeString() {
        return String.valueOf(code);
    }
}