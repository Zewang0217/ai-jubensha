package org.jubensha.aijubenshabackend.service.investigation;

import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.exception.InvalidInvestigationException;
import org.jubensha.aijubenshabackend.core.exception.NoInvestigationChanceException;
import org.jubensha.aijubenshabackend.models.dto.InvestigationRequestDTO;
import org.jubensha.aijubenshabackend.models.dto.InvestigationResponseDTO;
import org.jubensha.aijubenshabackend.models.dto.InvestigationStatusDTO;
import org.jubensha.aijubenshabackend.models.entity.Clue;
import org.jubensha.aijubenshabackend.models.entity.Game;
import org.jubensha.aijubenshabackend.models.entity.Scene;
import org.jubensha.aijubenshabackend.service.clue.ClueService;
import org.jubensha.aijubenshabackend.service.game.GameService;
import org.jubensha.aijubenshabackend.service.scene.SceneService;
import org.jubensha.aijubenshabackend.websocket.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 搜证服务实现类
 * 实现玩家搜证相关的业务逻辑
 *
 * @author luobo
 * @date 2026-02-10
 */
@Slf4j
@Service
public class InvestigationServiceImpl implements InvestigationService {

    /**
     * 内存中存储游戏的工作流上下文
     * key: gameId, value: WorkflowContext
     * 注意：生产环境应使用Redis等分布式缓存
     */
    private final Map<Long, WorkflowContext> workflowContextCache = new ConcurrentHashMap<>();

    private final GameService gameService;
    private final ClueService clueService;
    private final SceneService sceneService;
    private final WebSocketService webSocketService;

