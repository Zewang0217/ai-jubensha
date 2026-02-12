package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 搜证请求数据传输对象
 * 用于接收玩家搜证请求
 *
 * @author luobo
 * @date 2026-02-10
 */
@Data
public class InvestigationRequestDTO {

    /**
     * 玩家ID
     */
    @NotNull(message = "玩家ID不能为空")
    private Long playerId;

    /**
     * 场景ID
     */
    @NotNull(message = "场景ID不能为空")
    private Long sceneId;

    /**
     * 可选：指定线索ID（如果不指定则随机返回场景中的一个线索）
     */
    private Long clueId;

    /**
     * 可选：搜证备注或玩家留言
     */
    private String remark;
}
