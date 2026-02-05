package org.jubensha.aijubenshabackend.models.dto;

import lombok.Data;
import org.jubensha.aijubenshabackend.models.entity.Scene;

import java.time.LocalDateTime;

/**
 * 场景响应数据传输对象
 */
@Data
public class SceneResponseDTO {

    private Long id;
    private Long scriptId;
    // 关联剧本名称
    private String scriptName;
    private String name;
    private String description;
    private String imageUrl;
    private String availableActions;
    private LocalDateTime createTime;

    /**
     * 从实体对象创建响应DTO
     *
     * @param scene 场景实体
     * @return 场景响应DTO
     */
    public static SceneResponseDTO fromEntity(Scene scene) {
        SceneResponseDTO dto = new SceneResponseDTO();
        dto.setId(scene.getId());
        dto.setScriptId(scene.getScript() != null ? scene.getScript().getId() : null);
        dto.setScriptName(scene.getScript() != null ? scene.getScript().getName() : null);
        dto.setName(scene.getName());
        dto.setDescription(scene.getDescription());
        dto.setImageUrl(scene.getImageUrl());
        dto.setAvailableActions(scene.getAvailableActions());
        dto.setCreateTime(scene.getCreateTime());
        return dto;
    }
}