package org.jubensha.aijubenshabackend.service.game;

import org.jubensha.aijubenshabackend.ai.service.DiscussionService;
import org.jubensha.aijubenshabackend.models.entity.Game;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.models.enums.GameStatus;
import org.jubensha.aijubenshabackend.repository.game.GameRepository;
import org.jubensha.aijubenshabackend.service.investigation.InvestigationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameServiceImpl implements GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);

    private final GameRepository gameRepository;
    private final DiscussionService discussionService;
    private final InvestigationService investigationService;

    @Autowired
    public GameServiceImpl(GameRepository gameRepository, DiscussionService discussionService, InvestigationService investigationService) {
        this.gameRepository = gameRepository;
        this.discussionService = discussionService;
        this.investigationService = investigationService;
    }

    @Override
    public Game createGame(Game game) {
        logger.info("Creating new game: {}", game.getGameCode());
        return gameRepository.save(game);
    }

    @Override
    public Optional<Game> getGameById(Long id) {
        logger.info("Getting game by id: {}", id);
        return gameRepository.findById(id);
    }

    @Override
    public Optional<Game> getGameByGameCode(String gameCode) {
        logger.info("Getting game by game code: {}", gameCode);
        return gameRepository.findByGameCode(gameCode);
    }

    @Override
    public List<Game> getAllGames() {
        logger.info("Getting all games");
        return gameRepository.findAll();
    }

    @Override
    public List<Game> getGamesByStatus(GameStatus status) {
        logger.info("Getting games by status: {}", status);
        return gameRepository.findByStatus(status);
    }


    @Override
    public List<Game> getGamesByCurrentPhase(GamePhase currentPhase) {
        logger.info("Getting games by current phase: {}", currentPhase);
        return gameRepository.findByCurrentPhase(currentPhase);
    }

    @Override
    public Game updateGame(Long id, Game game) {
        logger.info("Updating game: {}", id);
        Optional<Game> existingGame = gameRepository.findById(id);
        if (existingGame.isPresent()) {
            Game updatedGame = existingGame.get();

            // 只更新非 null 的字段，特别注意不要更新 scriptId（因为 GameUpdateDTO 中没有这个字段）
            if (game.getGameCode() != null) {
                updatedGame.setGameCode(game.getGameCode());
            }
            // 注意：scriptId 不应该在更新时修改，所以这里不设置 scriptId
            if (game.getStatus() != null) {
                updatedGame.setStatus(game.getStatus());
            }
            if (game.getCurrentPhase() != null) {
                updatedGame.setCurrentPhase(game.getCurrentPhase());
            }
            if (game.getStartTime() != null) {
                updatedGame.setStartTime(game.getStartTime());
            }
            if (game.getEndTime() != null) {
                updatedGame.setEndTime(game.getEndTime());
            }

            return gameRepository.save(updatedGame);
        } else {
            throw new IllegalArgumentException("Game not found with id: " + id);
        }
    }

    @Override
    public Game updateGameStatus(Long id, GameStatus status) {
        logger.info("Updating game status: {} to {}", id, status);
        Optional<Game> existingGame = gameRepository.findById(id);
        if (existingGame.isPresent()) {
            Game updatedGame = existingGame.get();
            updatedGame.setStatus(status);
            return gameRepository.save(updatedGame);
        } else {
            throw new IllegalArgumentException("Game not found with id: " + id);
        }
    }

    @Override
    public Game updateGamePhase(Long id, GamePhase phase) {
        logger.info("Updating game phase: {} to {}", id, phase);
        Optional<Game> existingGame = gameRepository.findById(id);
        if (existingGame.isPresent()) {
            Game updatedGame = existingGame.get();
            updatedGame.setCurrentPhase(phase);
            return gameRepository.save(updatedGame);
        } else {
            throw new IllegalArgumentException("Game not found with id: " + id);
        }
    }

    @Override
    public void deleteGame(Long id) {
        logger.info("Deleting game: {}", id);
        gameRepository.deleteById(id);
    }

    @Override
    public List<Game> getGamesByScriptId(Long scriptId) {
        logger.info("Getting games by script id: {}", scriptId);
        return gameRepository.findByScriptId(scriptId);
    }

    @Override
    public List<Game> getGamesByScriptIdAndStatus(Long scriptId, GameStatus status) {
        logger.info("Getting games by status: {} and script id: {}", status, scriptId);
        return gameRepository.findByScriptIdAndStatus(scriptId, status);
    }

    @Override
    public Game startGame(Long id) {
        logger.info("Starting game: {}", id);
        Optional<Game> existingGame = gameRepository.findById(id);
        if (existingGame.isPresent()) {
            Game updatedGame = existingGame.get();
            updatedGame.setStatus(GameStatus.STARTED);
            updatedGame.setCurrentPhase(GamePhase.INTRODUCTION);
            updatedGame.setStartTime(java.time.LocalDateTime.now());
            return gameRepository.save(updatedGame);
        } else {
            throw new IllegalArgumentException("Game not found with id: " + id);
        }
    }

    @Override
    public Game endGame(Long id) {
        logger.info("Ending game: {}", id);
        Optional<Game> existingGame = gameRepository.findById(id);
        if (existingGame.isPresent()) {
            Game updatedGame = existingGame.get();
            updatedGame.setStatus(GameStatus.ENDED);
            updatedGame.setEndTime(java.time.LocalDateTime.now());
            return gameRepository.save(updatedGame);
        } else {
            throw new IllegalArgumentException("Game not found with id: " + id);
        }
    }

    @Override
    public Game cancelGame(Long id) {
        logger.info("Canceling game: {}", id);
        Optional<Game> existingGame = gameRepository.findById(id);
        if (existingGame.isPresent()) {
            Game updatedGame = existingGame.get();
            updatedGame.setStatus(GameStatus.CANCELED);
            updatedGame.setEndTime(java.time.LocalDateTime.now());
            return gameRepository.save(updatedGame);
        } else {
            throw new IllegalArgumentException("Game not found with id: " + id);
        }
    }

    /**
     * 优雅退出游戏
     * <p>
     * 停止游戏相关的所有后台任务，包括讨论服务、计时器等，
     * 并更新游戏状态为已结束。
     * </p>
     *
     * @param id 游戏ID
     * @return 更新后的游戏实体
     * @throws IllegalArgumentException 当游戏不存在时抛出
     */
    @Override
    public Game exitGame(Long id) {
        logger.info("优雅退出游戏，游戏ID: {}", id);

        // 验证游戏是否存在
        Optional<Game> existingGame = gameRepository.findById(id);
        if (existingGame.isEmpty()) {
            logger.error("游戏不存在，游戏ID: {}", id);
            throw new IllegalArgumentException("Game not found with id: " + id);
        }

        Game game = existingGame.get();
        logger.info("当前游戏状态: {}, 当前阶段: {}", game.getStatus(), game.getCurrentPhase());

        try {
            // 停止讨论服务，清理所有后台任务
            logger.info("停止讨论服务，游戏ID: {}", id);
            discussionService.stopDiscussion(id);
            logger.info("讨论服务已停止，游戏ID: {}", id);
        } catch (Exception e) {
            // 记录错误但不中断退出流程
            logger.warn("停止讨论服务时发生错误，游戏ID: {}, 错误: {}", id, e.getMessage(), e);
        }

        try {
            // 清理工作流上下文缓存，防止旧玩家ID残留
            logger.info("清理工作流上下文缓存，游戏ID: {}", id);
            investigationService.removeWorkflowContext(id);
            logger.info("工作流上下文缓存已清理，游戏ID: {}", id);
        } catch (Exception e) {
            // 记录错误但不中断退出流程
            logger.warn("清理工作流上下文缓存时发生错误，游戏ID: {}, 错误: {}", id, e.getMessage(), e);
        }

        // 调用 endGame 更新游戏状态为 ENDED
        logger.info("更新游戏状态为已结束，游戏ID: {}", id);
        Game endedGame = endGame(id);
        logger.info("游戏已优雅退出，游戏ID: {}, 结束时间: {}", id, endedGame.getEndTime());

        return endedGame;
    }
}