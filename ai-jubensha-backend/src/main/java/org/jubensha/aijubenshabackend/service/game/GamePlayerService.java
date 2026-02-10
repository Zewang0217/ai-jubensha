package org.jubensha.aijubenshabackend.service.game;

import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.jubensha.aijubenshabackend.models.enums.GamePlayerStatus;

import java.util.List;
import java.util.Optional;

public interface GamePlayerService {

    /**
     * 创建游戏玩家关系
     */
    GamePlayer createGamePlayer(GamePlayer gamePlayer);

    /**
     * 根据ID获取游戏玩家关系
     */
    Optional<GamePlayer> getGamePlayerById(Long id);

    /**
     * 根据游戏ID和玩家ID获取游戏玩家关系
     */
    Optional<GamePlayer> getGamePlayerByGameIdAndPlayerId(Long gameId, Long playerId);

    /**
     * 根据游戏ID获取所有游戏玩家关系
     */
    List<GamePlayer> getGamePlayersByGameId(Long gameId);

    /**
     * 根据玩家ID获取所有游戏玩家关系
     */
    List<GamePlayer> getGamePlayersByPlayerId(Long playerId);

    /**
     * 根据角色ID获取所有游戏玩家关系
     */
    List<GamePlayer> getGamePlayersByCharacterId(Long characterId);

    /**
     * 根据游戏ID和角色ID获取游戏玩家关系
     */
    Optional<GamePlayer> getGamePlayerByGameIdAndCharacterId(Long gameId, Long characterId);

    /**
     * 根据游戏ID和是否为DM获取游戏玩家关系
     */
    List<GamePlayer> getGamePlayersByGameIdAndIsDm(Long gameId, Boolean isDm);

    /**
     * 更新游戏玩家关系
     */
    GamePlayer updateGamePlayer(Long id, GamePlayer gamePlayer);

    /**
     * 更新游戏玩家状态
     */
    GamePlayer updateGamePlayerStatus(Long id, GamePlayerStatus status);

    /**
     * 删除游戏玩家关系
     */
    void deleteGamePlayer(Long id);

    /**
     * 删除游戏的所有玩家关系
     */
    void deleteGamePlayersByGameId(Long gameId);

    /**
     * 删除玩家的所有游戏关系
     */
    void deleteGamePlayersByPlayerId(Long playerId);
}
