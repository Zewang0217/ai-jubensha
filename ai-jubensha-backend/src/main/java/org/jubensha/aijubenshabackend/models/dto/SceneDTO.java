package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

public class SceneDTO {
    @Data
    public static class SceneCreateRequest {
        @NotNull(message = "剧本ID不能为空")
        private Long scriptId;

        @NotBlank(message = "场景名称不能为空")
        private String name;

        @NotBlank(message = "场景描述不能为空")
        private String description;

        private String image;

        private String availableActions;
    }
    @Data
    public static class SceneUpdateRequest {
        @NotNull(message = "剧本ID不能为空")
        private Long scriptId;

        @NotBlank(message = "场景名称不能为空")
        private String name;

        @NotBlank(message = "场景描述不能为空")
        private String description;

        private String image;

        private String availableActions;
    }
    @Data
    public static class SceneResponse {
        private Long id;
        private Long scriptId;
        private String scriptName;  // 关联剧本名称
        private String name;
        private String description;
        private String image;
        private String availableActions;
        private LocalDateTime createTime;
    }

}
