package org.jubensha.aijubenshabackend.models.enums;

import java.util.Arrays;
import java.util.List;

/**
 * 游戏阶段枚举
 * 
 * <p>与前端 PHASE_TYPE 保持一致，定义游戏流程的各个阶段。</p>
 * 
 * <p>阶段序列：</p>
 * <ol>
 *   <li>SCRIPT_OVERVIEW - 剧本概览</li>
 *   <li>CHARACTER_ASSIGNMENT - 角色分配</li>
 *   <li>SCRIPT_READING - 剧本阅读</li>
 *   <li>INVESTIGATION - 搜证阶段</li>
 *   <li>DISCUSSION - 讨论阶段</li>
 *   <li>SUMMARY - 总结阶段</li>
 * </ol>
 * 
 * @author zewang
 * @version 2.0
 * @since 2026
 */
public enum GamePhase {
    /** 剧本概览阶段 */
    SCRIPT_OVERVIEW("剧本概览", "了解剧本基本信息"),
    
    /** 角色分配阶段 */
    CHARACTER_ASSIGNMENT("角色分配", "查看你的角色和任务"),
    
    /** 剧本阅读阶段 */
    SCRIPT_READING("剧本阅读", "阅读剧本内容"),
    
    /** 搜证阶段 */
    INVESTIGATION("线索搜证", "探索场景收集线索"),
    
    /** 讨论阶段 */
    DISCUSSION("推理讨论", "分享线索推理真相"),
    
    /** 总结阶段 */
    SUMMARY("真相揭晓", "查看最终结果");

    /**
     * 阶段序列常量
     * 定义游戏流程的标准阶段顺序
     */
    public static final List<GamePhase> PHASE_SEQUENCE = Arrays.asList(
        SCRIPT_OVERVIEW,
        CHARACTER_ASSIGNMENT,
        SCRIPT_READING,
        INVESTIGATION,
        DISCUSSION,
        SUMMARY
    );

    /**
     * 阶段标题
     */
    private final String title;
    
    /**
     * 阶段描述
     */
    private final String description;

    /**
     * 构造函数
     * 
     * @param title 阶段标题
     * @param description 阶段描述
     */
    GamePhase(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /**
     * 获取阶段标题
     * 
     * @return 阶段标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取阶段描述
     * 
     * @return 阶段描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取下一个阶段
     * 
     * @return 下一个阶段，如果当前是最后一个阶段则返回 null
     */
    public GamePhase getNextPhase() {
        int currentIndex = PHASE_SEQUENCE.indexOf(this);
        if (currentIndex < 0 || currentIndex >= PHASE_SEQUENCE.size() - 1) {
            return null;
        }
        return PHASE_SEQUENCE.get(currentIndex + 1);
    }

    /**
     * 获取上一个阶段
     * 
     * @return 上一个阶段，如果当前是第一个阶段则返回 null
     */
    public GamePhase getPreviousPhase() {
        int currentIndex = PHASE_SEQUENCE.indexOf(this);
        if (currentIndex <= 0) {
            return null;
        }
        return PHASE_SEQUENCE.get(currentIndex - 1);
    }

    /**
     * 检查是否是最后一个阶段
     * 
     * @return 如果是最后一个阶段返回 true，否则返回 false
     */
    public boolean isLastPhase() {
        return this == SUMMARY;
    }

    /**
     * 检查是否是第一个阶段
     * 
     * @return 如果是第一个阶段返回 true，否则返回 false
     */
    public boolean isFirstPhase() {
        return this == SCRIPT_OVERVIEW;
    }

    /**
     * 获取阶段在序列中的索引
     * 
     * @return 阶段索引，从 0 开始
     */
    public int getIndex() {
        return PHASE_SEQUENCE.indexOf(this);
    }

    /**
     * 根据索引获取阶段
     * 
     * @param index 阶段索引
     * @return 对应的阶段，如果索引无效则返回 null
     */
    public static GamePhase getByIndex(int index) {
        if (index < 0 || index >= PHASE_SEQUENCE.size()) {
            return null;
        }
        return PHASE_SEQUENCE.get(index);
    }

    /**
     * 获取阶段总数
     * 
     * @return 阶段总数
     */
    public static int getTotalPhases() {
        return PHASE_SEQUENCE.size();
    }

    /**
     * 转换为前端格式（小写下划线）
     * 
     * @return 前端格式的阶段名称
     */
    public String toFrontendFormat() {
        return this.name().toLowerCase();
    }

    /**
     * 从前端格式解析
     * 
     * @param frontendPhase 前端格式的阶段名称
     * @return 对应的 GamePhase，如果无法解析则返回 null
     */
    public static GamePhase fromFrontendFormat(String frontendPhase) {
        if (frontendPhase == null || frontendPhase.isEmpty()) {
            return null;
        }
        try {
            return GamePhase.valueOf(frontendPhase.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
