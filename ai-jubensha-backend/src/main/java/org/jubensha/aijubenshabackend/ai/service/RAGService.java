package org.jubensha.aijubenshabackend.ai.service;

import java.util.List;
import java.util.Map;

/**
 * RAG检索服务接口
 * 提供语义搜索和基于线索的过滤查询功能
 *
 * 根据最终架构设计，主要使用两个集合：
 * 1. 对话记忆集合 (conversation_{gameId}) - 存储每局游戏的对话历史
 * 2. 全局记忆集合 (global_memory) - 存储所有剧本的线索和时间线数据
 */
public interface RAGService {

    /**
     * 语义搜索对话记忆
     * 在特定游戏的对话记忆集合中搜索相关对话
     *
     * @param gameId 游戏ID，用于确定对应的对话记忆集合 (conversation_{gameId})
     * @param playerId 玩家ID，用于过滤特定玩家的对话（可选，传null表示搜索所有玩家）
     * @param query 查询文本，将转换为向量进行相似性搜索
     * @param topK 返回结果数量
     * @return 检索结果，包含对话内容、玩家信息等
     */
    List<Map<String, Object>> searchConversationMemory(Long gameId, Long playerId, String query, int topK);

    /**
     * 语义搜索全局线索记忆
     * 在全局记忆集合中搜索线索类型的记录
     *
     * @param scriptId 剧本ID，用于过滤特定剧本的线索
     * @param characterId 角色ID，用于过滤特定角色的线索（可选，传null表示搜索所有角色）
     * @param query 查询文本，将转换为向量进行相似性搜索
     * @param topK 返回结果数量
     * @return 检索结果，包含线索内容、剧本信息等
     */
    List<Map<String, Object>> searchGlobalClueMemory(Long scriptId, Long characterId, String query, int topK);

    /**
     * 语义搜索全局时间线记忆
     * 在全局记忆集合中搜索时间线类型的记录
     *
     * @param scriptId 剧本ID，用于过滤特定剧本的时间线
     * @param characterId 角色ID，用于过滤特定角色的时间线（可选，传null表示搜索所有角色）
     * @param query 查询文本，将转换为向量进行相似性搜索
     * @param topK 返回结果数量
     * @return 检索结果，包含时间线内容、剧本信息等
     */
    List<Map<String, Object>> searchGlobalTimelineMemory(Long scriptId, Long characterId, String query, int topK);

    /**
     * 基于线索的过滤查询
     * 结合已发现的线索信息进行更精准的搜索
     *
     * @param gameId 游戏ID
     * @param playerId 玩家ID
     * @param discoveredClueIds 已发现的线索ID列表
     * @param query 查询文本
     * @param topK 返回结果数量
     * @return 检索结果
     */
    List<Map<String, Object>> filterByDiscoveredClues(Long gameId, Long playerId, List<Long> discoveredClueIds, String query, int topK);

    /**
     * 计算线索关联强度
     * 计算两个线索之间的语义关联强度
     *
     * @param gameId 游戏ID
     * @param clueId1 线索ID1
     * @param clueId2 线索ID2
     * @return 关联强度（0-100）
     */
    int calculateClueRelationStrength(Long gameId, Long clueId1, Long clueId2);

    /**
     * 插入对话记忆
     * 向特定游戏的对话记忆集合中插入新的对话记录
     *
     * @param gameId 游戏ID
     * @param playerId 玩家ID
     * @param playerName 玩家名称
     * @param content 对话内容
     * @return 插入的记录ID
     */
    Long insertConversationMemory(Long gameId, Long playerId, String playerName, String content);

    /**
     * 批量插入对话记忆
     * 批量向特定游戏的对话记忆集合中插入对话记录
     *
     * @param gameId 游戏ID
     * @param conversationRecords 对话记录列表，每条记录包含playerId、playerName、content
     * @return 插入的记录ID列表
     */
    List<Long> batchInsertConversationMemory(Long gameId, List<Map<String, Object>> conversationRecords);

    /**
     * 插入全局线索记忆
     * 向全局记忆集合中插入新的线索记录
     *
     * @param scriptId 剧本ID
     * @param characterId 角色ID
     * @param content 线索内容
     * @return 插入的记录ID
     */
    Long insertGlobalClueMemory(Long scriptId, Long characterId, String content);

    /**
     * 插入全局时间线记忆
     * 向全局记忆集合中插入新的时间线记录
     *
     * @param scriptId 剧本ID
     * @param characterId 角色ID
     * @param content 时间线内容
     * @param timestamp 时间点
     * @return 插入的记录ID
     */
    Long insertGlobalTimelineMemory(Long scriptId, Long characterId, String content, String timestamp);

    /**
     * 批量插入全局记忆
     * 批量向全局记忆集合中插入线索或时间线记录
     *
     * @param memoryRecords 记忆记录列表，每条记录包含scriptId、characterId、type、content等字段
     * @return 插入的记录ID列表
     */
    List<Long> batchInsertGlobalMemory(List<Map<String, Object>> memoryRecords);

    /**
     * 更新对话记忆
     * 更新特定游戏的对话记忆记录
     *
     * @param gameId 游戏ID
     * @param recordId 记录ID
     * @param content 新的对话内容
     * @return 是否更新成功
     */
    boolean updateConversationMemory(Long gameId, Long recordId, String content);

    /**
     * 更新全局记忆
     * 更新全局记忆集合中的记录
     *
     * @param recordId 记录ID
     * @param content 新的内容
     * @return 是否更新成功
     */
    boolean updateGlobalMemory(Long recordId, String content);

    /**
     * 删除对话记忆
     * 从特定游戏的对话记忆集合中删除记录
     *
     * @param gameId 游戏ID
     * @param recordId 记录ID
     * @return 是否删除成功
     */
    boolean deleteConversationMemory(Long gameId, Long recordId);

    /**
     * 删除全局记忆
     * 从全局记忆集合中删除记录
     *
     * @param recordId 记录ID
     * @return 是否删除成功
     */
    boolean deleteGlobalMemory(Long recordId);

    /**
     * 批量删除对话记忆
     * 批量从特定游戏的对话记忆集合中删除记录
     *
     * @param gameId 游戏ID
     * @param recordIds 记录ID列表
     * @return 删除成功的记录数
     */
    int batchDeleteConversationMemory(Long gameId, List<Long> recordIds);

    /**
     * 批量删除全局记忆
     * 批量从全局记忆集合中删除记录
     *
     * @param recordIds 记录ID列表
     * @return 删除成功的记录数
     */
    int batchDeleteGlobalMemory(List<Long> recordIds);
}
