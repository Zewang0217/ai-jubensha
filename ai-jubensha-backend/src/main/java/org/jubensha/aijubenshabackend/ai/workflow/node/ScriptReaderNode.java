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
import org.jubensha.aijubenshabackend.models.enums.GamePlayerStatus;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.jubensha.aijubenshabackend.service.game.GameService;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.jubensha.aijubenshabackend.websocket.service.WebSocketServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 剧本读取节点
 *
 * @author zewang
 * @version 1.0
 * @date 2026-02-01
 * @since 2026
 * <p>
 * 注意：以下部分需要使用Milvus向量数据库实现：
 * 1. insertCharacterToVectorDB方法：调用MemoryService.storeCharacterMemory方法，
 * 将角色信息存储到Milvus向量数据库
 * 2. 存储全局时间线：调用MemoryService.storeGlobalTimelineMemory方法，
 * 将角色时间线存储到Milvus向量数据库
 */

@Slf4j
public class ScriptReaderNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
//            log.debug("ScriptReaderNode: {}", context);
            log.info("执行节点：剧本读取");

            // 获取剧本ID
            Long scriptId = context.getScriptId();
            if (scriptId == null) {
                log.error("剧本ID为空，无法读取剧本");
                context.setErrorMessage("剧本ID为空，无法读取剧本");
                return WorkflowContext.saveContext(context);
            }

            // 获取玩家分配结果
            List<Map<String, Object>> playerAssignments = context.getPlayerAssignments();
            if (playerAssignments == null || playerAssignments.isEmpty()) {
                log.error("玩家分配结果为空，无法读取剧本");
                context.setErrorMessage("玩家分配结果为空，无法读取剧本");
                return WorkflowContext.saveContext(context);
            }

            try {
                // 获取角色服务
                CharacterService characterService = SpringContextUtil.getBean(CharacterService.class);
                // 获取AI服务
                AIService aiService = SpringContextUtil.getBean(AIService.class);
                // 获取WebSocket服务
                WebSocketServiceImpl webSocketService = SpringContextUtil.getBean(WebSocketServiceImpl.class);
                // 获取游戏服务
                GameService gameService = SpringContextUtil.getBean(GameService.class);
                // 获取玩家服务
                PlayerService playerService = SpringContextUtil.getBean(PlayerService.class);
                // 获取游戏玩家关系服务
                GamePlayerService gamePlayerService = SpringContextUtil.getBean(GamePlayerService.class);

                // 获取剧本的所有角色
                List<Character> characters = characterService.getCharactersByScriptId(scriptId);
                log.info("剧本 {} 共有 {} 个角色", scriptId, characters.size());

                // 确保游戏存在
                Long gameId = context.getGameId();
                
                // 检查gameId是否为null
                if (gameId == null) {
                    log.error("游戏ID为空，无法继续执行");
                    context.setErrorMessage("游戏ID为空，无法继续执行");
                    context.setSuccess(false);
                    return WorkflowContext.saveContext(context);
                }
                
                // 查询游戏是否存在
                var gameOpt = gameService.getGameById(gameId);
                
                if (!gameOpt.isPresent()) {
                    log.error("游戏不存在，游戏ID: {}", gameId);
                    context.setErrorMessage("游戏不存在，游戏ID: " + gameId);
                    context.setSuccess(false);
                    return WorkflowContext.saveContext(context);
                }
                
                log.info("游戏存在，游戏ID: {}", gameId);

                // 通知玩家读取剧本
                for (Map<String, Object> assignment : playerAssignments) {
                    String playerType = (String) assignment.get("playerType");
                    Long playerId = (Long) assignment.get("playerId");
                    Long characterId = (Long) assignment.get("characterId");
                    String characterName = (String) assignment.get("characterName");

                    // 创建GamePlayer关系
                    try {
                        // 检查玩家和角色是否存在
                        var playerOpt = playerService.getPlayerById(playerId);
                        var characterOpt = characterService.getCharacterById(characterId);
                        
                        if (playerOpt.isPresent() && characterOpt.isPresent()) {
                            // 使用已存在的游戏对象
                            Game game = gameOpt.get();
                            GamePlayer gamePlayer = new GamePlayer();
                            gamePlayer.setGame(game);
                            gamePlayer.setPlayer(playerOpt.get());
                            gamePlayer.setCharacter(characterOpt.get());
                            gamePlayer.setIsDm(false);
                            gamePlayer.setStatus(GamePlayerStatus.READY);
                            
                            gamePlayerService.createGamePlayer(gamePlayer);
                            log.info("创建游戏玩家关系成功：游戏ID={}, 玩家ID={}, 角色ID={}, 角色名称={}", 
                                    gameId, playerId, characterId, characterName);
                        } else {
                            log.warn("创建游戏玩家关系失败：必要实体不存在 - 玩家存在={}, 角色存在={}", 
                                    playerOpt.isPresent(), characterOpt.isPresent());
                        }
                    } catch (Exception e) {
                        log.warn("创建游戏玩家关系失败：{}", e.getMessage(), e);
                    }

                    if ("AI".equals(playerType)) {
                        // 为AI玩家将角色信息插入到向量数据库
                        insertCharacterToVectorDB(gameId, playerId, characterId, characters);

                        // 通知AI玩家读取剧本
                        aiService.notifyAIPlayerReadScript(playerId, characterId);
                        log.info("通知AI玩家 {} 读取角色 {} 的剧本", playerId, characterName);
                    } else {
                        // 通知真人玩家读取剧本
                        try {
//                            webSocketService.notifyPlayerReadScript(playerId, characterId);
                            log.info("通过WebSocket通知真人玩家 {} 读取角色 {} 的剧本", playerId, characterName);
                        } catch (Exception e) {
                            log.warn("WebSocket通知失败，使用日志记录", e);
                            log.info("通知真人玩家 {} 读取角色 {} 的剧本", playerId, characterName);
                        }
                    }
                }

                // 更新WorkflowContext
                context.setCurrentStep("剧本读取");
                context.setCharacterCount(characters.size());

                // ====== 阶段同步等待机制 ======
                // 区分真人玩家和AI玩家
                List<Long> realPlayerIds = new ArrayList<>();
                List<Long> aiPlayerIds = new ArrayList<>();
                for (Map<String, Object> assignment : playerAssignments) {
                    String playerType = (String) assignment.get("playerType");
                    Long playerId = (Long) assignment.get("playerId");
                    if ("REAL".equals(playerType)) {
                        realPlayerIds.add(playerId);
                    } else {
                        aiPlayerIds.add(playerId);
                    }
                }
                
                log.info("[阶段同步] 玩家分布 - 真人玩家: {}, AI玩家: {}", realPlayerIds.size(), aiPlayerIds.size());
                
                // 初始化阶段确认状态（只针对真人玩家）
                context.initPhaseConfirmations(realPlayerIds);
                
                // 设置节点状态为未就绪，等待前端确认
                context.setCurrentNodeReady(false);
                context.setWaitingMessage("等待玩家阅读剧本");
                
                // 更新游戏的工作流节点状态
                if (context.getGameId() != null) {
                    Game game = gameOpt.get();
                    game.setWorkflowNode("script_reader");
                    game.setNodeReady(false);
                    gameService.updateGame(context.getGameId(), game);
                }
                
                // 通过 WebSocket 推送阶段就绪通知
                webSocketService.broadcastPhaseReady(context.getGameId(), "script_reader", false, "等待玩家阅读剧本");
                
                // 根据玩家类型决定等待策略
                if (realPlayerIds.isEmpty()) {
                    // 全AI玩家模式：等待观察者确认即可
                    log.info("[阶段同步] 全AI玩家模式，等待观察者确认");
                    
                    int waitCount = 0;
                    int maxWaitTime = 1800; // 最大等待时间30分钟
                    int checkInterval = 3; // 检查间隔3秒
                    
                    // 等待观察者确认（通过 observerConfirmed 标志）
                    while (!context.isObserverConfirmed() && waitCount < maxWaitTime) {
                        Thread.sleep(checkInterval * 1000);
                        waitCount += checkInterval;
                        
                        // 每30秒输出一次等待日志
                        if (waitCount % 30 == 0) {
                            log.info("[阶段同步] 等待观察者确认剧本阅读，已等待: {}秒", waitCount);
                        }
                    }
                    
                    if (context.isObserverConfirmed()) {
                        log.info("[阶段同步] 观察者已确认剧本阅读，继续工作流");
                    } else {
                        log.warn("[阶段同步] 等待观察者确认超时，强制继续工作流");
                    }
                } else {
                    // 有真人玩家模式：等待真人玩家确认
                    log.info("[阶段同步] 有真人玩家模式，等待真人玩家确认，玩家数量: {}", realPlayerIds.size());
                    
                    int waitCount = 0;
                    int maxWaitTime = 1800; // 最大等待时间30分钟
                    int checkInterval = 3; // 检查间隔3秒
                    
                    while (!context.isAllPlayersConfirmed() && waitCount < maxWaitTime) {
                        Thread.sleep(checkInterval * 1000);
                        waitCount += checkInterval;
                        
                        // 每30秒输出一次等待日志
                        if (waitCount % 30 == 0) {
                            log.info("[阶段同步] 等待真人玩家确认剧本阅读，已等待: {}秒，未确认玩家: {}",
                                    waitCount, context.getUnconfirmedPlayers());
                        }
                    }
                    
                    if (context.isAllPlayersConfirmed()) {
                        log.info("[阶段同步] 所有真人玩家已确认剧本阅读，继续工作流");
                    } else {
                        log.warn("[阶段同步] 等待超时，强制继续工作流");
                    }
                }
                
                context.setCurrentNodeReady(true);
                context.setWaitingMessage(null);
                
                // 更新游戏状态
                if (context.getGameId() != null) {
                    Game game = gameOpt.get();
                    game.setNodeReady(true);
                    gameService.updateGame(context.getGameId(), game);
                }
                
                // 通知前端节点已完成
                webSocketService.broadcastPhaseReady(context.getGameId(), "script_reader", true, "剧本阅读完成");

                context.setSuccess(true);

                log.info("剧本读取完成，共 {} 个角色", characters.size());

            } catch (Exception e) {
                log.error("读取剧本失败: {}", e.getMessage(), e);
                context.setErrorMessage("读取剧本失败: " + e.getMessage());
                context.setSuccess(false);
            }

            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 为AI玩家处理角色信息
     * 注意：暂时不向向量数据库存储任何信息，包括时间线和线索
     * 时间线将通过提示词提供，线索只在搜证环节存储
     */
    private static void insertCharacterToVectorDB(Long gameId, Long playerId, Long characterId, List<Character> characters) {
        // 查找对应的角色
        for (Character character : characters) {
            if (character.getId().equals(characterId)) {
                // 暂时不向向量数据库存储任何信息
                // 时间线将通过提示词提供，线索只在搜证环节存储
                log.info("[暂时不存储] 为AI玩家 {} 处理角色 {} 的信息", playerId, character.getName());
                log.info("[暂时不存储] 时间线长度: {}, 秘密长度: {}", 
                        character.getTimeline() != null ? character.getTimeline().length() : 0, 
                        character.getSecret() != null ? character.getSecret().length() : 0);
                break;
            }
        }
    }
}