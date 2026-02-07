package org.jubensha.aijubenshabackend.ai.service;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.agent.DMAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.PlayerAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.JudgeAgent;
import org.jubensha.aijubenshabackend.ai.service.util.PromptUtils;
import org.jubensha.aijubenshabackend.ai.tools.ToolManager;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI服务类，用于管理各种Agent
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-01-31 15:30
 * @since 2026
 * <p>
 * 注意：以下部分需要使用Milvus向量数据库实现：
 * 1. notifyAIPlayerReadScript：通知AI玩家读取剧本时，
 * 会触发角色信息存储到Milvus向量数据库
 * 2. PlayerAgent的推理过程：在AI推理时，
 * 会使用Milvus向量数据库检索相关记忆
 */

@Configuration
@Slf4j
public class AIService {

    /**
     * Agent实例缓存
     * 缓存策略：
     * - 最大缓存 100 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, Object> agentCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("Agent实例被移除，缓存键: {}, 原因: {}", key, cause);
            })
            .build();
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;
    @Resource
    private ToolManager toolManager;
    @Resource
    private org.jubensha.aijubenshabackend.ai.tools.permission.DMAgentToolManager dmAgentToolManager;
    @Resource
    private org.jubensha.aijubenshabackend.ai.tools.permission.PlayerAgentToolManager playerAgentToolManager;
    @Resource
    private org.jubensha.aijubenshabackend.ai.tools.permission.JudgeAgentToolManager judgeAgentToolManager;
    @Resource
    private PlayerService playerService;

    /**
     * 创建AI玩家
     */
    public Player createAIPlayer(String name) {
        Player aiPlayer = new Player();
        aiPlayer.setNickname(name);
        aiPlayer.setUsername(name);
        aiPlayer.setEmail(name + "@example.com");
        aiPlayer.setPassword("123456"); // 实际应用中应使用加密密码
        aiPlayer.setStatus(org.jubensha.aijubenshabackend.models.enums.PlayerStatus.ONLINE);
        aiPlayer.setRole(org.jubensha.aijubenshabackend.models.enums.PlayerRole.USER);
        return playerService.createPlayer(aiPlayer);
    }

    /**
     * 创建DM Agent
     */
    public Player createDMAgent() {
        // 检查是否已存在DM玩家
        Optional<Player> existingDM = playerService.getPlayerByUsername("DM");
        if (existingDM.isPresent()) {
            Player dm = existingDM.get();
            log.info("使用已存在的DM玩家，ID: {}", dm.getId());
            
            // 确保DM Agent实例存在
            String cacheKey = "dm:" + dm.getId();
            agentCache.get(cacheKey, key -> createDMAgentInstance(dm.getId()));
            
            return dm;
        }
        
        // 创建新的DM玩家
        Player dm = new Player();
        dm.setNickname("DM");
        dm.setUsername("DM");
        dm.setEmail("DM@example.com");
        dm.setPassword("123456");
        dm.setStatus(org.jubensha.aijubenshabackend.models.enums.PlayerStatus.ONLINE);
        dm.setRole(org.jubensha.aijubenshabackend.models.enums.PlayerRole.DM);
        Player savedDM = playerService.createPlayer(dm);

        // 创建DM Agent实例
        String cacheKey = "dm:" + savedDM.getId();
        agentCache.get(cacheKey, key -> createDMAgentInstance(savedDM.getId()));

        log.info("创建DM Agent，ID: {}", savedDM.getId());
        return savedDM;
    }

    /**
     * 创建Judge Agent
     */
    public Player createJudgeAgent() {
        // 检查是否已存在Judge玩家
        Optional<Player> existingJudge = playerService.getPlayerByUsername("Judge");
        if (existingJudge.isPresent()) {
            Player judge = existingJudge.get();
            log.info("使用已存在的Judge玩家，ID: {}", judge.getId());
            
            // 确保Judge Agent实例存在
            String cacheKey = "judge:" + judge.getId();
            agentCache.get(cacheKey, key -> createJudgeAgentInstance(judge.getId()));
            
            return judge;
        }
        
        // 创建新的Judge玩家
        Player judge = new Player();
        judge.setNickname("Judge");
        judge.setUsername("Judge");
        judge.setEmail("Judge@example.com");
        judge.setPassword("123456");
        judge.setStatus(org.jubensha.aijubenshabackend.models.enums.PlayerStatus.ONLINE);
        judge.setRole(org.jubensha.aijubenshabackend.models.enums.PlayerRole.USER);
        Player savedJudge = playerService.createPlayer(judge);

        // 创建Judge Agent实例
        String cacheKey = "judge:" + savedJudge.getId();
        agentCache.get(cacheKey, key -> createJudgeAgentInstance(savedJudge.getId()));

        log.info("创建Judge Agent，ID: {}", savedJudge.getId());
        return savedJudge;
    }

    /**
     * 为AI玩家创建Agent
     */
    public void createPlayerAgent(Long playerId, Long characterId) {
        String cacheKey = "player:" + playerId;
        agentCache.get(cacheKey, key -> createPlayerAgentInstance(playerId, characterId));
        log.info("为AI玩家创建Agent，玩家ID: {}, 角色ID: {}", playerId, characterId);
    }

