package org.jubensha.aijubenshabackend.ai.service;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记忆分层管理服务
 * 实现短期、中期、长期记忆的分层管理
 */
@Slf4j
@Service
public class MemoryHierarchyService {

    /**
     * 短期记忆缓存
     * 缓存最近15分钟的对话和搜索结果
     * 最大缓存5000条记录
     */
    private final Cache<String, List<Map<String, Object>>> shortTermMemoryCache = Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(Duration.ofMinutes(15))
            .expireAfterAccess(Duration.ofMinutes(5))
            .recordStats()
            .removalListener((key, value, cause) -> {
                log.debug("短期记忆被移除，缓存键: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 重要记忆缓存
     * 缓存关键信息，过期时间更长
     */
    private final Cache<String, List<Map<String, Object>>> importantMemoryCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(15))
            .recordStats()
            .removalListener((key, value, cause) -> {
                log.debug("重要记忆被移除，缓存键: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 中期记忆索引
     * 存储游戏会话期间的记忆索引
     */
    private final Map<String, List<String>> mediumTermMemoryIndex = new ConcurrentHashMap<>();

    /**
     * 长期记忆索引
     * 存储全局知识库的索引
     */
    private final Map<String, List<String>> longTermMemoryIndex = new ConcurrentHashMap<>();

    /**
     * RAG服务
     */
    private final RAGService ragService;

    /**
     * 构造函数
     */
    public MemoryHierarchyService(RAGService ragService) {
        this.ragService = ragService;
    }

    /**
     * 存储短期记忆
     *
     * @param key     缓存键
     * @param memory  记忆内容
     */
    public void storeShortTermMemory(String key, List<Map<String, Object>> memory) {
        try {
            shortTermMemoryCache.put(key, memory);
            log.debug("存储短期记忆，缓存键: {}, 记忆数量: {}", key, memory.size());
        } catch (Exception e) {
            log.error("存储短期记忆失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 存储重要记忆
     *
     * @param key     缓存键
     * @param memory  记忆内容
     */
    public void storeImportantMemory(String key, List<Map<String, Object>> memory) {
        try {
            importantMemoryCache.put(key, memory);
            log.debug("存储重要记忆，缓存键: {}, 记忆数量: {}", key, memory.size());
        } catch (Exception e) {
            log.error("存储重要记忆失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取短期记忆
     *
     * @param key 缓存键
     * @return 记忆内容
     */
    public List<Map<String, Object>> getShortTermMemory(String key) {
        try {
            List<Map<String, Object>> memory = shortTermMemoryCache.getIfPresent(key);
            if (memory != null) {
                log.debug("获取短期记忆，缓存键: {}, 记忆数量: {}", key, memory.size());
                return memory;
            }
            
            // 尝试从重要记忆中获取
            memory = importantMemoryCache.getIfPresent(key);
            if (memory != null) {
                log.debug("获取重要记忆，缓存键: {}, 记忆数量: {}", key, memory.size());
                return memory;
            }
            
            log.debug("记忆不存在，缓存键: {}", key);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("获取记忆失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 检查记忆是否重要，需要更长时间的缓存
     *
     * @param results 检索结果
     * @return 是否为重要记忆
     */
    private boolean isImportantMemory(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            return false;
        }
        
        // 检查结果质量
        double averageScore = results.stream()
                .mapToDouble(result -> (Double) result.getOrDefault("score", 0.0))
                .average()
                .orElse(0.0);
        
        // 检查是否包含关键信息
        boolean containsKeyInfo = results.stream()
                .anyMatch(result -> {
                    String content = (String) result.getOrDefault("content", "");
                    return content.contains("凶手") || content.contains("动机") || 
                           content.contains("时间线") || content.contains("线索") ||
                           content.contains("证据") || content.contains("真相");
                });
        
        return averageScore > 0.7 || containsKeyInfo;
    }

    /**
     * 存储中期记忆索引
     *
     * @param sessionId 会话ID
     * @param memoryIds 记忆ID列表
     */
    public void storeMediumTermMemoryIndex(String sessionId, List<String> memoryIds) {
        try {
            mediumTermMemoryIndex.put(sessionId, memoryIds);
            log.debug("存储中期记忆索引，会话ID: {}, 记忆数量: {}", sessionId, memoryIds.size());
        } catch (Exception e) {
            log.error("存储中期记忆索引失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取中期记忆索引
     *
     * @param sessionId 会话ID
     * @return 记忆ID列表
     */
    public List<String> getMediumTermMemoryIndex(String sessionId) {
        try {
            List<String> memoryIds = mediumTermMemoryIndex.get(sessionId);
            if (memoryIds != null) {
                log.debug("获取中期记忆索引，会话ID: {}, 记忆数量: {}", sessionId, memoryIds.size());
            } else {
                log.debug("中期记忆索引不存在，会话ID: {}", sessionId);
            }
            return memoryIds != null ? memoryIds : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取中期记忆索引失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 存储长期记忆索引
     *
     * @param category 类别
     * @param memoryIds 记忆ID列表
     */
    public void storeLongTermMemoryIndex(String category, List<String> memoryIds) {
        try {
            longTermMemoryIndex.put(category, memoryIds);
            log.debug("存储长期记忆索引，类别: {}, 记忆数量: {}", category, memoryIds.size());
        } catch (Exception e) {
            log.error("存储长期记忆索引失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取长期记忆索引
     *
     * @param category 类别
     * @return 记忆ID列表
     */
    public List<String> getLongTermMemoryIndex(String category) {
        try {
            List<String> memoryIds = longTermMemoryIndex.get(category);
            if (memoryIds != null) {
                log.debug("获取长期记忆索引，类别: {}, 记忆数量: {}", category, memoryIds.size());
            } else {
                log.debug("长期记忆索引不存在，类别: {}", category);
            }
            return memoryIds != null ? memoryIds : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取长期记忆索引失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 多级记忆检索
     * 按照短期记忆 -> 中期记忆 -> 长期记忆的顺序进行检索
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @param query    查询文本
     * @param topK     返回结果数量
     * @return 检索结果
     */
    public List<Map<String, Object>> multiLevelRetrieval(Long gameId, Long playerId, String query, int topK) {
        try {
            log.info("开始多级记忆检索，游戏ID: {}, 玩家ID: {}, 查询: {}", gameId, playerId, query);

            // 1. 尝试从短期记忆中检索
            String shortTermKey = generateShortTermKey(gameId, playerId, query);
            List<Map<String, Object>> shortTermResults = getShortTermMemory(shortTermKey);

            if (!shortTermResults.isEmpty()) {
                log.debug("从短期记忆中获取结果，数量: {}", shortTermResults.size());
                return shortTermResults.stream()
                        .limit(topK)
                        .toList();
            }

            // 2. 分析查询类型，选择合适的检索策略
            List<Map<String, Object>> allResults = new ArrayList<>();
            
            // 根据查询内容选择检索策略
            if (query.contains("线索")) {
                // 线索查询 - 优先检索线索记忆
                allResults.addAll(ragService.searchGlobalClueMemory(null, playerId, query, topK));
            } else if (query.contains("时间线")) {
                // 时间线查询 - 优先检索时间线记忆
                allResults.addAll(ragService.searchGlobalTimelineMemory(null, playerId, query, topK));
            } else {
                // 通用查询 - 检索对话记忆
                allResults.addAll(ragService.searchConversationMemory(gameId, playerId, query, topK));
            }

            // 3. 整合结果，去重并排序
            List<Map<String, Object>> mergedResults = mergeAndDeduplicateResults(allResults);
            
            // 4. 根据记忆重要性选择存储策略
            if (!mergedResults.isEmpty()) {
                if (isImportantMemory(mergedResults)) {
                    storeImportantMemory(shortTermKey, mergedResults);
                    log.debug("结果被识别为重要记忆，存储到长期缓存");
                } else {
                    storeShortTermMemory(shortTermKey, mergedResults);
                    log.debug("结果存储到短期缓存");
                }
            }

            log.info("多级记忆检索完成，返回 {} 条结果", mergedResults.size());
            return mergedResults.stream()
                    .limit(topK)
                    .toList();

        } catch (Exception e) {
            log.error("多级记忆检索失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 合并并去重检索结果
     *
     * @param results 检索结果列表
     * @return 合并后的结果列表
     */
    private List<Map<String, Object>> mergeAndDeduplicateResults(List<Map<String, Object>> results) {
        // 使用LinkedHashMap去重，保持顺序
        Map<String, Map<String, Object>> deduplicatedMap = new LinkedHashMap<>();
        
        for (Map<String, Object> result : results) {
            // 生成唯一键
            String key = generateResultKey(result);
            if (!deduplicatedMap.containsKey(key)) {
                deduplicatedMap.put(key, result);
            }
        }
        
        // 转换为列表并按相似度排序
        List<Map<String, Object>> mergedList = new ArrayList<>(deduplicatedMap.values());
        mergedList.sort((a, b) -> {
            Double scoreA = (Double) a.getOrDefault("score", 0.0);
            Double scoreB = (Double) b.getOrDefault("score", 0.0);
            return scoreB.compareTo(scoreA);  // 降序排列
        });
        
        return mergedList;
    }

    /**
     * 生成结果唯一键
     *
     * @param result 结果映射
     * @return 唯一键
     */
    private String generateResultKey(Map<String, Object> result) {
        StringBuilder keyBuilder = new StringBuilder();
        
        if (result.containsKey("id")) {
            keyBuilder.append("id:").append(result.get("id")).append(":");
        }
        if (result.containsKey("content")) {
            String content = (String) result.get("content");
            keyBuilder.append("content:").append(content.hashCode());
        }
        
        return keyBuilder.toString();
    }

    /**
     * 智能记忆检索
     * 根据查询类型和上下文自动选择最优的检索策略
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @param query    查询文本
     * @param context  上下文信息
     * @param topK     返回结果数量
     * @return 检索结果
     */
    public List<Map<String, Object>> intelligentRetrieval(Long gameId, Long playerId, String query, String context, int topK) {
        try {
            log.info("开始智能记忆检索，游戏ID: {}, 玩家ID: {}, 查询: {}", gameId, playerId, query);

            // 1. 分析查询意图
            QueryIntent intent = analyzeQueryIntent(query, context);
            
            // 2. 根据意图选择检索策略
            List<Map<String, Object>> results = new ArrayList<>();
            
            switch (intent) {
                case CLUE_FINDING:
                    // 线索查找 - 结合对话记忆和线索记忆
                    results.addAll(ragService.searchConversationMemory(gameId, playerId, query, topK));
                    results.addAll(ragService.searchGlobalClueMemory(null, playerId, query, topK));
                    break;
                case TIMELINE_ANALYSIS:
                    // 时间线分析 - 结合对话记忆和时间线记忆
                    results.addAll(ragService.searchConversationMemory(gameId, playerId, query, topK));
                    results.addAll(ragService.searchGlobalTimelineMemory(null, playerId, query, topK));
                    break;
                case CHARACTER_ANALYSIS:
                    // 角色分析 - 主要检索对话记忆
                    results.addAll(ragService.searchConversationMemory(gameId, playerId, query, topK));
                    break;
                case GENERAL_INQUIRY:
                default:
                    // 通用查询 - 多级检索
                    results.addAll(multiLevelRetrieval(gameId, playerId, query, topK));
                    break;
            }
            
            // 3. 合并和排序结果
            List<Map<String, Object>> finalResults = mergeAndDeduplicateResults(results);
            
            log.info("智能记忆检索完成，返回 {} 条结果", finalResults.size());
            return finalResults.stream()
                    .limit(topK)
                    .toList();
            
        } catch (Exception e) {
            log.error("智能记忆检索失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 分析查询意图
     *
     * @param query   查询文本
     * @param context 上下文信息
     * @return 查询意图
     */
    private QueryIntent analyzeQueryIntent(String query, String context) {
        String combinedText = (query + " " + context).toLowerCase();
        
        if (combinedText.contains("线索") || combinedText.contains("证据") || combinedText.contains("发现")) {
            return QueryIntent.CLUE_FINDING;
        } else if (combinedText.contains("时间线") || combinedText.contains("何时") || combinedText.contains("时间")) {
            return QueryIntent.TIMELINE_ANALYSIS;
        } else if (combinedText.contains("角色") || combinedText.contains("玩家") || combinedText.contains("谁")) {
            return QueryIntent.CHARACTER_ANALYSIS;
        } else {
            return QueryIntent.GENERAL_INQUIRY;
        }
    }

    /**
     * 查询意图枚举
     */
    private enum QueryIntent {
        CLUE_FINDING,      // 线索查找
        TIMELINE_ANALYSIS,  // 时间线分析
        CHARACTER_ANALYSIS, // 角色分析
        GENERAL_INQUIRY     // 通用查询
    }

    /**
     * 生成短期记忆缓存键
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @param query    查询文本
     * @return 缓存键
     */
    private String generateShortTermKey(Long gameId, Long playerId, String query) {
        return String.format("short_term:%d:%d:%d", gameId, playerId, query.hashCode());
    }

    /**
     * 清理游戏会话的记忆
     *
     * @param gameId 游戏ID
     */
    public void clearGameSessionMemory(Long gameId) {
        try {
            log.info("清理游戏会话记忆，游戏ID: {}", gameId);

            // 清理中期记忆索引
            mediumTermMemoryIndex.remove("game:" + gameId);

            // 清理相关的短期记忆（通过缓存过期自动清理）
            
            log.debug("游戏会话记忆清理完成，游戏ID: {}", gameId);
        } catch (Exception e) {
            log.error("清理游戏会话记忆失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取记忆统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getMemoryStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        // 短期记忆统计
        stats.put("shortTermMemorySize", shortTermMemoryCache.estimatedSize());
        
        // 中期记忆统计
        stats.put("mediumTermMemorySize", mediumTermMemoryIndex.size());
        
        // 长期记忆统计
        stats.put("longTermMemorySize", longTermMemoryIndex.size());
        
        return stats;
    }

    /**
     * 预热记忆缓存
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     */
    public void warmupMemoryCache(Long gameId, Long playerId) {
        try {
            log.info("预热记忆缓存，游戏ID: {}, 玩家ID: {}", gameId, playerId);

            // 预热对话记忆
            List<Map<String, Object>> conversationMemory = ragService.searchConversationMemory(gameId, playerId, "", 10);
            if (!conversationMemory.isEmpty()) {
                String key = generateShortTermKey(gameId, playerId, "warmup");
                storeShortTermMemory(key, conversationMemory);
            }

            log.debug("记忆缓存预热完成，游戏ID: {}, 玩家ID: {}", gameId, playerId);
        } catch (Exception e) {
            log.error("预热记忆缓存失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 记忆整合
     * 将不同层次的记忆整合为统一的知识体系
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @param query    查询文本
     * @param topK     返回结果数量
     * @return 整合后的记忆
     */
    public List<Map<String, Object>> integrateMemory(Long gameId, Long playerId, String query, int topK) {
        try {
            log.info("开始记忆整合，游戏ID: {}, 玩家ID: {}, 查询: {}", gameId, playerId, query);

            // 1. 分析查询类型，确定整合策略
            IntegrationStrategy strategy = determineIntegrationStrategy(query);
            log.info("确定整合策略: {}", strategy);

            // 2. 从多个来源获取信息
            List<Map<String, Object>> allResults = new ArrayList<>();
            
            // 2.1 从对话记忆获取信息
            List<Map<String, Object>> conversationResults = ragService.searchConversationMemory(gameId, playerId, query, topK);
            allResults.addAll(conversationResults);
            
            // 2.2 从线索记忆获取信息
            List<Map<String, Object>> clueResults = ragService.searchGlobalClueMemory(null, playerId, query, topK);
            allResults.addAll(clueResults);
            
            // 2.3 从时间线记忆获取信息
            List<Map<String, Object>> timelineResults = ragService.searchGlobalTimelineMemory(null, playerId, query, topK);
            allResults.addAll(timelineResults);

            // 3. 智能整合结果
            List<Map<String, Object>> integratedResults = intelligentIntegration(allResults, strategy);

            // 4. 基于整合策略的结果优化
            List<Map<String, Object>> optimizedResults = optimizeResultsByStrategy(integratedResults, strategy);

            log.info("记忆整合完成，返回 {} 条结果", optimizedResults.size());
            return optimizedResults.stream()
                    .limit(topK)
                    .toList();

        } catch (Exception e) {
            log.error("记忆整合失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 整合策略枚举
     */
    private enum IntegrationStrategy {
        COMPREHENSIVE,    // 综合整合
        CLUE_CENTRIC,     // 线索中心
        TIMELINE_CENTRIC, // 时间线中心
        MOTIVE_CENTRIC,   // 动机中心
        RELATIONSHIP_CENTRIC // 关系中心
    }

    /**
     * 确定整合策略
     *
     * @param query 查询文本
     * @return 整合策略
     */
    private IntegrationStrategy determineIntegrationStrategy(String query) {
        String queryLower = query.toLowerCase();
        
        if (queryLower.contains("线索") || queryLower.contains("证据")) {
            return IntegrationStrategy.CLUE_CENTRIC;
        } else if (queryLower.contains("时间线")) {
            return IntegrationStrategy.TIMELINE_CENTRIC;
        } else if (queryLower.contains("动机")) {
            return IntegrationStrategy.MOTIVE_CENTRIC;
        } else if (queryLower.contains("关系")) {
            return IntegrationStrategy.RELATIONSHIP_CENTRIC;
        } else {
            return IntegrationStrategy.COMPREHENSIVE;
        }
    }

    /**
     * 智能整合结果
     *
     * @param allResults 所有结果
     * @param strategy   整合策略
     * @return 整合后的结果
     */
    private List<Map<String, Object>> intelligentIntegration(List<Map<String, Object>> allResults, IntegrationStrategy strategy) {
        // 1. 去重和基础排序
        List<Map<String, Object>> deduplicatedResults = mergeAndDeduplicateResults(allResults);
        
        // 2. 基于策略的加权排序
        List<Map<String, Object>> weightedResults = applyStrategyWeighting(deduplicatedResults, strategy);
        
        return weightedResults;
    }

    /**
     * 应用策略加权
     *
     * @param results 结果列表
     * @param strategy 整合策略
     * @return 加权后的结果
     */
    private List<Map<String, Object>> applyStrategyWeighting(List<Map<String, Object>> results, IntegrationStrategy strategy) {
        // 为结果添加策略权重
        for (Map<String, Object> result : results) {
            double baseScore = (double) result.getOrDefault("score", 0.0);
            double weight = calculateStrategyWeight(result, strategy);
            double weightedScore = baseScore * weight;
            result.put("weighted_score", weightedScore);
        }
        
        // 按加权分数排序
        results.sort((a, b) -> {
            Double scoreA = (Double) a.getOrDefault("weighted_score", a.getOrDefault("score", 0.0));
            Double scoreB = (Double) b.getOrDefault("weighted_score", b.getOrDefault("score", 0.0));
            return scoreB.compareTo(scoreA);
        });
        
        return results;
    }

    /**
     * 计算策略权重
     *
     * @param result   结果
     * @param strategy 整合策略
     * @return 权重
     */
    private double calculateStrategyWeight(Map<String, Object> result, IntegrationStrategy strategy) {
        double weight = 1.0;
        String content = (String) result.getOrDefault("content", "").toString().toLowerCase();
        
        switch (strategy) {
            case CLUE_CENTRIC:
                if (content.contains("线索") || content.contains("证据")) {
                    weight = 1.5;
                }
                break;
            case TIMELINE_CENTRIC:
                if (content.contains("时间") || content.contains("何时")) {
                    weight = 1.5;
                }
                break;
            case MOTIVE_CENTRIC:
                if (content.contains("动机") || content.contains("原因")) {
                    weight = 1.5;
                }
                break;
            case RELATIONSHIP_CENTRIC:
                if (content.contains("关系") || content.contains("联系")) {
                    weight = 1.5;
                }
                break;
            case COMPREHENSIVE:
            default:
                weight = 1.0;
                break;
        }
        
        return weight;
    }

    /**
     * 基于策略优化结果
     *
     * @param results 结果列表
     * @param strategy 整合策略
     * @return 优化后的结果
     */
    private List<Map<String, Object>> optimizeResultsByStrategy(List<Map<String, Object>> results, IntegrationStrategy strategy) {
        List<Map<String, Object>> optimizedResults = new ArrayList<>();
        
        // 确保包含不同类型的信息
        Map<String, Map<String, Object>> typeMap = new HashMap<>();
        
        for (Map<String, Object> result : results) {
            String content = (String) result.getOrDefault("content", "");
            String type = classifyResultType(content);
            
            if (!typeMap.containsKey(type)) {
                typeMap.put(type, result);
                optimizedResults.add(result);
            }
        }
        
        // 补充其他相关结果
        for (Map<String, Object> result : results) {
            if (optimizedResults.size() >= 10) break;
            if (!optimizedResults.contains(result)) {
                optimizedResults.add(result);
            }
        }
        
        return optimizedResults;
    }

    /**
     * 分类结果类型
     *
     * @param content 内容
     * @return 类型
     */
    private String classifyResultType(String content) {
        String contentLower = content.toLowerCase();
        
        if (contentLower.contains("线索") || contentLower.contains("证据")) {
            return "线索";        
        } else if (contentLower.contains("时间") || contentLower.contains("何时")) {
            return "时间线";
        } else if (contentLower.contains("动机") || contentLower.contains("原因")) {
            return "动机";
        } else if (contentLower.contains("关系") || contentLower.contains("联系")) {
            return "关系";
        } else if (contentLower.contains("背景") || contentLower.contains("故事")) {
            return "背景";
        } else {
            return "其他";
        }
    }
}
