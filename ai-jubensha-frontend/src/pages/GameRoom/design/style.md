# GameRoom Film Noir 风格指南

> **版本**: v1.0  
> **日期**: 2026-02-23  
> **适用**: AI-ScriptKill GameRoom 模块及子组件

---

## 1. 设计概述

### 1.1 设计理念

**Film Noir（黑色电影）复古侦探风格**

营造 1940 年代私人侦探事务所的沉浸式氛围：

- **神秘**: 深邃的阴影与戏剧性光影
- **复古**: 装饰艺术时期的视觉语言
- **档案感**: 文件夹、证据袋、案件编号等元素
- **沉浸**: 烟雾、扫描线、噪点纹理

### 1.2 设计关键词

```
神秘 · 复古 · 悬疑 · 档案 · 侦探 · 光影 · 颗粒感 · 装饰艺术
```

---

## 2. 色彩系统

### 2.1 主色调

```css
/* 背景色系 - 深邃石色 */
--bg-primary: #0c0a09

; /* stone-950 - 最深背景 */
--bg-secondary: #1c1917

; /* stone-900 - 卡片背景 */
--bg-tertiary: #292524

; /* stone-800 - 悬浮背景 */

/* 中性色 - 冷灰调 */
--text-primary: #f5f5f4

; /* stone-100 - 主要文字 */
--text-secondary: #a8a29e

; /* stone-400 - 次要文字 */
--text-muted: #57534e

; /* stone-600 - 禁用/提示文字 */

/* 强调色 - 琥珀金 */
--accent-primary: #f59e0b

; /* amber-500 - 主要强调 */
--accent-secondary: #d97706

; /* amber-600 - 次要强调 */
--accent-muted: #78350f

; /* amber-900 - 暗色强调 */
--accent-glow:

rgba
(
245
,
158
,
11
,
0.3
)
; /* 发光效果 */

/* 功能色 */
--success: #10b981

; /* emerald-500 - 成功/在线 */
--warning: #f59e0b

; /* amber-500 - 警告 */
--error: #ef4444

; /* red-500 - 错误 */
--info: #3b82f6

; /* blue-500 - 信息 */
```

### 2.2 色彩使用规范

| 场景    | 颜色     | Tailwind 类           |
|-------|--------|----------------------|
| 页面背景  | 深石色    | `bg-stone-950`       |
| 卡片/面板 | 石色 900 | `bg-stone-900/80`    |
| 主要文字  | 石色 100 | `text-stone-100`     |
| 次要文字  | 石色 400 | `text-stone-400`     |
| 强调元素  | 琥珀 500 | `text-amber-500`     |
| 边框    | 石色 700 | `border-stone-700`   |
| 悬停状态  | 石色 800 | `hover:bg-stone-800` |

---

## 3. 字体规范

### 3.1 字体家族

```css
/* 标题字体 - 衬线体营造复古感 */
--font-display: ui-serif, Georgia,

'Times New Roman'
,
serif

;

/* 正文字体 */
--font-body: ui-sans-serif, system-ui, sans-serif

;

/* 等宽字体 - 用于编号、代码 */
--font-mono: ui-monospace,

'Courier New'
,
monospace

;
```

### 3.2 字体层级

| 层级      | 大小                      | 字重                          | 字体           | 用途    |
|---------|-------------------------|-----------------------------|--------------|-------|
| Display | `text-2xl` ~ `text-3xl` | `font-bold`                 | `font-serif` | 页面主标题 |
| Heading | `text-xl` ~ `text-2xl`  | `font-semibold`             | `font-serif` | 区块标题  |
| Title   | `text-lg`               | `font-medium`               | `font-serif` | 卡片标题  |
| Body    | `text-sm` ~ `text-base` | `font-normal`               | `font-sans`  | 正文内容  |
| Caption | `text-xs`               | `font-normal`               | `font-sans`  | 辅助说明  |
| Label   | `text-xs`               | `uppercase tracking-widest` | `font-sans`  | 标签文字  |

### 3.3 字体样式规范

```jsx
// 装饰性标签
<span className="text-xs uppercase tracking-widest text-stone-500">
  CRIME SCENES
</span>

// 复古标题
<h2 className="text-2xl font-serif text-amber-100">
    Crime Scene Investigation
</h2>

// 档案编号
<span className="font-mono text-amber-500">
  #0001
</span>
```

