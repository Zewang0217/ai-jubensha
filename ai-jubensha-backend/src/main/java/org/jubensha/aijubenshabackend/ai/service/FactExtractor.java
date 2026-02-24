package org.jubensha.aijubenshabackend.ai.service;

import java.util.List;
import java.util.Map;

/**
 * 对话事实提取服务接口
 * 用于从对话中提取关键事实，支持父子文档检索策略
 */
public interface FactExtractor {

    /**
     * 从对话中提取关键事实
     *
     * @param content 对话内容
     * @return 提取的事实列表，每个事实包含content、type等字段
     */
    List<Map<String, Object>> extractFacts(String content);

    /**
     * 从对话中提取关键事实（带类型指定）
     *
     * @param content 对话内容
     * @param factTypes 要提取的事实类型列表，如["时间", "地点", "人物", "事件", "线索"]
     * @return 提取的事实列表
     */
    List<Map<String, Object>> extractFacts(String content, List<String> factTypes);

    /**
     * 批量提取事实
     *
     * @param contents 对话内容列表
     * @return 批量提取的事实列表
     */
    List<List<Map<String, Object>>> batchExtractFacts(List<String> contents);

    /**
     * 验证提取的事实质量
     *
     * @param facts 提取的事实列表
     * @return 事实质量评分（0-100）
     */
    int validateFactsQuality(List<Map<String, Object>> facts);
}
