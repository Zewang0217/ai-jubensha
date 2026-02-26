package org.jubensha.aijubenshabackend.ai.service;

import java.util.List;
import java.util.Map;

/**
 * 父文档存储服务接口
 * 用于存储和检索完整的原始消息（父文档）
 * 支持父子文档检索策略
 */
public interface ParentDocumentService {

    /**
     * 存储父文档
     *
     * @param gameId     游戏ID
     * @param playerId   玩家ID
     * @param playerName 玩家名称
     * @param content    完整消息内容
     * @param totalChunks 总块数
     * @return 父文档ID
     */
    Long storeParentDocument(Long gameId, Long playerId, String playerName, String content, int totalChunks);

    /**
     * 根据ID获取父文档
     *
     * @param parentId 父文档ID
     * @return 父文档信息，包含content、gameId、playerId、playerName等字段
     */
    Map<String, Object> getParentDocument(Long parentId);

    /**
     * 批量获取父文档
     *
     * @param parentIds 父文档ID列表
     * @return 父文档信息列表
     */
    List<Map<String, Object>> getParentDocuments(List<Long> parentIds);

    /**
     * 根据游戏ID获取父文档列表
     *
     * @param gameId 游戏ID
     * @param limit  限制数量
     * @return 父文档信息列表
     */
    List<Map<String, Object>> getParentDocumentsByGameId(Long gameId, int limit);

    /**
     * 更新父文档
     *
     * @param parentId 父文档ID
     * @param content  新的内容
     * @return 是否更新成功
     */
    boolean updateParentDocument(Long parentId, String content);

    /**
     * 删除父文档
     *
     * @param parentId 父文档ID
     * @return 是否删除成功
     */
    boolean deleteParentDocument(Long parentId);

    /**
     * 根据游戏ID删除所有父文档
     *
     * @param gameId 游戏ID
     * @return 删除的数量
     */
    int deleteParentDocumentsByGameId(Long gameId);
}
