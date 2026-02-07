package org.jubensha.aijubenshabackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.jubensha.aijubenshabackend.memory.MilvusCollectionManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG功能控制器
 * 用于处理RAG相关的操作，包括集合管理、对话记忆操作、全局记忆操作等
 */
@Slf4j
@RestController
@RequestMapping("/api/rag")
public class RAGController {

    private final RAGService ragService;
    private final MilvusCollectionManager collectionManager;

    public RAGController(RAGService ragService, MilvusCollectionManager collectionManager) {
        this.ragService = ragService;
        this.collectionManager = collectionManager;
    }

    /**
     * 初始化对话记忆集合
     *
     * @param request 包含gameId的请求
     * @return 初始化结果
     */
    @PostMapping("/initialize-conversation")
    public ResponseEntity<?> initializeConversationCollection(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            collectionManager.initializeConversationCollection(gameId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "对话记忆集合初始化成功",
                    "gameId", gameId
            ));
        } catch (Exception e) {
            log.error("初始化对话记忆集合失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "初始化失败", "message", e.getMessage()));
        }
    }

    /**
     * 删除对话记忆集合
     *
     * @param request 包含gameId的请求
     * @return 删除结果
     */
    @PostMapping("/drop-conversation")
    public ResponseEntity<?> dropConversationCollection(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            collectionManager.dropConversationCollection(gameId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "对话记忆集合删除成功",
                    "gameId", gameId
            ));
        } catch (Exception e) {
            log.error("删除对话记忆集合失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "删除失败", "message", e.getMessage()));
        }
    }

    /**
     * 检查集合是否存在
     *
     * @param collectionName 集合名称
     * @return 检查结果
     */
    @GetMapping("/check-collection")
    public ResponseEntity<?> checkCollectionExists(@RequestParam String collectionName) {
        try {
            boolean exists = collectionManager.collectionExists(collectionName);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "exists", exists,
                    "collectionName", collectionName
            ));
        } catch (Exception e) {
            log.error("检查集合存在性失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "检查失败", "message", e.getMessage()));
        }
    }

    /**
     * 插入对话记忆
     *
     * @param request 包含gameId、playerId、playerName和content的请求
     * @return 插入结果
     */
    @PostMapping("/insert-conversation")
    public ResponseEntity<?> insertConversationMemory(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            Long playerId = Long.parseLong(request.get("playerId").toString());
            String playerName = (String) request.get("playerName");
            String content = (String) request.get("content");

            Long recordId = ragService.insertConversationMemory(gameId, playerId, playerName, content);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "对话记忆插入成功",
                    "gameId", gameId,
                    "recordId", recordId
            ));
        } catch (Exception e) {
            log.error("插入对话记忆失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "插入失败", "message", e.getMessage()));
        }
    }

    /**
     * 批量插入对话记忆
     *
     * @param request 包含gameId和conversationRecords的请求
     * @return 插入结果
     */
    @PostMapping("/batch-insert-conversation")
    public ResponseEntity<?> batchInsertConversationMemory(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            List<Map<String, Object>> conversationRecords = (List<Map<String, Object>>) request.get("conversationRecords");

            List<Long> recordIds = ragService.batchInsertConversationMemory(gameId, conversationRecords);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "批量插入对话记忆成功",
                    "gameId", gameId,
                    "recordIds", recordIds,
                    "insertedCount", recordIds.size()
            ));
        } catch (Exception e) {
            log.error("批量插入对话记忆失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "批量插入失败", "message", e.getMessage()));
        }
    }

    /**
     * 搜索对话记忆
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID（可选）
     * @param query    查询文本
     * @param topK     返回结果数量
     * @return 搜索结果
     */
    @GetMapping("/search-conversation")
    public ResponseEntity<?> searchConversationMemory(
            @RequestParam Long gameId,
            @RequestParam(required = false) Long playerId,
            @RequestParam String query,
            @RequestParam int topK) {
        try {
            List<Map<String, Object>> results = ragService.searchConversationMemory(gameId, playerId, query, topK);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gameId", gameId);
            if (playerId != null) {
                response.put("playerId", playerId);
            }
            response.put("query", query);
            response.put("topK", topK);
            response.put("resultsCount", results.size());
            response.put("results", results);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("搜索对话记忆失败: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "搜索失败");
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "未知错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * 更新对话记忆
     *
     * @param request 包含gameId、recordId和content的请求
     * @return 更新结果
     */
    @PutMapping("/update-conversation")
    public ResponseEntity<?> updateConversationMemory(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            Long recordId = Long.parseLong(request.get("recordId").toString());
            String content = (String) request.get("content");

            boolean success = ragService.updateConversationMemory(gameId, recordId, content);
            return ResponseEntity.ok(Map.of(
                    "success", success,
                    "message", success ? "对话记忆更新成功" : "对话记忆更新失败",
                    "gameId", gameId,
                    "recordId", recordId
            ));
        } catch (Exception e) {
            log.error("更新对话记忆失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "更新失败", "message", e.getMessage()));
        }
    }

    /**
     * 删除对话记忆
     *
     * @param request 包含gameId和recordId的请求
     * @return 删除结果
     */
    @DeleteMapping("/delete-conversation")
    public ResponseEntity<?> deleteConversationMemory(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            Long recordId = Long.parseLong(request.get("recordId").toString());

            boolean success = ragService.deleteConversationMemory(gameId, recordId);
            return ResponseEntity.ok(Map.of(
                    "success", success,
                    "message", success ? "对话记忆删除成功" : "对话记忆删除失败",
                    "gameId", gameId,
                    "recordId", recordId
            ));
        } catch (Exception e) {
            log.error("删除对话记忆失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "删除失败", "message", e.getMessage()));
        }
    }

    /**
     * 批量删除对话记忆
     *
     * @param request 包含gameId和recordIds的请求
     * @return 删除结果
     */
    @DeleteMapping("/batch-delete-conversation")
    public ResponseEntity<?> batchDeleteConversationMemory(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            List<Long> recordIds = (List<Long>) request.get("recordIds");

            int deletedCount = ragService.batchDeleteConversationMemory(gameId, recordIds);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "批量删除对话记忆成功",
                    "gameId", gameId,
                    "deletedCount", deletedCount,
                    "totalCount", recordIds.size()
            ));
        } catch (Exception e) {
            log.error("批量删除对话记忆失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "批量删除失败", "message", e.getMessage()));
        }
    }

    /**
     * 插入全局记忆
     *
     * @param request 包含scriptId、characterId、content和type的请求
     * @return 插入结果
     */
    @PostMapping("/insert-global")
    public ResponseEntity<?> insertGlobalMemory(@RequestBody Map<String, Object> request) {
        try {
            Long scriptId = Long.parseLong(request.get("scriptId").toString());
            Long characterId = Long.parseLong(request.get("characterId").toString());
            String content = (String) request.get("content");
            String type = (String) request.get("type"); // "clue" 或 "timeline"

            Long recordId;
            if ("clue".equals(type)) {
                recordId = ragService.insertGlobalClueMemory(scriptId, characterId, content);
            } else if ("timeline".equals(type)) {
                String timestamp = (String) request.get("timestamp");
                recordId = ragService.insertGlobalTimelineMemory(scriptId, characterId, content, timestamp);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "无效的记忆类型", "message", "type必须为'clue'或'timeline'"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "全局记忆插入成功",
                    "recordId", recordId,
                    "type", type
            ));
        } catch (Exception e) {
            log.error("插入全局记忆失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "插入失败", "message", e.getMessage()));
        }
    }

    /**
     * 批量插入全局记忆
     *
     * @param request 包含memoryRecords的请求
     * @return 插入结果
     */
    @PostMapping("/batch-insert-global")
    public ResponseEntity<?> batchInsertGlobalMemory(@RequestBody Map<String, Object> request) {
        try {
            List<Map<String, Object>> memoryRecords = (List<Map<String, Object>>) request.get("memoryRecords");
            List<Long> recordIds = ragService.batchInsertGlobalMemory(memoryRecords);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "批量插入全局记忆成功",
                    "recordIds", recordIds,
                    "insertedCount", recordIds.size()
            ));
        } catch (Exception e) {
            log.error("批量插入全局记忆失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "批量插入失败", "message", e.getMessage()));
        }
    }

    /**
     * 搜索全局线索记忆
     *
     * @param scriptId    剧本ID
     * @param characterId 角色ID（可选）
     * @param query       查询文本
     * @param topK        返回结果数量
     * @return 搜索结果
     */
    @GetMapping("/search-global-clue")
    public ResponseEntity<?> searchGlobalClueMemory(
            @RequestParam Long scriptId,
            @RequestParam(required = false) Long characterId,
            @RequestParam String query,
            @RequestParam int topK) {
        try {
            List<Map<String, Object>> results = ragService.searchGlobalClueMemory(scriptId, characterId, query, topK);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("scriptId", scriptId);
            if (characterId != null) {
                response.put("characterId", characterId);
            }
            response.put("query", query);
            response.put("topK", topK);
            response.put("resultsCount", results.size());
            response.put("results", results);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("搜索全局线索记忆失败: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "搜索失败");
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "未知错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * 搜索全局时间线记忆
     *
     * @param scriptId    剧本ID
     * @param characterId 角色ID（可选）
     * @param query       查询文本
     * @param topK        返回结果数量
     * @return 搜索结果
     */
    @GetMapping("/search-global-timeline")
    public ResponseEntity<?> searchGlobalTimelineMemory(
            @RequestParam Long scriptId,
            @RequestParam(required = false) Long characterId,
            @RequestParam String query,
            @RequestParam int topK) {
        try {
            List<Map<String, Object>> results = ragService.searchGlobalTimelineMemory(scriptId, characterId, query, topK);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("scriptId", scriptId);
            if (characterId != null) {
                response.put("characterId", characterId);
            }
            response.put("query", query);
            response.put("topK", topK);
            response.put("resultsCount", results.size());
            response.put("results", results);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("搜索全局时间线记忆失败: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "搜索失败");
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "未知错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * 更新全局记忆
     *
     * @param request 包含recordId和content的请求
     * @return 更新结果
     */
    @PutMapping("/update-global")
    public ResponseEntity<?> updateGlobalMemory(@RequestBody Map<String, Object> request) {
        try {
            Long recordId = Long.parseLong(request.get("recordId").toString());
            String content = (String) request.get("content");

            boolean success = ragService.updateGlobalMemory(recordId, content);
            return ResponseEntity.ok(Map.of(
                    "success", success,
                    "message", success ? "全局记忆更新成功" : "全局记忆更新失败",
                    "recordId", recordId
            ));
        } catch (Exception e) {
            log.error("更新全局记忆失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "更新失败", "message", e.getMessage()));
        }
    }

    /**
     * 删除全局记忆
     *
     * @param request 包含recordId的请求
     * @return 删除结果
     */
    @DeleteMapping("/delete-global")
    public ResponseEntity<?> deleteGlobalMemory(@RequestBody Map<String, Object> request) {
        try {
            Long recordId = Long.parseLong(request.get("recordId").toString());
            boolean success = ragService.deleteGlobalMemory(recordId);
            return ResponseEntity.ok(Map.of(
                    "success", success,
                    "message", success ? "全局记忆删除成功" : "全局记忆删除失败",
                    "recordId", recordId
            ));
        } catch (Exception e) {
            log.error("删除全局记忆失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "删除失败", "message", e.getMessage()));
        }
    }

    /**
     * 批量删除全局记忆
     *
     * @param request 包含recordIds的请求
     * @return 删除结果
     */
    @DeleteMapping("/batch-delete-global")
    public ResponseEntity<?> batchDeleteGlobalMemory(@RequestBody Map<String, Object> request) {
        try {
            List<Long> recordIds = (List<Long>) request.get("recordIds");
            int deletedCount = ragService.batchDeleteGlobalMemory(recordIds);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "批量删除全局记忆成功",
                    "deletedCount", deletedCount,
                    "totalCount", recordIds.size()
            ));
        } catch (Exception e) {
            log.error("批量删除全局记忆失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "批量删除失败", "message", e.getMessage()));
        }
    }

    /**
     * 基于线索的过滤查询
     *
     * @param request 包含gameId、playerId、discoveredClueIds、query和topK的请求
     * @return 查询结果
     */
    @PostMapping("/filter-by-clues")
    public ResponseEntity<?> filterByDiscoveredClues(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.parseLong(request.get("gameId").toString());
            Long playerId = Long.parseLong(request.get("playerId").toString());
            List<Long> discoveredClueIds = (List<Long>) request.get("discoveredClueIds");
            String query = (String) request.get("query");
            int topK = Integer.parseInt(request.get("topK").toString());

            List<Map<String, Object>> results = ragService.filterByDiscoveredClues(gameId, playerId, discoveredClueIds, query, topK);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "gameId", gameId,
                    "playerId", playerId,
                    "query", query,
                    "topK", topK,
                    "discoveredClueCount", discoveredClueIds.size(),
                    "resultsCount", results.size(),
                    "results", results
            ));
        } catch (Exception e) {
            log.error("基于线索的过滤查询失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "查询失败", "message", e.getMessage()));
        }
    }

    /**
     * 计算线索关联强度
     *
     * @param gameId  游戏ID
     * @param clueId1 线索ID1
     * @param clueId2 线索ID2
     * @return 关联强度结果
     */
    @GetMapping("/calculate-clue-relation")
    public ResponseEntity<?> calculateClueRelationStrength(
            @RequestParam Long gameId,
            @RequestParam Long clueId1,
            @RequestParam Long clueId2) {
        try {
            int strength = ragService.calculateClueRelationStrength(gameId, clueId1, clueId2);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "gameId", gameId,
                    "clueId1", clueId1,
                    "clueId2", clueId2,
                    "relationStrength", strength,
                    "message", "线索关联强度计算成功"
            ));
        } catch (Exception e) {
            log.error("计算线索关联强度失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "计算失败", "message", e.getMessage()));
        }
    }
}
