package org.jubensha.aijubenshabackend.models.enums;

/**
 * 应用程序常量类
 */
public class AppConstants {
    
    // 系统相关常量
    public static final String APP_NAME = "AI剧本杀后端系统";
    public static final String APP_VERSION = "1.0.0";
    public static final String SYSTEM_USER = "system";
    
    // 状态相关常量
    public static final int STATUS_SUCCESS = 200;
    public static final int STATUS_ERROR = 500;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_NOT_FOUND = 404;
    
    // 消息相关常量
    public static final String MESSAGE_SUCCESS = "操作成功";
    public static final String MESSAGE_ERROR = "操作失败";
    public static final String MESSAGE_PARAM_ERROR = "参数错误";
    public static final String MESSAGE_UNAUTHORIZED = "未授权访问";
    public static final String MESSAGE_FORBIDDEN = "禁止访问";
    public static final String MESSAGE_NOT_FOUND = "资源不存在";
    
    // 游戏相关常量
    public static final int MAX_PLAYERS = 8;
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_SCRIPT_LENGTH = 100000;
    public static final int MAX_CHARACTER_NAME_LENGTH = 20;
    
    // Redis相关常量
    public static final String REDIS_KEY_PREFIX = "ai_jubensha:";
    public static final int REDIS_EXPIRE_TIME = 3600; // 1小时
    
    // WebSocket相关常量
    public static final String WEBSOCKET_PATH = "/ws/game";
    public static final String WEBSOCKET_TOPIC_PREFIX = "/topic/game";
    
    // AI相关常量
    public static final int MAX_AI_RESPONSE_LENGTH = 1000;
    public static final int MAX_AI_CONTEXT_LENGTH = 10000;
    
    // 安全相关常量
    public static final String SECURITY_ROLE_USER = "ROLE_USER";
    public static final String SECURITY_ROLE_ADMIN = "ROLE_ADMIN";
}
