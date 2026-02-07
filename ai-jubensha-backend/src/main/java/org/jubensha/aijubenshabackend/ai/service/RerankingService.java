package org.jubensha.aijubenshabackend.ai.service;


import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 重排服务
 * 用于对搜索结果进行重排，提高搜索质量
 */
@Slf4j
@Service
public class RerankingService {

    private final EmbeddingModel embeddingModel;

    @Autowired
    public RerankingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * 对搜索结果进行重排
     *
     * @param query   查询文本
     * @param results 搜索结果列表
     * @param topK    返回结果数量
     * @return 重排后的结果列表
     */
    public List<Map<String, Object>> rerank(String query, List<Map<String, Object>> results, int topK) {
        try {
            log.info("开始重排搜索结果，查询: {}, 结果数量: {}", query, results.size());

            // 检查结果是否为空
            if (results.isEmpty()) {
                return results;
            }

            // 对结果进行重排
            List<Map<String, Object>> rerankedResults = results.stream()
                    .map(result -> {
                        // 计算与查询的相关性得分
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

            log.info("重排完成，返回 {} 条结果", rerankedResults.size());
            return rerankedResults;

        } catch (Exception e) {
            log.error("重排失败: {}", e.getMessage(), e);
            return results; // 失败时返回原始结果
        }
    }

    /**
     * 计算查询与结果的相关性得分
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

    /**
     * 计算两个向量的余弦相似度
     *
     * @param vec1 向量1
     * @param vec2 向量2
     * @return 余弦相似度
     */
    private double calculateCosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.size() != vec2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 对嵌入匹配结果进行重排
     *
     * @param query   查询文本
     * @param matches 嵌入匹配结果列表
     * @param topK    返回结果数量
     * @return 重排后的结果列表
     */
    public List<EmbeddingMatch> rerankEmbeddingMatches(String query, List<EmbeddingMatch> matches, int topK) {
        try {
            log.info("开始重排嵌入匹配结果，查询: {}, 结果数量: {}", query, matches.size());

            // 检查结果是否为空
            if (matches.isEmpty()) {
                return matches;
            }

            // 使用EmbeddingModel进行更准确的重排
            List<EmbeddingMatch> rerankedMatches = matches.stream()
                    .map(match -> {
                        // 获取文本段内容
                        TextSegment segment = (TextSegment) match.embedded();
                        String content = segment.text();

                        // 计算与查询的相关性得分
                        double relevanceScore = calculateRelevance(query, Map.of("content", content, "score", match.score()));

                        // 创建新的嵌入匹配结果
                        // 注意：这里简化处理，只使用原始的得分和嵌入
                        return match;
                    })
                    .sorted((a, b) -> {
                        // 计算每个匹配的相关性得分
                        TextSegment segmentA = (TextSegment) a.embedded();
                        TextSegment segmentB = (TextSegment) b.embedded();
                        
                        double scoreA = calculateRelevance(query, Map.of("content", segmentA.text(), "score", a.score()));
                        double scoreB = calculateRelevance(query, Map.of("content", segmentB.text(), "score", b.score()));
                        
                        return Double.compare(scoreB, scoreA);
                    })
                    .limit(topK)
                    .collect(Collectors.toList());

            log.info("重排完成，返回 {} 条结果", rerankedMatches.size());
            return rerankedMatches;

        } catch (Exception e) {
            log.error("重排嵌入匹配结果失败: {}", e.getMessage(), e);
            return matches; // 失败时返回原始结果
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
    public List<Map<String, Object>> twoStepRetrieval(String query, List<Map<String, Object>> candidateResults, int topK) {
        try {
            log.info("执行两步检索策略，查询: {}, 候选结果数量: {}", query, candidateResults.size());

            // 第一步：获取更多候选结果
            int candidateTopK = Math.min(topK * 3, candidateResults.size());
            List<Map<String, Object>> candidates = candidateResults.stream()
                    .limit(candidateTopK)
                    .collect(Collectors.toList());

            // 第二步：重排候选结果
            return rerank(query, candidates, topK);

        } catch (Exception e) {
            log.error("执行两步检索策略失败: {}", e.getMessage(), e);
            return candidateResults.stream()
                    .limit(topK)
                    .collect(Collectors.toList());
        }
    }
}
