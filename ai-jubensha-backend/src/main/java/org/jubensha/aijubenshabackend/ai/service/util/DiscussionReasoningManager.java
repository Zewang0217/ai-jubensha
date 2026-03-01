package org.jubensha.aijubenshabackend.ai.service.util;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.AIService;
import org.jubensha.aijubenshabackend.ai.service.MemoryHierarchyService;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.jubensha.aijubenshabackend.ai.service.agent.PlayerAgent;
import org.jubensha.aijubenshabackend.ai.tools.GetDiscussionHistoryTool;
import org.jubensha.aijubenshabackend.ai.tools.GetPlayerStatusTool;
import org.jubensha.aijubenshabackend.ai.service.util.MessageAccumulator;
import org.jubensha.aijubenshabackend.ai.service.util.TurnManager;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 讨论推理管理器
 * 管理AI的推理过程，协调工具调用和响应生成
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */

@Slf4j
@Component
public class DiscussionReasoningManager {

    @Autowired
    private AIService aiService;

    @Autowired
    private GetDiscussionHistoryTool getDiscussionHistoryTool;

    @Autowired
    private GetPlayerStatusTool getPlayerStatusTool;

    @Autowired
    private TurnManager turnManager;

    @Autowired
    private MessageAccumulator messageAccumulator;

    @Autowired
    private MemoryHierarchyService memoryHierarchyService;

    @Autowired
    private RAGService ragService;

    @Autowired
    private GamePlayerService gamePlayerService;

    @Autowired
    private CharacterService characterService;

    @Autowired
    private ScrollingSummaryManager scrollingSummaryManager;

    /**
     * 线程池，用于并行处理推理任务
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 推理结果缓存
     * 缓存策略：
     * - 最大缓存 1000 个结果
     * - 写入后 5 分钟过期
     * - 访问后 1 分钟过期
     */
    private final Cache<String, String> reasoningCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .expireAfterAccess(Duration.ofMinutes(1))
            .build();

    /**
     * 讨论历史缓存
     * 缓存策略：
     * - 最大缓存 500 个结果
     * - 写入后 2 分钟过期
     * - 访问后 30 秒过期
     */
    private final Cache<String, String> discussionHistoryCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(Duration.ofMinutes(2))
            .expireAfterAccess(Duration.ofSeconds(30))
            .build();

    /**
     * 滑动窗口大小，默认15条
     */
    private static final int SLIDING_WINDOW_SIZE = 15;

