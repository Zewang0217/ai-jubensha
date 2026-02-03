package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.models.enums.GameStatus;

import java.time.LocalDateTime;

/**
 * 游戏更新数据传输对象
 */
@Data
public class GameUpdateDTO {

    @Size(max = 50, message = "游戏房间码长度不能超过50个字符")
    private String gameCode;

    private GameStatus status;
    private GamePhase currentPhase;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}