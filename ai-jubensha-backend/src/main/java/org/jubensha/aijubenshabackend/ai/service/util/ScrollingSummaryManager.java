package org.jubensha.aijubenshabackend.ai.service.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.jubensha.aijubenshabackend.ai.service.agent.JudgeAgent;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 滚动摘要管理器
 * 管理讨论历史的滚动摘要，生成剧情快照作为长期记忆
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-03-01
 * @since 2026
 */

@Slf4j
@Component
public class ScrollingSummaryManager {

    @Autowired
    private RAGService ragService;

    /**
     * 摘要生成阈值，当发言达到此数量时生成摘要
     */
    private static final int SUMMARY_THRESHOLD = 20;

    /**
     * 摘要缓存
     * 缓存策略：
     * - 最大缓存 500 个结果
     * - 写入后 10 分钟过期
     * - 访问后 5 分钟过期
     */
    private final Cache<String, String> summaryCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(Duration.ofMinutes(10))
            .expireAfterAccess(Duration.ofMinutes(5))
            .build();

    /**
     * 发言计数缓存
     * 缓存策略：
     * - 最大缓存 1000 个结果
     * - 写入后 30 分钟过期
     */
    private final Cache<String, Integer> messageCountCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();

    /**
     * 定时任务线程池
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    /**
     * 获取剧情快照（长期记忆）
     *
     * @param gameId 游戏ID
     * @return 剧情快照
     */
    public String getPlotSnapshot(Long gameId) {
        try {
            // 生成缓存键
            String cacheKey = "plot_snapshot:" + gameId;

            // 尝试从缓存获取结果
            String cachedSummary = summaryCache.getIfPresent(cacheKey);
            if (cachedSummary != null) {
                log.debug("从缓存获取剧情快照，游戏ID: {}", gameId);
                return cachedSummary;
            }

            // 检查发言数量
            int messageCount = getMessageCount(gameId);
            log.debug("当前发言数量: {}, 游戏ID: {}", messageCount, gameId);

            // 如果发言数量达到阈值，生成新的摘要
            if (messageCount >= SUMMARY_THRESHOLD) {
                String newSummary = generatePlotSnapshot(gameId);
                if (newSummary != null && !newSummary.isEmpty()) {
                    // 缓存摘要
                    summaryCache.put(cacheKey, newSummary);
                    // 重置发言计数
                    resetMessageCount(gameId);
                    log.info("生成新的剧情快照，游戏ID: {}", gameId);
                    return newSummary;
                }
            }

            // 如果没有足够的发言或生成摘要失败，返回空
            return "";

        } catch (Exception e) {
            log.error("获取剧情快照失败: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 生成剧情快照
     *
     * @param gameId 游戏ID
     * @return 剧情快照
     */
    private String generatePlotSnapshot(Long gameId) {
        try {
            // 获取最近的讨论记录
            String query = "获取完整的讨论历史";
            List<Map<String, Object>> discussionHistory = ragService.searchConversationMemory(gameId, null, query, SUMMARY_THRESHOLD);

            if (discussionHistory.isEmpty()) {
                log.debug("讨论历史为空，游戏ID: {}", gameId);
                return "";
            }

            // 构建讨论历史文本
            StringBuilder historyBuilder = new StringBuilder();
            historyBuilder.append("请将以下讨论内容总结为一段50-100字的剧情快照，包含关键信息和讨论趋势：\n\n");

            for (Map<String, Object> message : discussionHistory) {
                String playerName = (String) message.getOrDefault("player_name", "未知玩家");
                String content = (String) message.getOrDefault("content", "");
                historyBuilder.append(String.format("%s: %s\n", playerName, content));
            }

            // 使用Judge Agent ID为2生成摘要
            org.jubensha.aijubenshabackend.ai.service.AIService aiService = SpringContextUtil.getBean(org.jubensha.aijubenshabackend.ai.service.AIService.class);
            JudgeAgent judgeAgent = aiService.getJudgeAgent(2L);
            if (judgeAgent == null) {
                log.error("Judge Agent ID为2不存在");
                // 回退到简单摘要
                return generateFallbackSummary(discussionHistory);
            }

            String summary = judgeAgent.summarizeDiscussion(historyBuilder.toString());
            log.debug("生成剧情快照: {}, 游戏ID: {}", summary, gameId);

            // 构建剧情快照格式
            if (summary != null && !summary.isEmpty()) {
                return "\n【前情提要（长期记忆）】\n" + summary + "\n";
            }

            return "";

        } catch (Exception e) {
            log.error("生成剧情快照失败: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 生成回退摘要（当Judge Agent不可用时）
     *
     * @param discussionHistory 讨论历史
     * @return 回退摘要
     */
    private String generateFallbackSummary(List<Map<String, Object>> discussionHistory) {
        try {
            // 简单的摘要生成逻辑
            StringBuilder summaryBuilder = new StringBuilder();
            int count = 0;
            for (Map<String, Object> message : discussionHistory) {
                String playerName = (String) message.getOrDefault("player_name", "未知玩家");
                String content = (String) message.getOrDefault("content", "");
                if (!content.isEmpty()) {
                    summaryBuilder.append(playerName).append("说：").append(content).append("。");
                    count++;
                    if (count >= 5) { // 最多包含5条消息
                        break;
                    }
                }
            }
            String summary = summaryBuilder.toString();
            if (summary.length() > 100) {
                summary = summary.substring(0, 100) + "...";
            }
            return "\n【前情提要（长期记忆）】\n" + summary + "\n";
        } catch (Exception e) {
            log.error("生成回退摘要失败: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 获取发言数量
     *
     * @param gameId 游戏ID
     * @return 发言数量
     */
    private int getMessageCount(Long gameId) {
        String cacheKey = "message_count:" + gameId;
        Integer count = messageCountCache.getIfPresent(cacheKey);
        return count != null ? count : 0;
    }

    /**
     * 增加发言计数
     *
     * @param gameId 游戏ID
     */
    public void incrementMessageCount(Long gameId) {
        String cacheKey = "message_count:" + gameId;
        Integer currentCount = messageCountCache.getIfPresent(cacheKey);
        int newCount = (currentCount != null ? currentCount : 0) + 1;
        messageCountCache.put(cacheKey, newCount);
        log.debug("增加发言计数，当前计数: {}, 游戏ID: {}", newCount, gameId);
    }

    /**
     * 重置发言计数
     *
     * @param gameId 游戏ID
     */
    private void resetMessageCount(Long gameId) {
        String cacheKey = "message_count:" + gameId;
        messageCountCache.put(cacheKey, 0);
        log.debug("重置发言计数，游戏ID: {}", gameId);
    }

    /**
     * 关闭资源
     */
    public void shutdown() {
        scheduler.shutdown();
    }
}