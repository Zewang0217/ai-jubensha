package org.jubensha.aijubenshabackend.memory;

import io.milvus.client.MilvusServiceClient;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.EmbeddingService;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记忆管理服务实现
 * 使用内存存储作为临时实现，确保代码能够编译通过
 * 后续可以替换为实际的Milvus实现
 * 
 * 注意：以下部分需要使用Milvus向量数据库实现：
 * 1. storeCharacterMemory：使用Milvus的insert操作存储角色记忆
 * 2. retrieveCharacterMemory：使用Milvus的search操作检索角色记忆
 * 3. storeConversationMemory：使用Milvus的insert操作存储对话记忆
 * 4. retrieveConversationMemory：使用Milvus的search操作检索对话记忆
 * 5. storeClueMemory：使用Milvus的insert操作存储线索记忆
 * 6. retrieveClueMemory：使用Milvus的search操作检索线索记忆
 * 7. storeGlobalClueMemory：使用Milvus的insert操作存储全局线索记忆
 * 8. retrieveGlobalClueMemory：使用Milvus的search操作检索全局线索记忆
 * 9. storeGlobalTimelineMemory：使用Milvus的insert操作存储全局时间线记忆
 * 10. retrieveGlobalTimelineMemory：使用Milvus的search操作检索全局时间线记忆
 * 11. deleteGameMemory：使用Milvus的delete操作删除游戏相关记忆
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
        for (Map.Entry<String, String> entry : characterInfo.entrySet()) {
            String fieldName = entry.getKey();
            String content = entry.getValue();

            if (content == null || content.isEmpty()) {
                continue;
            }

            // 使用RAGService存储对话记忆
            ragService.insertConversationMemory(gameId, playerId, fieldName, content);
        }

        log.info("存储角色记忆，游戏ID: {}, 玩家ID: {}, 角色ID: {}, 信息数量: {}", gameId, playerId, characterId, characterInfo.size());
    }

    @Override
    public List<Map<String, Object>> retrieveCharacterMemory(Long gameId, Long playerId, String query, int topK) {
        // 使用RAGService检索对话记忆
        return ragService.searchConversationMemory(gameId, playerId, query, topK);
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
        // 使用RAGService存储线索记忆
        ragService.insertConversationMemory(gameId, playerId, "clue", content);
        log.info("存储线索记忆，游戏ID: {}, 玩家ID: {}, 线索ID: {}", gameId, playerId, clueId);
    }

    @Override
    public List<Map<String, Object>> retrieveClueMemory(Long gameId, Long playerId, String query, int topK) {
        // 使用RAGService检索线索记忆
        return ragService.searchConversationMemory(gameId, playerId, query, topK);
    }

    @Override
    public void storeGlobalClueMemory(Long scriptId, Long characterId, String content) {
        // 使用RAGService存储全局线索记忆
        ragService.insertGlobalClueMemory(scriptId, characterId, content);
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

