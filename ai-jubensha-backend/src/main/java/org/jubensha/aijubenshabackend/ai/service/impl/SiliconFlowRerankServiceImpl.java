package org.jubensha.aijubenshabackend.ai.service.impl;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.client.SiliconFlowClient;
import org.jubensha.aijubenshabackend.ai.service.RerankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SiliconFlow重排服务实现
 * 使用SiliconFlow的rerank API进行重排
 */
@Slf4j
@Service
public class SiliconFlowRerankServiceImpl implements RerankService {

    private final SiliconFlowClient siliconFlowClient;

    @Value("${ai.reranking-model:BAAI/bge-reranker-v2-m3}")
    private String rerankingModel;

    @Value("${ai.reranking-base-url}")
    private String rerankingBaseUrl;

    @Value("${ai.reranking-api-key}")
    private String rerankingApiKey;

    @Autowired
    public SiliconFlowRerankServiceImpl(SiliconFlowClient siliconFlowClient) {
        this.siliconFlowClient = siliconFlowClient;
    }

    /**
     * 对搜索结果进行重排
     *
     * @param query   查询文本
     * @param results 搜索结果列表
     * @param topK    返回结果数量
     * @return 重排后的结果列表
     */
    @Override
    public List<Map<String, Object>> rerank(String query, List<Map<String, Object>> results, int topK) {
        try {
            log.info("开始使用SiliconFlow API重排搜索结果，查询: {}, 结果数量: {}", query, results.size());

            // 检查结果是否为空
            if (results.isEmpty()) {
                return results;
            }

            // 准备rerank API请求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", rerankingModel);
            requestBody.put("query", query);
            requestBody.put("top_n", topK);
            requestBody.put("return_documents", true);

            // 提取文档内容
            List<String> documents = results.stream()
                    .map(result -> {
                        String content = (String) result.get("content");
                        return content != null ? content : "";
                    })
                    .collect(Collectors.toList());
            requestBody.put("documents", documents);

            // 调用rerank API
            Map<String, Object> response;
            try {
                response = siliconFlowClient.callRerankApi(rerankingBaseUrl, rerankingApiKey, requestBody);
            } catch (Exception e) {
                log.warn("调用SiliconFlow rerank API失败，回退到简单重排: {}", e.getMessage());
                // 回退到简单重排
                return simpleRerank(query, results, topK);
            }

            // 处理响应
            List<Map<String, Object>> rerankedResults = processRerankResponse(response, results);

            log.info("重排完成，返回 {} 条结果", rerankedResults.size());
            return rerankedResults;

        } catch (Exception e) {
            log.error("重排失败: {}", e.getMessage(), e);
            return simpleRerank(query, results, topK); // 失败时回退到简单重排
        }
    }

    /**
     * 对嵌入匹配结果进行重排
     *
     * @param query   查询文本
     * @param matches 嵌入匹配结果列表
     * @param topK    返回结果数量
     * @return 重排后的结果列表
     */
    @Override
    public List<EmbeddingMatch> rerankEmbeddingMatches(String query, List<EmbeddingMatch> matches, int topK) {
        try {
            log.info("开始使用SiliconFlow API重排嵌入匹配结果，查询: {}, 结果数量: {}", query, matches.size());

            // 检查结果是否为空
            if (matches.isEmpty()) {
                return matches;
            }

            // 准备rerank API请求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", rerankingModel);
            requestBody.put("query", query);
            requestBody.put("top_n", topK);
            requestBody.put("return_documents", true);

            // 提取文档内容
            List<String> documents = matches.stream()
                    .map(match -> {
                        TextSegment segment = (TextSegment) match.embedded();
                        return segment.text();
                    })
                    .collect(Collectors.toList());
            requestBody.put("documents", documents);

            // 调用rerank API
            Map<String, Object> response;
            try {
                response = siliconFlowClient.callRerankApi(rerankingBaseUrl, rerankingApiKey, requestBody);
            } catch (Exception e) {
                log.warn("调用SiliconFlow rerank API失败，回退到原始排序: {}", e.getMessage());
                // 回退到原始排序
                return matches.stream()
                        .sorted((a, b) -> Double.compare(b.score(), a.score()))
                        .limit(topK)
                        .collect(Collectors.toList());
            }

            // 处理响应
            List<EmbeddingMatch> rerankedMatches = processEmbeddingMatchResponse(response, matches);

            log.info("重排完成，返回 {} 条结果", rerankedMatches.size());
            return rerankedMatches;

        } catch (Exception e) {
            log.error("重排嵌入匹配结果失败: {}", e.getMessage(), e);
            // 失败时回退到原始排序
            return matches.stream()
                    .sorted((a, b) -> Double.compare(b.score(), a.score()))
                    .limit(topK)
                    .collect(Collectors.toList());
        }
    }