    /**
     * 处理AI玩家的推理和讨论
     * 采用工具驱动的推理方案，只传递基本上下文
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @return 推理结果和讨论内容
     */
    public String processReasoningAndDiscussion(Long gameId, Long playerId) {
        try {
            // 生成缓存键
            String cacheKey = "reasoning:" + gameId + ":" + playerId + ":" + System.currentTimeMillis() / 10000; // 每10秒更新一次缓存

            // 尝试从缓存获取结果
            String cachedResult = reasoningCache.getIfPresent(cacheKey);
            if (cachedResult != null) {
                log.debug("从缓存获取推理结果，游戏ID: {}, 玩家ID: {}", gameId, playerId);
                return cachedResult;
            }

            log.info("开始处理AI玩家推理，游戏ID: {}, 玩家ID: {}", gameId, playerId);

            // 获取当前讨论阶段
            String currentPhase = turnManager.getCurrentPhase(gameId);

            // 获取Player Agent
            PlayerAgent playerAgent = aiService.getPlayerAgent(playerId);
            if (playerAgent == null) {
                log.error("Player Agent不存在，玩家ID: {}", playerId);
                return "无法获取AI玩家实例";
            }

            // 获取剧本ID
            Long scriptId = null;
            try {
                var gamePlayerOpt = gamePlayerService.getGamePlayerByGameIdAndPlayerId(gameId, playerId);
                if (gamePlayerOpt.isPresent()) {
                    var gamePlayer = gamePlayerOpt.get();
                    var character = gamePlayer.getCharacter();
                    if (character != null) {
                        scriptId = character.getScriptId();
                        log.debug("获取到剧本ID: {}，通过玩家 {} 的角色 {}", scriptId, playerId, character.getName());
                    }
                }
            } catch (Exception e) {
                log.error("获取剧本ID失败: {}", e.getMessage(), e);
            }

            // 查询公开线索
            String publicClues = "";
            if (scriptId != null) {
                try {
                    var clues = ragService.getPublicClues(scriptId, 20);
                    if (!clues.isEmpty()) {
                        StringBuilder cluesBuilder = new StringBuilder();
                        cluesBuilder.append("\n=== 公开线索 ===\n");
                        for (var clue : clues) {
                            String content = (String) clue.getOrDefault("content", "");
                            if (!content.isEmpty()) {
                                cluesBuilder.append(content).append("\n");
                            }
                        }
                        cluesBuilder.append("=== 公开线索结束 ===\n");
                        publicClues = cluesBuilder.toString();
                        log.debug("获取到 {} 条公开线索", clues.size());
                    }
                } catch (Exception e) {
                    log.error("查询公开线索失败: {}", e.getMessage(), e);
                }
            }

            // 获取剧情快照（长期记忆）
            String plotSnapshot = scrollingSummaryManager.getPlotSnapshot(gameId);
            
            // 获取近期讨论（滑动窗口）
            String recentDiscussion = getRecentDiscussion(gameId);

            // 构建完整的提示词，添加最终指令
            String prompt = "游戏ID：" + gameId + "\n" +
                           "玩家ID：" + playerId + "\n" +
                           currentPhase + publicClues + plotSnapshot + recentDiscussion + "\n\n" +
                           "请根据以上上下文，结合你的剧本进行发言：\n" +
                           "如果觉得信息不够，可以调用工具获取更多信息。\n" +
                           "请直接开始你的发言，不需要任何开场白。";

            // 调用推理方法生成讨论内容（传递完整的提示词）
            String result = playerAgent.reasonAndDiscuss(
                    gameId.toString(),
                    playerId.toString(),
                    prompt
            );

            // 缓存推理结果
            reasoningCache.put(cacheKey, result);

            log.info("AI玩家推理完成，游戏ID: {}, 玩家ID: {}", gameId, playerId);
            return result;

        } catch (Exception e) {
            log.error("处理AI玩家推理失败: {}", e.getMessage(), e);
            return "推理过程中出现错误";
        }
    }

    /**
     * 从记忆结果构建讨论历史文本
     *
     * @param memoryResults 记忆结果
     * @return 讨论历史文本
     */
    private String buildDiscussionHistoryFromMemory(List<Map<String, Object>> memoryResults) {
        StringBuilder historyBuilder = new StringBuilder();

        for (Map<String, Object> memory : memoryResults) {
            String content = (String) memory.getOrDefault("content", "");
            String playerName = (String) memory.getOrDefault("player_name", "Unknown");
            Object timestampObj = memory.getOrDefault("timestamp", System.currentTimeMillis());
            long timestamp = timestampObj instanceof Number ? ((Number) timestampObj).longValue() : System.currentTimeMillis();

            if (!content.isEmpty()) {
                historyBuilder.append(String.format("[%s] %s: %s\n", 
                        new java.util.Date(timestamp), playerName, content));
            }
        }

        return historyBuilder.toString();
    }

    /**
     * 分析特定讨论话题
     * 采用工具驱动的推理方案
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @param topic    讨论话题
     * @return 分析结果
     */
    public String analyzeDiscussionTopic(Long gameId, Long playerId, String topic) {
        try {
            // 生成缓存键
            String cacheKey = "analyze:" + gameId + ":" + playerId + ":" + topic.hashCode();

            // 尝试从缓存获取结果
            String cachedResult = reasoningCache.getIfPresent(cacheKey);
            if (cachedResult != null) {
                log.debug("从缓存获取话题分析结果，游戏ID: {}, 玩家ID: {}, 话题: {}", gameId, playerId, topic);
                return cachedResult;
            }

            log.info("分析讨论话题，游戏ID: {}, 玩家ID: {}, 话题: {}", gameId, playerId, topic);

            // 获取Player Agent
            PlayerAgent playerAgent = aiService.getPlayerAgent(playerId);
            if (playerAgent == null) {
                log.error("Player Agent不存在，玩家ID: {}", playerId);
                return "无法获取AI玩家实例";
            }

            // 调用分析方法（只传递基本信息，让AI通过工具获取详细内容）
            String result = playerAgent.analyzeTopic(
                    gameId.toString(),
                    playerId.toString(),
                    topic
            );

            // 缓存分析结果
            reasoningCache.put(cacheKey, result);

            log.info("话题分析完成，游戏ID: {}, 玩家ID: {}", gameId, playerId);
            return result;

        } catch (Exception e) {
            log.error("分析讨论话题失败: {}", e.getMessage(), e);
            return "分析过程中出现错误";
        }
    }

