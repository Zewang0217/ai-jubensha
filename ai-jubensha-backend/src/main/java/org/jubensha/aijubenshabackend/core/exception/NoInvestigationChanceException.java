package org.jubensha.aijubenshabackend.core.exception;

import org.jubensha.aijubenshabackend.core.exception.enums.ErrorCodeEnum;

/**
 * 搜证次数不足异常
 * 当玩家搜证次数已用完时抛出
 *
 * @author luobo
 * @date 2026-02-10
 */
public class NoInvestigationChanceException extends BusinessException {

    /**
     * 默认错误消息
     */
    private static final String DEFAULT_MESSAGE = "搜证次数已用完";

    /**
     * 使用默认消息创建异常
     */
    public NoInvestigationChanceException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * 使用自定义消息创建异常
     *
     * @param message 错误消息
     */
    public NoInvestigationChanceException(String message) {
        super(message);
    }

    /**
     * 使用错误码枚举创建异常
     *
     * @param errorCode 错误码枚举
     */
    public NoInvestigationChanceException(ErrorCodeEnum errorCode) {
        super(errorCode);
    }

    /**
     * 使用玩家ID创建异常，包含剩余次数信息
     *
     * @param playerId       玩家ID
     * @param remainingCount 剩余次数
     */
    public NoInvestigationChanceException(Long playerId, int remainingCount) {
        super(String.format("玩家 %d 搜证次数已用完（剩余 %d 次）", playerId, remainingCount));
        this.withData("playerId", playerId);
        this.withData("remainingCount", remainingCount);
    }
}
