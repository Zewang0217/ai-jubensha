package org.jubensha.aijubenshabackend.service.game;

import org.jubensha.aijubenshabackend.models.entity.Game;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.models.enums.GameStatus;

import java.util.List;
import java.util.Optional;

public interface GameService {
    
    /**
     * 创建新游戏
     */
    Game createGame(Game game);
    
    /**
     * 根据ID获取游戏
     */
    Optional<Game> getGameById(Long id);
    
    /**
     * 根据游戏码获取游戏
     */
    Optional<Game> getGameByGameCode(String gameCode);
    
    /**
     * 获取所有游戏
     */
    List<Game> getAllGames();

    /**
     * 根据status获取游戏
     */
    List<Game> getGamesByStatus(GameStatus status);

    /**
     * 根据阶段获取游戏
     */
    List<Game> getGamesByCurrentPhase(GamePhase currentPhase);
    
    /**
     * 更新游戏
     */
    Game updateGame(Long id, Game game);
    
    /**
     * 更新游戏状态
     */
    Game updateGameStatus(Long id, GameStatus status);
    
    /**
     * 更新游戏阶段
     */
    Game updateGamePhase(Long id, GamePhase phase);
    
    /**
     * 删除游戏
     */
    void deleteGame(Long id);
    
    /**
     * 根据剧本ID获取游戏
     */
    List<Game> getGamesByScriptId(Long scriptId);
    
    /**
     * 根据状态和剧本ID获取游戏
     */
    List<Game> getGamesByScriptIdAndStatus(Long scriptId, GameStatus status);
    
    /**
     * 开始游戏
     */
    Game startGame(Long id);
    
    /**
     * 结束游戏
     */
    Game endGame(Long id);
    
    /**
     * 取消游戏
     */
    Game cancelGame(Long id);
}