---

## 4. 组件规范

### 4.1 档案卡片 (Case File Card)

```jsx
// 标准档案卡片
<div className="bg-stone-900/80 border-2 border-stone-700 shadow-2xl shadow-black/50">
    {/* 文件夹标签 */}
    <div className="absolute -top-px left-8 -translate-y-full px-4 py-1 
                  bg-amber-900/40 border border-amber-700/50 border-b-0 
                  text-amber-200 text-xs font-serif">
        CASE FILE
    </div>

    {/* 内容区域 */}
    <div className="p-4 sm:p-6">
        {/* 内容 */}
    </div>

    {/* 角落装饰 */}
    <div className="absolute top-4 right-4 w-8 h-8 border-t-2 border-r-2 border-stone-600/30"/>
    <div className="absolute bottom-4 left-4 w-8 h-8 border-b-2 border-l-2 border-stone-600/30"/>
</div>
```

### 4.2 证据袋 (Evidence Bag)

```jsx
// 未揭示状态
<button className="w-full aspect-square border-2 border-dashed border-stone-600 
                   hover:border-amber-600/50 bg-stone-800/20 hover:bg-stone-800/40 
                   transition-all flex flex-col items-center justify-center">
    <div className="w-12 h-12 rounded-full border-2 border-stone-600 
                  group-hover:border-amber-600/50 flex items-center justify-center">
        <span className="text-stone-500 group-hover:text-amber-500/70 text-xl">?</span>
    </div>
    <span className="text-stone-600 group-hover:text-stone-400 text-xs uppercase tracking-widest">
    Unexamined
  </span>
</button>

// 已揭示状态
<div className="w-full aspect-square border border-amber-700/30 bg-amber-950/10 p-3">
    {/* 线索内容 */}
</div>
```

### 4.3 档案标签 (File Tab)

```jsx
// 激活状态
<button className="px-4 py-2 bg-amber-900/30 border-t-2 border-amber-600 
                   text-amber-200 text-sm font-serif transition-colors">
    剧本概览
</button>

// 未激活状态
<button className="px-4 py-2 border-t-2 border-transparent text-stone-500 
                   hover:text-stone-300 hover:bg-stone-800/30 text-sm font-serif 
                   transition-colors">
    角色分配
</button>
```

### 4.4 印章标签 (Stamp Label)

```jsx
// 机密印章
<div className="absolute -top-3 -right-3 px-3 py-1 bg-red-900/30 
                border border-red-700/50 text-red-400 text-xs font-bold 
                transform rotate-12 uppercase">
    Confidential
</div>

// 状态标签
<span className="px-2 py-0.5 bg-amber-900/50 text-amber-500 text-xs 
                 border border-amber-700/30 uppercase tracking-wider">
  {clueCount} ITEMS
</span>
```

### 4.5 按钮样式

```jsx
// 主要按钮
<button className="px-4 py-2 bg-amber-900/30 hover:bg-amber-900/50 
                   border border-amber-700/50 text-amber-200 
                   transition-colors font-serif">
    Continue Investigation
</button>

// 次要按钮
<button className="px-4 py-2 bg-stone-800 hover:bg-stone-700 
                   border border-stone-700 text-stone-300 
                   transition-colors text-sm">
    Close
</button>

// 危险按钮
<button className="px-4 py-2 bg-red-900/20 hover:bg-red-900/40 
                   border border-red-800/50 text-red-400 
                   transition-colors">
    Delete Evidence
</button>
```

---

## 5. 布局规范

### 5.1 页面结构

```
┌─────────────────────────────────────────┐
│  Header (侦探事务所招牌 + 档案标签)       │  ← flex-none
├─────────────────────────────────────────┤
│                                         │
│  Main Content (档案文件夹)               │  ← flex-1 min-h-0
│  ┌─────────────────────────────────┐    │
│  │  [CASE FILE]                    │    │  ← 标签突出
│  │                                 │    │
│  │     阶段内容区域                 │    │
│  │                                 │    │
│  └─────────────────────────────────┘    │
│                                         │
├─────────────────────────────────────────┤
│  Footer (事务所状态栏)                   │  ← flex-none
└─────────────────────────────────────────┘
```