    /**
     * 从记忆结果构建话题相关信息文本
     *
     * @param memoryResults 记忆结果
     * @param topic         讨论话题
     * @return 话题相关信息文本
     */
    private String buildTopicInfoFromMemory(List<Map<String, Object>> memoryResults, String topic) {
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("与话题 '").append(topic).append("' 相关的信息：\n\n");

        int count = 0;
        for (Map<String, Object> memory : memoryResults) {
            String content = (String) memory.getOrDefault("content", "");
            String playerName = (String) memory.getOrDefault("player_name", "Unknown");
            Double score = (Double) memory.getOrDefault("score", 0.0);

            if (!content.isEmpty() && score > 0.3) { // 只包含相似度大于0.3的结果
                infoBuilder.append(String.format("[%s] %s: %s\n", playerName, "相关信息", content));
                count++;
                if (count >= 10) { // 最多包含10条相关信息
                    break;
                }
            }
        }

        return infoBuilder.toString();
    }

    /**
     * 决定是否发起单聊
     * 采用工具驱动的推理方案
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @return 单聊决策结果
     */
    public String decidePrivateChat(Long gameId, Long playerId) {
        try {
            // 生成缓存键
            String cacheKey = "privatechat:" + gameId + ":" + playerId + ":" + System.currentTimeMillis() / 30000; // 每30秒更新一次缓存

            // 尝试从缓存获取结果
            String cachedResult = reasoningCache.getIfPresent(cacheKey);
            if (cachedResult != null) {
                log.debug("从缓存获取单聊决策结果，游戏ID: {}, 玩家ID: {}", gameId, playerId);
                return cachedResult;
            }

            log.info("决定是否发起单聊，游戏ID: {}, 玩家ID: {}", gameId, playerId);

            // 检查是否可以发起单聊
            // 使用TurnManager的requestPrivateChat方法来检查
            // 注意：这里传入一个临时的接收者ID，只是为了检查是否可以发起单聊
            boolean canChat = turnManager.requestPrivateChat(gameId, playerId, playerId);
            if (!canChat) {
                log.debug("当前无法发起单聊，游戏ID: {}, 玩家ID: {}", gameId, playerId);
                return "当前阶段或次数限制不允许发起单聊";
            }

            // 获取Player Agent
            PlayerAgent playerAgent = aiService.getPlayerAgent(playerId);
            if (playerAgent == null) {
                log.error("Player Agent不存在，玩家ID: {}", playerId);
                return "无法获取AI玩家实例";
            }

            // 调用决策方法（只传递基本信息，让AI通过工具获取详细内容）
            String result = playerAgent.decidePrivateChat(
                    gameId.toString(),
                    playerId.toString()
            );

            // 缓存决策结果
            reasoningCache.put(cacheKey, result);

            log.info("单聊决策完成，游戏ID: {}, 玩家ID: {}", gameId, playerId);
            return result;

        } catch (Exception e) {
            log.error("决定单聊失败: {}", e.getMessage(), e);
            return "决策过程中出现错误";
        }
    }

    /**
     * 从记忆结果构建玩家相关信息文本
     *
     * @param memoryResults 记忆结果
     * @return 玩家相关信息文本
     */
    private String buildPlayerInfoFromMemory(List<Map<String, Object>> memoryResults) {
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("其他玩家相关信息：\n\n");

        Map<String, StringBuilder> playerInfoMap = new HashMap<>();

        for (Map<String, Object> memory : memoryResults) {
            String content = (String) memory.getOrDefault("content", "");
            String playerName = (String) memory.getOrDefault("player_name", "Unknown");
            Double score = (Double) memory.getOrDefault("score", 0.0);

            if (!content.isEmpty() && score > 0.2) { // 只包含相似度大于0.2的结果
                playerInfoMap.computeIfAbsent(playerName, k -> new StringBuilder())
                        .append(content).append("\n");
            }
        }

        // 构建每个玩家的信息
        for (Map.Entry<String, StringBuilder> entry : playerInfoMap.entrySet()) {
            infoBuilder.append("玩家: " ).append(entry.getKey()).append("\n");
            infoBuilder.append("相关信息: " ).append(entry.getValue().toString());
            infoBuilder.append("\n");
        }

        return infoBuilder.toString();
    }

