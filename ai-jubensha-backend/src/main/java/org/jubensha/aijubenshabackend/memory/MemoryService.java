package org.jubensha.aijubenshabackend.memory;

import java.util.List;
import java.util.Map;

/**
 * 记忆管理服务接口
 * 负责管理角色记忆、对话记忆和线索记忆
 * <p>
 * 注意：以下方法需要使用Milvus向量数据库实现：
 * - 所有store*方法：将数据存储到Milvus向量数据库
 * - 所有retrieve*方法：从Milvus向量数据库检索数据
 * - deleteGameMemory方法：从Milvus向量数据库删除数据
 */
public interface MemoryService {

    /**
     * 存储角色记忆
     *
     * @param gameId        游戏ID
     * @param playerId      玩家ID
     * @param characterId   角色ID
     * @param characterInfo 角色信息
     */
    void storeCharacterMemory(Long gameId, Long playerId, Long characterId, Map<String, String> characterInfo);

    /**
     * 检索角色记忆
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @param query    查询文本
     * @param topK     返回结果数量
     * @return 检索结果
     */
    List<Map<String, Object>> retrieveCharacterMemory(Long gameId, Long playerId, String query, int topK);

    /**
     * 存储对话记忆
     *
     * @param gameId    游戏ID
     * @param playerId  玩家ID
     * @param content   对话内容
     * @param timestamp 时间戳
     */
    void storeConversationMemory(Long gameId, Long playerId, String content, long timestamp);

    /**
     * 检索对话记忆
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @param query    查询文本
     * @param topK     返回结果数量
     * @return 检索结果
     */
    List<Map<String, Object>> retrieveConversationMemory(Long gameId, Long playerId, String query, int topK);

    /**
     * 存储线索记忆
     *
     * @param gameId       游戏ID
     * @param playerId     玩家ID
     * @param clueId       线索ID
     * @param content      线索内容
     * @param discoveredBy 发现者
     */
    void storeClueMemory(Long gameId, Long playerId, Long clueId, String content, String discoveredBy);

    /**
     * 检索线索记忆
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @param query    查询文本
     * @param topK     返回结果数量
     * @return 检索结果
     */
    List<Map<String, Object>> retrieveClueMemory(Long gameId, Long playerId, String query, int topK);

    /**
     * 存储全局线索记忆
     *
     * @param scriptId    剧本ID
     * @param characterId 角色ID
     * @param content     线索内容
     */
    void storeGlobalClueMemory(Long scriptId, Long characterId, String content);

    /**
     * 检索全局线索记忆（跨游戏）
     *
     * @param scriptId    剧本ID
     * @param characterId 角色ID
     * @param query       查询文本
     * @param topK        返回结果数量
     * @return 检索结果
     */
    List<Map<String, Object>> retrieveGlobalClueMemory(Long scriptId, Long characterId, String query, int topK);

    /**
     * 存储全局时间线记忆
     *
     * @param scriptId    剧本ID
     * @param characterId 角色ID
     * @param content     时间线内容
     * @param timestamp   时间点
     */
    void storeGlobalTimelineMemory(Long scriptId, Long characterId, String content, String timestamp);

    /**
     * 检索全局时间线记忆
     *
     * @param scriptId    剧本ID
     * @param characterId 角色ID
     * @param query       查询文本
     * @param topK        返回结果数量
     * @return 检索结果
     */
    List<Map<String, Object>> retrieveGlobalTimelineMemory(Long scriptId, Long characterId, String query, int topK);

    /**
     * 删除游戏相关的所有记忆
     *
     * @param gameId 游戏ID
     */
    void deleteGameMemory(Long gameId);
}
