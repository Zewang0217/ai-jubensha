package org.jubensha.aijubenshabackend.ai.service;


import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * AI思考服务
 * 实现AI的思考过程，包括多轮检索策略和主动记忆检索
 */
@Slf4j
@Service
public class AIMindService {

    /**
     * RAG服务
     */
    private final RAGService ragService;

    /**
     * 记忆分层服务
     */
    private final MemoryHierarchyService memoryHierarchyService;

    /**
     * 重排服务
     */
    private final RerankingService rerankingService;

    /**
     * 思考状态缓存
     * 存储AI的思考过程和中间结果
     */
    private final Map<String, ThinkingState> thinkingStateCache = new ConcurrentHashMap<>();

    /**
     * 反馈数据缓存
     * 存储检索和分析的反馈数据，用于自适应调整
     */
    private final Map<String, FeedbackData> feedbackCache = new ConcurrentHashMap<>();

    /**
     * 检索策略成功率统计
     */
    private final Map<RetrievalStrategy, StrategyStats> strategyStats = new ConcurrentHashMap<>();

    /**
     * 构造函数
     */
    public AIMindService(RAGService ragService, MemoryHierarchyService memoryHierarchyService, RerankingService rerankingService) {
        this.ragService = ragService;
        this.memoryHierarchyService = memoryHierarchyService;
        this.rerankingService = rerankingService;
    }

