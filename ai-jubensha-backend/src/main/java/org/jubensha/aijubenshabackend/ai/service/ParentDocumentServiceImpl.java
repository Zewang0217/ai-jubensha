package org.jubensha.aijubenshabackend.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 父文档存储服务实现
 * 使用内存存储作为临时解决方案
 * 后续可替换为MongoDB等持久化存储
 */
@Slf4j
@Service
public class ParentDocumentServiceImpl implements ParentDocumentService {

    // 内存存储，使用ConcurrentHashMap确保线程安全
    private final Map<Long, Map<String, Object>> parentDocumentStore = new ConcurrentHashMap<>();
    private long nextId = 1;

    @Override
    public Long storeParentDocument(Long gameId, Long playerId, String playerName, String content, int totalChunks) {
        Long parentId = nextId++;
        
        Map<String, Object> parentDoc = new HashMap<>();
        parentDoc.put("id", parentId);
        parentDoc.put("game_id", gameId);
        parentDoc.put("player_id", playerId);
        parentDoc.put("player_name", playerName);
        parentDoc.put("content", content);
        parentDoc.put("total_chunks", totalChunks);
        parentDoc.put("timestamp", System.currentTimeMillis());
        
        parentDocumentStore.put(parentId, parentDoc);
        log.debug("存储父文档成功，ID: {}, 游戏ID: {}, 总块数: {}", parentId, gameId, totalChunks);
        
        return parentId;
    }

    @Override
    public Map<String, Object> getParentDocument(Long parentId) {
        Map<String, Object> parentDoc = parentDocumentStore.get(parentId);
        if (parentDoc == null) {
            log.warn("父文档不存在，ID: {}", parentId);
            return null;
        }
        log.debug("获取父文档成功，ID: {}", parentId);
        return parentDoc;
    }

    @Override
    public List<Map<String, Object>> getParentDocuments(List<Long> parentIds) {
        List<Map<String, Object>> parentDocs = new ArrayList<>();
        
        for (Long parentId : parentIds) {
            Map<String, Object> parentDoc = getParentDocument(parentId);
            if (parentDoc != null) {
                parentDocs.add(parentDoc);
            }
        }
        
        log.debug("批量获取父文档完成，请求数: {}, 成功数: {}", parentIds.size(), parentDocs.size());
        return parentDocs;
    }

    @Override
    public List<Map<String, Object>> getParentDocumentsByGameId(Long gameId, int limit) {
        List<Map<String, Object>> parentDocs = new ArrayList<>();
        
        for (Map<String, Object> parentDoc : parentDocumentStore.values()) {
            if (gameId.equals(parentDoc.get("game_id"))) {
                parentDocs.add(parentDoc);
                if (parentDocs.size() >= limit) {
                    break;
                }
            }
        }
        
        // 按时间戳降序排序
        parentDocs.sort((a, b) -> {
            Long timestampA = (Long) a.get("timestamp");
            Long timestampB = (Long) b.get("timestamp");
            return timestampB.compareTo(timestampA);
        });
        
        log.debug("根据游戏ID获取父文档完成，游戏ID: {}, 数量: {}", gameId, parentDocs.size());
        return parentDocs;
    }

    @Override
    public boolean updateParentDocument(Long parentId, String content) {
        Map<String, Object> parentDoc = parentDocumentStore.get(parentId);
        if (parentDoc == null) {
            log.warn("父文档不存在，无法更新，ID: {}", parentId);
            return false;
        }
        
        parentDoc.put("content", content);
        parentDoc.put("updated_at", System.currentTimeMillis());
        log.debug("更新父文档成功，ID: {}", parentId);
        return true;
    }

    @Override
    public boolean deleteParentDocument(Long parentId) {
        Map<String, Object> removed = parentDocumentStore.remove(parentId);
        boolean success = removed != null;
        log.debug("删除父文档{}", success ? "成功" : "失败" + ", ID: " + parentId);
        return success;
    }

    @Override
    public int deleteParentDocumentsByGameId(Long gameId) {
        int count = 0;
        Iterator<Map.Entry<Long, Map<String, Object>>> iterator = parentDocumentStore.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<Long, Map<String, Object>> entry = iterator.next();
            if (gameId.equals(entry.getValue().get("game_id"))) {
                iterator.remove();
                count++;
            }
        }
        
        log.debug("根据游戏ID删除父文档完成，游戏ID: {}, 删除数量: {}", gameId, count);
        return count;
    }
}
