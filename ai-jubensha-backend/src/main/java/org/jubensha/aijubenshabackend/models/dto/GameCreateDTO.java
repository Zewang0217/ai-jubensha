package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.models.enums.GameStatus;

import java.time.LocalDateTime;

/**
 * 游戏创建数据传输对象
 */
@Data
public class GameCreateDTO {

    @NotNull(message = "剧本ID不能为空")
    private Long scriptId;

    @Size(max = 50, message = "游戏房间码长度不能超过50个字符")
    private String gameCode;

    private GameStatus status;
    private GamePhase currentPhase;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /**
     * 默认构造函数，设置默认值
     */
    public GameCreateDTO() {
        this.status = GameStatus.CREATED;
        this.currentPhase = GamePhase.INTRODUCTION;
    }
}