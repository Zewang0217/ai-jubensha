package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 阶段确认请求数据传输对象
 *
 * @author zewang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhaseConfirmDTO {
    /**
     * 玩家ID
     */
    @NotNull(message = "玩家ID不能为空")
    private Long playerId;

    /**
     * 当前阶段名称
     */
    @NotNull(message = "阶段名称不能为空")
    private String phase;
}
