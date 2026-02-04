package org.jubensha.aijubenshabackend.service.player;

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

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public Player createPlayer(Player player) {
        logger.info("Saving player: {}", player.getUsername());

        // 检查用户名和邮箱是否已存在
        if (player.getId() == null) {
            if (isUsernameExists(player.getUsername())) {
                throw new IllegalArgumentException("用户名已存在: " + player.getUsername());
            }
            if (isEmailExists(player.getEmail())) {
                throw new IllegalArgumentException("邮箱已存在: " + player.getEmail());
            }
        }

        // 保存到数据库
        return playerRepository.save(player);
    }

    @Override
    public Optional<Player> getPlayerById(Long id) {
        logger.info("Getting player by id: {}", id);
        return playerRepository.findById(id);
    }

    @Override
    public Optional<Player> getPlayerByUsername(String username) {
        logger.info("Getting player by username: {}", username);
        return playerRepository.findByUsername(username);
    }

    @Override
    public Optional<Player> getPlayerByEmail(String email) {
        logger.info("Getting player by email: {}", email);
        return playerRepository.findByEmail(email);
    }

    @Override
    public List<Player> getAllPlayers() {
        logger.info("Getting all players");
        return playerRepository.findAll();
    }

    @Override
    public List<Player> getOnlinePlayers() {
        logger.info("Getting online players");
        return playerRepository.findByStatus(PlayerStatus.ONLINE);
    }

    @Override
    public Player updatePlayer(Long id, Player player) {
        logger.info("Updating player: {}", id);

        Optional<Player> existingPlayer = playerRepository.findById(id);
        if (existingPlayer.isPresent()) {
            Player updatedPlayer = existingPlayer.get();

            // 更新允许的字段
            if (player.getUsername() != null && !player.getUsername().trim().isEmpty()) {
                // 检查用户名是否被其他用户使用
                if (!player.getUsername().equals(updatedPlayer.getUsername()) &&
                        isUsernameExists(player.getUsername())) {
                    throw new IllegalArgumentException("用户名已存在: " + player.getUsername());
                }
                updatedPlayer.setUsername(player.getUsername());
            }

            if (player.getNickname() != null && !player.getNickname().trim().isEmpty()) {
                updatedPlayer.setNickname(player.getNickname());
            }

            if (player.getPassword() != null && !player.getPassword().trim().isEmpty()) {
                updatedPlayer.setPassword(player.getPassword());
            }

            if (player.getEmail() != null && !player.getEmail().trim().isEmpty()) {
                // 检查邮箱是否被其他用户使用
                if (!player.getEmail().equals(updatedPlayer.getEmail()) &&
                        isEmailExists(player.getEmail())) {
                    throw new IllegalArgumentException("邮箱已存在: " + player.getEmail());
                }
                updatedPlayer.setEmail(player.getEmail());
            }

            if (player.getAvatarUrl() != null) {
                updatedPlayer.setAvatarUrl(player.getAvatarUrl());
            }

            if (player.getStatus() != null) {
                updatedPlayer.setStatus(player.getStatus());
            }

            if (player.getRole() != null) {
                updatedPlayer.setRole(player.getRole());
            }

            return playerRepository.save(updatedPlayer);
        } else {
            throw new IllegalArgumentException("Player not found with id: " + id);
        }
    }

    @Override
    public Player updatePlayerStatus(Long id, PlayerStatus status) {
        logger.info("Updating player status: {} to {}", id, status);

        // 查询现有玩家
        Optional<Player> existingPlayer = playerRepository.findById(id);
        if (existingPlayer.isPresent()) {
            Player updatedPlayer = existingPlayer.get();
            updatedPlayer.setStatus(status);
            return playerRepository.save(updatedPlayer);
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
    public List<Player> getPlayersByStatus(PlayerStatus status) {
        logger.info("Getting players by status: {}", status);
        return playerRepository.findByStatus(status);
    }

    @Override
    public List<Player> getPlayersByRole(PlayerRole role) {
        logger.info("Getting players by role: {}", role);
        return playerRepository.findByRole(role);
    }

    @Override
    public List<Player> getPlayersByStatusAndRole(PlayerStatus status, PlayerRole role) {
        logger.info("Getting players by status: {} and role: {}", status, role);
        return playerRepository.findByStatusAndRole(status, role);
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

    @Override
    public List<Player> getPlayersByNickname(String nickname) {
        logger.info("Getting players by nickname: {}", nickname);
        if (nickname == null || nickname.trim().isEmpty()) {
            return getAllPlayers();
        }
        return playerRepository.findByNicknameContaining(nickname.trim());
    }

    @Override
    public List<Player> getPlayersByCreateTimeRange(String startTime, String endTime) {
        logger.info("Getting players by create time range: {} to {}", startTime, endTime);
        // 这里可以添加具体的实现逻辑，目前返回所有玩家
        return getAllPlayers();
    }

    @Override
    public Player updatePlayerRole(Long id, PlayerRole role) {
        logger.info("Updating player role: {} to {}", id, role);
        Optional<Player> existingPlayer = playerRepository.findById(id);
        if (existingPlayer.isPresent()) {
            Player updatedPlayer = existingPlayer.get();
            updatedPlayer.setRole(role);
            return playerRepository.save(updatedPlayer);
        } else {
            throw new IllegalArgumentException("Player not found with id: " + id);
        }
    }

    @Override
    public Player updatePlayerAvatar(Long id, String avatarUrl) {
        logger.info("Updating player avatar: {}", id);
        Optional<Player> existingPlayer = playerRepository.findById(id);
        if (existingPlayer.isPresent()) {
            Player updatedPlayer = existingPlayer.get();
            updatedPlayer.setAvatarUrl(avatarUrl);
            return playerRepository.save(updatedPlayer);
        } else {
            throw new IllegalArgumentException("Player not found with id: " + id);
        }
    }
}
