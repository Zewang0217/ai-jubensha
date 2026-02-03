package org.jubensha.aijubenshabackend.models.dto;

import lombok.Data;
import org.jubensha.aijubenshabackend.models.entity.Character;

import java.time.LocalDateTime;

/**
 * 角色响应数据传输对象
 */
@Data
public class CharacterResponseDTO {

    private Long id;
    private Long scriptId;
    private String name;
    private String description;
    private String backgroundStory;
    private String secret;
    private String avatarUrl;
    private LocalDateTime createTime;

    /**
     * 从实体对象创建响应DTO
     *
     * @param character 角色实体
     * @return 角色响应DTO
     */
    public static CharacterResponseDTO fromEntity(Character character) {
        CharacterResponseDTO dto = new CharacterResponseDTO();
        dto.setId(character.getId());
        dto.setScriptId(character.getScriptId());
        dto.setName(character.getName());
        dto.setDescription(character.getDescription());
        dto.setBackgroundStory(character.getBackgroundStory());
        dto.setSecret(character.getSecret());
        dto.setAvatarUrl(character.getAvatarUrl());
        dto.setCreateTime(character.getCreateTime());
        return dto;
    }
}