### 5.2 间距系统

| 尺寸 | 值               | 用途    |
|----|-----------------|-------|
| xs | `p-2` / `gap-2` | 紧凑元素  |
| sm | `p-3` / `gap-3` | 标准内边距 |
| md | `p-4` / `gap-4` | 卡片内边距 |
| lg | `p-6` / `gap-6` | 区块间距  |
| xl | `p-8` / `gap-8` | 页面边距  |

### 5.3 响应式断点

```jsx
// 移动优先
sm: '640px'   // 小屏
md: '768px'   // 平板
lg: '1024px'  // 桌面
xl: '1280px'  // 大屏

// 使用示例
< div
className = "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4" >
```

---

## 6. 动画规范

### 6.1 过渡时长

```css
--duration-fast:

150
ms

; /* 悬停、快速反馈 */
--duration-normal:

200
ms

; /* 标准过渡 */
--duration-slow:

300
ms

; /* 阶段切换 */
--duration-dramatic:

500
ms

; /* 重要揭示 */
```

### 6.2 缓动函数

```css
--ease-default:

cubic-bezier
(
0.25
,
0.1
,
0.25
,
1
)
;
--ease-out:

cubic-bezier
(
0
,
0
,
0.2
,
1
)
;
--ease-in-out:

cubic-bezier
(
0.4
,
0
,
0.2
,
1
)
;
--ease-bounce:

cubic-bezier
(
0.68
,
-
0.55
,
0.265
,
1.55
)
;
```

### 6.3 动画模式

```jsx
// 1. 阶段切换动画
<motion.div
    initial={{opacity: 0, x: 20}}
    animate={{opacity: 1, x: 0}}
    exit={{opacity: 0, x: -20}}
    transition={{duration: 0.2, ease: [0.25, 0.1, 0.25, 1]}}
>

    // 2. 打字机效果
    const typingVariants = {
    hidden: {width: 0},
    visible: {
    width: 'auto',
    transition: {
    duration: 0.8,
    ease: 'linear',
},
},
}

    // 3. 揭示动画 (证据袋)
    <motion.div
        initial={{opacity: 0, scale: 0.95}}
        animate={{opacity: 1, scale: 1}}
        transition={{delay: index * 0.1}}
    >

        // 4. 脉冲发光 (信号指示)
        <div className="animate-pulse bg-amber-500/30"/>

        // 5. 打字机光标闪烁
        <span className="animate-pulse">▋</span>
```

---

## 7. 背景效果

### 7.1 基础背景

```jsx
// 深色渐变背景
<div className="bg-gradient-to-b from-stone-950 via-stone-900 to-stone-950">

    // 档案柜纹理背景
    <div
        className="bg-stone-950 bg-[linear-gradient(rgba(30,27,24,0.5)_1px,transparent_1px),linear-gradient(90deg,rgba(30,27,24,0.5)_1px,transparent_1px)] bg-[size:40px_40px]">
```

### 7.2 氛围效果

```jsx
// 烟雾效果
<div className="fixed inset-0 pointer-events-none">
    <div className="absolute top-0 right-0 w-96 h-96 bg-purple-900/20 rounded-full blur-3xl"/>
    <div className="absolute bottom-0 left-0 w-96 h-96 bg-amber-900/10 rounded-full blur-3xl"/>
</div>

// 扫描线效果
<div
    className="pointer-events-none absolute inset-0 bg-[repeating-linear-gradient(0deg,transparent,transparent_2px,rgba(0,0,0,0.03)_2px,rgba(0,0,0,0.03)_4px)]"/>

// 噪点纹理
<div className="pointer-events-none fixed inset-0 opacity-[0.03] mix-blend-overlay"
     style={{backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E")`}}/>

// 百叶窗投影
<div
    className="pointer-events-none absolute inset-0 bg-[repeating-linear-gradient(90deg,transparent,transparent_40px,rgba(0,0,0,0.1)_40px,rgba(0,0,0,0.1)_60px)]"/>
