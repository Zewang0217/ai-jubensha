package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.GamePlayerStatus;

/**
 * 游戏玩家创建数据传输对象
 */
@Data
public class GamePlayerCreateDTO {

    @NotNull(message = "游戏ID不能为空")
    private Long gameId;

    @NotNull(message = "玩家ID不能为空")
    private Long playerId;

    @NotNull(message = "角色ID不能为空")
    private Long characterId;

    private Boolean isDm;

    private GamePlayerStatus status;

    /**
     * 默认构造函数，设置默认值
     */
    public GamePlayerCreateDTO() {
        this.isDm = false;
        this.status = GamePlayerStatus.READY;
    }
}