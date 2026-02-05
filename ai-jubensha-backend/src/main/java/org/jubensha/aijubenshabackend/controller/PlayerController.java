package org.jubensha.aijubenshabackend.controller;

import jakarta.validation.Valid;
import org.jubensha.aijubenshabackend.models.dto.PlayerCreateDTO;
import org.jubensha.aijubenshabackend.models.dto.PlayerResponseDTO;
import org.jubensha.aijubenshabackend.models.dto.PlayerUpdateDTO;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;
import org.jubensha.aijubenshabackend.models.enums.PlayerStatus;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     *
     * @param playerCreateDTO 玩家创建DTO
     * @return 创建的玩家响应DTO
     */
    @PostMapping
    public ResponseEntity<PlayerResponseDTO> createPlayer(@Valid @RequestBody PlayerCreateDTO playerCreateDTO) {
        Player player = new Player();
        player.setUsername(playerCreateDTO.getUsername());
        player.setPassword(playerCreateDTO.getPassword());
        player.setNickname(playerCreateDTO.getNickname());
        player.setEmail(playerCreateDTO.getEmail());
        // 默认角色为普通用户
        player.setRole(PlayerRole.USER);
        // 默认状态为在线
        player.setStatus(PlayerStatus.ONLINE);

        Player createdPlayer = playerService.createPlayer(player);
        PlayerResponseDTO responseDTO = PlayerResponseDTO.fromEntity(createdPlayer);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    /**
     * 更新玩家
     *
     * @param id              玩家ID
     * @param playerUpdateDTO 玩家更新DTO
     * @return 更新后的玩家响应DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponseDTO> updatePlayer(@PathVariable Long id, @Valid @RequestBody PlayerUpdateDTO playerUpdateDTO) {
        Player player = new Player();
        player.setUsername(playerUpdateDTO.getUsername());
        player.setNickname(playerUpdateDTO.getNickname());
        player.setPassword(playerUpdateDTO.getPassword());
        player.setEmail(playerUpdateDTO.getEmail());
        player.setAvatarUrl(playerUpdateDTO.getAvatarUrl());
        player.setStatus(playerUpdateDTO.getStatus());

        try {
            Player updatedPlayer = playerService.updatePlayer(id, player);
            PlayerResponseDTO responseDTO = PlayerResponseDTO.fromEntity(updatedPlayer);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 删除玩家
     *
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
     *
     * @param id 玩家ID
     * @return 玩家详情DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponseDTO> getPlayerById(@PathVariable Long id) {
        Optional<Player> player = playerService.getPlayerById(id);
        return player.map(value -> {
            PlayerResponseDTO responseDTO = PlayerResponseDTO.fromEntity(value);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 根据用户名查询玩家
     *
     * @param username 用户名
     * @return 玩家详情DTO
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<PlayerResponseDTO> getPlayerByUsername(@PathVariable String username) {
        Optional<Player> player = playerService.getPlayerByUsername(username);
        return player.map(value -> {
            PlayerResponseDTO responseDTO = PlayerResponseDTO.fromEntity(value);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 根据昵称查询玩家
     *
     * @param nickname 昵称
     * @return 玩家详情DTO列表
     */
    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<List<PlayerResponseDTO>> getPlayersByNickname(@PathVariable String nickname) {
        List<Player> players = playerService.getPlayersByNickname(nickname);
        List<PlayerResponseDTO> responseDTOs = players.stream()
                .map(PlayerResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据邮箱查询玩家
     *
     * @param email 邮箱
     * @return 玩家详情DTO
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<PlayerResponseDTO> getPlayerByEmail(@PathVariable String email) {
        Optional<Player> player = playerService.getPlayerByEmail(email);
        return player.map(value -> {
            PlayerResponseDTO responseDTO = PlayerResponseDTO.fromEntity(value);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 查询所有玩家
     *
     * @return 玩家详情DTO列表
     */
    @GetMapping
    public ResponseEntity<List<PlayerResponseDTO>> getAllPlayers() {
        List<Player> players = playerService.getAllPlayers();
        List<PlayerResponseDTO> responseDTOs = players.stream()
                .map(PlayerResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 根据状态查询玩家
     *
     * @param status 状态
     * @return 玩家响应DTO列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PlayerResponseDTO>> getPlayersByStatus(@PathVariable String status) {
        try {
            PlayerStatus playerStatus = PlayerStatus.valueOf(status.toUpperCase());
            List<Player> players = playerService.getPlayersByStatus(playerStatus);
            List<PlayerResponseDTO> responseDTOs = players.stream()
                    .map(PlayerResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 根据角色查询玩家
     *
     * @param role 角色
     * @return 玩家响应DTO列表
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<PlayerResponseDTO>> getPlayersByRole(@PathVariable String role) {
        try {
            PlayerRole playerRole = PlayerRole.valueOf(role.toUpperCase());
            List<Player> players = playerService.getPlayersByRole(playerRole);
            List<PlayerResponseDTO> responseDTOs = players.stream()
                    .map(PlayerResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 根据状态和角色查询玩家
     *
     * @param status 状态
     * @param role   角色
     * @return 玩家响应DTO列表
     */
    @GetMapping("/status/{status}/role/{role}")
    public ResponseEntity<List<PlayerResponseDTO>> getPlayersByStatusAndRole(@PathVariable String status, @PathVariable String role) {
        try {
            PlayerStatus playerStatus = PlayerStatus.valueOf(status.toUpperCase());
            PlayerRole playerRole = PlayerRole.valueOf(role.toUpperCase());
            List<Player> players = playerService.getPlayersByStatusAndRole(playerStatus, playerRole);
            List<PlayerResponseDTO> responseDTOs = players.stream()
                    .map(PlayerResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 根据创建时间范围查询玩家
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 玩家响应DTO列表
     */
    @GetMapping("/created")
    public ResponseEntity<List<PlayerResponseDTO>> getPlayersByCreateTimeRange(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        List<Player> players = playerService.getPlayersByCreateTimeRange(startTime, endTime);
        List<PlayerResponseDTO> responseDTOs = players.stream()
                .map(PlayerResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    /**
     * 更新玩家状态
     *
     * @param id     玩家ID
     * @param status 新状态
     * @return 更新后的玩家响应DTO
     */
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<PlayerResponseDTO> updatePlayerStatus(@PathVariable Long id, @PathVariable String status) {
        try {
            PlayerStatus playerStatus = PlayerStatus.valueOf(status.toUpperCase());
            Player player = playerService.updatePlayerStatus(id, playerStatus);
            PlayerResponseDTO responseDTO = PlayerResponseDTO.fromEntity(player);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 更新玩家角色
     *
     * @param id   玩家ID
     * @param role 新角色
     * @return 更新后的玩家响应DTO
     */
    @PutMapping("/{id}/role/{role}")
    public ResponseEntity<PlayerResponseDTO> updatePlayerRole(@PathVariable Long id, @PathVariable String role) {
        try {
            PlayerRole playerRole = PlayerRole.valueOf(role.toUpperCase());
            Player player = playerService.updatePlayerRole(id, playerRole);
            PlayerResponseDTO responseDTO = PlayerResponseDTO.fromEntity(player);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 更新玩家登录时间
     *
     * @param id 玩家ID
     * @return 响应
     */
    @PutMapping("/{id}/login")
    public ResponseEntity<Void> updateLastLoginTime(@PathVariable Long id) {
        try {
            playerService.updateLastLoginTime(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 更新玩家头像
     *
     * @param id        玩家ID
     * @param avatarUrl 头像URL
     * @return 更新后的玩家响应DTO
     */
    @PutMapping("/{id}/avatar")
    public ResponseEntity<PlayerResponseDTO> updatePlayerAvatar(@PathVariable Long id, @RequestParam String avatarUrl) {
        try {
            Player player = playerService.updatePlayerAvatar(id, avatarUrl);
            PlayerResponseDTO responseDTO = PlayerResponseDTO.fromEntity(player);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
