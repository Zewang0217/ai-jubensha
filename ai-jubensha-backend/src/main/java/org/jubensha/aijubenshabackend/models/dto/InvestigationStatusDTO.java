package org.jubensha.aijubenshabackend.models.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 搜证状态数据传输对象
 * 用于返回玩家的搜证状态信息
 *
 * @author luobo
 * @date 2026-02-10
 */
@Data
public class InvestigationStatusDTO {

    /**
     * 玩家ID
     */
    private Long playerId;

    /**
     * 剩余搜证次数
     */
    private int remainingChances;

    /**
     * 总搜证次数
     */
    private int totalChances;

    /**
     * 已使用次数
     */
    private int usedChances;

    /**
     * 当前搜证轮次
     */
    private int currentRound;

    /**
     * 当前游戏阶段
     */
    private String currentPhase;

    /**
     * 搜证历史记录
     */
    private List<Map<String, Object>> investigationHistory;

    /**
     * 可搜证的场景列表
     */
    private List<Map<String, Object>> availableScenes;

    /**
     * 是否还可以搜证
     */
    private boolean canInvestigate;

    /**
     * 创建状态DTO
     *
     * @param playerId             玩家ID
     * @param remainingChances     剩余次数
     * @param totalChances         总次数
     * @param currentPhase         当前阶段
     * @param investigationHistory 搜证历史
     * @return 状态DTO
     */
    public static InvestigationStatusDTO of(Long playerId, int remainingChances, int totalChances,
                                            String currentPhase, List<Map<String, Object>> investigationHistory) {
        InvestigationStatusDTO dto = new InvestigationStatusDTO();
        dto.setPlayerId(playerId);
        dto.setRemainingChances(remainingChances);
        dto.setTotalChances(totalChances);
        dto.setUsedChances(totalChances - remainingChances);
        dto.setCurrentPhase(currentPhase);
        dto.setInvestigationHistory(investigationHistory);
        dto.setCanInvestigate(remainingChances > 0);
        return dto;
    }
}