    /**
     * 开始思考过程
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @param task     思考任务
     * @return 思考过程ID
     */
    public String startThinking(Long gameId, Long playerId, String task) {
        try {
            String thinkingId = generateThinkingId(gameId, playerId, task);
            ThinkingState thinkingState = new ThinkingState(gameId, playerId, task);
            thinkingStateCache.put(thinkingId, thinkingState);

            log.info("开始思考过程，思考ID: {}, 游戏ID: {}, 玩家ID: {}, 任务: {}", thinkingId, gameId, playerId, task);
            return thinkingId;
        } catch (Exception e) {
            log.error("开始思考过程失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 执行多轮思考
     *
     * @param thinkingId 思考过程ID
     * @param maxRounds 最大思考轮数
     * @return 思考结果
     */
    public List<Map<String, Object>> executeMultiRoundThinking(String thinkingId, int maxRounds) {
        try {
            ThinkingState thinkingState = thinkingStateCache.get(thinkingId);
            if (thinkingState == null) {
                log.error("思考状态不存在，思考ID: {}", thinkingId);
                return new ArrayList<>();
            }

            log.info("执行多轮思考，思考ID: {}, 最大轮数: {}", thinkingId, maxRounds);

            List<Map<String, Object>> finalResults = new ArrayList<>();

            for (int round = 1; round <= maxRounds; round++) {
                log.debug("执行思考轮次: {}", round);

                // 1. 分析当前任务和已有信息
                String currentQuery = analyzeCurrentTask(thinkingState, round);

                // 2. 执行记忆检索
                List<Map<String, Object>> retrievalResults = executeMemoryRetrieval(thinkingState, currentQuery, 20);

                // 3. 分析检索结果
                AnalysisResult analysisResult = analyzeRetrievalResults(thinkingState, retrievalResults, round);

                // 4. 更新思考状态
                updateThinkingState(thinkingState, analysisResult, retrievalResults);

                // 5. 检查是否需要继续思考
                if (analysisResult.isComplete()) {
                    log.debug("思考完成，轮次: {}", round);
                    finalResults = retrievalResults;
                    break;
                }

                // 7. 保存中间结果
                finalResults = retrievalResults;
            }

            log.info("多轮思考完成，思考ID: {}, 返回 {} 条结果", thinkingId, finalResults.size());
            return finalResults;

        } catch (Exception e) {
            log.error("执行多轮思考失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 分析当前任务
     *
     * @param thinkingState 思考状态
     * @param round        当前轮次
     * @return 当前查询文本
     */
    private String analyzeCurrentTask(ThinkingState thinkingState, int round) {
        String task = thinkingState.getTask();
        List<AnalysisResult> previousResults = thinkingState.getAnalysisResults();

        if (round == 1) {
            // 第一轮，直接使用原始任务
            return task;
        } else {
            // 后续轮次，基于之前的分析结果调整查询
            if (!previousResults.isEmpty()) {
                AnalysisResult lastResult = previousResults.get(previousResults.size() - 1);
                if (!lastResult.getGaps().isEmpty()) {
                    // 基于知识 gaps 生成新的查询
                    return "关于" + String.join("和", lastResult.getGaps()) + "的信息";
                }
            }
            return task + " (更详细的信息)";
        }
    }

    /**
     * 执行记忆检索
     *
     * @param thinkingState 思考状态
     * @param query         查询文本
     * @param topK          返回结果数量
     * @return 检索结果
     */
    private List<Map<String, Object>> executeMemoryRetrieval(ThinkingState thinkingState, String query, int topK) {
        // 使用记忆分层服务执行智能检索
        String context = buildContextFromThinkingState(thinkingState);
        return memoryHierarchyService.intelligentRetrieval(
                thinkingState.getGameId(),
                thinkingState.getPlayerId(),
                query,
                context,
                topK
        );
    }

    /**
     * 从思考状态构建上下文
     *
     * @param thinkingState 思考状态
     * @return 上下文信息
     */
    private String buildContextFromThinkingState(ThinkingState thinkingState) {
        StringBuilder contextBuilder = new StringBuilder();
        
        // 1. 添加任务信息
        contextBuilder.append("任务: " + thinkingState.getTask()).append(" ");
        
        // 2. 添加思考轮次信息
        int currentRound = thinkingState.getCurrentRound();
        if (currentRound > 0) {
            contextBuilder.append("当前轮次: " + currentRound).append(" ");
        }
        
        // 3. 添加之前的分析结果
        List<AnalysisResult> analysisResults = thinkingState.getAnalysisResults();
        if (!analysisResults.isEmpty()) {
            AnalysisResult lastResult = analysisResults.get(analysisResults.size() - 1);
            contextBuilder.append("之前的分析: ");
            if (!lastResult.getGaps().isEmpty()) {
                contextBuilder.append("需要补充: " + String.join(", ", lastResult.getGaps()));
            } else if (lastResult.isComplete()) {
                contextBuilder.append("分析完成，信息充足");
            }
            contextBuilder.append(" ");
        }
        
        // 4. 添加之前的检索结果摘要
        List<List<Map<String, Object>>> retrievalResults = thinkingState.getRetrievalResults();
        if (!retrievalResults.isEmpty()) {
            List<Map<String, Object>> lastResults = retrievalResults.get(retrievalResults.size() - 1);
            if (!lastResults.isEmpty()) {
                contextBuilder.append("已获取信息: " + lastResults.size() + "条结果 ");
                // 添加最相关结果的摘要
                Map<String, Object> topResult = lastResults.stream()
                        .max((a, b) -> {
                            Double scoreA = (Double) a.getOrDefault("score", 0.0);
                            Double scoreB = (Double) b.getOrDefault("score", 0.0);
                            return scoreA.compareTo(scoreB);
                        })
                        .orElse(null);
                if (topResult != null) {
                    String content = (String) topResult.getOrDefault("content", "");
                    if (content.length() > 50) {
                        content = content.substring(0, 50) + "...";
                    }
                    contextBuilder.append("最相关信息: " + content).append(" ");
                }
            }
        }
        
        // 5. 添加任务类型标签
        String taskType = analyzeTaskType(thinkingState.getTask());
        contextBuilder.append("任务类型: " + taskType).append(" ");
        
        return contextBuilder.toString().trim();
    }

    /**
     * 分析任务类型
     *
     * @param task 任务文本
     * @return 任务类型
     */
    private String analyzeTaskType(String task) {
        String taskLower = task.toLowerCase();
        
        if (taskLower.contains("凶手") || taskLower.contains("谁是")) {
            return "凶手识别";
        } else if (taskLower.contains("动机")) {
            return "动机分析";
        } else if (taskLower.contains("时间线")) {
            return "时间线分析";
        } else if (taskLower.contains("线索") || taskLower.contains("证据")) {
            return "线索分析";
        } else if (taskLower.contains("关系")) {
            return "关系分析";
        } else if (taskLower.contains("背景")) {
            return "背景信息";
        } else if (taskLower.contains("讨论")) {
            return "讨论分析";
        } else if (taskLower.contains("分析")) {
            return "综合分析";
        } else {
            return "通用查询";
        }
    }

    /**
     * 构建增强型上下文
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @param task     任务文本
     * @param history  历史上下文
     * @return 增强型上下文
     */
    private String buildEnhancedContext(Long gameId, Long playerId, String task, String history) {
        StringBuilder contextBuilder = new StringBuilder();
        
        // 1. 添加游戏和玩家信息
        contextBuilder.append("游戏ID: " + gameId).append(" ");
        contextBuilder.append("玩家ID: " + playerId).append(" ");
        
        // 2. 添加任务信息
        contextBuilder.append("当前任务: " + task).append(" ");
        
        // 3. 添加历史上下文
        if (history != null && !history.isEmpty()) {
            contextBuilder.append("历史上下文: " + history).append(" ");
        }
        
        // 4. 添加任务类型分析
        String taskType = analyzeTaskType(task);
        contextBuilder.append("任务类型: " + taskType).append(" ");
        
        // 5. 添加时间信息
        contextBuilder.append("当前时间: " + System.currentTimeMillis()).append(" ");
        
        return contextBuilder.toString().trim();
    }

    /**
     * 分析检索结果
     *
     * @param thinkingState    思考状态
     * @param retrievalResults 检索结果
     * @param round            当前轮次
     * @return 分析结果
     */
    private AnalysisResult analyzeRetrievalResults(ThinkingState thinkingState, List<Map<String, Object>> retrievalResults, int round) {
        List<String> gaps = new ArrayList<>();
        boolean complete = false;

        String task = thinkingState.getTask();
        
        // 分析检索结果，识别知识 gaps
        if (retrievalResults.isEmpty()) {
            gaps.add("基本信息");
        } else {
            // 1. 检查结果的相关性
            double averageScore = retrievalResults.stream()
                    .mapToDouble(result -> (double) result.getOrDefault("score", 0.0))
                    .average()
                    .orElse(0.0);

            if (averageScore < 0.4) {
                gaps.add("相关信息");
            } else if (averageScore < 0.7) {
                gaps.add("高质量信息");
            }

            // 2. 检查结果的数量和多样性
            if (retrievalResults.size() < 5) {
                gaps.add("足够信息");
            } else if (retrievalResults.size() < 8) {
                gaps.add("详细信息");
            }

            // 3. 基于任务内容的gap分析
            gaps.addAll(analyzeTaskSpecificGaps(task, retrievalResults));

            // 4. 检查信息完整性
            complete = checkInformationCompleteness(task, retrievalResults, averageScore);
        }

        return new AnalysisResult(gaps, complete, round);
    }

    /**
     * 基于任务内容的gap分析
     *
     * @param task              思考任务
     * @param retrievalResults 检索结果
     * @return 任务特定的gap列表
     */
    private List<String> analyzeTaskSpecificGaps(String task, List<Map<String, Object>> retrievalResults) {
        List<String> gaps = new ArrayList<>();
        String taskLower = task.toLowerCase();
        
        // 分析结果内容
        StringBuilder contentBuilder = new StringBuilder();
        for (Map<String, Object> result : retrievalResults) {
            String content = (String) result.getOrDefault("content", "");
            contentBuilder.append(content).append(" ");
        }
        String allContent = contentBuilder.toString().toLowerCase();
        
        // 根据任务类型分析特定gap
        if (taskLower.contains("凶手") || taskLower.contains("谁是")) {
            // 凶手分析任务
            if (!allContent.contains("动机")) {
                gaps.add("动机信息");
            }
            if (!allContent.contains("时间")) {
                gaps.add("时间线信息");
            }
            if (!allContent.contains("证据")) {
                gaps.add("证据信息");
            }
        } else if (taskLower.contains("动机")) {
            // 动机分析任务
            if (!allContent.contains("原因")) {
                gaps.add("具体原因");
            }
            if (!allContent.contains("背景")) {
                gaps.add("背景信息");
            }
        } else if (taskLower.contains("时间线") || taskLower.contains("何时")) {
            // 时间线分析任务
            if (!allContent.contains("顺序")) {
                gaps.add("事件顺序");
            }
            if (!allContent.contains("地点")) {
                gaps.add("地点信息");
            }
        } else if (taskLower.contains("线索") || taskLower.contains("证据")) {
            // 线索分析任务
            if (!allContent.contains("关联")) {
                gaps.add("线索关联");
            }
            if (!allContent.contains("重要")) {
                gaps.add("关键线索");
            }
        }
        
        return gaps;
    }

    /**
     * 检查信息完整性
     *
     * @param task          思考任务
     * @param results       检索结果
     * @param averageScore  平均相似度得分
     * @return 是否完整
     */
    private boolean checkInformationCompleteness(String task, List<Map<String, Object>> results, double averageScore) {
        // 基于多个维度判断信息完整性
        
        // 1. 结果数量阈值
        int minResultsThreshold = 8;
        if (results.size() < minResultsThreshold) {
            return false;
        }
        
        // 2. 平均相似度阈值
        double minScoreThreshold = 0.6;
        if (averageScore < minScoreThreshold) {
            return false;
        }
        
        // 3. 结果多样性检查
        boolean hasDiverseResults = checkResultDiversity(results);
        if (!hasDiverseResults) {
            return false;
        }
        
        // 4. 任务特定的完整性检查
        return checkTaskSpecificCompleteness(task, results);
    }

    /**
     * 检查结果多样性
     *
     * @param results 检索结果
     * @return 是否具有多样性
     */
    private boolean checkResultDiversity(List<Map<String, Object>> results) {
        if (results.size() < 3) {
            return false;
        }
        
        // 检查内容多样性
        List<String> contents = new ArrayList<>();
        for (Map<String, Object> result : results) {
            String content = (String) result.getOrDefault("content", "");
            if (!content.isEmpty()) {
                contents.add(content);
            }
        }
        
        // 简单的多样性检查：确保至少有3个不同的内容
        long uniqueContents = contents.stream().distinct().count();
        return uniqueContents >= 3;
    }

    /**
     * 检查任务特定的完整性
     *
     * @param task     思考任务
     * @param results  检索结果
     * @return 是否完整
     */
    private boolean checkTaskSpecificCompleteness(String task, List<Map<String, Object>> results) {
        String taskLower = task.toLowerCase();
        StringBuilder contentBuilder = new StringBuilder();
        
        for (Map<String, Object> result : results) {
            String content = (String) result.getOrDefault("content", "");
            contentBuilder.append(content).append(" ");
        }
        String allContent = contentBuilder.toString().toLowerCase();
        
        if (taskLower.contains("凶手")) {
            // 凶手分析需要包含动机、时间线和证据
            return allContent.contains("动机") && allContent.contains("时间") && allContent.contains("证据");
        } else if (taskLower.contains("动机")) {
            // 动机分析需要包含原因和背景
            return allContent.contains("原因") && allContent.contains("背景");
        } else if (taskLower.contains("时间线")) {
            // 时间线分析需要包含顺序和地点
            return allContent.contains("顺序") && allContent.contains("地点");
        } else if (taskLower.contains("线索")) {
            // 线索分析需要包含关联和重要性
            return allContent.contains("关联") && allContent.contains("重要");
        }
        
        // 通用任务的默认完整性检查
        return true;
    }

    /**
     * 更新思考状态
     *
     * @param thinkingState    思考状态
     * @param analysisResult   分析结果
     * @param retrievalResults 检索结果
     */
    private void updateThinkingState(ThinkingState thinkingState, AnalysisResult analysisResult, List<Map<String, Object>> retrievalResults) {
        thinkingState.addAnalysisResult(analysisResult);
        thinkingState.addRetrievalResults(retrievalResults);
        thinkingState.incrementRound();
    }

    /**
     * 获取思考状态
     *
     * @param thinkingId 思考过程ID
     * @return 思考状态
     */
    public ThinkingState getThinkingState(String thinkingId) {
        return thinkingStateCache.get(thinkingId);
    }

    /**
     * 清理思考状态
     *
     * @param thinkingId 思考过程ID
     */
    public void cleanupThinkingState(String thinkingId) {
        thinkingStateCache.remove(thinkingId);
        log.debug("清理思考状态，思考ID: {}", thinkingId);
    }

    /**
     * 生成思考过程ID
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @param task     思考任务
     * @return 思考过程ID
     */
    private String generateThinkingId(Long gameId, Long playerId, String task) {
        return String.format("thinking:%d:%d:%d:%d", gameId, playerId, task.hashCode(), System.currentTimeMillis());
    }

    /**
     * 主动记忆检索
     * 让AI根据当前任务主动选择记忆检索策略
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @param task     当前任务
     * @param context  上下文信息
     * @return 检索结果
     */
    public List<Map<String, Object>> executeActiveMemoryRetrieval(Long gameId, Long playerId, String task, String context) {
        try {
            log.info("执行主动记忆检索，游戏ID: {}, 玩家ID: {}, 任务: {}", gameId, playerId, task);

            // 1. 分析任务类型，选择检索策略
            RetrievalStrategy strategy = selectRetrievalStrategy(task, context);

            // 2. 执行相应的检索策略
            List<Map<String, Object>> results = executeRetrievalStrategy(gameId, playerId, task, strategy);

            // 3. 对结果进行重排
            List<Map<String, Object>> rerankedResults = rerankingService.twoStepRetrieval(task, results, 15);

            log.info("主动记忆检索完成，游戏ID: {}, 玩家ID: {}, 返回 {} 条结果", gameId, playerId, rerankedResults.size());
            return rerankedResults;

        } catch (Exception e) {
            log.error("执行主动记忆检索失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 选择检索策略
     *
     * @param task    当前任务
     * @param context 上下文信息
     * @return 检索策略
     */
    private RetrievalStrategy selectRetrievalStrategy(String task, String context) {
        String combinedText = (task + " " + context).toLowerCase();
        
        // 根据任务类型选择检索策略
        if (combinedText.contains("线索") && combinedText.contains("关联")) {
            return RetrievalStrategy.EVIDENCE_FOCUS;
        } else if (combinedText.contains("线索")) {
            return RetrievalStrategy.CLUE_FOCUS;
        } else if (combinedText.contains("时间线") && combinedText.contains("重建")) {
            return RetrievalStrategy.TIMELINE_RECONSTRUCTION_FOCUS;
        } else if (combinedText.contains("时间线")) {
            return RetrievalStrategy.TIMELINE_FOCUS;
        } else if (combinedText.contains("玩家") && combinedText.contains("关系")) {
            return RetrievalStrategy.RELATIONSHIP_FOCUS;
        } else if (combinedText.contains("玩家")) {
            return RetrievalStrategy.PLAYER_FOCUS;
        } else if (combinedText.contains("讨论")) {
            return RetrievalStrategy.DISCUSSION_FOCUS;
        } else if (combinedText.contains("动机")) {
            return RetrievalStrategy.MOTIVE_FOCUS;
        } else if (combinedText.contains("背景") || combinedText.contains("故事")) {
            return RetrievalStrategy.BACKGROUND_FOCUS;
        } else if (combinedText.contains("证据")) {
            return RetrievalStrategy.EVIDENCE_FOCUS;
        } else {
            return RetrievalStrategy.GLOBAL_SEARCH;
        }
    }

    /**
     * 执行检索策略
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @param task     当前任务
     * @param strategy 检索策略
     * @return 检索结果
     */
    private List<Map<String, Object>> executeRetrievalStrategy(Long gameId, Long playerId, String task, RetrievalStrategy strategy) {
        switch (strategy) {
            case CLUE_FOCUS:
                // 专注于线索检索
                return memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "线索 " + task, 25);
            case TIMELINE_FOCUS:
                // 专注于时间线检索
                return memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "时间线 " + task, 25);
            case PLAYER_FOCUS:
                // 专注于玩家相关信息检索
                return memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "玩家 " + task, 25);
            case DISCUSSION_FOCUS:
                // 专注于讨论历史检索
                return memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "讨论 " + task, 25);
            case MOTIVE_FOCUS:
                // 专注于动机分析检索
                return memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "动机分析 " + task, 25);
            case EVIDENCE_FOCUS:
                // 专注于证据分析检索
                return memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "证据分析 " + task, 25);
            case RELATIONSHIP_FOCUS:
                // 专注于关系分析检索
                return memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "关系分析 " + task, 25);
            case BACKGROUND_FOCUS:
                // 专注于背景信息检索
                return memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "背景信息 " + task, 30);
            case TIMELINE_RECONSTRUCTION_FOCUS:
                // 专注于时间线重建检索
                return memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "时间线重建 " + task, 30);
            case GLOBAL_SEARCH:
            default:
                // 全局搜索
                return memoryHierarchyService.multiLevelRetrieval(gameId, playerId, task, 30);
        }
    }

