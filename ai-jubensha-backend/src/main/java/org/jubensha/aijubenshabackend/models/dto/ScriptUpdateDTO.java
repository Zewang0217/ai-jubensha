package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;

/**
 * 剧本更新数据传输对象
 */
@Data
public class ScriptUpdateDTO {

    @NotBlank(message = "剧本名称不能为空")
    private String name;

    @NotBlank(message = "剧本描述不能为空")
    private String description;

    private String author;

    @NotNull(message = "难度等级不能为空")
    private DifficultyLevel difficulty;

    @Positive(message = "游戏时长必须为正数")
    private Integer duration;

    @Positive(message = "玩家人数必须为正数")
    private Integer playerCount;

    private String coverImageUrl;
}