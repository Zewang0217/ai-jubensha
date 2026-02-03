package org.jubensha.aijubenshabackend.ai.workflow.node;


import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.service.AIService;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.memory.MemoryService;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
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
 * 
 * 注意：以下部分需要使用Milvus向量数据库实现：
 * 1. insertCharacterToVectorDB方法：调用MemoryService.storeCharacterMemory方法，
 *    将角色信息存储到Milvus向量数据库
 * 2. 存储全局时间线：调用MemoryService.storeGlobalTimelineMemory方法，
 *    将角色时间线存储到Milvus向量数据库
 */

@Slf4j
public class ScriptReaderNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.debug("ScriptReaderNode: {}", context);
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
                
                // 获取剧本的所有角色
                List<Character> characters = characterService.getCharactersByScriptId(scriptId);
                log.info("剧本 {} 共有 {} 个角色", scriptId, characters.size());
                
                // 通知玩家读取剧本
            for (Map<String, Object> assignment : playerAssignments) {
                String playerType = (String) assignment.get("playerType");
                Long playerId = (Long) assignment.get("playerId");
                Long characterId = (Long) assignment.get("characterId");
                String characterName = (String) assignment.get("characterName");
                
                if ("AI".equals(playerType)) {
                    // 为AI玩家将角色信息插入到向量数据库
                    insertCharacterToVectorDB(context.getGameId(), playerId, characterId, characters);
                    
                    // 通知AI玩家读取剧本
                    aiService.notifyAIPlayerReadScript(playerId, characterId);
                    log.info("通知AI玩家 {} 读取角色 {} 的剧本", playerId, characterName);
                } else {
                    // 通知真人玩家读取剧本
                    // 这里可以通过WebSocket或其他方式通知前端
                    log.info("通知真人玩家 {} 读取角色 {} 的剧本", playerId, characterName);
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
                // 准备角色信息
                Map<String, String> characterInfo = new HashMap<>();
                characterInfo.put("name", character.getName());
                characterInfo.put("description", character.getDescription());
                characterInfo.put("background", character.getBackgroundStory());
                characterInfo.put("secret", character.getSecret());
                characterInfo.put("timeline", character.getTimeline());
                
                // 获取记忆服务
                MemoryService memoryService = SpringContextUtil.getBean(MemoryService.class);
                
                // 存储角色记忆到向量数据库
                memoryService.storeCharacterMemory(gameId, playerId, characterId, characterInfo);
                
                // 存储全局时间线（如果有）
                if (character.getTimeline() != null && !character.getTimeline().isEmpty()) {
                    memoryService.storeGlobalTimelineMemory(character.getScriptId(), characterId, character.getTimeline(), "game_start");
                }
                
                log.info("为AI玩家 {} 插入角色 {} 的信息到向量数据库", playerId, character.getName());
                break;
            }
        }
    }
}