```

---

## 8. 图标规范

### 8.1 图标库

使用 `lucide-react` 图标库，选择风格硬朗的图标：

```jsx
import {
    FileText, Search, Users, MessageSquare, CheckCircle,
    Lock, Unlock, Eye, EyeOff, ChevronRight, ChevronLeft,
    Radio, Bug, LogOut, FolderOpen, Fingerprint
} from 'lucide-react'
```

### 8.2 图标尺寸

| 用途   | 尺寸    | 类名            |
|------|-------|---------------|
| 内联文字 | 14px  | `w-3.5 h-3.5` |
| 按钮图标 | 16px  | `w-4 h-4`     |
| 标准图标 | 20px  | `w-5 h-5`     |
| 大图标  | 24px  | `w-6 h-6`     |
| 装饰图标 | 32px+ | `w-8 h-8`     |

### 8.3 图标颜色

```jsx
// 默认图标
<Icon className="w-4 h-4 text-stone-500"/>

// 强调图标
<Icon className="w-4 h-4 text-amber-500"/>

// 禁用图标
<Icon className="w-4 h-4 text-stone-600"/>
```

---

## 9. 命名约定

### 9.1 组件命名

```jsx
// 阶段组件
ScriptOverview.jsx      // 剧本概览
CharacterAssignment.jsx // 角色分配
ScriptReading.jsx       // 剧本阅读
Investigation.jsx       // 搜证阶段
Discussion.jsx          // 讨论阶段
Summary.jsx             // 总结阶段

// 子组件
EvidenceBag.jsx         // 证据袋
CrimeScenePhoto.jsx     // 现场照片
CaseFileTab.jsx         // 档案标签
RadioStatus.jsx         // 无线电状态
```

### 9.2 类名顺序

```jsx
// 推荐顺序: 布局 > 尺寸 > 颜色 > 交互 > 响应式
<button className="
  /* 布局 */
  flex items-center justify-center gap-2
  /* 尺寸 */
  w-full px-4 py-2
  /* 颜色 */
  bg-stone-800 text-stone-300
  /* 边框 */
  border border-stone-700
  /* 交互 */
  hover:bg-stone-700 transition-colors
  /* 响应式 */
  sm:w-auto
">
```

### 9.3 自定义属性

```jsx
// 阶段类型常量
PHASE_TYPE = {
    SCRIPT_OVERVIEW: 'script_overview',
    CHARACTER_ASSIGNMENT: 'character_assignment',
    SCRIPT_READING: 'script_reading',
    INVESTIGATION: 'investigation',
    DISCUSSION: 'discussion',
    SUMMARY: 'summary',
}

// 组件静态属性
Component.phaseType = PHASE_TYPE.INVESTIGATION
Component.displayName = 'Investigation'
```

---

## 10. 最佳实践

### 10.1 性能优化

```jsx
// 1. 使用 React.memo 包裹组件
export default memo(Component)

// 2. 使用 useMemo 缓存计算值
const computedValue = useMemo(() => expensiveOperation(data), [data])

// 3. 使用 useCallback 缓存回调
const handleClick = useCallback(() => { ...
}, [deps])

// 4. 延迟加载阶段组件
const PhaseComponents = {
    [PHASE_TYPE.INVESTIGATION]: React.lazy(() => import('./phases/Investigation')),
}

// 5. 动画性能优化
<motion.div
    initial={false}  // 禁用初始动画
    style={{willChange: 'transform, opacity'}}  // GPU 加速
>
```

### 10.2 可访问性

```jsx
// 按钮需有明确的 aria-label
<button aria-label="搜索线索" onClick={onSearch}>
    <Search className="w-4 h-4"/>
</button>

// 进度指示
<div role="progressbar" aria-valuenow={progress} aria-valuemax={100}>

    // 焦点状态
    <button className="focus:outline-none focus:ring-2 focus:ring-amber-500/50">
