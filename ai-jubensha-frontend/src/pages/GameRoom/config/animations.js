/**
 * @fileoverview 阶段动画配置
 * @description 定义游戏阶段组件使用的动画配置
 * @author zewang
 */

/**
 * 容器动画配置
 * @description 用于包裹多个子元素的容器，实现交错动画效果
 * @type {Object}
 */
export const containerVariants = {
  hidden: {opacity: 0},
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.05,
      delayChildren: 0.1,
    },
  },
}

/**
 * 单项动画配置
 * @description 用于单个元素的入场动画
 * @type {Object}
 */
export const itemVariants = {
  hidden: {opacity: 0, y: 12},
  visible: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.4,
      ease: [0.25, 0.1, 0.25, 1],
    },
  },
}

/**
 * 缩放入场动画配置
 * @description 用于需要缩放效果的元素
 * @type {Object}
 */
export const scaleInVariants = {
  hidden: {opacity: 0, scale: 0.8},
  visible: {
    opacity: 1,
    scale: 1,
    transition: {
      duration: 0.6,
      ease: [0.25, 0.1, 0.25, 1],
    },
  },
}

/**
 * 滑动动画配置
 * @description 用于左右滑动切换的内容
 * @param {number} direction - 滑动方向（1: 向右, -1: 向左）
 * @returns {Object} 动画配置对象
 */
export const slideVariants = {
  enter: (direction) => ({
    x: direction > 0 ? 20 : -20,
    opacity: 0,
  }),
  center: {
    x: 0,
    opacity: 1,
  },
  exit: (direction) => ({
    x: direction < 0 ? 20 : -20,
    opacity: 0,
  }),
}

/**
 * 淡入淡出动画配置
 * @description 简单的透明度动画
 * @type {Object}
 */
export const fadeVariants = {
  hidden: {opacity: 0},
  visible: {
    opacity: 1,
    transition: {
      duration: 0.3,
    },
  },
  exit: {
    opacity: 0,
    transition: {
      duration: 0.2,
    },
  },
}

/**
 * 弹窗动画配置
 * @description 用于模态框和弹窗的动画
 * @type {Object}
 */
export const modalVariants = {
  backdrop: {
    hidden: {opacity: 0},
    visible: {opacity: 1},
    exit: {opacity: 0},
  },
  content: {
    hidden: {scale: 0.9, opacity: 0},
    visible: {
      scale: 1,
      opacity: 1,
      transition: {
        type: 'spring',
        damping: 25,
        stiffness: 300,
      },
    },
    exit: {
      scale: 0.9,
      opacity: 0,
    },
  },
}

/**
 * 阶段切换动画配置
 * @description 用于阶段之间的切换过渡
 * @type {Object}
 */
export const phaseTransitionConfig = {
  initial: {opacity: 0, y: 8},
  animate: {opacity: 1, y: 0},
  exit: {opacity: 0, y: -8},
  transition: {duration: 0.25, ease: 'easeOut'},
}

/**
 * 动画持续时间常量
 * @readonly
 * @enum {number}
 */
export const ANIMATION_DURATION = {
  /** 快速动画 */
  fast: 0.2,
  /** 普通动画 */
  normal: 0.3,
  /** 慢速动画 */
  slow: 0.5,
  /** 弹窗动画 */
  modal: 0.6,
}

/**
 * 动画缓动函数
 * @readonly
 * @type {Object}
 */
export const EASING = {
  /** 平滑缓动 */
  smooth: [0.25, 0.1, 0.25, 1],
  /** 弹性缓动 */
  spring: {type: 'spring', damping: 25, stiffness: 300},
  /** 线性 */
  linear: 'linear',
}

export default {
  containerVariants,
  itemVariants,
  scaleInVariants,
  slideVariants,
  fadeVariants,
  modalVariants,
  phaseTransitionConfig,
  ANIMATION_DURATION,
  EASING,
}
