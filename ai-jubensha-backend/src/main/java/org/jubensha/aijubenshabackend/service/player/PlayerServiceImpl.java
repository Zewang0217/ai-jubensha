package org.jubensha.aijubenshabackend.service.player;

import org.jubensha.aijubenshabackend.models.dto.PlayerDTO;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;
import org.jubensha.aijubenshabackend.models.enums.PlayerStatus;
import org.jubensha.aijubenshabackend.repository.player.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlayerServiceImpl implements PlayerService {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);
    
    private final PlayerRepository playerRepository;
    
    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public PlayerDTO.PlayerResponse createPlayer(PlayerDTO.PlayerCreateRequest request) {
        logger.info("Creating new player with DTO: {}", request.getUsername());
        
        // 检查用户名和邮箱是否已存在
        if (isUsernameExists(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + request.getUsername());
        }
        if (isEmailExists(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在: " + request.getEmail());
        }
        
        // 创建Player实体
        Player player = new Player();
        player.setUsername(request.getUsername());
        player.setPassword(request.getPassword());
        player.setEmail(request.getEmail());
        player.setNickname(request.getNickname());
        player.setRole(PlayerRole.USER); // 默认为普通用户
        player.setStatus(PlayerStatus.OFFLINE); // 默认为离线状态
        
        // 保存到数据库
        Player savedPlayer = playerRepository.save(player);
        
        // 转换为响应DTO
        return convertToResponse(savedPlayer);
    }
    
    @Override
    public Optional<PlayerDTO.PlayerDetailResponse> getPlayerById(Long id) {
        logger.info("Getting player by id: {}", id);
        Optional<Player> playerOpt = playerRepository.findById(id);
        return playerOpt.map(this::convertToDetailResponse);
    }
    
    @Override
    public Optional<PlayerDTO.PlayerDetailResponse> getPlayerByUsername(String username) {
        logger.info("Getting player by username: {}", username);
        Optional<Player> playerOpt = playerRepository.findByUsername(username);
        return playerOpt.map(this::convertToDetailResponse);
    }
    
    @Override
    public Optional<PlayerDTO.PlayerDetailResponse> getPlayerByEmail(String email) {
        logger.info("Getting player by email: {}", email);
        Optional<Player> playerOpt = playerRepository.findByEmail(email);
        return playerOpt.map(this::convertToDetailResponse);
    }
    
    @Override
    public List<PlayerDTO.PlayerDetailResponse> getAllPlayers() {
        logger.info("Getting all players");
        List<Player> players = playerRepository.findAll();
        return players.stream()
                .map(this::convertToDetailResponse)
                .collect(Collectors.toList());

    }
    
    @Override
    public List<PlayerDTO.PlayerDetailResponse> getOnlinePlayers() {
        logger.info("Getting online players");
        List<Player> players = playerRepository.findByStatus(PlayerStatus.ONLINE);
        return players.stream()
                .map(this::convertToDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PlayerDTO.PlayerResponse updatePlayer(Long id, PlayerDTO.PlayerUpdateRequest request) {
        logger.info("Updating player with DTO: {}", id);
        
        Optional<Player> existingPlayer = playerRepository.findById(id);
        if (existingPlayer.isPresent()) {
            Player updatedPlayer = existingPlayer.get();
            
            // 更新允许的字段
            if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
                // 检查用户名是否被其他用户使用
                if (!request.getUsername().equals(updatedPlayer.getUsername()) && 
                    isUsernameExists(request.getUsername())) {
                    throw new IllegalArgumentException("用户名已存在: " + request.getUsername());
                }
                updatedPlayer.setUsername(request.getUsername());
            }
            
            if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
                updatedPlayer.setNickname(request.getNickname());
            }
            
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                updatedPlayer.setPassword(request.getPassword());
            }
            
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                // 检查邮箱是否被其他用户使用
                if (!request.getEmail().equals(updatedPlayer.getEmail()) && 
                    isEmailExists(request.getEmail())) {
                    throw new IllegalArgumentException("邮箱已存在: " + request.getEmail());
                }
                updatedPlayer.setEmail(request.getEmail());
            }
            
            if (request.getAvatar() != null) {
                updatedPlayer.setAvatar(request.getAvatar());
            }
            
            if (request.getStatus() != null) {
                updatedPlayer.setStatus(request.getStatus());
            }
            
            Player savedPlayer = playerRepository.save(updatedPlayer);
            return convertToResponse(savedPlayer);
        } else {
            throw new IllegalArgumentException("Player not found with id: " + id);
        }
    }

    @Override
    public PlayerDTO.PlayerResponse updatePlayerStatus(Long id, String status) {
        logger.info("Updating player status: {} to {}", id, status);

        // 字符串到枚举的安全转换
        PlayerStatus playerStatus;
        try {
            playerStatus = PlayerStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid player status: {}", status);
            throw new IllegalArgumentException("Invalid player status: " + status);
        }

        // 查询现有玩家
        Optional<Player> existingPlayer = playerRepository.findById(id);
        if (existingPlayer.isPresent()) {
            Player updatedPlayer = existingPlayer.get();
            // 使用转换后的枚举值
            updatedPlayer.setStatus(playerStatus);
            Player savedPlayer = playerRepository.save(updatedPlayer);
            // 转换为DTO返回
            return convertToResponse(savedPlayer);
        } else {
            throw new IllegalArgumentException("Player not found with id: " + id);
        }
    }


    
    @Override
    public void deletePlayer(Long id) {
        logger.info("Deleting player: {}", id);
        playerRepository.deleteById(id);
    }
    
    @Override
    public boolean validateLogin(String username, String password) {
        logger.info("Validating login for: {}", username);
        Optional<Player> player = playerRepository.findByUsername(username);
        return player.isPresent() && player.get().getPassword().equals(password);
    }
    
    @Override
    public boolean isUsernameExists(String username) {
        logger.info("Checking if username exists: {}", username);
        return playerRepository.existsByUsername(username);
    }
    
    @Override
    public boolean isEmailExists(String email) {
        logger.info("Checking if email exists: {}", email);
        return playerRepository.existsByEmail(email);
    }

    @Override
    public List<PlayerDTO.PlayerResponse> getPlayersByStatus(String status) {
        logger.info("Getting players by status: {}", status);

        // 字符串到枚举的安全转换
        PlayerStatus playerStatus;
        try {
            playerStatus = PlayerStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid player status: {}", status);
            throw new IllegalArgumentException("Invalid player status: " + status);
        }

        List<Player> players = playerRepository.findByStatus(playerStatus);
        return players.stream()
                .map(this::convertToResponse)
                .toList();
    }


    @Override
    public List<PlayerDTO.PlayerResponse> getPlayersByRole(String role) {
        logger.info("Getting players by role: {}", role);

        // 字符串到枚举的安全转换
        PlayerRole playerRole;
        try {
            playerRole = PlayerRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid player role: {}", role);
            throw new IllegalArgumentException("Invalid player role: " + role);
        }

        List<Player> players = playerRepository.findByRole(playerRole);
        return players.stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public List<PlayerDTO.PlayerResponse> getPlayersByStatusAndRole(String status, String role) {
        logger.info("Getting players by status: {} and role: {}", status, role);

        // 字符串到枚举的安全转换
        PlayerStatus playerStatus;
        PlayerRole playerRole;

        try {
            playerStatus = PlayerStatus.valueOf(status.toUpperCase());
            playerRole = PlayerRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid parameters - status: {}, role: {}", status, role);
            throw new IllegalArgumentException("Invalid parameters: " + e.getMessage());
        }

        List<Player> players = playerRepository.findByStatusAndRole(playerStatus, playerRole);
        return players.stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public void updateLastLoginTime(Long id) {
        logger.info("Updating last login time for player: {}", id);
        Optional<Player> existingPlayer = playerRepository.findById(id);
        if (existingPlayer.isPresent()) {
            Player updatedPlayer = existingPlayer.get();
            updatedPlayer.setLastLoginAt(LocalDateTime.now());
            playerRepository.save(updatedPlayer);
            logger.info("Successfully updated last login time for player: {}", id);
        } else {
            logger.warn("Player not found with id: {}", id);
            throw new IllegalArgumentException("Player not found with id: " + id);
        }
    }
    
    /**
     * 将Player实体转换为PlayerResponse DTO
     */
    private PlayerDTO.PlayerResponse convertToResponse(Player player) {
        PlayerDTO.PlayerResponse response = new PlayerDTO.PlayerResponse();
        response.setId(player.getId());
        response.setUsername(player.getUsername());
        response.setNickname(player.getNickname());
        response.setEmail(player.getEmail());
        response.setAvatar(player.getAvatar());
        response.setRole(player.getRole());
        response.setStatus(player.getStatus());
        response.setCreateTime(player.getCreateTime());
        return response;
    }
    
    /**
     * 将Player实体转换为PlayerDetailResponse DTO
     */
    private PlayerDTO.PlayerDetailResponse convertToDetailResponse(Player player) {
        PlayerDTO.PlayerDetailResponse response = new PlayerDTO.PlayerDetailResponse();
        response.setId(player.getId());
        response.setUsername(player.getUsername());
        response.setNickname(player.getNickname());
        response.setEmail(player.getEmail());
        response.setAvatar(player.getAvatar());
        response.setRole(player.getRole());
        response.setStatus(player.getStatus());
        response.setCreateTime(player.getCreateTime());
        response.setUpdateTime(player.getUpdateTime());
        response.setLastLoginAt(player.getLastLoginAt());
        return response;
    }
}