    /**
     * 构建推理链
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @param query    查询文本
     * @return 推理链
     */
    public ReasoningChain buildReasoningChain(Long gameId, Long playerId, String query) {
        try {
            log.info("构建推理链，游戏ID: {}, 玩家ID: {}, 查询: {}", gameId, playerId, query);

            ReasoningChain reasoningChain = new ReasoningChain(gameId, playerId, query);

            // 评估问题复杂度，确定推理链深度
            int maxDepth = evaluateProblemComplexity(query);
            log.info("评估问题复杂度，确定推理链深度: {}", maxDepth);

            // 执行多轮检索，构建推理链
            for (int step = 1; step <= maxDepth; step++) {
                String currentQuery = generateChainQuery(query, step, maxDepth);
                List<Map<String, Object>> results = executeActiveMemoryRetrieval(gameId, playerId, currentQuery, "推理步骤 " + step);

                if (!results.isEmpty()) {
                    reasoningChain.addStep(new ReasoningStep(step, currentQuery, results));
                }

            }

            log.info("推理链构建完成，游戏ID: {}, 玩家ID: {}, 步骤数: {}", gameId, playerId, reasoningChain.getSteps().size());
            return reasoningChain;

        } catch (Exception e) {
            log.error("构建推理链失败: {}", e.getMessage(), e);
            return new ReasoningChain(gameId, playerId, query);
        }
    }

