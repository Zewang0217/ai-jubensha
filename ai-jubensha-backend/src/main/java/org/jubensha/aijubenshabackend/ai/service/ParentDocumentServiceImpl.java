package org.jubensha.aijubenshabackend.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.Dialogue;
import org.jubensha.aijubenshabackend.models.enums.DialogueType;
import org.jubensha.aijubenshabackend.repository.character.CharacterRepository;
import org.jubensha.aijubenshabackend.repository.dialogue.DialogueRepository;
import org.jubensha.aijubenshabackend.repository.game.GamePlayerRepository;
import org.jubensha.aijubenshabackend.repository.game.GameRepository;
import org.jubensha.aijubenshabackend.repository.player.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private DialogueRepository dialogueRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

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
        // 创建Dialogue实体
        Dialogue dialogue = new Dialogue();
        
        // 设置关联实体
        dialogue.setGame(gameRepository.findById(gameId).orElse(null));
        dialogue.setPlayer(playerRepository.findById(playerId).orElse(null));
        
        // 获取角色信息
        gamePlayerRepository.findByGameIdAndPlayerId(gameId, playerId).ifPresent(gamePlayer -> {
            dialogue.setCharacter(gamePlayer.getCharacter());
        });
        
        // 设置其他字段
        dialogue.setContent(content);
        dialogue.setType(DialogueType.CHAT);
        
        // 保存到数据库
        Dialogue savedDialogue = dialogueRepository.save(dialogue);
        Long parentId = savedDialogue.getId();
        
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
        try {
            Optional<Dialogue> dialogueOpt = dialogueRepository.findById(parentId);
            if (dialogueOpt.isPresent()) {
                Dialogue dialogue = dialogueOpt.get();
                // 转换为Map格式
                parentDoc = convertToMap(dialogue);
                // 存入缓存
                parentDocumentCache.put(parentId, parentDoc);
                log.debug("从数据库获取并缓存父文档，ID: {}", parentId);
                return parentDoc;
            } else {
                log.warn("父文档不存在，ID: {}", parentId);
                return null;
            }
        } catch (Exception e) {
            log.warn("获取父文档失败，ID: {}", parentId, e);
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
        try {
            List<Dialogue> dialogues = dialogueRepository.findByIdIn(missingParentIds);
            
            // 存入缓存并添加到结果中
            for (Dialogue dialogue : dialogues) {
                Long parentId = dialogue.getId();
                Map<String, Object> parentDoc = convertToMap(dialogue);
                parentDocumentCache.put(parentId, parentDoc);
                result.add(parentDoc);
                log.debug("从数据库获取并缓存父文档，ID: {}", parentId);
            }
            
            return result;
        } catch (Exception e) {
            log.error("批量获取父文档失败: {}", e.getMessage(), e);
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
        try {
            List<Dialogue> dialogues = dialogueRepository.findByGameIdOrderByTimestampDesc(
                    gameId, PageRequest.of(0, limit));
            
            // 转换为Map列表
            parentDocs = new ArrayList<>();
            for (Dialogue dialogue : dialogues) {
                parentDocs.add(convertToMap(dialogue));
            }
            
            // 存入缓存
            gameParentDocumentsCache.put(gameId, parentDocs);
            log.debug("从数据库获取并缓存游戏父文档列表，游戏ID: {}, 数量: {}", gameId, parentDocs.size());
            
            return parentDocs;
        } catch (Exception e) {
            log.error("根据游戏ID获取父文档失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public boolean updateParentDocument(Long parentId, String content) {
        try {
            Optional<Dialogue> dialogueOpt = dialogueRepository.findById(parentId);
            if (dialogueOpt.isPresent()) {
                Dialogue dialogue = dialogueOpt.get();
                dialogue.setContent(content);
                dialogueRepository.save(dialogue);
                
                // 更新成功，清除缓存
                parentDocumentCache.invalidate(parentId);
                // 同时清除游戏ID缓存，因为该游戏的父文档列表可能已变更
                Map<String, Object> parentDoc = getParentDocument(parentId);
                if (parentDoc != null) {
                    Long gameId = ((Number) parentDoc.get("game_id")).longValue();
                    gameParentDocumentsCache.invalidate(gameId);
                }
                log.debug("更新父文档成功，ID: {}", parentId);
                return true;
            } else {
                log.warn("父文档不存在，无法更新，ID: {}", parentId);
                return false;
            }
        } catch (Exception e) {
            log.error("更新父文档失败，ID: {}", parentId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteParentDocument(Long parentId) {
        // 先获取父文档信息，用于后续清除游戏ID缓存
        Map<String, Object> parentDoc = getParentDocument(parentId);
        
        try {
            dialogueRepository.deleteById(parentId);
            
            // 删除成功，清除缓存
            parentDocumentCache.invalidate(parentId);
            // 同时清除游戏ID缓存，因为该游戏的父文档列表已变更
            if (parentDoc != null) {
                Long gameId = ((Number) parentDoc.get("game_id")).longValue();
                gameParentDocumentsCache.invalidate(gameId);
            }
            log.debug("删除父文档成功，ID: {}", parentId);
            return true;
        } catch (Exception e) {
            log.error("删除父文档失败，ID: {}", parentId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public int deleteParentDocumentsByGameId(Long gameId) {
        try {
            int rowsAffected = dialogueRepository.deleteByGameId(gameId);
            
            if (rowsAffected > 0) {
                // 删除成功，清除游戏ID缓存
                gameParentDocumentsCache.invalidate(gameId);
                // 清除该游戏下所有父文档的缓存
                // 注意：这里无法直接知道该游戏下有哪些父文档ID，所以不清除单个父文档缓存
                // 单个父文档缓存会在过期后自动失效
            }
            
            log.debug("根据游戏ID删除父文档完成，游戏ID: {}, 删除数量: {}", gameId, rowsAffected);
            return rowsAffected;
        } catch (Exception e) {
            log.error("根据游戏ID删除父文档失败，游戏ID: {}", gameId, e);
            return 0;
        }
    }

    /**
     * 将Dialogue实体转换为Map格式
     * <p>
     * 用于保持与原有实现的兼容性
     *
     * @param dialogue Dialogue实体
     * @return Map格式的父文档信息
     */
    private Map<String, Object> convertToMap(Dialogue dialogue) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", dialogue.getId());
        map.put("game_id", dialogue.getGame() != null ? dialogue.getGame().getId() : null);
        map.put("player_id", dialogue.getPlayer() != null ? dialogue.getPlayer().getId() : null);
        map.put("player_name", dialogue.getPlayer() != null ? dialogue.getPlayer().getNickname() : null);
        map.put("content", dialogue.getContent());
        map.put("timestamp", dialogue.getTimestamp());
        return map;
    }
}
