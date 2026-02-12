package org.jubensha.aijubenshabackend.ai.workflow.node;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.service.AIService;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.jubensha.aijubenshabackend.service.game.GameService;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.jubensha.aijubenshabackend.models.entity.Game;
import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.jubensha.aijubenshabackend.models.enums.GamePlayerStatus;
import org.jubensha.aijubenshabackend.models.enums.GameStatus;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.websocket.service.WebSocketService;
import java.time.LocalDateTime;

import java.util.HashMap;
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
                WebSocketService webSocketService = SpringContextUtil.getBean(WebSocketService.class);
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
     * 为AI玩家将角色信息插入到向量数据库
     */
    private static void insertCharacterToVectorDB(Long gameId, Long playerId, Long characterId, List<Character> characters) {
        // 查找对应的角色
        for (Character character : characters) {
            if (character.getId().equals(characterId)) {
                // 获取RAG服务
                RAGService ragService = SpringContextUtil.getBean(RAGService.class);

                // 只存储全局时间线，不存储角色信息到对话记忆
                // 对话记忆是全局的，每个角色都可以查看，会导致角色信息泄露
                if (character.getTimeline() != null && !character.getTimeline().isEmpty()) {
                    // 存储全局时间线
                    ragService.insertGlobalTimelineMemory(character.getScriptId(), characterId, character.getTimeline(), "game_start");
                }

                // 存储角色相关的线索到全局线索记忆
                if (character.getSecret() != null && !character.getSecret().isEmpty()) {
                    ragService.insertGlobalClueMemory(character.getScriptId(), characterId, character.getSecret());
                }

                log.info("为AI玩家 {} 插入角色 {} 的时间线和线索到全局记忆", playerId, character.getName());
                break;
            }
        }
    }
}