    /**
     * 评估问题复杂度
     *
     * @param query 查询文本
     * @return 推理链深度
     */
    private int evaluateProblemComplexity(String query) {
        String queryLower = query.toLowerCase();
        int complexityScore = 3; // 默认深度
        
        // 基于查询长度评估复杂度
        if (query.length() > 100) {
            complexityScore += 1;
        }
        
        // 基于查询内容评估复杂度
        if (queryLower.contains("为什么") || queryLower.contains("原因")) {
            complexityScore += 1;
        }
        if (queryLower.contains("如何") || queryLower.contains("怎样")) {
            complexityScore += 1;
        }
        if (queryLower.contains("所有") || queryLower.contains("全部")) {
            complexityScore += 1;
        }
        if (queryLower.contains("关系") || queryLower.contains("联系")) {
            complexityScore += 1;
        }
        if (queryLower.contains("分析") || queryLower.contains("推理")) {
            complexityScore += 1;
        }
        
        // 限制深度范围
        return Math.max(3, Math.min(6, complexityScore));
    }

    /**
     * 生成推理链查询
     *
     * @param originalQuery 原始查询
     * @param step          推理步骤
     * @param maxDepth      最大深度
     * @return 查询文本
     */
    private String generateChainQuery(String originalQuery, int step, int maxDepth) {
        switch (step) {
            case 1:
                return "关于" + originalQuery + "的基本信息";
            case 2:
                return "关于" + originalQuery + "的详细分析";
            case 3:
                if (maxDepth <= 3) {
                    return "关于" + originalQuery + "的结论和证据";
                } else {
                    return "关于" + originalQuery + "的深度分析";
                }
            case 4:
                return "关于" + originalQuery + "的关联因素分析";
            case 5:
                return "关于" + originalQuery + "的证据链验证";
            case 6:
                return "关于" + originalQuery + "的最终结论和完整证据";
            default:
                return originalQuery;
        }
    }

