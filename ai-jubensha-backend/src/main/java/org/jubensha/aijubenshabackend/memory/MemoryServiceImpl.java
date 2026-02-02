package org.jubensha.aijubenshabackend.memory;

import io.milvus.client.MilvusServiceClient;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.EmbeddingService;
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

    // 内存存储结构
    private final Map<String, List<Map<String, Object>>> characterMemoryStore = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> conversationMemoryStore = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> clueMemoryStore = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> globalClueStore = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> globalTimelineStore = new ConcurrentHashMap<>();

    @Autowired
    public MemoryServiceImpl(EmbeddingService embeddingService, MilvusClientV2 milvusClientV2) {
        this.embeddingService = embeddingService;
        this.milvusClientV2 = milvusClientV2;
    }

    @Override
    public void storeCharacterMemory(Long gameId, Long playerId, Long characterId, Map<String, String> characterInfo) {
        String key = generateKey(gameId, playerId, characterId);
        List<Map<String, Object>> memories = characterMemoryStore.computeIfAbsent(key, k -> new ArrayList<>());

        for (Map.Entry<String, String> entry : characterInfo.entrySet()) {
            String fieldName = entry.getKey();
            String content = entry.getValue();

            if (content == null || content.isEmpty()) {
                continue;
            }

            Map<String, Object> memory = new HashMap<>();
            memory.put("field_name", fieldName);
            memory.put("content", content);
            memory.put("game_id", gameId);
            memory.put("player_id", playerId);
            memory.put("character_id", characterId);
            
            memories.add(memory);
        }

        log.info("存储角色记忆，游戏ID: {}, 玩家ID: {}, 角色ID: {}, 信息数量: {}", gameId, playerId, characterId, characterInfo.size());
    }

    @Override
    public List<Map<String, Object>> retrieveCharacterMemory(Long gameId, Long playerId, String query, int topK) {
        String key = generateKey(gameId, playerId, null);
        List<Map<String, Object>> memories = characterMemoryStore.getOrDefault(key, new ArrayList<>());
        
        // 简单的基于关键词的搜索
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> memory : memories) {
            String content = (String) memory.get("content");
            if (content != null && content.contains(query)) {
                Map<String, Object> result = new HashMap<>(memory);
                result.put("score", 1.0f); // 模拟相似度分数
                results.add(result);
                if (results.size() >= topK) {
                    break;
                }
            }
        }
        
        return results;
    }

    @Override
    public void storeConversationMemory(Long gameId, Long playerId, String content, long timestamp) {
        String key = generateKey(gameId, playerId, null);
        List<Map<String, Object>> memories = conversationMemoryStore.computeIfAbsent(key, k -> new ArrayList<>());

        Map<String, Object> memory = new HashMap<>();
        memory.put("content", content);
        memory.put("timestamp", timestamp);
        memory.put("game_id", gameId);
        memory.put("player_id", playerId);

        memories.add(memory);
        log.info("存储对话记忆，游戏ID: {}, 玩家ID: {}, 内容长度: {}", gameId, playerId, content.length());
    }

    @Override
    public List<Map<String, Object>> retrieveConversationMemory(Long gameId, Long playerId, String query, int topK) {
        String key = generateKey(gameId, playerId, null);
        List<Map<String, Object>> memories = conversationMemoryStore.getOrDefault(key, new ArrayList<>());
        
        // 简单的基于关键词的搜索
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> memory : memories) {
            String content = (String) memory.get("content");
            if (content != null && content.contains(query)) {
                Map<String, Object> result = new HashMap<>(memory);
                result.put("score", 1.0f); // 模拟相似度分数
                results.add(result);
                if (results.size() >= topK) {
                    break;
                }
            }
        }
        
        return results;
    }

    @Override
    public void storeClueMemory(Long gameId, Long playerId, Long clueId, String content, String discoveredBy) {
        String key = generateKey(gameId, playerId, null);
        List<Map<String, Object>> memories = clueMemoryStore.computeIfAbsent(key, k -> new ArrayList<>());

        Map<String, Object> memory = new HashMap<>();
        memory.put("clue_id", clueId);
        memory.put("content", content);
        memory.put("discovered_by", discoveredBy);
        memory.put("game_id", gameId);
        memory.put("player_id", playerId);

        memories.add(memory);
        log.info("存储线索记忆，游戏ID: {}, 玩家ID: {}, 线索ID: {}", gameId, playerId, clueId);
    }

    @Override
    public List<Map<String, Object>> retrieveClueMemory(Long gameId, Long playerId, String query, int topK) {
        String key = generateKey(gameId, playerId, null);
        List<Map<String, Object>> memories = clueMemoryStore.getOrDefault(key, new ArrayList<>());
        
        // 简单的基于关键词的搜索
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> memory : memories) {
            String content = (String) memory.get("content");
            if (content != null && content.contains(query)) {
                Map<String, Object> result = new HashMap<>(memory);
                result.put("score", 1.0f); // 模拟相似度分数
                results.add(result);
                if (results.size() >= topK) {
                    break;
                }
            }
        }
        
        return results;
    }

    @Override
    public void storeGlobalClueMemory(Long scriptId, Long characterId, String content) {
        String key = generateKey(scriptId, characterId, null);
        List<Map<String, Object>> clues = globalClueStore.computeIfAbsent(key, k -> new ArrayList<>());

        Map<String, Object> clue = new HashMap<>();
        clue.put("content", content);
        clue.put("script_id", scriptId);
        clue.put("character_id", characterId);

        clues.add(clue);
        log.info("存储全局线索记忆，剧本ID: {}, 角色ID: {}", scriptId, characterId);
    }

    @Override
    public List<Map<String, Object>> retrieveGlobalClueMemory(Long scriptId, Long characterId, String query, int topK) {
        String key = generateKey(scriptId, characterId, null);
        List<Map<String, Object>> clues = globalClueStore.getOrDefault(key, new ArrayList<>());
        
        // 简单的基于关键词的搜索
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> clue : clues) {
            String content = (String) clue.get("content");
            if (content != null && content.contains(query)) {
                Map<String, Object> result = new HashMap<>(clue);
                result.put("score", 1.0f); // 模拟相似度分数
                results.add(result);
                if (results.size() >= topK) {
                    break;
                }
            }
        }
        
        return results;
    }

    @Override
    public void storeGlobalTimelineMemory(Long scriptId, Long characterId, String content, String timestamp) {
        String key = generateKey(scriptId, characterId, null);
        List<Map<String, Object>> timelines = globalTimelineStore.computeIfAbsent(key, k -> new ArrayList<>());

        Map<String, Object> timeline = new HashMap<>();
        timeline.put("content", content);
        timeline.put("timestamp", timestamp);
        timeline.put("script_id", scriptId);
        timeline.put("character_id", characterId);

        timelines.add(timeline);
        log.info("存储全局时间线记忆，剧本ID: {}, 角色ID: {}", scriptId, characterId);
    }

    @Override
    public List<Map<String, Object>> retrieveGlobalTimelineMemory(Long scriptId, Long characterId, String query, int topK) {
        String key = generateKey(scriptId, characterId, null);
        List<Map<String, Object>> timelines = globalTimelineStore.getOrDefault(key, new ArrayList<>());
        
        // 简单的基于关键词的搜索
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> timeline : timelines) {
            String content = (String) timeline.get("content");
            if (content != null && content.contains(query)) {
                Map<String, Object> result = new HashMap<>(timeline);
                result.put("score", 1.0f); // 模拟相似度分数
                results.add(result);
                if (results.size() >= topK) {
                    break;
                }
            }
        }
        
        return results;
    }

    @Override
    public void deleteGameMemory(Long gameId) {
        // 删除游戏相关的所有记忆
        characterMemoryStore.keySet().removeIf(key -> key.startsWith(gameId.toString() + ":"));
        conversationMemoryStore.keySet().removeIf(key -> key.startsWith(gameId.toString() + ":"));
        clueMemoryStore.keySet().removeIf(key -> key.startsWith(gameId.toString() + ":"));
        log.info("删除游戏记忆，游戏ID: {}", gameId);
    }

    /**
     * 生成存储键
     */
    private String generateKey(Object... parts) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] != null) {
                key.append(parts[i]);
            }
            if (i < parts.length - 1) {
                key.append(":");
            }
        }
        return key.toString();
    }

    // 初始化集合
    private void initializeCollections() {
        try {
            createCollectionIfNotExists("character_memory");
            createCollectionIfNotExists("conversation_memory");
            createCollectionIfNotExists("clue_memory");
            createCollectionIfNotExists("global_clue_memory");
            createCollectionIfNotExists("global_timeline_memory");
        } catch (Exception e) {
            log.error("初始化 Milvus 集合失败：{}", e.getMessage(), e);
        }
    }

    // 创建 Milvus 集合
    private void createCollectionIfNotExists(String collectionName) {
        // 创建一个schema
        CreateCollectionReq.CollectionSchema schema =
            milvusClientV2.createSchema();


    }
}

