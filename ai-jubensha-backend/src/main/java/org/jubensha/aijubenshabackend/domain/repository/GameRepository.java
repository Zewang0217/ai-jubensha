package org.jubensha.aijubenshabackend.domain.repository;

import org.jubensha.aijubenshabackend.domain.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 游戏仓库接口
 */
@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    
    // 根据游戏房间码查询游戏
    Optional<Game> findByGameCode(String gameCode);
    
    // 根据状态查询游戏
    List<Game> findByStatus(String status);
    
    // 根据当前阶段查询游戏
    List<Game> findByCurrentPhase(String currentPhase);
    
    // 根据剧本ID查询游戏
    List<Game> findByScriptId(Long scriptId);
    
    // 根据状态和剧本ID查询游戏
    List<Game> findByStatusAndScriptId(String status, Long scriptId);
}
