package org.jubensha.aijubenshabackend.ai.workflow.node;


import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.ai.service.AIService;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.service.character.CharacterService;

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
            
            // 获取剧本ID
            Long scriptId = context.getScriptId();
            if (scriptId == null) {
                log.error("剧本ID为空，无法分配玩家");
                context.setErrorMessage("剧本ID为空，无法分配玩家");
                return WorkflowContext.saveContext(context);
            }
            
            // 获取角色服务
            CharacterService characterService = SpringContextUtil.getBean(CharacterService.class);
            // 获取AI服务
            AIService aiService = SpringContextUtil.getBean(AIService.class);
            
            // 获取剧本的所有角色
            List<Character> characters = characterService.getCharactersByScriptId(scriptId);
            int totalRoles = characters.size();
            log.info("剧本 {} 共有 {} 个角色", scriptId, totalRoles);
            
            // TODO: 假设真人玩家数量为1，后续可从WorkflowContext获取
            int realPlayerCount = 1;
            log.info("真人玩家数量：{}", realPlayerCount);
            
            // 计算需要的AI玩家数量
            int aiPlayerCount = totalRoles - realPlayerCount;
            log.info("需要的AI玩家数量：{}", aiPlayerCount);
            
            // 创建AI玩家
            List<Player> aiPlayers = new ArrayList<>();
            for (int i = 0; i < aiPlayerCount; i++) {
                Player aiPlayer = aiService.createAIPlayer("AI_" + (i + 1));
                aiPlayers.add(aiPlayer);
                log.info("创建AI玩家：{}", aiPlayer.getNickname());
            }
            
            // 分配角色（简化版，实际应用中可根据玩家特点进行智能分配）
            List<Map<String, Object>> assignments = new ArrayList<>();
            
            // 分配真人玩家（假设第一个角色给真人玩家）
            // TODO: 可拓展部分：玩家选择角色
            if (totalRoles > 0) {
                Character realPlayerCharacter = characters.get(0);
                Map<String, Object> realPlayerAssignment = Map.of(
                    "playerType", "REAL",
                    "playerId", 1L, // TODO: 假设真人玩家ID为1，实际应从数据库获取
                    "characterId", realPlayerCharacter.getId(),
                    "characterName", realPlayerCharacter.getName()
                );
                assignments.add(realPlayerAssignment);
                log.info("分配真人玩家到角色：{}", realPlayerCharacter.getName());
            }
            
            // 分配AI玩家到剩余角色
            for (int i = 1; i < totalRoles && i - 1 < aiPlayers.size(); i++) {
                Character character = characters.get(i);
                Player aiPlayer = aiPlayers.get(i - 1);
                Map<String, Object> aiPlayerAssignment = Map.of(
                    "playerType", "AI",
                    "playerId", aiPlayer.getId(),
                    "characterId", character.getId(),
                    "characterName", character.getName()
                );
                assignments.add(aiPlayerAssignment);
                // 为AI玩家创建Agent
                aiService.createPlayerAgent(aiPlayer.getId(), character.getId());
                log.info("分配AI玩家 {} 到角色：{}", aiPlayer.getNickname(), character.getName());
            }
            
            // 创建DM
            Player savedDM = aiService.createDMAgent();
            log.info("创建DM：{}", savedDM.getNickname());
            
            // 创建Judge
            Player savedJudge = aiService.createJudgeAgent();
            log.info("创建Judge：{}", savedJudge.getNickname());
            
            // 更新WorkflowContext
            context.setCurrentStep("玩家分配");
            // 存储玩家分配结果
            context.setPlayerAssignments(assignments);
            context.setDmId(savedDM.getId());
            context.setJudgeId(savedJudge.getId());
            context.setRealPlayerCount(realPlayerCount);
            context.setAiPlayerCount(aiPlayerCount);
            context.setTotalPlayerCount(totalRoles);
            context.setSuccess(true);
            
            log.info("玩家分配完成，共分配 {} 个角色", assignments.size());
            log.info("DM ID: {}", savedDM.getId());
            log.info("Judge ID: {}", savedJudge.getId());
            log.info("真人玩家数量: {}", realPlayerCount);
            log.info("AI玩家数量: {}", aiPlayerCount);
            
            return WorkflowContext.saveContext(context);
        });
    }
}
