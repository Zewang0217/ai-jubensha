/**
 * @fileoverview 动画配置
 * @description 游戏房间动画变体配置
 */

/**
 * 容器动画变体
 * 用于包裹多个子元素的容器，提供渐显和交错动画效果
 */
export const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
      delayChildren: 0.2,
    },
  },
}

/**
 * 子元素动画变体
 * 用于容器内的子元素，提供从下方滑入的动画效果
 */
export const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.5,
      ease: [0.25, 0.1, 0.25, 1],
    },
  },
}

/**
 * 缩放进入动画变体
 * 用于需要缩放效果的元素
 */
export const scaleInVariants = {
  hidden: { opacity: 0, scale: 0.8 },
  visible: {
    opacity: 1,
    scale: 1,
    transition: {
      duration: 0.4,
      ease: [0.25, 0.1, 0.25, 1],
    },
  },
}

/**
 * 淡入动画变体
 * 用于简单的淡入效果
 */
export const fadeInVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      duration: 0.3,
    },
  },
}

/**
 * 滑入动画变体
 * 用于从左侧或右侧滑入的元素
 */
export const slideInVariants = {
  hidden: { opacity: 0, x: -30 },
  visible: {
    opacity: 1,
    x: 0,
    transition: {
      duration: 0.4,
      ease: [0.25, 0.1, 0.25, 1],
    },
  },
}

/**
 * 弹跳动画变体
 * 用于需要弹跳效果的元素
 */
export const bounceVariants = {
  hidden: { opacity: 0, scale: 0.5 },
  visible: {
    opacity: 1,
    scale: 1,
    transition: {
      type: 'spring',
      damping: 15,
      stiffness: 300,
    },
  },
}

export default {
  containerVariants,
  itemVariants,
  scaleInVariants,
  fadeInVariants,
  slideInVariants,
  bounceVariants,
}
