/**
 * @fileoverview 主题配置
 * @description 游戏房间主题色彩配置
 */

/**
 * 阶段颜色配置
 * 用于各个阶段组件的统一色彩主题
 */
export const PHASE_COLORS = {
  // 主强调色 - 淡紫色
  primary: '#7C8CD6',
  // 次强调色 - 淡紫粉色
  secondary: '#A78BFA',
  // 萌系点缀 - 淡粉色
  accent: '#F5A9C9',
  // 成功色 - 薄荷绿
  success: '#5DD9A8',
  // 警告色 - 暖橙色
  warning: '#F5A9C9',
  // 错误色 - 玫瑰红
  error: '#E879A9',
  // 信息色 - 天蓝色
  info: '#7C8CD6',
}

/**
 * 背景色配置
 */
export const BACKGROUND_COLORS = {
  // 浅色主题背景
  light: ['#F5F7FA', '#EEF1F6', '#E4E8EE'],
  // 深色主题背景
  dark: ['#1A1D26', '#222631', '#2A2F3C'],
}

/**
 * 文字色配置
 */
export const TEXT_COLORS = {
  // 主要文字
  primary: '#2D3748',
  // 次要文字
  secondary: '#5A6978',
  // 弱化文字
  muted: '#8C96A5',
  // 强调文字
  accent: '#7C8CD6',
}

export default {
  PHASE_COLORS,
  BACKGROUND_COLORS,
  TEXT_COLORS,
}
