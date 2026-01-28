package org.jubensha.aijubenshabackend.service.player;

import org.jubensha.aijubenshabackend.domain.model.Player;
import org.jubensha.aijubenshabackend.core.constant.PlayerStatus;
import org.jubensha.aijubenshabackend.repository.player.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        logger.info("Creating new player: {}", player.getUsername());
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
        return playerRepository.findByStatus("online");
    }
    
    @Override
    public Player updatePlayer(Long id, Player player) {
        logger.info("Updating player: {}", id);
        Optional<Player> existingPlayer = playerRepository.findById(id);
        if (existingPlayer.isPresent()) {
            Player updatedPlayer = existingPlayer.get();
            updatedPlayer.setUsername(player.getUsername());
            updatedPlayer.setNickname(player.getNickname());
            updatedPlayer.setPassword(player.getPassword());
            updatedPlayer.setEmail(player.getEmail());
            updatedPlayer.setAvatarUrl(player.getAvatarUrl());
            updatedPlayer.setRole(player.getRole());
            updatedPlayer.setStatus(player.getStatus());
            return playerRepository.save(updatedPlayer);
        } else {
            throw new IllegalArgumentException("Player not found with id: " + id);
        }
    }
    
    @Override
    public Player updatePlayerStatus(Long id, PlayerStatus status) {
        logger.info("Updating player status: {} to {}", id, status);
        Optional<Player> existingPlayer = playerRepository.findById(id);
        if (existingPlayer.isPresent()) {
            Player updatedPlayer = existingPlayer.get();
            updatedPlayer.setStatus(status.toString());
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
    public List<Player> getPlayersByStatus(String status) {
        logger.info("Getting players by status: {}", status);
        return playerRepository.findByStatus(status);
    }
    
    @Override
    public List<Player> getPlayersByRole(String role) {
        logger.info("Getting players by role: {}", role);
        return playerRepository.findByRole(role);
    }
    
    @Override
    public List<Player> getPlayersByStatusAndRole(String status, String role) {
        logger.info("Getting players by status: {} and role: {}", status, role);
        return playerRepository.findByStatusAndRole(status, role);
    }
    
    @Override
    public void updateLastLoginTime(Long id) {
        logger.info("Updating last login time for player: {}", id);
        Optional<Player> existingPlayer = playerRepository.findById(id);
        if (existingPlayer.isPresent()) {
            Player updatedPlayer = existingPlayer.get();
            updatedPlayer.setLastLoginAt(java.time.LocalDateTime.now());
            playerRepository.save(updatedPlayer);
        }
    }
}