    /**
     * 生成推理链查询（兼容旧方法）
     *
     * @param originalQuery 原始查询
     * @param step          推理步骤
     * @return 查询文本
     */
    private String generateChainQuery(String originalQuery, int step) {
        return generateChainQuery(originalQuery, step, 3);
    }

    /**
     * 思考状态类
     */
    public static class ThinkingState {
        private final Long gameId;
        private final Long playerId;
        private final String task;
        private int currentRound;
        private final List<AnalysisResult> analysisResults;
        private final List<List<Map<String, Object>>> retrievalResults;
        private final long startTime;

        public ThinkingState(Long gameId, Long playerId, String task) {
            this.gameId = gameId;
            this.playerId = playerId;
            this.task = task;
            this.currentRound = 0;
            this.analysisResults = new ArrayList<>();
            this.retrievalResults = new ArrayList<>();
            this.startTime = System.currentTimeMillis();
        }

        public Long getGameId() {
            return gameId;
        }

        public Long getPlayerId() {
            return playerId;
        }

        public String getTask() {
            return task;
        }

        public int getCurrentRound() {
            return currentRound;
        }

        public List<AnalysisResult> getAnalysisResults() {
            return analysisResults;
        }

        public List<List<Map<String, Object>>> getRetrievalResults() {
            return retrievalResults;
        }

