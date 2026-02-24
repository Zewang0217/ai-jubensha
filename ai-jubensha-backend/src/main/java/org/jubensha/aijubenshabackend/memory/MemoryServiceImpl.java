package org.jubensha.aijubenshabackend.memory;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.EmbeddingService;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记忆管理服务实现
 * 基于RAGService和Milvus向量数据库提供高效的记忆存储和检索功能
 * <p>
 * 优化措施：
 * 1. 使用RAGService作为底层实现，统一管理对话记忆和全局记忆
 * 2. 对话记忆存储：通过RAGService.insertConversationMemory实现，支持消息分块和嵌入向量生成
 * 3. 线索记忆存储：通过RAGService.insertGlobalClueMemory实现，确保线索存储到全局记忆中
 * 4. 全局记忆管理：支持线索和时间线的存储与检索
 * <p>
 * 实现细节：
 * - storeCharacterMemory：暂时不存储，需要确定正确的存储位置
 * - retrieveCharacterMemory：暂时返回空列表，后续可根据业务需求实现
 * - storeConversationMemory：使用RAGService存储对话记忆
 * - retrieveConversationMemory：使用RAGService检索对话记忆
 * - storeClueMemory：只存储已发现的线索，确保在搜证环节被调用
 * - retrieveClueMemory：使用RAGService检索线索记忆
 * - storeGlobalClueMemory：使用RAGService存储全局线索记忆
 * - retrieveGlobalClueMemory：使用RAGService检索全局线索记忆
 * - storeGlobalTimelineMemory：使用RAGService存储全局时间线记忆
 * - retrieveGlobalTimelineMemory：使用RAGService检索全局时间线记忆
 * - deleteGameMemory：目前RAGService没有直接的删除游戏记忆的方法
 */
@Slf4j
@Service
public class MemoryServiceImpl implements MemoryService {

    private final EmbeddingService embeddingService;
    private final MilvusClientV2 milvusClientV2;
    private final RAGService ragService;

    @Autowired
    public MemoryServiceImpl(EmbeddingService embeddingService, MilvusClientV2 milvusClientV2, RAGService ragService) {
        this.embeddingService = embeddingService;
        this.milvusClientV2 = milvusClientV2;
        this.ragService = ragService;
    }

    @Override
    public void storeCharacterMemory(Long gameId, Long playerId, Long characterId, Map<String, String> characterInfo) {
        // 角色记忆暂时不存储，因为需要确定正确的存储位置
        // 后续可以根据业务需求实现存储逻辑
//        log.info("存储角色记忆，游戏ID: {}, 玩家ID: {}, 角色ID: {}, 信息数量: {}", gameId, playerId, characterId, characterInfo.size());
    }

    @Override
    public List<Map<String, Object>> retrieveCharacterMemory(Long gameId, Long playerId, String query, int topK) {
        // 角色记忆暂时返回空列表，后续可以根据业务需求实现检索逻辑

        return new ArrayList<>();
    }

    @Override
    public void storeConversationMemory(Long gameId, Long playerId, String content, long timestamp) {
        // 使用RAGService存储对话记忆
        ragService.insertConversationMemory(gameId, playerId, "player", content);
        log.info("存储对话记忆，游戏ID: {}, 玩家ID: {}, 内容长度: {}", gameId, playerId, content.length());
    }

    @Override
    public List<Map<String, Object>> retrieveConversationMemory(Long gameId, Long playerId, String query, int topK) {
        // 使用RAGService检索对话记忆
        return ragService.searchConversationMemory(gameId, playerId, query, topK);
    }

    @Override
    public void storeClueMemory(Long gameId, Long playerId, Long clueId, String content, String discoveredBy) {
        // 只存储已发现的线索，确保这是在搜证环节被调用
        log.info("[搜证环节] 存储线索记忆，游戏ID: {}, 玩家ID: {}, 线索ID: {}, 发现者: {}", 
                gameId, playerId, clueId, discoveredBy);
        
        // 线索应该存储到全局记忆中，而不是对话记忆中
        Long storedId = ragService.insertGlobalClueMemory(gameId, clueId, content, playerId);
        
        if (storedId != null) {
            log.info("[搜证环节] 线索存储成功，存储ID: {}", storedId);
        } else {
            log.warn("[搜证环节] 线索存储失败");
        }
    }

    @Override
    public List<Map<String, Object>> retrieveClueMemory(Long gameId, Long playerId, String query, int topK) {
        log.info("检索线索记忆，游戏ID: {}, 玩家ID: {}", gameId, playerId);
        List<Map<String, Object>> result = ragService.searchGlobalClueMemory(gameId, playerId, query, topK);
        return result;
    }

    @Override
    public void storeGlobalClueMemory(Long scriptId, Long characterId, String content, Long playerId) {
        // 使用RAGService存储全局线索记忆
        ragService.insertGlobalClueMemory(scriptId, characterId, content, playerId);
        log.info("存储全局线索记忆，剧本ID: {}, 角色ID: {}", scriptId, characterId);
    }

    @Override
    public List<Map<String, Object>> retrieveGlobalClueMemory(Long scriptId, Long characterId, String query, int topK) {
        // 使用RAGService检索全局线索记忆
        return ragService.searchGlobalClueMemory(scriptId, characterId, query, topK);
    }

    @Override
    public void storeGlobalTimelineMemory(Long scriptId, Long characterId, String content, String timestamp) {
        // 使用RAGService存储全局时间线记忆
        ragService.insertGlobalTimelineMemory(scriptId, characterId, content, timestamp);
        log.info("存储全局时间线记忆，剧本ID: {}, 角色ID: {}", scriptId, characterId);
    }

    @Override
    public List<Map<String, Object>> retrieveGlobalTimelineMemory(Long scriptId, Long characterId, String query, int topK) {
        // 使用RAGService检索全局时间线记忆
        return ragService.searchGlobalTimelineMemory(scriptId, characterId, query, topK);
    }

    @Override
    public void deleteGameMemory(Long gameId) {
        // 目前RAGService没有直接的删除游戏记忆的方法
        // 后续可以实现批量删除对话记忆的功能
        log.info("删除游戏记忆，游戏ID: {}", gameId);
    }


}