    @Autowired
    public InvestigationServiceImpl(GameService gameService,
                                    ClueService clueService,
                                    SceneService sceneService,
                                    WebSocketService webSocketService) {
        this.gameService = gameService;
        this.clueService = clueService;
        this.sceneService = sceneService;
        this.webSocketService = webSocketService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvestigationResponseDTO investigate(Long gameId, InvestigationRequestDTO request) {
        Long playerId = request.getPlayerId();
        Long sceneId = request.getSceneId();

        log.info("玩家 {} 在游戏 {} 中搜证场景 {}", playerId, gameId, sceneId);

        // 1. 验证游戏状态
        Game game = validateGameState(gameId);

        // 2. 获取工作流上下文
        WorkflowContext context = getWorkflowContext(gameId);
        if (context == null) {
            log.error("游戏 {} 的工作流上下文不存在", gameId);
            throw new InvalidInvestigationException("游戏状态异常，无法搜证");
        }

        // 3. 验证玩家是否可以搜证
        if (!context.hasInvestigationChance(playerId)) {
            int remaining = context.getRemainingInvestigationCount(playerId);
            log.warn("玩家 {} 搜证次数已用完（剩余 {} 次）", playerId, remaining);
            throw new NoInvestigationChanceException(playerId, remaining);
        }

        // 4. 验证场景是否有效
        Scene scene = validateScene(sceneId, context);

        // 5. 获取线索
        Clue clue = getClueFromScene(scene, request.getClueId());
        if (clue == null) {
            log.warn("场景 {} 中没有可获取的线索", sceneId);
            throw InvalidInvestigationException.invalidScene(sceneId);
        }

        // 6. 扣减搜证次数
        boolean consumed = context.consumeInvestigationChance(playerId);
        if (!consumed) {
            log.error("扣减玩家 {} 搜证次数失败", playerId);
            throw new NoInvestigationChanceException(playerId, 0);
        }

        // 7. 记录搜证历史
        context.recordInvestigationHistory(playerId, sceneId, clue.getId(), clue.getName());

        // 8. 保存更新后的上下文
        saveWorkflowContext(gameId, context);

        // 9. 构建响应
        int remainingChances = context.getRemainingInvestigationCount(playerId);
        InvestigationResponseDTO response = InvestigationResponseDTO.success(
                clue, remainingChances, WorkflowContext.DEFAULT_INVESTIGATION_LIMIT
        );
        // TODO: 支持多轮搜证
        response.setCurrentRound(1);
        response.setInvestigationHistory(context.getInvestigationHistory(playerId));

        // 10. 通过WebSocket通知游戏内其他玩家（可选）
        notifyClueFound(gameId, playerId, clue);

        log.info("玩家 {} 搜证成功，获得线索 {}，剩余 {} 次机会",
                playerId, clue.getName(), remainingChances);

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvestigationStatusDTO getInvestigationStatus(Long gameId, Long playerId) {
        WorkflowContext context = getWorkflowContext(gameId);
        if (context == null) {
            return null;
        }

        int remainingChances = context.getRemainingInvestigationCount(playerId);
        List<Map<String, Object>> history = context.getInvestigationHistory(playerId);

        InvestigationStatusDTO status = InvestigationStatusDTO.of(
                playerId,
                remainingChances,
                WorkflowContext.DEFAULT_INVESTIGATION_LIMIT,
                context.getCurrentPhase(),
                history
        );

        // 设置可搜证场景
        if (context.getScenes() != null) {
            List<Map<String, Object>> sceneInfos = new ArrayList<>();
            for (Scene scene : context.getScenes()) {
                Map<String, Object> sceneInfo = new HashMap<>();
                sceneInfo.put("sceneId", scene.getId());
                sceneInfo.put("sceneName", scene.getName());
                sceneInfo.put("description", scene.getDescription());
                sceneInfos.add(sceneInfo);
            }
            status.setAvailableScenes(sceneInfos);
        }

        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canInvestigate(Long gameId, Long playerId) {
        WorkflowContext context = getWorkflowContext(gameId);
        if (context == null) {
            return false;
        }
        return context.hasInvestigationChance(playerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Long, InvestigationStatusDTO> getAllPlayersInvestigationStatus(Long gameId) {
        WorkflowContext context = getWorkflowContext(gameId);
        if (context == null) {
            return Collections.emptyMap();
        }

        Map<Long, InvestigationStatusDTO> result = new HashMap<>();
        Map<Long, Integer> allCounts = context.getAllInvestigationCounts();

        for (Long playerId : allCounts.keySet()) {
            result.put(playerId, getInvestigationStatus(gameId, playerId));
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initInvestigationPhase(Long gameId, List<Long> playerIds, Integer totalRounds) {
        log.info("初始化游戏 {} 的搜证阶段，玩家数量: {}，轮次: {}",
                gameId, playerIds.size(), totalRounds);

        WorkflowContext context = getWorkflowContext(gameId);
        if (context == null) {
            context = new WorkflowContext();
            context.setGameId(gameId);
        }

        context.initInvestigationCounts(playerIds);
        saveWorkflowContext(gameId, context);

        log.info("游戏 {} 的搜证阶段初始化完成", gameId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowContext getWorkflowContext(Long gameId) {
        return workflowContextCache.get(gameId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveWorkflowContext(Long gameId, WorkflowContext context) {
        workflowContextCache.put(gameId, context);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证游戏状态
     * 检查游戏是否存在且处于搜证阶段
     *
     * @param gameId 游戏ID
     * @return 游戏实体
     * @throws InvalidInvestigationException 当游戏状态无效时抛出
     */
    private Game validateGameState(Long gameId) {
        Optional<Game> gameOpt = gameService.getGameById(gameId);
        if (gameOpt.isEmpty()) {
            log.error("游戏 {} 不存在", gameId);
            throw new InvalidInvestigationException("游戏不存在");
        }

        Game game = gameOpt.get();

        // 检查游戏是否处于搜证阶段
        // 暂时注释掉，因为当前阶段可能是 FIRST_INVESTIGATION 或其他变体
        // if (game.getCurrentPhase() == null ||
        //     !game.getCurrentPhase().toString().contains("INVESTIGATION")) {
        //     log.warn("游戏 {} 当前阶段为 {}，不在搜证阶段", gameId, game.getCurrentPhase());
        //     throw InvalidInvestigationException.notInInvestigationPhase(
        //         game.getCurrentPhase() != null ? game.getCurrentPhase().toString() : "null"
        //     );
        // }

        return game;
    }

    /**
     * 验证场景是否有效
     *
     * @param sceneId 场景ID
     * @param context 工作流上下文
     * @return 场景实体
     * @throws InvalidInvestigationException 当场景无效时抛出
     */
    private Scene validateScene(Long sceneId, WorkflowContext context) {
        if (context.getScenes() == null) {
            log.error("工作流上下文中没有场景列表");
            throw InvalidInvestigationException.invalidScene(sceneId);
        }

        return context.getScenes().stream()
                .filter(s -> s.getId().equals(sceneId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("场景 {} 不在当前游戏的场景列表中", sceneId);
                    return InvalidInvestigationException.invalidScene(sceneId);
                });
    }

    /**
     * 从场景中获取线索
     * 如果指定了线索ID则返回对应线索，否则随机返回场景中的一个线索
     *
     * @param scene  场景
     * @param clueId 线索ID（可选）
     * @return 线索实体，如果没有可用线索则返回null
     */
    private Clue getClueFromScene(Scene scene, Long clueId) {
        List<Clue> clues = clueService.getCluesByScene(scene.getName());

        if (clues == null || clues.isEmpty()) {
            return null;
        }

        // 如果指定了线索ID，返回对应线索
        if (clueId != null) {
            return clues.stream()
                    .filter(c -> c.getId().equals(clueId))
                    .findFirst()
                    .orElse(null);
        }

        // 否则随机返回一个线索
        Random random = new Random();
        return clues.get(random.nextInt(clues.size()));
    }

    /**
     * 通知游戏内其他玩家有玩家发现了线索
     *
     * @param gameId   游戏ID
     * @param playerId 发现线索的玩家ID
     * @param clue     发现的线索
     */
    private void notifyClueFound(Long gameId, Long playerId, Clue clue) {
        try {
            webSocketService.sendClueFoundMessage(gameId, clue.getName(), clue.getDescription());
            log.debug("已通知游戏 {} 内的玩家 {} 发现了线索 {}", gameId, playerId, clue.getName());
        } catch (Exception e) {
            log.warn("发送线索发现通知失败: {}", e.getMessage());
        }
    }
}
