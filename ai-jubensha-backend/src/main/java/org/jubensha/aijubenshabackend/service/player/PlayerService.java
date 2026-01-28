package org.jubensha.aijubenshabackend.service.player;

import org.jubensha.aijubenshabackend.domain.model.Player;
import org.jubensha.aijubenshabackend.core.constant.PlayerStatus;

import java.util.List;
import java.util.Optional;

public interface PlayerService {
    
    /**
     * 创建新玩家
     */
    Player createPlayer(Player player);
    
    /**
     * 根据ID获取玩家
     */
    Optional<Player> getPlayerById(Long id);
    
    /**
     * 根据用户名获取玩家
     */
    Optional<Player> getPlayerByUsername(String username);
    
    /**
     * 根据邮箱获取玩家
     */
    Optional<Player> getPlayerByEmail(String email);
    
    /**
     * 获取所有玩家
     */
    List<Player> getAllPlayers();
    
    /**
     * 获取在线玩家
     */
    List<Player> getOnlinePlayers();
    
    /**
     * 更新玩家
     */
    Player updatePlayer(Long id, Player player);
    
    /**
     * 更新玩家状态
     */
    Player updatePlayerStatus(Long id, PlayerStatus status);
    
    /**
     * 删除玩家
     */
    void deletePlayer(Long id);
    
    /**
     * 验证玩家登录
     */
    boolean validateLogin(String username, String password);
    
    /**
     * 检查用户名是否已存在
     */
    boolean isUsernameExists(String username);
    
    /**
     * 检查邮箱是否已存在
     */
    boolean isEmailExists(String email);
    
    /**
     * 根据状态获取玩家
     */
    List<Player> getPlayersByStatus(String status);
    
    /**
     * 根据角色获取玩家
     */
    List<Player> getPlayersByRole(String role);
    
    /**
     * 根据状态和角色获取玩家
     */
    List<Player> getPlayersByStatusAndRole(String status, String role);
    
    /**
     * 更新玩家最后登录时间
     */
    void updateLastLoginTime(Long id);
}