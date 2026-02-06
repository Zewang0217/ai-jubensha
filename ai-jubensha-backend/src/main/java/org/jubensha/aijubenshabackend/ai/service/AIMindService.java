package org.jubensha.aijubenshabackend.ai.service;


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
        
        // 添加任务信息
        contextBuilder.append("任务: ").append(thinkingState.getTask()).append(" ");
        
        // 添加之前的分析结果
        List<AnalysisResult> analysisResults = thinkingState.getAnalysisResults();
        if (!analysisResults.isEmpty()) {
            AnalysisResult lastResult = analysisResults.get(analysisResults.size() - 1);
            contextBuilder.append("之前的分析: ");
            if (!lastResult.getGaps().isEmpty()) {
                contextBuilder.append("需要补充: " + String.join(", ", lastResult.getGaps()));
            }
        }
        
        return contextBuilder.toString();
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

        // 分析检索结果，识别知识 gaps
        if (retrievalResults.isEmpty()) {
            gaps.add("基本信息");
        } else {
            // 检查结果的相关性和完整性
            double averageScore = retrievalResults.stream()
                    .mapToDouble(result -> (double) result.getOrDefault("score", 0.0))
                    .average()
                    .orElse(0.0);

            if (averageScore < 0.5) {
                gaps.add("相关信息");
            }

            // 检查是否包含足够的信息
            if (retrievalResults.size() >= 10) {
                complete = true;
            }
        }

        return new AnalysisResult(gaps, complete, round);
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
        // 根据任务类型选择检索策略
        if (task.contains("线索")) {
            return RetrievalStrategy.CLUE_FOCUS;
        } else if (task.contains("时间线")) {
            return RetrievalStrategy.TIMELINE_FOCUS;
        } else if (task.contains("玩家")) {
            return RetrievalStrategy.PLAYER_FOCUS;
        } else if (task.contains("讨论")) {
            return RetrievalStrategy.DISCUSSION_FOCUS;
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

            // 执行多轮检索，构建推理链
            for (int step = 1; step <= 3; step++) {
                String currentQuery = generateChainQuery(query, step);
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
     * 生成推理链查询
     *
     * @param originalQuery 原始查询
     * @param step          推理步骤
     * @return 查询文本
     */
    private String generateChainQuery(String originalQuery, int step) {
        switch (step) {
            case 1:
                return "关于" + originalQuery + "的基本信息";
            case 2:
                return "关于" + originalQuery + "的详细分析";
            case 3:
                return "关于" + originalQuery + "的结论和证据";
            default:
                return originalQuery;
        }
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
        DISCUSSION_FOCUS    // 讨论专注
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
            return steps.size() >= 3;
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
}
