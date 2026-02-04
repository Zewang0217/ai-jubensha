package org.jubensha.aijubenshabackend.models.dto;

import lombok.Data;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;

import java.time.LocalDateTime;

/**
 * 剧本响应数据传输对象
 */
@Data
public class ScriptResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String author;
    private DifficultyLevel difficulty;
    private Integer duration;
    private Integer playerCount;
    private String coverImageUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 从实体对象创建响应DTO
     *
     * @param script 剧本实体
     * @return 剧本响应DTO
     */
    public static ScriptResponseDTO fromEntity(Script script) {
        ScriptResponseDTO dto = new ScriptResponseDTO();
        dto.setId(script.getId());
        dto.setName(script.getName());
        dto.setDescription(script.getDescription());
        dto.setAuthor(script.getAuthor());
        dto.setDifficulty(script.getDifficulty());
        dto.setDuration(script.getDuration());
        dto.setPlayerCount(script.getPlayerCount());
        dto.setCoverImageUrl(script.getCoverImageUrl());
        dto.setCreateTime(script.getCreateTime());
        dto.setUpdateTime(script.getUpdateTime());
        return dto;
    }
}