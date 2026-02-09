package org.jubensha.aijubenshabackend.models.dto;

import lombok.Data;
import org.jubensha.aijubenshabackend.models.entity.Clue;
import org.jubensha.aijubenshabackend.models.enums.ClueType;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;

import java.time.LocalDateTime;

/**
 * 线索响应数据传输对象
 */
@Data
public class ClueResponseDTO {

    private Long id;
    private Long scriptId;
    private String name;
    private String description;
    private ClueType type;
    private ClueVisibility visibility;
    private String scene;
    private String imageUrl;
    private Integer importance;
    private LocalDateTime createTime;

    /**
     * 从实体对象创建响应DTO
     *
     * @param clue 线索实体
     * @return 线索响应DTO
     */
    public static ClueResponseDTO fromEntity(Clue clue) {
        ClueResponseDTO dto = new ClueResponseDTO();
        dto.setId(clue.getId());
        dto.setScriptId(clue.getScriptId());
        dto.setName(clue.getName());
        dto.setDescription(clue.getDescription());
        dto.setType(clue.getType());
        dto.setVisibility(clue.getVisibility());
        dto.setScene(clue.getScene());
        dto.setImageUrl(clue.getImageUrl());
        dto.setImportance(clue.getImportance());
        dto.setCreateTime(clue.getCreateTime());
        return dto;
    }
}