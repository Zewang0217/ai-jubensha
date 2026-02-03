package org.jubensha.aijubenshabackend.controller;

import org.jubensha.aijubenshabackend.models.dto.PlayerDTO;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


import java.util.List;
import java.util.Optional;

/**
 * 玩家控制器
 */
@RestController
@RequestMapping("/api/players")
public class PlayerController {
    
    private final PlayerService playerService;
    
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }
    
    /**
     * 创建玩家
     * @param request 玩家实体
     * @return 创建的玩家
     */
    @PostMapping
    public ResponseEntity<PlayerDTO.PlayerResponse> createPlayer(
            @Valid @RequestBody PlayerDTO.PlayerCreateRequest request) {
        PlayerDTO.PlayerResponse response = playerService.createPlayer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    
    /**
     * 更新玩家
     * @param id 玩家ID
     * @param request 玩家更新请求DTO
     * @return 更新后的玩家响应DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlayerDTO.PlayerResponse> updatePlayer(@PathVariable Long id,
                                               @RequestBody PlayerDTO.PlayerUpdateRequest request) {
        // 普通用户只能更新自己的基本信息
        PlayerDTO.PlayerResponse response = playerService.updatePlayer(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 删除玩家 添加权限验证
     * @param id 玩家ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * 根据ID查询玩家
     * @param id 玩家ID
     * @return 玩家详细信息DTO
     */
    @GetMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<PlayerDTO.PlayerDetailResponse> getPlayerById(@PathVariable Long id) {
        Optional<PlayerDTO.PlayerDetailResponse> player = playerService.getPlayerById(id);
        return player.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    
    /**
     * 根据用户名查询玩家
     * @param username 用户名
     * @return 玩家实体
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<PlayerDTO.PlayerDetailResponse> getPlayerByUsername(@PathVariable String username) {
        Optional<PlayerDTO.PlayerDetailResponse> player = playerService.getPlayerByUsername(username);
        return player.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 根据邮箱查询玩家
     * @param email 邮箱
     * @return 玩家实体
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<PlayerDTO.PlayerDetailResponse> getPlayerByEmail(@PathVariable String email) {
        Optional<PlayerDTO.PlayerDetailResponse> player = playerService.getPlayerByEmail(email);
        return player.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 查询所有玩家
     * @return 玩家列表
     */
    @GetMapping
    public ResponseEntity<List<PlayerDTO.PlayerDetailResponse>> getAllPlayers() {
        List<PlayerDTO.PlayerDetailResponse> players = playerService.getAllPlayers();
        return new ResponseEntity<>(players, HttpStatus.OK);
    }
    
    /**
     * 根据状态查询玩家
     * @param status 状态
     * @return 玩家列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PlayerDTO.PlayerResponse>> getPlayersByStatus(@PathVariable String status) {
        List<PlayerDTO.PlayerResponse> players = playerService.getPlayersByStatus(status);
        return new ResponseEntity<>(players, HttpStatus.OK);
    }
    
    /**
     * 根据角色查询玩家
     * @param role 角色
     * @return 玩家列表
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<PlayerDTO.PlayerResponse>> getPlayersByRole(@PathVariable String role) {
        List<PlayerDTO.PlayerResponse> players = playerService.getPlayersByRole(role);
        return new ResponseEntity<>(players, HttpStatus.OK);
    }
    
    /**
     * 根据状态和角色查询玩家
     * @param status 状态
     * @param role 角色
     * @return 玩家列表
     */
    @GetMapping("/status/{status}/role/{role}")
    public ResponseEntity<List<PlayerDTO.PlayerResponse>> getPlayersByStatusAndRole(@PathVariable String status, @PathVariable String role) {
        List<PlayerDTO.PlayerResponse> players = playerService.getPlayersByStatusAndRole(status, role);
        return new ResponseEntity<>(players, HttpStatus.OK);
    }
    
    /**
     * 更新玩家登录时间
     * @param id 玩家ID
     * @return 响应
     */
    @PutMapping("/{id}/login")
    public ResponseEntity<Void> updateLastLoginTime(@PathVariable Long id) {
        playerService.updateLastLoginTime(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
