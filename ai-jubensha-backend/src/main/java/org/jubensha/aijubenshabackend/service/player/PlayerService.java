package org.jubensha.aijubenshabackend.service.player;

import org.jubensha.aijubenshabackend.models.dto.PlayerDTO;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;
import org.jubensha.aijubenshabackend.models.enums.PlayerStatus;

import java.util.List;
import java.util.Optional;

public interface PlayerService {

    /**
     * 创建新玩家
     */
    PlayerDTO.PlayerResponse createPlayer(PlayerDTO.PlayerCreateRequest request);

    
    /**
     * 根据ID获取玩家
     */
    Optional<PlayerDTO.PlayerDetailResponse> getPlayerById(Long id);
    
    /**
     * 根据用户名获取玩家
     */
    Optional<PlayerDTO.PlayerDetailResponse> getPlayerByUsername(String username);
    
    /**
     * 根据邮箱获取玩家
     */
    Optional<PlayerDTO.PlayerDetailResponse> getPlayerByEmail(String email);
    
    /**
     * 获取所有玩家
     */
    List<PlayerDTO.PlayerDetailResponse> getAllPlayers();


    /**
     * 获取在线玩家
     */
    List<PlayerDTO.PlayerDetailResponse> getOnlinePlayers();
    
    /**
     * 更新玩家
     */
    PlayerDTO.PlayerResponse updatePlayer(Long id, PlayerDTO.PlayerUpdateRequest request);
    
    /**
     * 更新玩家状态
     */
    PlayerDTO.PlayerResponse updatePlayerStatus(Long id, String status);
    
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
    List<PlayerDTO.PlayerResponse> getPlayersByStatus(String status);
    
    /**
     * 根据角色获取玩家
     */
    List<PlayerDTO.PlayerResponse> getPlayersByRole(String role);
    
    /**
     * 根据状态和角色获取玩家
     */
    List<PlayerDTO.PlayerResponse> getPlayersByStatusAndRole(String status, String role);
    
    /**
     * 更新玩家最后登录时间
     */
    void updateLastLoginTime(Long id);
}