```

### 10.3 代码组织

```
src/pages/GameRoom/
├── phases/                    # 阶段组件
│   ├── ScriptOverview.jsx
│   ├── CharacterAssignment.jsx
│   ├── Investigation.jsx
│   ├── Discussion.jsx
│   ├── Summary.jsx
│   └── index.js              # 统一导出
├── hooks/                     # 自定义 Hooks
│   ├── usePhaseManager.js
│   ├── useDebugMode.js
│   └── index.js
├── types/                     # 类型定义
│   └── index.js
├── services/                  # 服务层
│   └── mockData.js
├── design/                    # 设计文档
│   └── style.md              # 本文件
└── GameRoom.jsx              # 主组件
```

---

## 11. 常见模式

### 11.1 档案文件夹模式

```jsx
<div className="relative bg-stone-900/80 border-2 border-stone-700">
    {/* 标签 */}
    <div className="absolute -top-px left-8 -translate-y-full ...">
        LABEL
    </div>

    {/* 内容 */}
    <div className="p-4 sm:p-6">
        {/* ... */}
    </div>

    {/* 角落装饰 */}
    <div className="absolute top-4 right-4 w-8 h-8 border-t-2 border-r-2 border-stone-600/30"/>
    <div className="absolute bottom-4 left-4 w-8 h-8 border-b-2 border-l-2 border-stone-600/30"/>
</div>
```

### 11.2 证据收集模式

```jsx
// 进度指示
<div className="flex items-center gap-2">
  <span className="text-stone-500 text-xs">
    {revealedCount} OF {totalCount} EVIDENCE COLLECTED
  </span>
    <div className="w-32 h-2 bg-stone-800 rounded-full overflow-hidden">
        <motion.div
            className="h-full bg-gradient-to-r from-amber-700 to-amber-500"
            animate={{width: `${progress}%`}}
        />
    </div>
</div>
```

### 11.3 场景选择模式

```jsx
<button className={`
  relative w-full aspect-[4/3] border-2 overflow-hidden
  ${isSelected
    ? 'border-amber-600 ring-2 ring-amber-600/20'
    : 'border-stone-600 hover:border-stone-500'
}
`}>
    {/* 渐变背景 */}
    <div className={`
    absolute inset-0
    ${isSelected
        ? 'bg-gradient-to-br from-amber-950/50 to-stone-900'
        : 'bg-gradient-to-br from-stone-800/50 to-stone-900'
    }
  `}/>

    {/* 状态标签 */}
    <span className={`
    text-xs font-mono
    ${isSelected ? 'text-amber-500' : 'text-stone-500'}
  `}>
    {isSelected ? '● ACTIVE' : '○ AVAILABLE'}
  </span>
</button>
```

---

## 12. 调试工具

### 12.1 调试面板样式

```jsx
// 侦探笔记风格调试面板
<div className="fixed bottom-4 right-4 z-50 w-80 
                bg-stone-800/95 backdrop-blur-xl 
                border border-stone-700/50 rounded-lg shadow-2xl">
    {/* 抽屉把手 */}
    <div className="absolute -top-3 left-1/2 -translate-x-1/2 
                  w-16 h-1.5 bg-stone-700 rounded-full"/>

    {/* 内容 */}
    <div className="p-4 space-y-4">
        {/* 开关 */}
        <div className="flex items-center justify-between">
            <span className="text-sm text-stone-400">Simulation Mode</span>
            <button className="relative w-12 h-6 bg-stone-700 ...">
                <motion.div animate={{x: isOn ? 24 : 2}}/>
            </button>
        </div>
    </div>

    {/* 档案标签 */}
    <div className="px-4 py-2 bg-amber-900/20 border-t border-stone-700/50">
    <span className="text-xs text-amber-500/70 uppercase tracking-wider">
      Confidential
    </span>
    </div>
</div>
```

---

## 13. 参考资源

### 13.1 设计灵感

- **Film Noir**: 双重赔偿 (Double Indemnity, 1944), 马耳他之鹰 (The Maltese Falcon, 1941)
- **装饰艺术**: Art Deco 建筑风格、几何图案、对称布局
- **配色参考**: 柯达黑白胶片色调、深棕色调色板

### 13.2 技术参考

- **Tailwind CSS**: https://tailwindcss.com/docs
- **Framer Motion**: https://www.framer.com/motion/
- **Lucide Icons**: https://lucide.dev/icons/

---

## 14. 更新日志

| 版本   | 日期         | 更新内容                  |
|------|------------|-----------------------|
| v1.0 | 2026-02-23 | 初始版本，Film Noir 风格系统建立 |

---

> **注意**: 本文档是 GameRoom 模块的设计规范，所有新组件开发必须遵循此指南以保持视觉一致性。

