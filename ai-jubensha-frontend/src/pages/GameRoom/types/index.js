/**
 * @fileoverview GameRoom 类型定义
 * @description 定义阶段相关的类型和常量，支持灵活的阶段组合
 */

// =============================================================================
// 阶段类型枚举
// =============================================================================

/**
 * @readonly
 * @enum {string}
 * @description 游戏阶段类型枚举
 */
export const PHASE_TYPE = {
    /** 剧本生成/概述 */
    SCRIPT_OVERVIEW: 'script_overview',
    /** 角色分配 */
    CHARACTER_ASSIGNMENT: 'character_assignment',
    /** 剧本阅读 */
    SCRIPT_READING: 'script_reading',
    /** 搜证阶段 */
    INVESTIGATION: 'investigation',
    /** 讨论阶段 */
    DISCUSSION: 'discussion',
    /** 总结阶段 */
    SUMMARY: 'summary',
}

// =============================================================================
// 阶段配置
// =============================================================================

/**
 * @readonly
 * @type {Record<string, PhaseConfig>}
 * @description 阶段配置映射表
 */
export const PHASE_CONFIG = {
    [PHASE_TYPE.SCRIPT_OVERVIEW]: {
        id: PHASE_TYPE.SCRIPT_OVERVIEW,
        title: '剧本概览',
        description: '了解剧本基本信息',
        icon: 'BookOpen',
        allowSkip: false,
        allowBack: false,
    },
    [PHASE_TYPE.CHARACTER_ASSIGNMENT]: {
        id: PHASE_TYPE.CHARACTER_ASSIGNMENT,
        title: '角色分配',
        description: '查看你的角色和任务',
        icon: 'User',
        allowSkip: false,
        allowBack: true,
    },
    [PHASE_TYPE.SCRIPT_READING]: {
        id: PHASE_TYPE.SCRIPT_READING,
        title: '阅读剧本',
        description: '阅读剧本内容',
        icon: 'Scroll',
        allowSkip: true,
        allowBack: true,
    },
    [PHASE_TYPE.INVESTIGATION]: {
        id: PHASE_TYPE.INVESTIGATION,
        title: '线索搜证',
        description: '探索场景收集线索',
        icon: 'Search',
        allowSkip: false,
        allowBack: true,
    },
    [PHASE_TYPE.DISCUSSION]: {
        id: PHASE_TYPE.DISCUSSION,
        title: '推理讨论',
        description: '分享线索推理真相',
        icon: 'MessageCircle',
        allowSkip: false,
        allowBack: true,
    },
    [PHASE_TYPE.SUMMARY]: {
        id: PHASE_TYPE.SUMMARY,
        title: '真相揭晓',
        description: '查看最终结果',
        icon: 'Trophy',
        allowSkip: false,
        allowBack: false,
    },
}

// =============================================================================
// 默认阶段流程（固定流程模式）
// =============================================================================

/**
 * @readonly
 * @type {string[]}
 * @description 默认的游戏阶段流程
 */
export const DEFAULT_PHASE_SEQUENCE = [
    PHASE_TYPE.SCRIPT_OVERVIEW,
    PHASE_TYPE.CHARACTER_ASSIGNMENT,
    PHASE_TYPE.SCRIPT_READING,
    PHASE_TYPE.INVESTIGATION,
    PHASE_TYPE.DISCUSSION,
    PHASE_TYPE.SUMMARY,
]

// =============================================================================
// 阶段动作类型
// =============================================================================

/**
 * @readonly
 * @enum {string}
 * @description 阶段动作类型枚举
 */
export const PHASE_ACTION = {
    /** 进入阶段 */
    ENTER: 'enter',
    /** 离开阶段 */
    EXIT: 'exit',
    /** 阶段完成 */
    COMPLETE: 'complete',
    /** 跳转到指定阶段 */
    SKIP: 'skip',
    /** 返回上一阶段 */
    BACK: 'back',
}

// =============================================================================
// JSDoc 类型定义（用于IDE提示）
// =============================================================================

/**
 * @typedef {Object} PhaseConfig
 * @property {string} id - 阶段唯一标识
 * @property {string} title - 阶段标题
 * @property {string} description - 阶段描述
 * @property {string} icon - 图标名称
 * @property {boolean} allowSkip - 是否允许跳过
 * @property {boolean} allowBack - 是否允许返回
 */

/**
 * @typedef {Object} PhaseState
 * @property {string} currentPhase - 当前阶段ID
 * @property {string} previousPhase - 上一阶段ID
 * @property {number} currentIndex - 当前阶段索引
 * @property {string[]} phaseSequence - 阶段序列
 * @property {Record<string, any>} phaseData - 各阶段数据缓存
 * @property {boolean} isTransitioning - 是否正在切换
 */

/**
 * @typedef {Object} PhaseProps
 * @property {PhaseConfig} config - 阶段配置
 * @property {Object} gameData - 游戏数据
 * @property {Object} playerData - 玩家数据
 * @property {Function} onComplete - 阶段完成回调
 * @property {Function} onSkip - 跳过阶段回调
 * @property {Function} onBack - 返回上一阶段回调
 * @property {Function} onAction - 通用动作回调
 * @property {boolean} isActive - 是否为当前激活阶段
 */

/**
 * @typedef {Object} CharacterInfo
 * @property {string} id - 角色ID
 * @property {string} name - 角色名称
 * @property {string} avatar - 头像URL
 * @property {string} description - 角色描述
 * @property {string} background - 角色背景故事
 * @property {string} secret - 角色秘密
 * @property {string} goal - 角色目标
 * @property {boolean} isPlayer - 是否由玩家控制
 * @property {boolean} isAI - 是否AI控制
 */

/**
 * @typedef {Object} SceneInfo
 * @property {string} id - 场景ID
 * @property {string} name - 场景名称
 * @property {string} description - 场景描述
 * @property {string} imageUrl - 场景图片
 * @property {boolean} isLocked - 是否锁定
 * @property {number} clueCount - 线索数量
 */

/**
 * @typedef {Object} ClueInfo
 * @property {string} id - 线索ID
 * @property {string} name - 线索名称
 * @property {string} description - 线索描述
 * @property {string} type - 线索类型
 * @property {string} sceneId - 所属场景ID
 * @property {boolean} isRevealed - 是否已发现
 * @property {string} imageUrl - 线索图片
 */

/**
 * @typedef {Object} VoteInfo
 * @property {string} voterId - 投票者ID
 * @property {string} targetId - 被投票者ID
 * @property {string} reason - 投票理由
 */
