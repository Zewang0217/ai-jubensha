package org.jubensha.aijubenshabackend.models.dto;

import lombok.Data;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;
import org.jubensha.aijubenshabackend.models.enums.PlayerStatus;

import java.time.LocalDateTime;

/**
 * 玩家响应数据传输对象
 */
@Data
public class PlayerResponseDTO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatarUrl;
    private PlayerRole role;
    private PlayerStatus status;
    private LocalDateTime createTime;

    /**
     * 从实体对象创建响应DTO
     *
     * @param player 玩家实体
     * @return 玩家响应DTO
     */
    public static PlayerResponseDTO fromEntity(Player player) {
        PlayerResponseDTO dto = new PlayerResponseDTO();
        dto.setId(player.getId());
        dto.setUsername(player.getUsername());
        dto.setNickname(player.getNickname());
        dto.setEmail(player.getEmail());
        dto.setAvatarUrl(player.getAvatarUrl());
        dto.setRole(player.getRole());
        dto.setStatus(player.getStatus());
        dto.setCreateTime(player.getCreateTime());
        return dto;
    }
}