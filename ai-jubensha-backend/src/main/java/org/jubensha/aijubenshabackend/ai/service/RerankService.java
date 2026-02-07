package org.jubensha.aijubenshabackend.ai.service;

import java.util.List;
import java.util.Map;

/**
 * 重排服务接口
 * 用于对搜索结果进行重排，提高搜索质量
 */
public interface RerankService {

    /**
     * 对搜索结果进行重排
     *
     * @param query   查询文本
     * @param results 搜索结果列表
     * @param topK    返回结果数量
     * @return 重排后的结果列表
     */
    List<Map<String, Object>> rerank(String query, List<Map<String, Object>> results, int topK);

    /**
     * 对嵌入匹配结果进行重排
     *
     * @param query   查询文本
     * @param matches 嵌入匹配结果列表
     * @param topK    返回结果数量
     * @return 重排后的结果列表
     */
    List<dev.langchain4j.store.embedding.EmbeddingMatch> rerankEmbeddingMatches(String query, List<dev.langchain4j.store.embedding.EmbeddingMatch> matches, int topK);

    /**
     * 两步检索策略
     * 先通过向量搜索获取候选结果，再通过重排模型优化排序
     *
     * @param query          查询文本
     * @param candidateResults 候选结果列表
     * @param topK           返回结果数量
     * @return 重排后的结果列表
     */
    List<Map<String, Object>> twoStepRetrieval(String query, List<Map<String, Object>> candidateResults, int topK);
}
