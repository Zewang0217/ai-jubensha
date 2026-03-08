package org.jubensha.aijubenshabackend.ai.service.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.agent.JudgeAgent;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.entity.Dialogue;
import org.jubensha.aijubenshabackend.repository.dialogue.DialogueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private DialogueRepository dialogueRepository;

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
            // 生成缓存键
            String cacheKey = "plot_snapshot:" + gameId;

            // 获取之前的旧摘要（滚雪球式摘要的核心）
            String oldSummary = summaryCache.getIfPresent(cacheKey);
            if (oldSummary != null) {
                log.debug("获取到旧摘要，将用于生成滚动摘要，游戏ID: {}", gameId);
            }

            // 使用 DialogueRepository 获取最近的讨论记录（按时间倒序）
            Pageable pageable = PageRequest.of(0, SUMMARY_THRESHOLD);
            List<Dialogue> dialogues = dialogueRepository.findByGameIdOrderByTimestampDesc(gameId, pageable);

            if (dialogues.isEmpty()) {
                log.debug("讨论历史为空，游戏ID: {}", gameId);
                // 如果没有新对话但有旧摘要，返回旧摘要
                return oldSummary != null ? oldSummary : "";
            }

            // 将 Dialogue 实体转换为 Map 格式（保持与原有代码兼容）
            List<Map<String, Object>> discussionHistory = dialogues.stream()
                    .map(dialogue -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("player_name", dialogue.getPlayer() != null ? dialogue.getPlayer().getNickname() : "未知玩家");
                        map.put("content", dialogue.getContent());
                        map.put("timestamp", dialogue.getTimestamp());
                        return map;
                    })
                    .collect(Collectors.toList());

            // 构建讨论历史文本（滚雪球式摘要Prompt）
            StringBuilder historyBuilder = new StringBuilder();
            historyBuilder.append("你是一个剧本杀法官。请根据【前情提要】和【最新讨论内容】，更新并输出一段100字以内的全局剧情快照：\n\n");

            // 如果有旧摘要，将其加入Prompt
            if (oldSummary != null && !oldSummary.isEmpty()) {
                // 清理旧摘要中的格式标记，只保留纯内容
                String cleanedOldSummary = oldSummary.replace("\n【前情提要（长期记忆）】\n", "").replace("\n", "").trim();
                historyBuilder.append("【前情提要】：\n").append(cleanedOldSummary).append("\n\n");
            }

            historyBuilder.append("【最新讨论内容】：\n");

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
                // 回退到简单摘要，同时保留旧摘要
                String newFallbackSummary = generateFallbackSummary(discussionHistory, oldSummary);
                return newFallbackSummary;
            }

            String summary = judgeAgent.summarizeDiscussion(historyBuilder.toString());
            log.debug("生成剧情快照: {}, 游戏ID: {}", summary, gameId);

            // 构建剧情快照格式
            if (summary != null && !summary.isEmpty()) {
                return "\n【前情提要（长期记忆）】\n" + summary + "\n";
            }

            // 如果生成失败但有旧摘要，返回旧摘要
            return oldSummary != null ? oldSummary : "";

        } catch (Exception e) {
            log.error("生成剧情快照失败: {}", e.getMessage(), e);
            // 异常时尝试返回旧摘要
            String cacheKey = "plot_snapshot:" + gameId;
            String oldSummary = summaryCache.getIfPresent(cacheKey);
            return oldSummary != null ? oldSummary : "";
        }
    }

    /**
     * 生成回退摘要（当Judge Agent不可用时）
     *
     * @param discussionHistory 讨论历史
     * @param oldSummary 旧摘要（可为null）
     * @return 回退摘要
     */
    private String generateFallbackSummary(List<Map<String, Object>> discussionHistory, String oldSummary) {
        try {
            StringBuilder summaryBuilder = new StringBuilder();

            // 如果有旧摘要，先添加旧摘要的简化版本
            if (oldSummary != null && !oldSummary.isEmpty()) {
                String cleanedOld = oldSummary.replace("\n【前情提要（长期记忆）】\n", "").replace("\n", "").trim();
                if (cleanedOld.length() > 50) {
                    cleanedOld = cleanedOld.substring(0, 50) + "...";
                }
                summaryBuilder.append(cleanedOld).append(" ");
            }

            // 添加新消息摘要
            int count = 0;
            for (Map<String, Object> message : discussionHistory) {
                String playerName = (String) message.getOrDefault("player_name", "未知玩家");
                String content = (String) message.getOrDefault("content", "");
                if (!content.isEmpty()) {
                    summaryBuilder.append(playerName).append("说：").append(content).append("。");
                    count++;
                    if (count >= 3) { // 最多包含3条消息（因为有旧摘要）
                        break;
                    }
                }
            }

            String summary = summaryBuilder.toString();
            if (summary.length() > 150) {
                summary = summary.substring(0, 150) + "...";
            }
            return "\n【前情提要（长期记忆）】\n" + summary + "\n";
        } catch (Exception e) {
            log.error("生成回退摘要失败: {}", e.getMessage(), e);
            return oldSummary != null ? oldSummary : "";
        }
    }

    /**
     * 合并旧摘要和新摘要
     *
     * @param oldSummary 旧摘要
     * @param newSummary 新摘要
     * @return 合并后的摘要
     */
    private String mergeSummaries(String oldSummary, String newSummary) {
        try {
            // 清理格式标记
            String cleanedOld = oldSummary.replace("\n【前情提要（长期记忆）】\n", "").replace("\n", "").trim();
            String cleanedNew = newSummary.replace("\n【前情提要（长期记忆）】\n", "").replace("\n", "").trim();

            // 如果旧摘要已经很长，只保留后半部分
            if (cleanedOld.length() > 100) {
                cleanedOld = cleanedOld.substring(cleanedOld.length() - 100);
            }

            // 合并摘要
            String merged = cleanedOld + " " + cleanedNew;

            // 限制总长度
            if (merged.length() > 150) {
                merged = merged.substring(0, 150) + "...";
            }

            return "\n【前情提要（长期记忆）】\n" + merged + "\n";
        } catch (Exception e) {
            log.error("合并摘要失败: {}", e.getMessage());
            return newSummary; // 失败时返回新摘要
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