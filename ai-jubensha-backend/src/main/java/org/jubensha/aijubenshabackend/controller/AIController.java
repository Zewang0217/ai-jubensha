package org.jubensha.aijubenshabackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.AIMindService;
import org.jubensha.aijubenshabackend.ai.service.DiscussionService;
import org.jubensha.aijubenshabackend.ai.service.MemoryHierarchyService;
import org.jubensha.aijubenshabackend.ai.service.MessageQueueService;
import org.jubensha.aijubenshabackend.ai.service.RerankingService;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI功能测试控制器
 * 用于测试AI相关的功能，包括agent通信、推理链、多轮思考等
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIMindService aiMindService;
    private final MemoryHierarchyService memoryHierarchyService;
    private final RerankingService rerankingService;
    private final RAGService ragService;
    private final MessageQueueService messageQueueService;
    private final DiscussionService discussionService;

    public AIController(AIMindService aiMindService, MemoryHierarchyService memoryHierarchyService,
                       RerankingService rerankingService, RAGService ragService,
                       MessageQueueService messageQueueService, DiscussionService discussionService) {
        this.aiMindService = aiMindService;
        this.memoryHierarchyService = memoryHierarchyService;
        this.rerankingService = rerankingService;
        this.ragService = ragService;
        this.messageQueueService = messageQueueService;
        this.discussionService = discussionService;
    }

    /**
     * 测试agent之间的通信
     *
     * @param request 包含gameId、playerId和message的请求
     * @return 测试结果
     */
    @PostMapping("/test-communication")
    public ResponseEntity<?> testCommunication(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            Long playerId = Long.parseLong(request.get("playerId").toString());
            String message = (String) request.get("message");

            log.info("测试agent通信，游戏ID: {}, 玩家ID: {}, 消息: {}", gameId, playerId, message);

            // 测试通信功能 - 实际发送消息到RabbitMQ
            java.util.List<Long> recipientIds = java.util.List.of(playerId);
            messageQueueService.sendDiscussionMessage(message, recipientIds);

            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "Agent通信测试成功",
                    "gameId", gameId,
                    "playerId", playerId,
                    "testMessage", message,
                    "mqSent", true
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("测试agent通信失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "测试失败", "message", e.getMessage()));
        }
    }

    /**
     * 测试推理链
     *
     * @param request 包含gameId、playerId和query的请求
     * @return 推理链结果
     */
    @PostMapping("/test-reasoning-chain")
    public ResponseEntity<?> testReasoningChain(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            Long playerId = Long.parseLong(request.get("playerId").toString());
            String query = (String) request.get("query");

            log.info("测试推理链，游戏ID: {}, 玩家ID: {}, 查询: {}", gameId, playerId, query);

            // 构建推理链
            var reasoningChain = aiMindService.buildReasoningChain(gameId, playerId, query);

            // 构建响应
            Map<String, Object> response = Map.of(
                    "success", true,
                    "gameId", gameId,
                    "playerId", playerId,
                    "query", query,
                    "reasoningChain", Map.of(
                            "steps", reasoningChain.getSteps().stream()
                                    .map(step -> Map.of(
                                            "step", step.getStep(),
                                            "query", step.getQuery(),
                                            "evidenceCount", step.getEvidence().size()
                                    ))
                                    .toList(),
                            "isComplete", reasoningChain.isComplete(),
                            "creationTime", reasoningChain.getCreationTime()
                    )
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("测试推理链失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "测试失败", "message", e.getMessage()));
        }
    }

    /**
     * 测试多轮思考
     *
     * @param request 包含gameId、playerId、task和maxRounds的请求
     * @return 多轮思考结果
     */
    @PostMapping("/test-multi-round-thinking")
    public ResponseEntity<?> testMultiRoundThinking(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            Long playerId = Long.parseLong(request.get("playerId").toString());
            String task = (String) request.get("task");
            int maxRounds = Integer.parseInt(request.get("maxRounds").toString());

            log.info("测试多轮思考，游戏ID: {}, 玩家ID: {}, 任务: {}, 最大轮数: {}", gameId, playerId, task, maxRounds);

            // 开始思考过程
            String thinkingId = aiMindService.startThinking(gameId, playerId, task);

            // 执行多轮思考
            List<Map<String, Object>> results = aiMindService.executeMultiRoundThinking(thinkingId, maxRounds);

            // 获取思考状态
            var thinkingState = aiMindService.getThinkingState(thinkingId);

            // 构建响应
            Map<String, Object> response = Map.of(
                    "success", true,
                    "gameId", gameId,
                    "playerId", playerId,
                    "task", task,
                    "thinkingId", thinkingId,
                    "maxRounds", maxRounds,
                    "resultsCount", results.size(),
                    "currentRound", thinkingState != null ? thinkingState.getCurrentRound() : 0,
                    "analysisResultsCount", thinkingState != null ? thinkingState.getAnalysisResults().size() : 0
            );

            // 清理思考状态
            aiMindService.cleanupThinkingState(thinkingId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("测试多轮思考失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "测试失败", "message", e.getMessage()));
        }
    }

    /**
     * 测试记忆检索
     *
     * @param request 包含gameId、playerId、query和topK的请求
     * @return 记忆检索结果
     */
    @PostMapping("/test-memory-retrieval")
    public ResponseEntity<?> testMemoryRetrieval(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            Long playerId = Long.parseLong(request.get("playerId").toString());
            String query = (String) request.get("query");
            int topK = Integer.parseInt(request.get("topK").toString());

            log.info("测试记忆检索，游戏ID: {}, 玩家ID: {}, 查询: {}, TopK: {}", gameId, playerId, query, topK);

            // 执行多级记忆检索
            List<Map<String, Object>> results = memoryHierarchyService.multiLevelRetrieval(gameId, playerId, query, topK);

            // 构建响应
            Map<String, Object> response = Map.of(
                    "success", true,
                    "gameId", gameId,
                    "playerId", playerId,
                    "query", query,
                    "topK", topK,
                    "resultsCount", results.size(),
                    "results", results.stream()
                            .map(result -> Map.of(
                                    "content", result.get("content"),
                                    "score", result.get("score"),
                                    "playerName", result.get("player_name")
                            ))
                            .limit(5) // 只返回前5个结果
                            .toList()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("测试记忆检索失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "测试失败", "message", e.getMessage()));
        }
    }

    /**
     * 测试智能记忆检索
     *
     * @param request 包含gameId、playerId、query、context和topK的请求
     * @return 智能检索结果
     */
    @PostMapping("/test-intelligent-retrieval")
    public ResponseEntity<?> testIntelligentRetrieval(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            Long playerId = Long.parseLong(request.get("playerId").toString());
            String query = (String) request.get("query");
            String context = (String) request.get("context");
            int topK = Integer.parseInt(request.get("topK").toString());

            log.info("测试智能记忆检索，游戏ID: {}, 玩家ID: {}, 查询: {}, 上下文长度: {}, TopK: {}",
                    gameId, playerId, query, context.length(), topK);

            // 执行智能记忆检索
            List<Map<String, Object>> results = memoryHierarchyService.intelligentRetrieval(gameId, playerId, query, context, topK);

            // 构建响应
            Map<String, Object> response = Map.of(
                    "success", true,
                    "gameId", gameId,
                    "playerId", playerId,
                    "query", query,
                    "context", context,
                    "topK", topK,
                    "resultsCount", results.size(),
                    "results", results.stream()
                            .map(result -> Map.of(
                                    "content", result.get("content"),
                                    "score", result.get("score"),
                                    "playerName", result.get("player_name")
                            ))
                            .limit(5) // 只返回前5个结果
                            .toList()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("测试智能记忆检索失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "测试失败", "message", e.getMessage()));
        }
    }

    /**
     * 测试重排功能
     *
     * @param request 包含query、results和topK的请求
     * @return 重排结果
     */
    @PostMapping("/test-reranking")
    public ResponseEntity<?> testReranking(@RequestBody Map<String, Object> request) {
        try {
            String query = (String) request.get("query");
            List<Map<String, Object>> results = (List<Map<String, Object>>) request.get("results");
            int topK = Integer.parseInt(request.get("topK").toString());

            log.info("测试重排功能，查询: {}, 结果数量: {}, TopK: {}", query, results.size(), topK);

            // 执行重排
            List<Map<String, Object>> rerankedResults = rerankingService.rerank(query, results, topK);

            // 构建响应
            Map<String, Object> response = Map.of(
                    "success", true,
                    "query", query,
                    "originalResultsCount", results.size(),
                    "rerankedResultsCount", rerankedResults.size(),
                    "topK", topK,
                    "rerankedResults", rerankedResults.stream()
                            .map(result -> Map.of(
                                    "content", result.get("content"),
                                    "relevanceScore", result.get("relevance_score"),
                                    "originalScore", result.get("score")
                            ))
                            .toList()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("测试重排功能失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "测试失败", "message", e.getMessage()));
        }
    }

    /**
     * 测试主动记忆检索
     *
     * @param request 包含gameId、playerId、task和context的请求
     * @return 主动检索结果
     */
    @PostMapping("/test-active-retrieval")
    public ResponseEntity<?> testActiveRetrieval(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            Long playerId = Long.parseLong(request.get("playerId").toString());
            String task = (String) request.get("task");
            String context = (String) request.get("context");

            log.info("测试主动记忆检索，游戏ID: {}, 玩家ID: {}, 任务: {}, 上下文长度: {}",
                    gameId, playerId, task, context.length());

            // 执行主动记忆检索
            List<Map<String, Object>> results = aiMindService.executeActiveMemoryRetrieval(gameId, playerId, task, context);

            // 构建响应
            Map<String, Object> response = Map.of(
                    "success", true,
                    "gameId", gameId,
                    "playerId", playerId,
                    "task", task,
                    "context", context,
                    "resultsCount", results.size(),
                    "results", results.stream()
                            .map(result -> Map.of(
                                    "content", result.get("content"),
                                    "score", result.get("score"),
                                    "playerName", result.get("player_name")
                            ))
                            .limit(5) // 只返回前5个结果
                            .toList()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("测试主动记忆检索失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "测试失败", "message", e.getMessage()));
        }
    }

    /**
     * 测试知识gap分析
     *
     * @param request 包含gameId、playerId、task和maxRounds的请求
     * @return 知识gap分析结果
     */
    @PostMapping("/test-knowledge-gap")
    public ResponseEntity<?> testKnowledgeGap(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            Long playerId = Long.parseLong(request.get("playerId").toString());
            String task = (String) request.get("task");
            int maxRounds = Integer.parseInt(request.get("maxRounds").toString());

            log.info("测试知识gap分析，游戏ID: {}, 玩家ID: {}, 任务: {}, 最大轮数: {}", gameId, playerId, task, maxRounds);

            // 开始思考过程
            String thinkingId = aiMindService.startThinking(gameId, playerId, task);

            // 执行多轮思考
            List<Map<String, Object>> results = aiMindService.executeMultiRoundThinking(thinkingId, maxRounds);

            // 获取思考状态
            var thinkingState = aiMindService.getThinkingState(thinkingId);

            // 提取知识gap
            List<Map<String, Object>> knowledgeGaps = new ArrayList<>();
            if (thinkingState != null) {
                knowledgeGaps = thinkingState.getAnalysisResults().stream()
                        .map(analysisResult -> {
                            Map<String, Object> gapInfo = new java.util.HashMap<>();
                            gapInfo.put("round", analysisResult.getRound());
                            gapInfo.put("gaps", analysisResult.getGaps());
                            gapInfo.put("isComplete", analysisResult.isComplete());
                            return gapInfo;
                        })
                        .collect(java.util.stream.Collectors.toList());
            }

            // 构建响应
            Map<String, Object> response = Map.of(
                    "success", true,
                    "gameId", gameId,
                    "playerId", playerId,
                    "task", task,
                    "thinkingId", thinkingId,
                    "maxRounds", maxRounds,
                    "resultsCount", results.size(),
                    "knowledgeGapsCount", knowledgeGaps.size(),
                    "knowledgeGaps", knowledgeGaps
            );

            // 清理思考状态
            aiMindService.cleanupThinkingState(thinkingId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("测试知识gap分析失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "测试失败", "message", e.getMessage()));
        }
    }

    /**
     * 测试讨论节点功能
     *
     * @param request 包含gameId、playerIds、dmId和judgeId的请求
     * @return 讨论测试结果
     */
    @PostMapping("/test-discussion")
    public ResponseEntity<?> testDiscussion(@RequestBody Map<String, Object> request) {
        try {
            // 解析请求参数
            Long gameId = Long.parseLong(request.get("gameId").toString());
            List<Long> playerIds = ((List<?>) request.get("playerIds")).stream()
                    .map(id -> Long.parseLong(id.toString()))
                    .collect(Collectors.toList());
            Long dmId = Long.parseLong(request.get("dmId").toString());
            Long judgeId = Long.parseLong(request.get("judgeId").toString());

            log.info("测试讨论节点，游戏ID: {}, 玩家数量: {}, DM ID: {}, Judge ID: {}", 
                    gameId, playerIds.size(), dmId, judgeId);

            // 验证参数有效性
            if (playerIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "playerIds cannot be empty"));
            }

            // 启动讨论服务
            discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

            // 启动陈述阶段
            discussionService.startStatementPhase();

            // 获取讨论状态
            Map<String, Object> discussionState = discussionService.getDiscussionState();

            // 构建响应
            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "讨论节点测试成功启动",
                    "gameId", gameId,
                    "playerIds", playerIds,
                    "dmId", dmId,
                    "judgeId", judgeId,
                    "discussionState", discussionState
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("测试讨论节点失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "测试失败", "message", e.getMessage()));
        }
    }

    /**
     * 继续讨论流程
     *
     * @param request 包含gameId和phase的请求
     * @return 讨论流程结果
     */
    @PostMapping("/continue-discussion")
    public ResponseEntity<?> continueDiscussion(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            String phase = (String) request.get("phase");

            log.info("继续讨论流程，游戏ID: {}, 阶段: {}", gameId, phase);

            // 根据阶段继续讨论流程
            switch (phase) {
                case "FREE_DISCUSSION":
                    discussionService.startFreeDiscussionPhase();
                    break;
                case "PRIVATE_CHAT":
                    discussionService.startPrivateChatPhase();
                    break;
                case "ANSWER":
                    discussionService.startAnswerPhase();
                    break;
                case "END":
                    Map<String, Object> endResult = discussionService.endDiscussion();
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "讨论已结束",
                            "endResult", endResult
                    ));
                default:
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid phase: " + phase));
            }

            // 获取讨论状态
            Map<String, Object> discussionState = discussionService.getDiscussionState();

            // 构建响应
            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "讨论流程已继续",
                    "gameId", gameId,
                    "phase", phase,
                    "discussionState", discussionState
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("继续讨论流程失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "操作失败", "message", e.getMessage()));
        }
    }
} 