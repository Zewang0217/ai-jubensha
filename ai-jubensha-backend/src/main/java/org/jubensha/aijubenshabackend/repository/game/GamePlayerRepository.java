package org.jubensha.aijubenshabackend.repository.game;

import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    /**
     * 根据游戏ID和玩家ID获取游戏玩家关系
     * 使用 LIMIT 1 确保只返回一条记录，避免重复数据导致的错误
     */
    @Query("SELECT gp FROM GamePlayer gp WHERE gp.game.id = :gameId AND gp.player.id = :playerId ORDER BY gp.id DESC LIMIT 1")
    Optional<GamePlayer> findByGameIdAndPlayerId(@Param("gameId") Long gameId, @Param("playerId") Long playerId);

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
    @Query("SELECT gp FROM GamePlayer gp WHERE gp.game.id = :gameId AND gp.character.id = :characterId ORDER BY gp.id DESC LIMIT 1")
    Optional<GamePlayer> findByGameIdAndCharacterId(@Param("gameId") Long gameId, @Param("characterId") Long characterId);

    /**
     * 根据游戏ID和是否为DM获取游戏玩家关系
     */
    List<GamePlayer> findByGameIdAndIsDm(Long gameId, Boolean isDm);
}