        public long getStartTime() {
            return startTime;
        }

        public void incrementRound() {
            currentRound++;
        }

        public void addAnalysisResult(AnalysisResult result) {
            analysisResults.add(result);
        }

        public void addRetrievalResults(List<Map<String, Object>> results) {
            retrievalResults.add(results);
        }
    }

    /**
     * 分析结果类
     */
    public static class AnalysisResult {
        private final List<String> gaps;
        private final boolean complete;
        private final int round;

        public AnalysisResult(List<String> gaps, boolean complete, int round) {
            this.gaps = gaps;
            this.complete = complete;
            this.round = round;
        }

        public List<String> getGaps() {
            return gaps;
        }

        public boolean isComplete() {
            return complete;
        }

        public int getRound() {
            return round;
        }
    }

    /**
     * 检索策略枚举
     */
    public enum RetrievalStrategy {
        GLOBAL_SEARCH,      // 全局搜索
        CLUE_FOCUS,         // 线索专注
        TIMELINE_FOCUS,     // 时间线专注
        PLAYER_FOCUS,       // 玩家专注
        DISCUSSION_FOCUS,   // 讨论专注
        MOTIVE_FOCUS,       // 动机分析专注
        EVIDENCE_FOCUS,     // 证据分析专注
        RELATIONSHIP_FOCUS, // 关系分析专注
        BACKGROUND_FOCUS,   // 背景信息专注
        TIMELINE_RECONSTRUCTION_FOCUS // 时间线重建专注
    }

