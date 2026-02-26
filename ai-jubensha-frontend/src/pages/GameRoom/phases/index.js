/**
 * @fileoverview GameRoom 阶段组件导出
 * @description 统一导出所有阶段组件和类型
 */

// =============================================================================
// 先导入所有组件
// =============================================================================

import ScriptOverview from './ScriptOverview'
import CharacterAssignment from './CharacterAssignment'
import ScriptReading from './ScriptReading'
import Investigation from './Investigation'
import Discussion from './Discussion'
import Summary from './Summary'

import {DEFAULT_PHASE_SEQUENCE, PHASE_ACTION, PHASE_CONFIG, PHASE_TYPE,} from '../types'

import {usePhaseManager} from '../hooks/usePhaseManager'

// =============================================================================
// 阶段组件导出
// =============================================================================

export {ScriptOverview}
export {CharacterAssignment}
export {ScriptReading}
export {Investigation}
export {Discussion}
export {Summary}

// =============================================================================
// 类型和常量导出
// =============================================================================

export {PHASE_TYPE, PHASE_CONFIG, DEFAULT_PHASE_SEQUENCE, PHASE_ACTION}

// =============================================================================
// Hooks 导出
// =============================================================================

export {usePhaseManager}

/**
 * 阶段组件映射表
 * @type {Record<string, React.ComponentType>}
 */
export const PHASE_COMPONENTS = {
    [PHASE_TYPE.SCRIPT_OVERVIEW]: ScriptOverview,
    [PHASE_TYPE.CHARACTER_ASSIGNMENT]: CharacterAssignment,
    [PHASE_TYPE.SCRIPT_READING]: ScriptReading,
    [PHASE_TYPE.INVESTIGATION]: Investigation,
    [PHASE_TYPE.DISCUSSION]: Discussion,
    [PHASE_TYPE.SUMMARY]: Summary,
}

/**
 * 根据阶段类型获取组件
 *
 * @param {string} phaseType - 阶段类型
 * @returns {React.ComponentType|null} 阶段组件
 */
export function getPhaseComponent(phaseType) {
    return PHASE_COMPONENTS[phaseType] || null
}

/**
 * 检查阶段类型是否有效
 *
 * @param {string} phaseType - 阶段类型
 * @returns {boolean} 是否有效
 */
export function isValidPhaseType(phaseType) {
    return Object.values(PHASE_TYPE).includes(phaseType)
}