    /**
     * 异步处理推理任务
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @return 异步任务
     */
    public CompletableFuture<String> processReasoningAsync(Long gameId, Long playerId) {
        return CompletableFuture.supplyAsync(
                () -> processReasoningAndDiscussion(gameId, playerId),
                executorService
        );
    }

    /**
     * 关闭资源
     */
    public void shutdown() {
        executorService.shutdown();
    }

    /**
     * 获取近期讨论内容（滑动窗口）
     *
     * @param gameId 游戏ID
     * @return 近期讨论内容
     */
    public String getRecentDiscussion(Long gameId) {
        try {
            // 生成缓存键
            String cacheKey = "recent_discussion:" + gameId + ":" + System.currentTimeMillis() / 30000; // 每30秒更新一次缓存

            // 尝试从缓存获取结果
            String cachedDiscussion = discussionHistoryCache.getIfPresent(cacheKey);
            if (cachedDiscussion != null) {
                log.debug("从缓存获取近期讨论，游戏ID: {}", gameId);
                return cachedDiscussion;
            }

            // 从数据库获取最近的聊天记录
            String query = "获取最近的讨论历史";
            List<Map<String, Object>> recentMessages = ragService.searchConversationMemory(gameId, null, query, SLIDING_WINDOW_SIZE);

            // 按时间顺序排序（从早到晚）
            recentMessages.sort((a, b) -> {
                long timestampA = getTimestamp(a);
                long timestampB = getTimestamp(b);
                return Long.compare(timestampA, timestampB);
            });

            // 构建近期讨论文本
            StringBuilder discussionBuilder = new StringBuilder();
            discussionBuilder.append("\n【近期讨论（短期记忆）】\n");
            
            for (Map<String, Object> message : recentMessages) {
                String playerName = (String) message.getOrDefault("player_name", "未知玩家");
                String content = (String) message.getOrDefault("content", "");
                long timestamp = getTimestamp(message);
                
                String timeStr = new SimpleDateFormat("HH:mm").format(new Date(timestamp));
                discussionBuilder.append(String.format("[%s] %s: %s\n", timeStr, playerName, content));
            }

            if (recentMessages.isEmpty()) {
                discussionBuilder.append("暂无近期讨论记录\n");
            }

            String recentDiscussion = discussionBuilder.toString();
            
            // 缓存近期讨论
            discussionHistoryCache.put(cacheKey, recentDiscussion);
            
            log.debug("获取到 {} 条近期讨论记录，游戏ID: {}", recentMessages.size(), gameId);
            return recentDiscussion;

        } catch (Exception e) {
            log.error("获取近期讨论失败: {}", e.getMessage(), e);
            return "\n【近期讨论（短期记忆）】\n暂无近期讨论记录\n";
        }
    }

    /**
     * 从消息中获取时间戳
     *
     * @param message 消息Map
     * @return 时间戳（毫秒）
     */
    private long getTimestamp(Map<String, Object> message) {
        Object timestampObj = message.get("timestamp");
        if (timestampObj != null) {
            if (timestampObj instanceof java.time.LocalDateTime) {
                // 处理 LocalDateTime 类型
                java.time.LocalDateTime localDateTime = (java.time.LocalDateTime) timestampObj;
                return localDateTime.toInstant(java.time.ZoneOffset.of("+8")).toEpochMilli();
            } else if (timestampObj instanceof Long) {
                // 处理 Long 类型
                return (Long) timestampObj;
            } else if (timestampObj instanceof Number) {
                // 处理其他数字类型
                return ((Number) timestampObj).longValue();
            }
        }
        return System.currentTimeMillis();
    }
}
