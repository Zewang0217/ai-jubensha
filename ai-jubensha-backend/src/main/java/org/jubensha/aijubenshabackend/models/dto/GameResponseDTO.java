package org.jubensha.aijubenshabackend.models.dto;

import lombok.Data;
import org.jubensha.aijubenshabackend.models.entity.Game;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.models.enums.GameStatus;

import java.time.LocalDateTime;

/**
 * 游戏响应数据传输对象
 */
@Data
public class GameResponseDTO {

    private Long id;
    private Long scriptId;
    private String gameCode;
    private GameStatus status;
    private GamePhase currentPhase;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 从实体对象创建响应DTO
     *
     * @param game 游戏实体
     * @return 游戏响应DTO
     */
    public static GameResponseDTO fromEntity(Game game) {
        GameResponseDTO dto = new GameResponseDTO();
        dto.setId(game.getId());
        dto.setScriptId(game.getScriptId());
        dto.setGameCode(game.getGameCode());
        dto.setStatus(game.getStatus());
        dto.setCurrentPhase(game.getCurrentPhase());
        dto.setStartTime(game.getStartTime());
        dto.setEndTime(game.getEndTime());
        dto.setCreateTime(game.getCreateTime());
        dto.setUpdateTime(game.getUpdateTime());
        return dto;
    }
}