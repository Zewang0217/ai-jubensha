package org.jubensha.aijubenshabackend.models.dto;

import lombok.Data;
import org.jubensha.aijubenshabackend.models.entity.Clue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 搜证响应数据传输对象
 * 用于返回搜证结果给前端
 *
 * @author luobo
 * @date 2026-02-10
 */
@Data
public class InvestigationResponseDTO {

    /**
     * 是否搜证成功
     */
    private boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 获得的线索信息
     */
    private ClueInfo clue;

    /**
     * 剩余搜证次数
     */
    private int remainingChances;

    /**
     * 总搜证次数
     */
    private int totalChances;

    /**
     * 当前搜证轮次
     */
    private int currentRound;

    /**
     * 搜证时间
     */
    private LocalDateTime investigateTime;

    /**
     * 玩家的搜证历史
     */
    private List<Map<String, Object>> investigationHistory;

    /**
     * 创建成功响应
     *
     * @param clue             获得的线索
     * @param remainingChances 剩余次数
     * @param totalChances     总次数
     * @return 响应DTO
     */
    public static InvestigationResponseDTO success(Clue clue, int remainingChances, int totalChances) {
        InvestigationResponseDTO dto = new InvestigationResponseDTO();
        dto.setSuccess(true);
        dto.setMessage("搜证成功");
        dto.setClue(ClueInfo.fromEntity(clue));
        dto.setRemainingChances(remainingChances);
        dto.setTotalChances(totalChances);
        dto.setInvestigateTime(LocalDateTime.now());
        return dto;
    }

    /**
     * 创建失败响应
     *
     * @param message 错误消息
     * @return 响应DTO
     */
    public static InvestigationResponseDTO failure(String message) {
        InvestigationResponseDTO dto = new InvestigationResponseDTO();
        dto.setSuccess(false);
        dto.setMessage(message);
        dto.setInvestigateTime(LocalDateTime.now());
        return dto;
    }

    /**
     * 线索信息内部类
     */
    @Data
    public static class ClueInfo {
        /**
         * 线索ID
         */
        private Long clueId;

        /**
         * 线索名称
         */
        private String clueName;

        /**
         * 线索描述
         */
        private String description;

        /**
         * 线索类型
         */
        private String type;

        /**
         * 线索重要度
         */
        private Integer importance;

        /**
         * 所属场景
         */
        private String scene;

        /**
         * 线索图片URL
         */
        private String imageUrl;

        /**
         * 从 Clue 实体创建 ClueInfo
         *
         * @param clue 线索实体
         * @return 线索信息DTO
         */
        public static ClueInfo fromEntity(Clue clue) {
            if (clue == null) {
                return null;
            }
            ClueInfo info = new ClueInfo();
            info.setClueId(clue.getId());
            info.setClueName(clue.getName());
            info.setDescription(clue.getDescription());
            info.setType(clue.getType() != null ? clue.getType().name() : null);
            info.setImportance(clue.getImportance());
            info.setScene(clue.getScene());
            info.setImageUrl(clue.getImageUrl());
            return info;
        }
    }
}