    /**
     * 创建DM Agent实例
     */
    private Object createDMAgentInstance(Long dmId) {
        log.info("创建新的DM Agent实例, DM ID：{}", dmId);
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(dmId)
                .maxMessages(20)
                .build();

        return AiServices.builder(DMAgent.class)
                .chatModel(chatModel)
                .chatMemory(chatMemory)
                .tools(dmAgentToolManager.getAvailableTools())
                .hallucinatedToolNameStrategy(toolExecutionRequest ->
                        ToolExecutionResultMessage.from(toolExecutionRequest,
                                "Error: there is no tool called" + toolExecutionRequest.name()))
                .maxSequentialToolsInvocations(20)
                .build();
    }

    /**
     * 创建Judge Agent实例
     */
    private Object createJudgeAgentInstance(Long judgeId) {
        log.info("创建新的Judge Agent实例, Judge ID：{}", judgeId);
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(judgeId)
                .maxMessages(20)
                .build();

        return AiServices.builder(JudgeAgent.class)
                .chatModel(chatModel)
                .chatMemory(chatMemory)
                .tools(judgeAgentToolManager.getAvailableTools())
                .hallucinatedToolNameStrategy(toolExecutionRequest ->
                        ToolExecutionResultMessage.from(toolExecutionRequest,
                                "Error: there is no tool called" + toolExecutionRequest.name()))
                .maxSequentialToolsInvocations(20)
                .build();
    }

    /**
     * 创建Player Agent实例
     */
    private Object createPlayerAgentInstance(Long playerId, Long characterId) {
        log.info("创建新的Player Agent实例, 玩家ID：{}, 角色ID：{}", playerId, characterId);
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(playerId)
                .maxMessages(50) // 增加内存容量，支持更长的对话历史
                .build();

        return AiServices.builder(PlayerAgent.class)
                .chatModel(chatModel)
                .chatMemory(chatMemory)
                .tools(playerAgentToolManager.getAvailableTools())
                .hallucinatedToolNameStrategy(toolExecutionRequest ->
                        ToolExecutionResultMessage.from(toolExecutionRequest,
                                "Error: there is no tool called " + toolExecutionRequest.name()))
                .maxSequentialToolsInvocations(30) // 增加最大工具调用次数
                .build();
    }

    /**
     * 获取DM Agent
     */
    public DMAgent getDMAgent(Long dmId) {
        String cacheKey = "dm:" + dmId;
        return (DMAgent) agentCache.getIfPresent(cacheKey);
    }

    /**
     * 获取Judge Agent
     */
    public JudgeAgent getJudgeAgent(Long judgeId) {
        String cacheKey = "judge:" + judgeId;
        return (JudgeAgent) agentCache.getIfPresent(cacheKey);
    }

    /**
     * 获取Player Agent
     */
    public PlayerAgent getPlayerAgent(Long playerId) {
        String cacheKey = "player:" + playerId;
        return (PlayerAgent) agentCache.getIfPresent(cacheKey);
    }

    /**
     * 批量获取Player Agent
     */
    public Map<Long, PlayerAgent> getPlayerAgents(List<Long> playerIds) {
        Map<Long, PlayerAgent> agents = new HashMap<>();
        for (Long playerId : playerIds) {
            PlayerAgent agent = getPlayerAgent(playerId);
            if (agent != null) {
                agents.put(playerId, agent);
            }
        }
        return agents;
    }

    /**
     * 预创建Player Agent
     */
    public void preCreatePlayerAgent(Long playerId, Long characterId) {
        createPlayerAgentInstance(playerId, characterId);
        log.info("预创建Player Agent实例，玩家ID: {}, 角色ID: {}", playerId, characterId);
    }

    /**
     * 通知AI玩家读取剧本
     */
    public void notifyAIPlayerReadScript(Long playerId, Long characterId) {
        // 通知AI玩家读取剧本
        // 这里可以通过消息队列或其他方式通知AI玩家
        log.info("通知AI玩家 {} 读取角色 {} 的剧本", playerId, characterId);

        // 获取Player Agent并发送读取剧本的指令
        PlayerAgent playerAgent = getPlayerAgent(playerId);
        if (playerAgent != null) {
            // 这里可以调用Player Agent的方法来读取剧本
            // 例如：playerAgent.readScript(characterId);
        }
    }

    /**
     * 通知AI玩家开始搜证
     */
    public void notifyAIPlayerStartInvestigation(Long playerId, List<Map<String, Object>> investigationScenes) {
        // 通知AI玩家开始搜证
        // 这里可以通过消息队列或其他方式通知AI玩家
        log.info("通知AI玩家 {} 开始第一轮搜证", playerId);

        // 获取Player Agent并发送开始搜证的指令
        PlayerAgent playerAgent = getPlayerAgent(playerId);
        if (playerAgent != null) {
            // 这里可以调用Player Agent的方法来开始搜证
            // 例如：playerAgent.startInvestigation(investigationScenes);
        }
    }
}
