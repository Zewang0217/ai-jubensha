package org.jubensha.aijubenshabackend.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.core.exception.InvalidInvestigationException;
import org.jubensha.aijubenshabackend.core.exception.NoInvestigationChanceException;
import org.jubensha.aijubenshabackend.models.dto.InvestigationRequestDTO;
import org.jubensha.aijubenshabackend.models.dto.InvestigationResponseDTO;
import org.jubensha.aijubenshabackend.models.dto.InvestigationStatusDTO;
import org.jubensha.aijubenshabackend.service.investigation.InvestigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 搜证控制器
 * 处理玩家搜证相关的HTTP请求
 *
 * @author luobo
 * @date 2026-02-10
 */
@Slf4j
@RestController
@RequestMapping("/api/games/{gameId}/investigation")
public class InvestigationController {

    private final InvestigationService investigationService;

    @Autowired
    public InvestigationController(InvestigationService investigationService) {
        this.investigationService = investigationService;
    }

    /**
     * 执行搜证操作
     * 玩家消耗一次搜证机会，获得指定场景中的一个线索
     *
     * @param gameId  游戏ID
     * @param request 搜证请求（包含玩家ID和场景ID）
     * @return 搜证响应，包含获得的线索和剩余次数
     */
    @PostMapping
    public ResponseEntity<?> investigate(
            @PathVariable Long gameId,
            @Valid @RequestBody InvestigationRequestDTO request) {

        log.info("收到搜证请求: 游戏={}, 玩家={}, 场景={}",
                gameId, request.getPlayerId(), request.getSceneId());

        try {
            InvestigationResponseDTO response = investigationService.investigate(gameId, request);
            return ResponseEntity.ok(response);

        } catch (NoInvestigationChanceException e) {
            log.warn("玩家 {} 搜证次数已用完", request.getPlayerId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("NO_CHANCE", e.getMessage(), e.getDataCopy()));

        } catch (InvalidInvestigationException e) {
            log.warn("无效的搜证操作: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("INVALID_INVESTIGATION", e.getMessage(), e.getDataCopy()));

        } catch (Exception e) {
            log.error("搜证操作失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", "搜证操作失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取玩家的搜证状态
     * 包括剩余次数、搜证历史等信息
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @return 搜证状态
     */
    @GetMapping("/status")
    public ResponseEntity<?> getInvestigationStatus(
            @PathVariable Long gameId,
            @RequestParam Long playerId) {

        log.debug("获取玩家 {} 在游戏 {} 的搜证状态", playerId, gameId);

        try {
            InvestigationStatusDTO status = investigationService.getInvestigationStatus(gameId, playerId);

            if (status == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("NOT_FOUND", "未找到玩家的搜证状态", null));
            }

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("获取搜证状态失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", "获取搜证状态失败: " + e.getMessage(), null));
        }
    }

    /**
     * 检查玩家是否可以搜证
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @return 是否可以搜证
     */
    @GetMapping("/can-investigate")
    public ResponseEntity<?> canInvestigate(
            @PathVariable Long gameId,
            @RequestParam Long playerId) {

        log.debug("检查玩家 {} 在游戏 {} 是否可以搜证", playerId, gameId);

        try {
            boolean canInvestigate = investigationService.canInvestigate(gameId, playerId);

            Map<String, Object> response = new HashMap<>();
            response.put("canInvestigate", canInvestigate);
            response.put("gameId", gameId);
            response.put("playerId", playerId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("检查搜证权限失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", "检查搜证权限失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取游戏中所有玩家的搜证状态
     * 主要用于游戏主持人和管理功能
     *
     * @param gameId 游戏ID
     * @return 所有玩家的搜证状态
     */
    @GetMapping("/all-status")
    public ResponseEntity<?> getAllPlayersInvestigationStatus(
            @PathVariable Long gameId) {

        log.debug("获取游戏 {} 所有玩家的搜证状态", gameId);

        try {
            Map<Long, InvestigationStatusDTO> allStatus =
                    investigationService.getAllPlayersInvestigationStatus(gameId);

            return ResponseEntity.ok(allStatus);

        } catch (Exception e) {
            log.error("获取所有玩家搜证状态失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", "获取搜证状态失败: " + e.getMessage(), null));
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 创建错误响应对象
     *
     * @param code    错误码
     * @param message 错误消息
     * @param data    附加数据
     * @return 错误响应Map
     */
    private Map<String, Object> createErrorResponse(String code, String message, Map<String, Object> data) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        error.put("success", false);
        if (data != null && !data.isEmpty()) {
            error.put("data", data);
        }
        return error;
    }
}
