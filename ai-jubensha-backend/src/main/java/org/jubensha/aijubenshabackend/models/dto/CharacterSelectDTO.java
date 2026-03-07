package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色选择请求数据传输对象
 * 用于真人玩家选择角色时提交的数据
 *
 * @author zewang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterSelectDTO {

    /**
     * 角色ID
     * 玩家选择的角色标识
     */
    @NotNull(message = "角色ID不能为空")
    private Long characterId;

    /**
     * 玩家昵称
     * 可选，如果不提供则自动生成
     */
    private String nickname;
}
