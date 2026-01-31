package org.jubensha.aijubenshabackend.controller;

import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;
import org.jubensha.aijubenshabackend.models.enums.PlayerStatus;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * @param player 玩家实体
     * @return 创建的玩家
     */
    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        Player createdPlayer = playerService.createPlayer(player);
        return new ResponseEntity<>(createdPlayer, HttpStatus.CREATED);
    }
    
    /**
     * 更新玩家
     * @param id 玩家ID
     * @param player 玩家实体
     * @return 更新后的玩家
     */
    @PutMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @RequestBody Player player) {
        Player updatedPlayer = playerService.updatePlayer(id, player);
        return new ResponseEntity<>(updatedPlayer, HttpStatus.OK);
    }
    
    /**
     * 删除玩家
     * @param id 玩家ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * 根据ID查询玩家
     * @param id 玩家ID
     * @return 玩家实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        Optional<Player> player = playerService.getPlayerById(id);
        return player.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 根据用户名查询玩家
     * @param username 用户名
     * @return 玩家实体
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<Player> getPlayerByUsername(@PathVariable String username) {
        Optional<Player> player = playerService.getPlayerByUsername(username);
        return player.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 根据邮箱查询玩家
     * @param email 邮箱
     * @return 玩家实体
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Player> getPlayerByEmail(@PathVariable String email) {
        Optional<Player> player = playerService.getPlayerByEmail(email);
        return player.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 查询所有玩家
     * @return 玩家列表
     */
    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        List<Player> players = playerService.getAllPlayers();
        return new ResponseEntity<>(players, HttpStatus.OK);
    }
    
    /**
     * 根据状态查询玩家
     * @param status 状态
     * @return 玩家列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Player>> getPlayersByStatus(@PathVariable String status) {
        List<Player> players = playerService.getPlayersByStatus(status);
        return new ResponseEntity<>(players, HttpStatus.OK);
    }
    
    /**
     * 根据角色查询玩家
     * @param role 角色
     * @return 玩家列表
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<Player>> getPlayersByRole(@PathVariable String role) {
        List<Player> players = playerService.getPlayersByRole(role);
        return new ResponseEntity<>(players, HttpStatus.OK);
    }
    
    /**
     * 根据状态和角色查询玩家
     * @param status 状态
     * @param role 角色
     * @return 玩家列表
     */
    @GetMapping("/status/{status}/role/{role}")
    public ResponseEntity<List<Player>> getPlayersByStatusAndRole(@PathVariable String status, @PathVariable String role) {
        List<Player> players = playerService.getPlayersByStatusAndRole(status, role);
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
