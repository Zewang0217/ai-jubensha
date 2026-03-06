package org.jubensha.aijubenshabackend.service.game;

import java.util.Map;
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

    /**
     * 优雅退出游戏
     * <p>
     * 停止游戏相关的所有后台任务，包括讨论服务、计时器等，
     * 并更新游戏状态为已结束。
     * </p>
     *
     * @param id 游戏ID
     * @return 更新后的游戏实体
     */
    Game exitGame(Long id);

    /**
     * 推进游戏到下一阶段
     * <p>
     * 根据当前阶段计算下一阶段，并更新数据库。
     * 如果当前是最后一个阶段，则不进行推进。
     * </p>
     *
     * @param id 游戏ID
     * @return 推进后的游戏实体，如果无法推进则返回 null
     */
    Game advancePhase(Long id);

    /**
     * 推进游戏到下一阶段并广播通知
     * <p>
     * 推进阶段后通过 WebSocket 广播 PHASE_CHANGE 消息通知所有客户端。
     * </p>
     *
     * @param id 游戏ID
     * @return 推进结果，包含新阶段、旧阶段等信息
     */
    Map<String, Object> advancePhaseWithNotification(Long id);
}