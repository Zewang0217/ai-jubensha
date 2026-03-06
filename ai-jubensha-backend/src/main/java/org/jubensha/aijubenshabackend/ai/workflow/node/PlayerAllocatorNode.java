package org.jubensha.aijubenshabackend.ai.workflow.node;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.service.AIService;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.Game;
import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.models.enums.GamePlayerStatus;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.jubensha.aijubenshabackend.service.game.GameService;
import org.jubensha.aijubenshabackend.service.investigation.InvestigationService;
import org.jubensha.aijubenshabackend.websocket.service.WebSocketServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 分配玩家的节点
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-01-31 14:19
 * @since 2026
 */

@Slf4j
public class PlayerAllocatorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.debug("PlayerAllocatorNode: 步骤={}, 剧本ID={}, 剧本名称={}",
                    context.getCurrentStep(),
                    context.getScriptId(),
                    context.getScriptName());
            log.info("执行节点：玩家分配");

            Long scriptId = context.getScriptId();
            if (scriptId == null) {
                log.error("剧本ID为空，无法分配玩家");
                context.setErrorMessage("剧本ID为空，无法分配玩家");
                return WorkflowContext.saveContext(context);
            }

            Long gameId = context.getGameId();
            if (gameId == null) {
                log.error("游戏ID为空，无法分配玩家");
                context.setErrorMessage("游戏ID为空，无法分配玩家");
                return WorkflowContext.saveContext(context);
            }

            CharacterService characterService = SpringContextUtil.getBean(CharacterService.class);
            AIService aiService = SpringContextUtil.getBean(AIService.class);
            GamePlayerService gamePlayerService = SpringContextUtil.getBean(GamePlayerService.class);
            GameService gameService = SpringContextUtil.getBean(GameService.class);

            Game game = gameService.getGameById(gameId)
                    .orElseThrow(() -> new IllegalStateException("游戏不存在，游戏ID: " + gameId));

            List<Character> characters = characterService.getCharactersByScriptId(scriptId);
            int totalRoles = characters.size();
            log.info("剧本 {} 共有 {} 个角色", scriptId, totalRoles);

            int realPlayerCount = getRealPlayerCount(context, game);
            log.info("真人玩家数量：{}", realPlayerCount);

            int aiPlayerCount = Math.max(0, totalRoles - realPlayerCount);
            log.info("需要的AI玩家数量：{}", aiPlayerCount);

            List<Player> aiPlayers = createAIPlayers(aiService, aiPlayerCount);

            List<Map<String, Object>> assignments = new ArrayList<>();
            
            // 真人模式：等待前端选择角色
            if (realPlayerCount > 0) {
                log.info("[角色选择] 真人模式，等待前端选择角色...");
                
                // 保存上下文到 InvestigationService 缓存
                InvestigationService investigationService = SpringContextUtil.getBean(InvestigationService.class);
                investigationService.saveWorkflowContext(context.getGameId(), context);
                
                // 广播角色选择就绪通知
                WebSocketServiceImpl webSocketService = SpringContextUtil.getBean(WebSocketServiceImpl.class);
                webSocketService.broadcastPhaseReady(context.getGameId(), "character_assignment", false, "请选择你的角色");
                
                // 等待角色选择
                int waitCount = 0;
                int maxWaitTime = 1800; // 最大等待时间30分钟
                int checkInterval = 3; // 检查间隔3秒
                
                while (!context.isCharacterSelected() && waitCount < maxWaitTime) {
                    Thread.sleep(checkInterval * 1000);
                    waitCount += checkInterval;
                    
                    // 从缓存中获取最新的上下文状态
                    WorkflowContext latestContext = investigationService.getWorkflowContext(context.getGameId());
                    if (latestContext != null) {
                        context = latestContext;
                    }
                    
                    // 每30秒输出一次等待日志
                    if (waitCount % 30 == 0) {
                        log.info("[角色选择] 等待真人玩家选择角色，已等待: {}秒", waitCount);
                    }
                }
                
                if (context.isCharacterSelected()) {
                    log.info("[角色选择] 真人玩家已选择角色，角色ID: {}, 玩家ID: {}", 
                            context.getSelectedCharacterId(), context.getSelectedPlayerId());
                    
                    // 从数据库中获取所有已分配的角色（包括真人玩家选择的角色）
                    List<GamePlayer> existingGamePlayers = gamePlayerService.getGamePlayersByGameId(gameId);
                    for (GamePlayer gp : existingGamePlayers) {
                        if (gp.getCharacter() != null && gp.getPlayer() != null) {
                            // 根据玩家的实际角色类型设置 playerType
                            // 只有 role 为 REAL 的才是真人玩家，其他都是 AI 玩家
                            PlayerRole playerRole = gp.getPlayer().getRole();
                            String playerType;
                            if (playerRole == PlayerRole.REAL) {
                                playerType = "REAL";
                            } else {
                                playerType = "AI";
                            }
                            
                            log.info("[角色选择] 玩家 {} 角色: {}, 设置 playerType: {}", 
                                    gp.getPlayer().getNickname(), playerRole, playerType);
                            
                            Map<String, Object> assignment = Map.of(
                                    "playerType", playerType,
                                    "playerId", gp.getPlayer().getId(),
                                    "characterId", gp.getCharacter().getId(),
                                    "characterName", gp.getCharacter().getName()
                            );
                            assignments.add(assignment);
                            log.info("[角色选择] 已分配 {} 玩家 {} 到角色：{}", 
                                    playerType, gp.getPlayer().getNickname(), gp.getCharacter().getName());
                        }
                    }
                } else {
                    log.warn("[角色选择] 等待角色选择超时，强制继续工作流");
                }
            } else {
                // 观察者模式：不创建真人玩家，所有角色由AI填充
                log.info("[观察者模式] 自动分配所有角色给AI玩家");
            }
            
            // AI玩家填充剩余角色
            assignAIPlayers(aiService, gamePlayerService, game, characters, realPlayerCount, aiPlayers, assignments, context, gameId);

            Player savedDM = aiService.createDMAgent();
            Player savedJudge = aiService.createJudgeAgent();
            log.info("创建DM：{}", savedDM.getNickname());
            log.info("创建Judge：{}", savedJudge.getNickname());

            updateContext(context, assignments, savedDM, savedJudge, realPlayerCount, aiPlayerCount, totalRoles);

            // 更新游戏阶段到剧本阅读并广播阶段变化
            try {
                GamePhase previousPhase = game.getCurrentPhase();
                game.setCurrentPhase(GamePhase.SCRIPT_READING);
                gameService.updateGame(gameId, game);
                log.info("[阶段变化] 游戏阶段已更新为: SCRIPT_READING");
                
                // 广播阶段变化通知
                WebSocketServiceImpl webSocketService = SpringContextUtil.getBean(WebSocketServiceImpl.class);
                webSocketService.broadcastPhaseChange(gameId, previousPhase, GamePhase.SCRIPT_READING, "进入剧本阅读阶段");
                log.info("[阶段变化] 已广播阶段变化通知");
            } catch (Exception e) {
                log.error("[阶段变化] 更新游戏阶段失败: {}", e.getMessage());
            }

            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 获取真人玩家数量
     * @param context 工作流上下文
     * @param game 游戏实体
     * @return 真人玩家数量
     */
    private static int getRealPlayerCount(WorkflowContext context, Game game) {
        Integer realPlayerCount = context.getRealPlayerCount();
        
        // 如果上下文中没有，尝试从数据库中读取
        if (realPlayerCount == null && game != null) {
            realPlayerCount = game.getRealPlayerCount();
            if (realPlayerCount != null) {
                log.info("从数据库中读取真人玩家数量: {}", realPlayerCount);
            }
        }
        
        if (realPlayerCount == null) {
            realPlayerCount = 1;
            log.info("WorkflowContext和数据库中均未设置真人玩家数量，默认设置为1");
        } else if (realPlayerCount < 0) {
            realPlayerCount = 0;
            log.warn("真人玩家数量为负数，自动调整为0");
        }
        return realPlayerCount;
    }

    /**
     * 创建AI玩家
     * @param aiService AI服务
     * @param aiPlayerCount AI玩家数量
     * @return AI玩家列表
     */
    private static List<Player> createAIPlayers(AIService aiService, int aiPlayerCount) {
        List<Player> aiPlayers = new ArrayList<>();
        long timestamp = System.currentTimeMillis();
        for (int i = 0; i < aiPlayerCount; i++) {
            String uniqueName = "AI_" + timestamp + "_" + (i + 1);
            Player aiPlayer = aiService.createAIPlayer(uniqueName);
            aiPlayers.add(aiPlayer);
            log.info("创建AI玩家：{}", aiPlayer.getNickname());
        }
        return aiPlayers;
    }

    /**
     * 分配AI玩家
     * @param aiService AI服务
     * @param gamePlayerService 游戏玩家关系服务
     * @param game 游戏实体
     * @param characters 角色列表
     * @param realPlayerCount 真人玩家数量
     * @param aiPlayers AI玩家列表
     * @param assignments 分配结果列表
     * @param context 工作流上下文
     * @param gameId 游戏ID
     */
    private static void assignAIPlayers(AIService aiService, 
                                        GamePlayerService gamePlayerService,
                                        Game game,
                                        List<Character> characters, 
                                        int realPlayerCount, 
                                        List<Player> aiPlayers, 
                                        List<Map<String, Object>> assignments,
                                        WorkflowContext context,
                                        Long gameId) {
        // 获取所有已分配的角色ID（从数据库查询）
        java.util.Set<Long> assignedCharacterIds = new java.util.HashSet<>();
        
        // 如果是真人模式，从数据库中查询已分配的角色
        if (realPlayerCount > 0) {
            List<GamePlayer> existingGamePlayers = gamePlayerService.getGamePlayersByGameId(gameId);
            for (GamePlayer gp : existingGamePlayers) {
                if (gp.getCharacter() != null) {
                    assignedCharacterIds.add(gp.getCharacter().getId());
                    log.info("[AI分配] 角色已被真人玩家选择，跳过：{}", gp.getCharacter().getName());
                }
            }
        }
        
        int aiPlayerIndex = 0;
        
        for (int i = 0; i < characters.size() && aiPlayerIndex < aiPlayers.size(); i++) {
            Character character = characters.get(i);
            
            // 跳过已被真人玩家选择的角色
            if (assignedCharacterIds.contains(character.getId())) {
                log.info("[AI分配] 跳过已被真人玩家选择的角色：{}", character.getName());
                continue;
            }
            
            Player aiPlayer = aiPlayers.get(aiPlayerIndex);
            aiPlayerIndex++;
            
            createGamePlayerRecord(gamePlayerService, game, aiPlayer, character, false);
            
            Map<String, Object> assignment = Map.of(
                    "playerType", "AI",
                    "playerId", aiPlayer.getId(),
                    "characterId", character.getId(),
                    "characterName", character.getName()
            );
            assignments.add(assignment);
            
            aiService.createPlayerAgent(aiPlayer.getId(), character.getId());
            log.info("[AI分配] 分配AI玩家 {} 到角色：{}", aiPlayer.getNickname(), character.getName());
        }
        
        log.info("[AI分配] AI玩家分配完成，共分配 {} 个AI玩家", aiPlayerIndex);
    }

    /**
     * 创建游戏玩家关系记录
     * @param gamePlayerService 游戏玩家关系服务
     * @param game 游戏实体
     * @param player 玩家实体
     * @param character 角色实体
     * @param isDm 是否为DM
     */
    private static void createGamePlayerRecord(GamePlayerService gamePlayerService,
                                               Game game,
                                               Player player,
                                               Character character,
                                               boolean isDm) {
        try {
            // 检查是否已存在相同的记录
            List<GamePlayer> existingRecords = gamePlayerService.getGamePlayersByGameId(game.getId());
            for (GamePlayer existing : existingRecords) {
                if (existing.getPlayer() != null && existing.getPlayer().getId().equals(player.getId())
                        && existing.getCharacter() != null && existing.getCharacter().getId().equals(character.getId())) {
                    log.info("游戏玩家关系已存在，跳过创建：游戏ID={}, 玩家ID={}, 角色ID={}", 
                            game.getId(), player.getId(), character.getId());
                    return;
                }
            }
            
            GamePlayer gamePlayer = new GamePlayer();
            gamePlayer.setGame(game);
            gamePlayer.setPlayer(player);
            gamePlayer.setCharacter(character);
            gamePlayer.setIsDm(isDm);
            gamePlayer.setStatus(GamePlayerStatus.PLAYING);
            
            gamePlayerService.createGamePlayer(gamePlayer);
            log.info("创建游戏玩家关系成功：游戏ID={}, 玩家ID={}, 角色ID={}, 角色名称={}", 
                    game.getId(), player.getId(), character.getId(), character.getName());
        } catch (Exception e) {
            log.error("创建游戏玩家关系失败：游戏ID={}, 玩家ID={}, 角色ID={}, 错误={}", 
                    game.getId(), player.getId(), character.getId(), e.getMessage(), e);
        }
    }

    /**
     * 更新WorkflowContext
     * @param context 工作流上下文
     * @param assignments 分配结果列表
     * @param savedDM DM玩家
     * @param savedJudge Judge玩家
     * @param realPlayerCount 真人玩家数量
     * @param aiPlayerCount AI玩家数量
     * @param totalRoles 总角色数
     */
    private static void updateContext(WorkflowContext context, List<Map<String, Object>> assignments, 
                                     Player savedDM, Player savedJudge, int realPlayerCount, 
                                     int aiPlayerCount, int totalRoles) {
        context.setCurrentStep("玩家分配");
        context.setPlayerAssignments(assignments);
        context.setDmId(savedDM.getId());
        context.setJudgeId(savedJudge.getId());
        context.setRealPlayerCount(realPlayerCount);
        context.setAiPlayerCount(aiPlayerCount);
        context.setTotalPlayerCount(totalRoles);
        context.setSuccess(true);

        Long gameId = context.getGameId();
        if (gameId != null) {
            try {
                GameService gameService = SpringContextUtil.getBean(GameService.class);
                var gameOpt = gameService.getGameById(gameId);
                if (gameOpt.isPresent()) {
                    Game game = gameOpt.get();
                    game.setRealPlayerCount(realPlayerCount);
                    gameService.updateGame(gameId, game);
                    log.info("[数据库] 已保存真人玩家数量 {} 到游戏 {}", realPlayerCount, gameId);
                }
            } catch (Exception e) {
                log.error("[数据库] 保存真人玩家数量失败: {}", e.getMessage(), e);
            }
        }

        log.info("玩家分配完成，共分配 {} 个角色", assignments.size());
        log.info("DM ID: {}", savedDM.getId());
        log.info("Judge ID: {}", savedJudge.getId());
        log.info("真人玩家数量: {}", realPlayerCount);
        log.info("AI玩家数量: {}", aiPlayerCount);
    }
}
