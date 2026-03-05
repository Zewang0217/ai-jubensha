package org.jubensha.aijubenshabackend.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 阶段状态响应数据传输对象
 *
 * @author zewang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhaseStatusDTO {
    /**
     * 当前阶段名称
     */
    private String currentPhase;

    /**
     * 当前工作流节点名称
     */
    private String workflowNode;

    /**
     * 是否准备好进入下一阶段
     */
    private Boolean isReadyForNext;

    /**
     * 未确认的玩家ID列表
     */
    private List<Long> waitingForPlayers;

    /**
     * 状态描述信息
     */
    private String message;
}
