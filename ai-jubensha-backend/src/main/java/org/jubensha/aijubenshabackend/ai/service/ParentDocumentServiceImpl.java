package org.jubensha.aijubenshabackend.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * 父文档存储服务实现
 * 使用SQL数据库的dialogues表格存储父文档
 * 支持父子文档检索策略
 */
@Slf4j
@Service
public class ParentDocumentServiceImpl implements ParentDocumentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 父文档缓存，使用Caffeine提高性能
    private final Cache<Long, Map<String, Object>> parentDocumentCache = Caffeine.newBuilder()
            .maximumSize(1000)  // 最大缓存1000个父文档
            .expireAfterWrite(10, TimeUnit.MINUTES)  // 10分钟过期
            .build();

    // 按游戏ID缓存父文档列表
    private final Cache<Long, List<Map<String, Object>>> gameParentDocumentsCache = Caffeine.newBuilder()
            .maximumSize(100)  // 最大缓存100个游戏的父文档列表
            .expireAfterWrite(5, TimeUnit.MINUTES)  // 5分钟过期
            .build();

    @Override
    @Transactional
    public Long storeParentDocument(Long gameId, Long playerId, String playerName, String content, int totalChunks) {
        // 注意：dialogues表格需要character_id字段，但ParentDocumentService接口没有提供
        // 这里使用0作为默认值，表示未知角色
        Long characterId = 0L;
        
        // 插入到dialogues表格
        String insertSql = "INSERT INTO dialogues (game_id, player_id, character_id, content, type) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertSql, gameId, playerId, characterId, content, "CHAT");
        
        // 获取自增ID
        Long parentId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        
        log.debug("存储父文档成功，ID: {}, 游戏ID: {}, 总块数: {}", parentId, gameId, totalChunks);
        
        return parentId;
    }

    @Override
    public Map<String, Object> getParentDocument(Long parentId) {
        // 先从缓存获取
        Map<String, Object> parentDoc = parentDocumentCache.getIfPresent(parentId);
        if (parentDoc != null) {
            log.debug("从缓存获取父文档，ID: {}", parentId);
            return parentDoc;
        }
        
        // 缓存未命中，从数据库查询
        String selectSql = "SELECT id, game_id, player_id, content, timestamp FROM dialogues WHERE id = ?";
        
        try {
            parentDoc = jdbcTemplate.queryForMap(selectSql, parentId);
            // 存入缓存
            parentDocumentCache.put(parentId, parentDoc);
            log.debug("从数据库获取并缓存父文档，ID: {}", parentId);
            return parentDoc;
        } catch (Exception e) {
            log.warn("父文档不存在，ID: {}", parentId);
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getParentDocuments(List<Long> parentIds) {
        if (parentIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        List<Long> missingParentIds = new ArrayList<>();
        
        // 先从缓存获取
        for (Long parentId : parentIds) {
            Map<String, Object> parentDoc = parentDocumentCache.getIfPresent(parentId);
            if (parentDoc != null) {
                result.add(parentDoc);
                log.debug("从缓存获取父文档，ID: {}", parentId);
            } else {
                missingParentIds.add(parentId);
            }
        }
        
        // 如果所有父文档都在缓存中，直接返回
        if (missingParentIds.isEmpty()) {
            return result;
        }
        
        // 缓存未命中的父文档，从数据库查询
        String placeholders = String.join(",", Collections.nCopies(missingParentIds.size(), "?"));
        String selectSql = "SELECT id, game_id, player_id, content, timestamp FROM dialogues WHERE id IN (" + placeholders + ")";
        
        try {
            List<Map<String, Object>> dbResults = jdbcTemplate.queryForList(selectSql, missingParentIds.toArray());
            
            // 存入缓存并添加到结果中
            for (Map<String, Object> parentDoc : dbResults) {
                Long parentId = ((Number) parentDoc.get("id")).longValue();
                parentDocumentCache.put(parentId, parentDoc);
                result.add(parentDoc);
                log.debug("从数据库获取并缓存父文档，ID: {}", parentId);
            }
            
            return result;
        } catch (Exception e) {
            log.error("批量获取父文档失败: {}", e.getMessage());
            return result; // 返回已从缓存获取的结果
        }
    }

    @Override
    public List<Map<String, Object>> getParentDocumentsByGameId(Long gameId, int limit) {
        // 先从缓存获取
        List<Map<String, Object>> parentDocs = gameParentDocumentsCache.getIfPresent(gameId);
        if (parentDocs != null) {
            log.debug("从缓存获取游戏父文档列表，游戏ID: {}", gameId);
            // 如果缓存中的列表长度大于limit，返回前limit个
            if (parentDocs.size() > limit) {
                return parentDocs.subList(0, limit);
            }
            return parentDocs;
        }
        
        // 缓存未命中，从数据库查询
        String selectSql = "SELECT id, game_id, player_id, content, timestamp FROM dialogues WHERE game_id = ? ORDER BY timestamp DESC LIMIT ?";
        
        try {
            parentDocs = jdbcTemplate.queryForList(selectSql, gameId, limit);
            
            // 存入缓存
            gameParentDocumentsCache.put(gameId, parentDocs);
            log.debug("从数据库获取并缓存游戏父文档列表，游戏ID: {}, 数量: {}", gameId, parentDocs.size());
            
            return parentDocs;
        } catch (Exception e) {
            log.error("根据游戏ID获取父文档失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public boolean updateParentDocument(Long parentId, String content) {
        String updateSql = "UPDATE dialogues SET content = ? WHERE id = ?";
        
        int rowsAffected = jdbcTemplate.update(updateSql, content, parentId);
        boolean success = rowsAffected > 0;
        
        if (success) {
            // 更新成功，清除缓存
            parentDocumentCache.invalidate(parentId);
            // 同时清除游戏ID缓存，因为该游戏的父文档列表可能已变更
            Map<String, Object> parentDoc = getParentDocument(parentId);
            if (parentDoc != null) {
                Long gameId = ((Number) parentDoc.get("game_id")).longValue();
                gameParentDocumentsCache.invalidate(gameId);
            }
            log.debug("更新父文档成功，ID: {}", parentId);
        } else {
            log.warn("父文档不存在，无法更新，ID: {}", parentId);
        }
        
        return success;
    }

    @Override
    @Transactional
    public boolean deleteParentDocument(Long parentId) {
        // 先获取父文档信息，用于后续清除游戏ID缓存
        Map<String, Object> parentDoc = getParentDocument(parentId);
        
        String deleteSql = "DELETE FROM dialogues WHERE id = ?";
        
        int rowsAffected = jdbcTemplate.update(deleteSql, parentId);
        boolean success = rowsAffected > 0;
        
        if (success) {
            // 删除成功，清除缓存
            parentDocumentCache.invalidate(parentId);
            // 同时清除游戏ID缓存，因为该游戏的父文档列表已变更
            if (parentDoc != null) {
                Long gameId = ((Number) parentDoc.get("game_id")).longValue();
                gameParentDocumentsCache.invalidate(gameId);
            }
        }
        
        log.debug("删除父文档{}，ID: {}", success ? "成功" : "失败", parentId);
        return success;
    }

    @Override
    @Transactional
    public int deleteParentDocumentsByGameId(Long gameId) {
        String deleteSql = "DELETE FROM dialogues WHERE game_id = ?";
        
        int rowsAffected = jdbcTemplate.update(deleteSql, gameId);
        
        if (rowsAffected > 0) {
            // 删除成功，清除游戏ID缓存
            gameParentDocumentsCache.invalidate(gameId);
            // 清除该游戏下所有父文档的缓存
            // 注意：这里无法直接知道该游戏下有哪些父文档ID，所以不清除单个父文档缓存
            // 单个父文档缓存会在过期后自动失效
        }
        
        log.debug("根据游戏ID删除父文档完成，游戏ID: {}, 删除数量: {}", gameId, rowsAffected);
        return rowsAffected;
    }
}
