package org.jubensha.aijubenshabackend.ai.service;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.agent.DMAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.PlayerAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.JudgeAgent;
import org.jubensha.aijubenshabackend.ai.service.util.PromptUtils;
import org.jubensha.aijubenshabackend.ai.tools.ToolManager;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
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
//    @Resource
//    private ToolManager toolManager;
    @Resource
    private org.jubensha.aijubenshabackend.ai.tools.permission.DMAgentToolManager dmAgentToolManager;
    @Resource
    private org.jubensha.aijubenshabackend.ai.tools.permission.PlayerAgentToolManager playerAgentToolManager;
    @Resource
    private org.jubensha.aijubenshabackend.ai.tools.permission.JudgeAgentToolManager judgeAgentToolManager;
    @Resource
    private PlayerService playerService;
    @Resource
    private CharacterService characterService;

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
        aiPlayer.setRole(org.jubensha.aijubenshabackend.models.enums.PlayerRole.AI);
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

        return AiServices.builder(DMAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
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

        return AiServices.builder(JudgeAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
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

        // 获取角色信息，包括backgroundStory和secret
        org.jubensha.aijubenshabackend.service.character.CharacterService characterService = SpringContextUtil.getBean(org.jubensha.aijubenshabackend.service.character.CharacterService.class);
        org.jubensha.aijubenshabackend.models.entity.Character character = null;
        try {
            Optional<Character> characterOptional = characterService.getCharacterById(characterId);
            character = characterOptional.orElse(null);
            if (character != null) {
                log.info("获取到角色信息: {}, 背景故事长度: {}, 秘密长度: {}", 
                        character.getName(), 
                        character.getBackgroundStory() != null ? character.getBackgroundStory().length() : 0, 
                        character.getSecret() != null ? character.getSecret().length() : 0);
            }
        } catch (Exception e) {
            log.error("获取角色信息失败: {}", e.getMessage(), e);
        }

        // 构建包含角色信息的系统消息
        final org.jubensha.aijubenshabackend.models.entity.Character finalCharacter = character;
        String systemMessage = buildPlayerAgentSystemMessage(finalCharacter, playerId);

        return AiServices.builder(PlayerAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .tools(playerAgentToolManager.getAvailableTools())
                .systemMessageProvider(memoryId -> systemMessage)
                .hallucinatedToolNameStrategy(toolExecutionRequest ->
                        ToolExecutionResultMessage.from(toolExecutionRequest,
                                "Error: there is no tool called " + toolExecutionRequest.name()))
                .maxSequentialToolsInvocations(20) // 增加最大工具调用次数
                .build();
    }

    /**
     * 构建Player Agent的系统消息，包含角色信息
     * @param character 角色信息
     * @param playerId 玩家ID
     * @return 系统消息
     */
    private String buildPlayerAgentSystemMessage(org.jubensha.aijubenshabackend.models.entity.Character character, Long playerId) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("你是一个剧本杀游戏中的AI玩家。\n\n");
        
        // 核心身份信息 - 这是最重要的
        if (character != null) {
            sb.append("【你的身份】\n");
            sb.append("你的角色名称是：").append(character.getName()).append("\n");
            sb.append("你必须是").append(character.getName()).append("这个角色，不能是其他任何人。\n");
            sb.append("在所有发言中，你都必须以").append(character.getName()).append("的身份说话。\n\n");
            
            sb.append("【角色背景】\n");
            if (character.getBackgroundStory() != null && !character.getBackgroundStory().isEmpty()) {
                sb.append(character.getBackgroundStory()).append("\n\n");
            } else {
                sb.append("暂无背景故事\n\n");
            }
            
            sb.append("【角色秘密】\n");
            if (character.getSecret() != null && !character.getSecret().isEmpty()) {
                sb.append(character.getSecret()).append("\n\n");
            } else {
                sb.append("暂无秘密信息\n\n");
            }
            
            sb.append("【角色时间线】\n");
            if (character.getTimeline() != null && !character.getTimeline().isEmpty()) {
                sb.append(character.getTimeline()).append("\n\n");
            } else {
                sb.append("暂无时间线信息\n\n");
            }
        }
        
        sb.append("【重要规则】\n");
        sb.append("1. 你必须始终保持角色一致性，你是").append(character != null ? character.getName() : "某个角色").append("\n");
        sb.append("2. 不要说\"我是AI\"、\"我是AI助手\"等话，你是剧本杀中的角色\n");
        sb.append("3. 不要虚构你没有的信息，使用工具获取真实信息\n");
        sb.append("4. 在发言历史中看到自己角色的发言时，那是你之前说的话，要保持连贯性\n");
        sb.append("5. 工具使用优先：在进行推理和决策前，务必通过调用工具获取相关信息\n");
        
        return sb.toString();
    }

    /**
     * 创建无工具的Player Agent实例，用于答题阶段
     */
    private Object createPlayerAgentInstanceWithoutTools(Long playerId, Long characterId) {
        log.info("创建新的无工具Player Agent实例, 玩家ID：{}, 角色ID：{}", playerId, characterId);

        // 获取角色信息，包括backgroundStory和secret
        org.jubensha.aijubenshabackend.service.character.CharacterService characterService = SpringContextUtil.getBean(org.jubensha.aijubenshabackend.service.character.CharacterService.class);
        org.jubensha.aijubenshabackend.models.entity.Character character = null;
        try {
            Optional<Character> characterOptional = characterService.getCharacterById(characterId);
            character = characterOptional.orElse(null);
            if (character != null) {
                log.info("获取到角色信息: {}, 背景故事长度: {}, 秘密长度: {}", 
                        character.getName(), 
                        character.getBackgroundStory() != null ? character.getBackgroundStory().length() : 0, 
                        character.getSecret() != null ? character.getSecret().length() : 0);
            }
        } catch (Exception e) {
            log.error("获取角色信息失败: {}", e.getMessage(), e);
        }

        // 构建包含角色信息的系统消息
        final org.jubensha.aijubenshabackend.models.entity.Character finalCharacter = character;
        String systemMessage = buildPlayerAgentSystemMessage(finalCharacter, playerId);

        return AiServices.builder(PlayerAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .systemMessageProvider(memoryId -> systemMessage)
                // 不添加任何工具，避免在答题阶段调用工具
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
     * 如果Agent不存在，尝试从数据库重新创建
     */
    public PlayerAgent getPlayerAgent(Long playerId) {
        String cacheKey = "player:" + playerId;
        PlayerAgent agent = (PlayerAgent) agentCache.getIfPresent(cacheKey);

        if (agent == null) {
            log.warn("Player Agent不存在，尝试重新创建，玩家ID: {}", playerId);

            // 尝试从数据库获取角色信息并重新创建Agent
            try {
                Optional<Player> playerOpt = playerService.getPlayerById(playerId);
                if (playerOpt.isPresent()) {
                    Player player = playerOpt.get();
                    // 查找该玩家关联的游戏和角色
                    org.jubensha.aijubenshabackend.service.game.GamePlayerService gamePlayerService =
                            SpringContextUtil.getBean(org.jubensha.aijubenshabackend.service.game.GamePlayerService.class);

                    // 这里需要gameId，但getPlayerAgent方法没有传入，所以先返回null
                    // 实际重建应该在调用方处理
                    log.warn("无法自动重建Player Agent，缺少gameId信息，玩家ID: {}", playerId);
                }
            } catch (Exception e) {
                log.error("尝试重建Player Agent失败，玩家ID: {}", playerId, e);
            }
        }

        return agent;
    }

    /**
     * 获取或创建Player Agent
     * 如果Agent不存在，使用提供的gameId和characterId重新创建
     */
    public PlayerAgent getOrCreatePlayerAgent(Long playerId, Long gameId, Long characterId) {
        String cacheKey = "player:" + playerId;
        PlayerAgent agent = (PlayerAgent) agentCache.getIfPresent(cacheKey);

        if (agent == null) {
            log.info("Player Agent不存在，重新创建，玩家ID: {}, 游戏ID: {}, 角色ID: {}", playerId, gameId, characterId);

            try {
                // 重新创建Agent
                createPlayerAgent(playerId, characterId);
                agent = (PlayerAgent) agentCache.getIfPresent(cacheKey);

                if (agent != null) {
                    log.info("Player Agent重新创建成功，玩家ID: {}", playerId);
                } else {
                    log.error("Player Agent重新创建失败，玩家ID: {}", playerId);
                }
            } catch (Exception e) {
                log.error("重新创建Player Agent失败，玩家ID: {}", playerId, e);
            }
        }

        return agent;
    }

    /**
     * 获取无工具的Player Agent，用于答题阶段
     */
    public PlayerAgent getPlayerAgentWithoutTools(Long playerId, Long characterId) {
        String cacheKey = "player:no-tools:" + playerId;
        return (PlayerAgent) agentCache.get(cacheKey, key -> createPlayerAgentInstanceWithoutTools(playerId, characterId));
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

        // 获取角色信息，包括backgroundStory和secret
        org.jubensha.aijubenshabackend.service.character.CharacterService characterService = SpringContextUtil.getBean(org.jubensha.aijubenshabackend.service.character.CharacterService.class);
        org.jubensha.aijubenshabackend.models.entity.Character character = null;
        try {
            Optional<Character> characterOptional = characterService.getCharacterById(characterId);
            character = characterOptional.orElse(null);
            if (character != null) {
                log.info("获取到角色信息: {}, 背景故事长度: {}, 秘密长度: {}", 
                        character.getName(), 
                        character.getBackgroundStory() != null ? character.getBackgroundStory().length() : 0, 
                        character.getSecret() != null ? character.getSecret().length() : 0);
            }
        } catch (Exception e) {
            log.error("获取角色信息失败: {}", e.getMessage(), e);
        }

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

    /**
     * 让AI玩家参与讨论，传递角色信息
     */
    public String discussWithCharacterInfo(Long gameId, Long playerId, Long characterId, String topic) {
        log.info("让AI玩家 {} 参与讨论，话题：{}", playerId, topic);

        // 获取角色信息，包括backgroundStory和secret
        org.jubensha.aijubenshabackend.service.character.CharacterService characterService = SpringContextUtil.getBean(org.jubensha.aijubenshabackend.service.character.CharacterService.class);
        org.jubensha.aijubenshabackend.models.entity.Character character = null;
        try {
            Optional<Character> characterOptional = characterService.getCharacterById(characterId);
            character = characterOptional.orElse(null);
            if (character != null) {
                log.info("获取到角色信息: {}, 背景故事长度: {}, 秘密长度: {}", 
                        character.getName(), 
                        character.getBackgroundStory() != null ? character.getBackgroundStory().length() : 0, 
                        character.getSecret() != null ? character.getSecret().length() : 0);
            }
        } catch (Exception e) {
            log.error("获取角色信息失败: {}", e.getMessage(), e);
        }

        // 获取Player Agent并参与讨论
        PlayerAgent playerAgent = getPlayerAgent(playerId);
        if (playerAgent != null && character != null) {
            try {
                // 调用Player Agent的discussWithCharacterInfo方法，传递角色信息
                return playerAgent.discussWithCharacterInfo(
                        gameId.toString(),
                        playerId.toString(),
                        topic,
                        character.getName(),
                        character.getBackgroundStory() != null ? character.getBackgroundStory() : "",
                        character.getSecret() != null ? character.getSecret() : "",
                        character.getTimeline() != null ? character.getTimeline() : ""
                );
            } catch (Exception e) {
                log.error("AI玩家参与讨论失败: {}", e.getMessage(), e);
            }
        }
        return "";
    }
}