    /**
     * 两步检索策略
     * 先通过向量搜索获取候选结果，再通过重排模型优化排序
     *
     * @param query          查询文本
     * @param candidateResults 候选结果列表
     * @param topK           返回结果数量
     * @return 重排后的结果列表
     */
    @Override
    public List<Map<String, Object>> twoStepRetrieval(String query, List<Map<String, Object>> candidateResults, int topK) {
        try {
            log.info("执行两步检索策略，查询: {}, 候选结果数量: {}", query, candidateResults.size());

            // 第一步：获取更多候选结果
            int candidateTopK = Math.min(topK * 3, candidateResults.size());
            List<Map<String, Object>> candidates = candidateResults.stream()
                    .limit(candidateTopK)
                    .collect(Collectors.toList());

            // 第二步：使用SiliconFlow API重排候选结果
            return rerank(query, candidates, topK);

        } catch (Exception e) {
            log.error("执行两步检索策略失败: {}", e.getMessage(), e);
            return candidateResults.stream()
                    .limit(topK)
                    .collect(Collectors.toList());
        }
    }

    /**
     * 处理rerank API响应
     *
     * @param response API响应
     * @param originalResults 原始结果列表
     * @return 重排后的结果列表
     */
    private List<Map<String, Object>> processRerankResponse(Map<String, Object> response, List<Map<String, Object>> originalResults) {
        List<Map<String, Object>> rerankedResults = new ArrayList<>();

        try {
            // 解析响应结果
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (results == null || results.isEmpty()) {
                return originalResults;
            }

            // 根据响应中的索引和分数重新排序原始结果
            for (Map<String, Object> result : results) {
                int index = (Integer) result.get("index");
                double score = 0.0;
                
                // 尝试获取relevance_score字段（SiliconFlow API返回的字段名）
                if (result.containsKey("relevance_score")) {
                    score = (Double) result.get("relevance_score");
                } else if (result.containsKey("score")) {
                    // 兼容其他API可能返回的score字段
                    score = (Double) result.get("score");
                }
                
                if (index >= 0 && index < originalResults.size()) {
                    Map<String, Object> originalResult = originalResults.get(index);
                    originalResult.put("relevance_score", score);
                    rerankedResults.add(originalResult);
                }
            }

        } catch (Exception e) {
            log.warn("处理rerank响应失败，使用原始结果: {}", e.getMessage());
            return originalResults;
        }

        return rerankedResults;
    }

    /**
     * 处理嵌入匹配结果的rerank响应
     *
     * @param response API响应
     * @param originalMatches 原始匹配结果列表
     * @return 重排后的匹配结果列表
     */
    private List<EmbeddingMatch> processEmbeddingMatchResponse(Map<String, Object> response, List<EmbeddingMatch> originalMatches) {
        List<EmbeddingMatch> rerankedMatches = new ArrayList<>();

        try {
            // 解析响应结果
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (results == null || results.isEmpty()) {
                return originalMatches;
            }

            // 根据响应中的索引和分数重新排序原始结果
            for (Map<String, Object> result : results) {
                int index = (Integer) result.get("index");
                
                if (index >= 0 && index < originalMatches.size()) {
                    rerankedMatches.add(originalMatches.get(index));
                }
            }

        } catch (Exception e) {
            log.warn("处理rerank响应失败，使用原始结果: {}", e.getMessage());
            return originalMatches;
        }

        return rerankedMatches;
    }

    /**
     * 简单重排实现（作为回退方案）
     *
     * @param query   查询文本
     * @param results 搜索结果列表
     * @param topK    返回结果数量
     * @return 重排后的结果列表
     */
    private List<Map<String, Object>> simpleRerank(String query, List<Map<String, Object>> results, int topK) {
        // 使用简单的文本相似度计算进行重排
        return results.stream()
                .map(result -> {
                    double relevanceScore = calculateRelevance(query, result);
                    result.put("relevance_score", relevanceScore);
                    return result;
                })
                .sorted((a, b) -> {
                    Double scoreA = (Double) a.getOrDefault("relevance_score", 0.0);
                    Double scoreB = (Double) b.getOrDefault("relevance_score", 0.0);
                    return scoreB.compareTo(scoreA);
                })
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * 计算查询与结果的相关性得分（作为回退方案）
     *
     * @param query  查询文本
     * @param result 搜索结果
     * @return 相关性得分
     */
    private double calculateRelevance(String query, Map<String, Object> result) {
        try {
            // 获取结果内容
            String content = (String) result.get("content");
            if (content == null) {
                return 0.0;
            }

            // 简单的文本相似度计算
            double score = 0.0;

            // 计算查询和内容的相似度
            String[] queryWords = query.toLowerCase().split("\\s+");
            String[] contentWords = content.toLowerCase().split("\\s+");

            int matchCount = 0;
            for (String queryWord : queryWords) {
                for (String contentWord : contentWords) {
                    if (queryWord.equals(contentWord)) {
                        matchCount++;
                        break;
                    }
                }
            }

            // 计算重叠度
            if (queryWords.length > 0) {
                score = (double) matchCount / queryWords.length;
            }

            // 结合原始相似度得分
            double originalScore = (double) result.getOrDefault("score", 0.0);
            return 0.7 * score + 0.3 * originalScore;

        } catch (Exception e) {
            log.error("计算相关性得分失败: {}", e.getMessage(), e);
            return (double) result.getOrDefault("score", 0.0);
        }
    }
}
