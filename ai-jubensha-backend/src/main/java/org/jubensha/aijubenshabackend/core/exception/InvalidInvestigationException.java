package org.jubensha.aijubenshabackend.core.exception;

import org.jubensha.aijubenshabackend.core.exception.enums.ErrorCodeEnum;

/**
 * 无效搜证操作异常
 * 当玩家在不正确的阶段搜证或搜证无效场景时抛出
 *
 * @author luobo
 * @date 2026-02-10
 */
public class InvalidInvestigationException extends BusinessException {

    /**
     * 使用自定义消息创建异常
     *
     * @param message 错误消息
     */
    public InvalidInvestigationException(String message) {
        super(message);
    }

    /**
     * 使用错误码枚举创建异常
     *
     * @param errorCode 错误码枚举
     */
    public InvalidInvestigationException(ErrorCodeEnum errorCode) {
        super(errorCode);
    }

    /**
     * 创建游戏不在搜证阶段的异常
     *
     * @param currentPhase 当前阶段
     * @return InvalidInvestigationException 异常实例
     */
    public static InvalidInvestigationException notInInvestigationPhase(String currentPhase) {
        InvalidInvestigationException exception = new InvalidInvestigationException(
                String.format("当前游戏阶段为 %s，不在搜证阶段", currentPhase)
        );
        exception.withData("currentPhase", currentPhase);
        return exception;
    }

    /**
     * 创建场景无效异常
     *
     * @param sceneId 场景ID
     * @return InvalidInvestigationException 异常实例
     */
    public static InvalidInvestigationException invalidScene(Long sceneId) {
        InvalidInvestigationException exception = new InvalidInvestigationException(
                String.format("场景 %d 不存在或不可搜证", sceneId)
        );
        exception.withData("sceneId", sceneId);
        return exception;
    }

    /**
     * 创建玩家不在游戏中异常
     *
     * @param playerId 玩家ID
     * @param gameId   游戏ID
     * @return InvalidInvestigationException 异常实例
     */
    public static InvalidInvestigationException playerNotInGame(Long playerId, Long gameId) {
        InvalidInvestigationException exception = new InvalidInvestigationException(
                String.format("玩家 %d 不在游戏 %d 中", playerId, gameId)
        );
        exception.withData("playerId", playerId);
        exception.withData("gameId", gameId);
        return exception;
    }
}
