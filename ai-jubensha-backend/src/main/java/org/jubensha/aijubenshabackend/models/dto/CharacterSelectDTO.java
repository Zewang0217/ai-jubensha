package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色选择请求DTO
 * 
 * <p>用于真人玩家选择角色时提交的数据。</p>
 * 
 * @author zewang
 * @version 1.0
 * @since 2026
 */
@Data
public class CharacterSelectDTO {
    
    /**
     * 选择的角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long characterId;
    
    /**
     * 玩家昵称（可选，用于创建真人玩家）
     */
    private String nickname;
}