    /**
     * 推理链类
     */
    public static class ReasoningChain {
        private final Long gameId;
        private final Long playerId;
        private final String query;
        private final List<ReasoningStep> steps;
        private final long creationTime;

        public ReasoningChain(Long gameId, Long playerId, String query) {
            this.gameId = gameId;
            this.playerId = playerId;
            this.query = query;
            this.steps = new ArrayList<>();
            this.creationTime = System.currentTimeMillis();
        }

        public Long getGameId() {
            return gameId;
        }

        public Long getPlayerId() {
            return playerId;
        }

        public String getQuery() {
            return query;
        }

        public List<ReasoningStep> getSteps() {
            return steps;
        }

        public long getCreationTime() {
            return creationTime;
        }

        public void addStep(ReasoningStep step) {
            steps.add(step);
        }

        public boolean isComplete() {
            // 动态判断推理链是否完整
            // 对于简单问题，3步即可
            // 对于复杂问题，需要更多步骤
            int minSteps = 3;
            return steps.size() >= minSteps;
        }
    }

    /**
     * 推理步骤类
     */
    public static class ReasoningStep {
        private final int step;
        private final String query;
        private final List<Map<String, Object>> evidence;

        public ReasoningStep(int step, String query, List<Map<String, Object>> evidence) {
            this.step = step;
            this.query = query;
            this.evidence = evidence;
        }

        public int getStep() {
            return step;
        }

        public String getQuery() {
            return query;
        }

        public List<Map<String, Object>> getEvidence() {
            return evidence;
        }
    }

    /**
     * 反馈数据类
     */
    public static class FeedbackData {
        private final String task;
        private final String strategy;
        private final int successCount;
        private final int totalCount;
        private final double averageScore;
        private final long lastUpdated;

        public FeedbackData(String task, String strategy, int successCount, int totalCount, double averageScore) {
            this.task = task;
            this.strategy = strategy;
            this.successCount = successCount;
            this.totalCount = totalCount;
            this.averageScore = averageScore;
            this.lastUpdated = System.currentTimeMillis();
        }

        public String getTask() {
            return task;
        }

        public String getStrategy() {
            return strategy;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public double getAverageScore() {
            return averageScore;
        }

        public long getLastUpdated() {
            return lastUpdated;
        }

        public double getSuccessRate() {
            return totalCount > 0 ? (double) successCount / totalCount : 0.0;
        }
    }

    /**
     * 策略统计类
     */
    public static class StrategyStats {
        private int successCount;
        private int totalCount;
        private double totalScore;
        private long lastUsed;

        public StrategyStats() {
            this.successCount = 0;
            this.totalCount = 0;
            this.totalScore = 0.0;
            this.lastUsed = System.currentTimeMillis();
        }

