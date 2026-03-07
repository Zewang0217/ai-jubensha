/**
 * @fileoverview 阶段主题颜色配置
 * @description 定义游戏阶段组件使用的主题颜色常量
 * @author zewang
 */

/**
 * 阶段主题颜色
 * @readonly
 * @enum {string}
 * @description 游戏阶段使用的主题颜色常量
 */
export const PHASE_COLORS = {
  /** 主色调 - 淡紫色 */
  primary: '#7C8CD6',
  /** 次色调 - 紫罗兰 */
  secondary: '#A78BFA',
  /** 强调色 - 淡粉色 */
  accent: '#F5A9C9',
  /** 成功色 - 青绿色 */
  success: '#5DD9A8',
  /** 警告色 - 琥珀色 */
  warning: '#FBBF24',
  /** 错误色 - 红色 */
  error: '#F87171',
  
  /** 文字颜色 */
  text: {
    primary: '#2D3748',
    secondary: '#5A6978',
    muted: '#8C96A5',
    light: '#E8ECF2',
  },
  
  /** 背景颜色 */
  background: {
    light: '#EEF1F6',
    card: '#222631',
    dark: '#1A1D26',
  },
  
  /** 边框颜色 */
  border: {
    light: '#E0E5EE',
    dark: '#363D4D',
    muted: '#2A2F3C',
  },
}

/**
 * 渐变色配置
 * @readonly
 * @type {Object}
 * @description 渐变色样式配置
 */
export const PHASE_GRADIENTS = {
  /** 主渐变 - 从主色到次色 */
  primary: `linear-gradient(to right, ${PHASE_COLORS.primary}, ${PHASE_COLORS.secondary})`,
  /** 强调渐变 - 从次色到强调色 */
  accent: `linear-gradient(to right, ${PHASE_COLORS.primary}, ${PHASE_COLORS.secondary}, ${PHASE_COLORS.accent})`,
  /** 成功渐变 */
  success: `linear-gradient(to right, ${PHASE_COLORS.success}, #4ECDC4)`,
  /** 强调色渐变 */
  pink: `linear-gradient(to right, ${PHASE_COLORS.accent}, #E879A9)`,
}

/**
 * Tailwind CSS 颜色类映射
 * @readonly
 * @type {Object}
 * @description 用于动态生成 Tailwind CSS 类名
 */
export const PHASE_COLOR_CLASSES = {
  primary: {
    bg: 'bg-[#7C8CD6]',
    bgLight: 'bg-[#7C8CD6]/10',
    bgLighter: 'bg-[#7C8CD6]/20',
    text: 'text-[#7C8CD6]',
    border: 'border-[#7C8CD6]',
    borderLight: 'border-[#7C8CD6]/30',
  },
  secondary: {
    bg: 'bg-[#A78BFA]',
    bgLight: 'bg-[#A78BFA]/10',
    text: 'text-[#A78BFA]',
    border: 'border-[#A78BFA]',
  },
  accent: {
    bg: 'bg-[#F5A9C9]',
    bgLight: 'bg-[#F5A9C9]/10',
    text: 'text-[#F5A9C9]',
    border: 'border-[#F5A9C9]',
    borderLight: 'border-[#F5A9C9]/30',
  },
  success: {
    bg: 'bg-[#5DD9A8]',
    bgLight: 'bg-[#5DD9A8]/10',
    text: 'text-[#5DD9A8]',
    border: 'border-[#5DD9A8]',
    borderLight: 'border-[#5DD9A8]/30',
  },
  error: {
    bg: 'bg-[#F87171]',
    bgLight: 'bg-[#F87171]/10',
    text: 'text-[#F87171]',
    border: 'border-[#F87171]',
  },
}

export default PHASE_COLORS
