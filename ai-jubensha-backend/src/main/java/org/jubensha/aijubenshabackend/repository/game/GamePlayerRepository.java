package org.jubensha.aijubenshabackend.repository.game;

import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    /**
     * 根据游戏ID和玩家ID获取游戏玩家关系
     */
    Optional<GamePlayer> findByGameIdAndPlayerId(Long gameId, Long playerId);

    /**
     * 根据游戏ID获取所有游戏玩家关系
     */
    List<GamePlayer> findByGameId(Long gameId);

    /**
     * 根据玩家ID获取所有游戏玩家关系
     */
    List<GamePlayer> findByPlayerId(Long playerId);

    /**
     * 根据角色ID获取所有游戏玩家关系
     */
    List<GamePlayer> findByCharacterId(Long characterId);

    /**
     * 根据游戏ID和角色ID获取游戏玩家关系
     */
    Optional<GamePlayer> findByGameIdAndCharacterId(Long gameId, Long characterId);

    /**
     * 根据游戏ID和是否为DM获取游戏玩家关系
     */
    List<GamePlayer> findByGameIdAndIsDm(Long gameId, Boolean isDm);
}