        public synchronized void recordAttempt(double score, boolean success) {
            totalCount++;
            totalScore += score;
            if (success) {
                successCount++;
            }
            lastUsed = System.currentTimeMillis();
        }

        public synchronized double getSuccessRate() {
            return totalCount > 0 ? (double) successCount / totalCount : 0.0;
        }

        public synchronized double getAverageScore() {
            return totalCount > 0 ? totalScore / totalCount : 0.0;
        }

        public synchronized int getTotalCount() {
            return totalCount;
        }

        public long getLastUsed() {
            return lastUsed;
        }
    }

    /**
     * 记录检索反馈
     *
     * @param task     任务文本
     * @param strategy 检索策略
     * @param score    平均相似度分数
     * @param success  是否成功
     */
    public void recordRetrievalFeedback(String task, RetrievalStrategy strategy, double score, boolean success) {
        try {
            // 记录策略统计
            StrategyStats stats = strategyStats.computeIfAbsent(strategy, k -> new StrategyStats());
            stats.recordAttempt(score, success);

            // 记录任务级别的反馈
            String feedbackKey = generateFeedbackKey(task, strategy.name());
            FeedbackData existingData = feedbackCache.get(feedbackKey);

            if (existingData != null) {
                int newSuccessCount = existingData.getSuccessCount() + (success ? 1 : 0);
                int newTotalCount = existingData.getTotalCount() + 1;
                double newAverageScore = (existingData.getAverageScore() * existingData.getTotalCount() + score) / newTotalCount;

                FeedbackData newData = new FeedbackData(
                        task,
                        strategy.name(),
                        newSuccessCount,
                        newTotalCount,
                        newAverageScore
                );
                feedbackCache.put(feedbackKey, newData);
            } else {
                FeedbackData newData = new FeedbackData(
                        task,
                        strategy.name(),
                        success ? 1 : 0,
                        1,
                        score
                );
                feedbackCache.put(feedbackKey, newData);
            }

            log.debug("记录检索反馈，任务: {}, 策略: {}, 分数: {}, 成功: {}", task, strategy, score, success);
        } catch (Exception e) {
            log.error("记录检索反馈失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 生成反馈键
     *
     * @param task     任务文本
     * @param strategy 策略名称
     * @return 反馈键
     */
    private String generateFeedbackKey(String task, String strategy) {
        return "feedback:" + task.hashCode() + ":" + strategy;
    }

    /**
     * 自适应调整检索策略
     *
     * @param task 当前任务
     * @return 调整后的检索策略
     */
    private RetrievalStrategy adaptRetrievalStrategy(String task) {
        // 基于历史反馈数据选择最优策略
        double bestScore = 0.0;
        RetrievalStrategy bestStrategy = null;

        for (RetrievalStrategy strategy : RetrievalStrategy.values()) {
            String feedbackKey = generateFeedbackKey(task, strategy.name());
            FeedbackData feedbackData = feedbackCache.get(feedbackKey);

            if (feedbackData != null) {
                double successRate = feedbackData.getSuccessRate();
                double averageScore = feedbackData.getAverageScore();
                double combinedScore = successRate * 0.7 + averageScore * 0.3;

                if (combinedScore > bestScore) {
                    bestScore = combinedScore;
                    bestStrategy = strategy;
                }
            }
        }

        // 如果没有历史数据，使用默认策略选择
        if (bestStrategy == null) {
            return selectRetrievalStrategy(task, "");
        }

        log.debug("自适应选择检索策略: {}, 得分: {}", bestStrategy, bestScore);
        return bestStrategy;
    }

    /**
     * 获取策略统计信息
     *
     * @return 策略统计信息
     */
    public Map<String, Object> getStrategyStats() {
        Map<String, Object> statsMap = new HashMap<>();

        for (Map.Entry<RetrievalStrategy, StrategyStats> entry : strategyStats.entrySet()) {
            RetrievalStrategy strategy = entry.getKey();
            StrategyStats stats = entry.getValue();

            Map<String, Object> strategyStatsMap = new HashMap<>();
            strategyStatsMap.put("successRate", stats.getSuccessRate());
            strategyStatsMap.put("averageScore", stats.getAverageScore());
            strategyStatsMap.put("totalCount", stats.getTotalCount());
            strategyStatsMap.put("lastUsed", stats.getLastUsed());

            statsMap.put(strategy.name(), strategyStatsMap);
        }

        return statsMap;
    }
}
