package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;

import java.time.LocalDateTime;

public class ScriptDTO {
    @Data
    public static class ScriptCreateRequest {
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

        private String coverImage;
    }

    @Data
    public static class ScriptUpdateRequest {
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

        private String coverImage;
    }

    @Data
    public static class ScriptResponse {
        private Long id;
        private String name;
        private String description;
        private String author;
        private DifficultyLevel difficulty;
        private Integer duration;
        private Integer playerCount;
        private String coverImage;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }
}
