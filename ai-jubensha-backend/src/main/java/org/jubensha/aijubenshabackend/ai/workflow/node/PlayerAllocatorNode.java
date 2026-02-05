package org.jubensha.aijubenshabackend.ai.workflow.node;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.service.AIService;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;
import org.jubensha.aijubenshabackend.models.enums.PlayerStatus;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
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

            // 获取服务实例
            CharacterService characterService = SpringContextUtil.getBean(CharacterService.class);
            AIService aiService = SpringContextUtil.getBean(AIService.class);
            PlayerService playerService = SpringContextUtil.getBean(PlayerService.class);

            // 获取剧本的所有角色
            List<Character> characters = characterService.getCharactersByScriptId(scriptId);
            int totalRoles = characters.size();
            log.info("剧本 {} 共有 {} 个角色", scriptId, totalRoles);

            // 确定真人玩家数量
            int realPlayerCount = getRealPlayerCount(context);
            log.info("真人玩家数量：{}", realPlayerCount);

            // 计算需要的AI玩家数量
            int aiPlayerCount = Math.max(0, totalRoles - realPlayerCount);
            log.info("需要的AI玩家数量：{}", aiPlayerCount);

            // 创建AI玩家
            List<Player> aiPlayers = createAIPlayers(aiService, aiPlayerCount);

            // 分配角色
            List<Map<String, Object>> assignments = new ArrayList<>();
            assignRealPlayers(playerService, characters, realPlayerCount, assignments);
            assignAIPlayers(aiService, characters, realPlayerCount, aiPlayers, assignments);

            // 创建DM和Judge
            Player savedDM = aiService.createDMAgent();
            Player savedJudge = aiService.createJudgeAgent();
            log.info("创建DM：{}", savedDM.getNickname());
            log.info("创建Judge：{}", savedJudge.getNickname());

            // 更新WorkflowContext
            updateContext(context, assignments, savedDM, savedJudge, realPlayerCount, aiPlayerCount, totalRoles);

            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 获取真人玩家数量
     */
    private static int getRealPlayerCount(WorkflowContext context) {
        Integer realPlayerCount = context.getRealPlayerCount();
        if (realPlayerCount == null || realPlayerCount <= 0) {
            realPlayerCount = 1;
            log.info("WorkflowContext中未设置真人玩家数量，默认设置为1");
        }
        return realPlayerCount;
    }

    /**
     * 创建AI玩家
     */
    private static List<Player> createAIPlayers(AIService aiService, int count) {
        List<Player> aiPlayers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Player aiPlayer = aiService.createAIPlayer("AI_" + (i + 1));
            aiPlayers.add(aiPlayer);
            log.info("创建AI玩家：{}", aiPlayer.getNickname());
        }
        return aiPlayers;
    }

    /**
     * 分配真人玩家
     */
    private static void assignRealPlayers(PlayerService playerService, List<Character> characters, 
                                         int realPlayerCount, List<Map<String, Object>> assignments) {
        // 获取所有真人玩家
        List<Player> realPlayers = getRealPlayers(playerService);
        
        // 分配真人玩家到角色
        for (int i = 0; i < realPlayerCount && i < characters.size(); i++) {
            Character character = characters.get(i);
            Player realPlayer = getOrCreateRealPlayer(playerService, realPlayers, i);
            
            Map<String, Object> assignment = Map.of(
                    "playerType", "REAL",
                    "playerId", realPlayer.getId(),
                    "characterId", character.getId(),
                    "characterName", character.getName()
            );
            assignments.add(assignment);
            log.info("分配真人玩家 {} 到角色：{}", realPlayer.getNickname(), character.getName());
        }
    }

    /**
     * 获取所有真人玩家
     */
    private static List<Player> getRealPlayers(PlayerService playerService) {
        try {
            return playerService.getPlayersByRole("REAL");
        } catch (Exception e) {
            log.warn("获取真人玩家失败，返回空列表", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取或创建真人玩家
     */
    private static Player getOrCreateRealPlayer(PlayerService playerService, List<Player> realPlayers, int index) {
        // 尝试从现有真人玩家中获取
        if (realPlayers != null && !realPlayers.isEmpty() && index < realPlayers.size()) {
            Player player = realPlayers.get(index);
            log.info("从数据库获取真人玩家：{}", player.getNickname());
            return player;
        }
        
        // 创建新的真人玩家
        try {
            Player newPlayer = createRealPlayer(playerService, "REAL_PLAYER_" + (index + 1));
            log.info("创建真人玩家：{}", newPlayer.getNickname());
            return newPlayer;
        } catch (Exception e) {
            // 如果创建失败，使用默认玩家
            log.warn("创建真人玩家失败，使用默认玩家", e);
            Player defaultPlayer = new Player();
            defaultPlayer.setId((long) (index + 1));
            defaultPlayer.setNickname("REAL_PLAYER_" + (index + 1));
            defaultPlayer.setRole(PlayerRole.valueOf("REAL"));
            return defaultPlayer;
        }
    }

    /**
     * 创建真人玩家
     */
    private static Player createRealPlayer(PlayerService playerService, String nickname) {
        Player player = new Player();
        player.setUsername(nickname.toLowerCase());
        player.setNickname(nickname);
        player.setPassword("123456"); // 默认密码
        player.setEmail(nickname + "@example.com");
        player.setRole(PlayerRole.valueOf("REAL"));
        player.setStatus(PlayerStatus.ONLINE);
        return playerService.createPlayer(player);
    }

    /**
     * 分配AI玩家
     */
    private static void assignAIPlayers(AIService aiService, List<Character> characters, 
                                       int realPlayerCount, List<Player> aiPlayers, 
                                       List<Map<String, Object>> assignments) {
        for (int i = realPlayerCount; i < characters.size() && i - realPlayerCount < aiPlayers.size(); i++) {
            Character character = characters.get(i);
            Player aiPlayer = aiPlayers.get(i - realPlayerCount);
            
            Map<String, Object> assignment = Map.of(
                    "playerType", "AI",
                    "playerId", aiPlayer.getId(),
                    "characterId", character.getId(),
                    "characterName", character.getName()
            );
            assignments.add(assignment);
            
            // 为AI玩家创建Agent
            aiService.createPlayerAgent(aiPlayer.getId(), character.getId());
            log.info("分配AI玩家 {} 到角色：{}", aiPlayer.getNickname(), character.getName());
        }
    }

    /**
     * 更新WorkflowContext
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

        log.info("玩家分配完成，共分配 {} 个角色", assignments.size());
        log.info("DM ID: {}", savedDM.getId());
        log.info("Judge ID: {}", savedJudge.getId());
        log.info("真人玩家数量: {}", realPlayerCount);
        log.info("AI玩家数量: {}", aiPlayerCount);
    }
}
