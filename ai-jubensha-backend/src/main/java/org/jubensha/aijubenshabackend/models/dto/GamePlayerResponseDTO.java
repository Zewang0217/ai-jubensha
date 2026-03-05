package org.jubensha.aijubenshabackend.models.dto;

import lombok.Data;
import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.jubensha.aijubenshabackend.models.enums.GamePlayerStatus;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;

import java.time.LocalDateTime;

/**
 * 游戏玩家响应数据传输对象
 */
@Data
public class GamePlayerResponseDTO {

    private Long id;
    private Long gameId;
    private Long playerId;
    private Long characterId;
    private Boolean isDm;
    private GamePlayerStatus status;
    private LocalDateTime joinTime;
    /**
     * 玩家角色类型（REAL/AI）
     */
    private PlayerRole playerRole;

    /**
     * 从实体对象创建响应DTO
     *
     * @param gamePlayer 游戏玩家实体
     * @return 游戏玩家响应DTO
     */
    public static GamePlayerResponseDTO fromEntity(GamePlayer gamePlayer) {
        GamePlayerResponseDTO dto = new GamePlayerResponseDTO();
        dto.setId(gamePlayer.getId());
        dto.setGameId(gamePlayer.getGame() != null ? gamePlayer.getGame().getId() : null);
        dto.setPlayerId(gamePlayer.getPlayer() != null ? gamePlayer.getPlayer().getId() : null);
        dto.setCharacterId(gamePlayer.getCharacter() != null ? gamePlayer.getCharacter().getId() : null);
        dto.setIsDm(gamePlayer.getIsDm());
        dto.setStatus(gamePlayer.getStatus());
        dto.setJoinTime(gamePlayer.getJoinTime());
        // 添加玩家角色类型
        if (gamePlayer.getPlayer() != null) {
            dto.setPlayerRole(gamePlayer.getPlayer().getRole());
        }
        return dto;
    }
}