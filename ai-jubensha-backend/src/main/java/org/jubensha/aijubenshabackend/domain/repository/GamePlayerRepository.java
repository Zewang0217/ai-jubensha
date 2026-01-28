package org.jubensha.aijubenshabackend.domain.repository;

import org.jubensha.aijubenshabackend.domain.model.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 游戏玩家关联仓库接口
 */
@Repository
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {
    
    // 根据游戏ID查询游戏玩家关联
    List<GamePlayer> findByGameId(Long gameId);
    
    // 根据玩家ID查询游戏玩家关联
    List<GamePlayer> findByPlayerId(Long playerId);
    
    // 根据角色ID查询游戏玩家关联
    List<GamePlayer> findByCharacterId(Long characterId);
    
    // 根据游戏ID和玩家ID查询游戏玩家关联
    GamePlayer findByGameIdAndPlayerId(Long gameId, Long playerId);
    
    // 根据游戏ID和状态查询游戏玩家关联
    List<GamePlayer> findByGameIdAndStatus(Long gameId, String status);
    
    // 根据游戏ID和角色查询游戏玩家关联
    List<GamePlayer> findByGameIdAndRole(Long gameId, String role);
}
