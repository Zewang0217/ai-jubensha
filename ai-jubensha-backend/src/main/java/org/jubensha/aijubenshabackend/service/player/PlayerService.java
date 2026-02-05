package org.jubensha.aijubenshabackend.service.player;

import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;
import org.jubensha.aijubenshabackend.models.enums.PlayerStatus;

import java.util.List;
import java.util.Optional;

public interface PlayerService {

    /**
     * 保存玩家
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
    List<Player> getPlayersByStatus(PlayerStatus status);

    /**
     * 根据角色获取玩家
     */
    List<Player> getPlayersByRole(PlayerRole role);

    /**
     * 根据状态和角色获取玩家
     */
    List<Player> getPlayersByStatusAndRole(PlayerStatus status, PlayerRole role);

    /**
     * 更新玩家最后登录时间
     */
    void updateLastLoginTime(Long id);

    /**
     * 根据昵称查询玩家
     */
    List<Player> getPlayersByNickname(String nickname);

    /**
     * 根据创建时间范围查询玩家
     */
    List<Player> getPlayersByCreateTimeRange(String startTime, String endTime);

    /**
     * 更新玩家角色
     */
    Player updatePlayerRole(Long id, PlayerRole role);

    /**
     * 更新玩家头像
     */
    Player updatePlayerAvatar(Long id, String avatarUrl);
}