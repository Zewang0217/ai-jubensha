package org.jubensha.aijubenshabackend.models.dto;

import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.GamePlayerStatus;

/**
 * 游戏玩家更新数据传输对象
 */
@Data
public class GamePlayerUpdateDTO {

    private Long gameId;
    private Long playerId;
    private Long characterId;
    private Boolean isDm;
    private GamePlayerStatus status;
}