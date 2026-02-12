# AI-ScriptKill 设计规范文档

## 概述

本文档定义了 AI-ScriptKill 剧本杀平台的前端设计规范，确保所有 Agent 在进行 UI 开发时保持风格一致性。

**设计哲学**：现代化、沉浸式的蓝白色系主题，融合玻璃态效果与流畅动画，营造悬疑推理的氛围感。

---

## 1. 色彩系统

### 1.1 主色调 - 蓝色系

| Token                 | 色值        | 用途               |
|-----------------------|-----------|------------------|
| `--color-primary-50`  | `#eff6ff` | 最浅背景、悬停状态        |
| `--color-primary-100` | `#dbeafe` | 浅背景、徽章、标签        |
| `--color-primary-200` | `#bfdbfe` | 边框、分隔线           |
| `--color-primary-300` | `#93c5fd` | 装饰元素、悬停边框        |
| `--color-primary-400` | `#60a5fa` | 次要按钮、图标          |
| `--color-primary-500` | `#3b82f6` | **主品牌色**、主要按钮、链接 |
| `--color-primary-600` | `#2563eb` | 按钮悬停、强调文字        |
| `--color-primary-700` | `#1d4ed8` | 深色按钮、渐变终点        |
| `--color-primary-800` | `#1e40af` | 深色背景、Footer      |
| `--color-primary-900` | `#1e3a8a` | 最深色、文字强调         |

### 1.2 辅助色 - slate 灰色系

| Token                   | 色值        | 用途                |
|-------------------------|-----------|-------------------|
| `--color-secondary-50`  | `#f8fafc` | 页面背景              |
| `--color-secondary-100` | `#f1f5f9` | 卡片背景              |
| `--color-secondary-200` | `#e2e8f0` | 边框、分隔线            |
| `--color-secondary-300` | `#cbd5e1` | 禁用状态、次要边框         |
| `--color-secondary-400` | `#94a3b8` | 占位符文字             |
| `--color-secondary-500` | `#64748b` | 次要文字              |
| `--color-secondary-600` | `#475569` | 正文文字              |
| `--color-secondary-700` | `#334155` | 标题文字              |
| `--color-secondary-800` | `#1e293b` | 深色标题              |
| `--color-secondary-900` | `#0f172a` | **最深色**、Footer 背景 |

### 1.3 语义化颜色

| 用途 | 色值        | Tailwind 类         |
|----|-----------|--------------------|
| 成功 | `#10b981` | `text-emerald-500` |
| 警告 | `#f59e0b` | `text-amber-500`   |
| 错误 | `#ef4444` | `text-red-500`     |
| 信息 | `#3b82f6` | `text-blue-500`    |

### 1.4 渐变色使用规范

**主按钮渐变**：

```css
background:

linear-gradient
(
135
deg, #3b82f6, #1d4ed8

)
;
```

**Hero 标题渐变**：

```css
background:

linear-gradient
(
to right, #2563eb, #3b82f6, #1d4ed8

)
;
-webkit-background-clip: text

;
-webkit-text-fill-color: transparent

;
```

**CTA 区域背景**：

```css
background:

linear-gradient
(
to bottom right, #2563eb, #1e40af

)
;
```

---

## 2. 排版系统

### 2.1 字体家族

```css
font-family:

'Inter'
,
system-ui, -apple-system, BlinkMacSystemFont,

'Segoe UI'
,
Roboto, sans-serif

;
```

### 2.2 字体大小规范

| 级别      | 大小                | 字重             | 行高  | 字间距     | 用途    |
|---------|-------------------|----------------|-----|---------|-------|
| Hero 标题 | 5xl-8xl (48-96px) | 700 (bold)     | 1.2 | -0.02em | 首页大标题 |
| H1      | 4xl-5xl (36-48px) | 700            | 1.2 | -0.02em | 页面主标题 |
| H2      | 3xl-4xl (30-36px) | 700            | 1.2 | -0.02em | 区块标题  |
| H3      | xl-2xl (20-24px)  | 600 (semibold) | 1.3 | -0.01em | 卡片标题  |
| H4      | lg-xl (18-20px)   | 600            | 1.4 | 0       | 小标题   |
| 正文大     | lg (18px)         | 400            | 1.6 | 0       | 重要描述  |
| 正文      | base (16px)       | 400            | 1.6 | 0       | 普通文字  |
| 正文小     | sm (14px)         | 400            | 1.5 | 0       | 辅助文字  |
| 标签      | xs (12px)         | 500            | 1.4 | 0       | 标签、徽章 |

### 2.3 文字颜色规范

- **深色背景上的主文字**：`text-white`
- **深色背景上的次要文字**：`text-blue-100`
- **浅色背景上的主文字**：`text-slate-800`
- **浅色背景上的次要文字**：`text-slate-600`
- **描述文字**：`text-slate-500`

---

## 3. 间距系统

### 3.1 区块间距

| 场景      | 上下内边距        | Tailwind 类           |
|---------|--------------|----------------------|
| Hero 区块 | min-h-screen | `min-h-screen pt-20` |
| 标准区块    | 128px (8rem) | `py-32`              |
| 紧凑区块    | 64px (4rem)  | `py-16`              |
| 内容间距    | 32px (2rem)  | `gap-8`              |

### 3.2 容器宽度

```css
max-w-7xl:

1280
px /* 主内容区 */
max-w-5xl:

1024
px /* Hero 内容 */
max-w-4xl:

896
px /* CTA 区域 */
max-w-2xl:

672
px /* 文字内容 */
max-w-lg:

512
px

/* 小卡片 */
```

### 3.3 页面边距

```css
px-4:

16
px /* 移动端 */
px-6:

24
px /* 平板 */
px-8:

32
px

/* 桌面端 */
```

---

## 4. 组件规范

### 4.1 按钮

**主按钮 (Primary)**：

```jsx
<Link
    className="inline-flex items-center px-8 py-4 bg-gradient-to-r from-blue-500 to-blue-700 text-white text-lg font-semibold rounded-2xl shadow-xl shadow-blue-500/30 hover:shadow-2xl hover:shadow-blue-500/40 transition-all duration-300">
    按钮文字
    <svg className="w-5 h-5 ml-2">...</svg>
</Link>
```

**次要按钮 (Secondary)**：

```jsx
<Link
    className="inline-flex items-center px-8 py-4 bg-white/80 backdrop-blur-sm text-slate-700 text-lg font-semibold rounded-2xl border border-slate-200 hover:border-blue-300 hover:bg-white hover:text-blue-600 shadow-lg shadow-slate-200/50 transition-all duration-300">
    <svg className="w-5 h-5 mr-2">...</svg>
    按钮文字
</Link>
```

**CTA 白色按钮**：

```jsx
<Link
    className="inline-flex items-center px-10 py-5 bg-white text-blue-600 text-lg font-bold rounded-2xl shadow-2xl shadow-blue-900/30 hover:shadow-blue-900/50 transition-all duration-300">
    按钮文字
</Link>
```

### 4.2 卡片

**FeatureCard 组件**：

- 玻璃态背景：`bg-gradient-to-br from-white/80 to-white/40 backdrop-blur-xl`
- 圆角：`rounded-2xl`
- 边框：`border border-white/60`
- 阴影：`shadow-lg shadow-blue-900/5`
- 悬停：`hover:shadow-xl hover:shadow-blue-900/10 hover:border-blue-200/80`
- 悬停位移：`hover:-translate-y-2`
- 内边距：`p-8`

**图标容器**：

```jsx
<div
    className="w-14 h-14 mb-6 rounded-xl bg-gradient-to-br from-blue-500 to-blue-700 flex items-center justify-center text-white shadow-lg shadow-blue-500/30">
    {/* Icon */}
</div>
```

### 4.3 导航栏 (Header)

- 固定定位：`fixed top-0 left-0 right-0 z-50`
- 初始状态：透明背景
- 滚动状态：`bg-white/90 backdrop-blur-xl shadow-lg shadow-blue-900/5 border-b border-blue-100/50`
- 高度：`h-16 md:h-20`
- Logo 尺寸：`w-10 h-10 rounded-xl`

### 4.4 页脚 (Footer)

- 背景：`bg-slate-900`
- 顶部边框：`bg-gradient-to-r from-transparent via-blue-500/50 to-transparent`
- 装饰光晕：蓝色渐变模糊圆形
- 链接颜色：`text-slate-400 hover:text-blue-400`

---

## 5. 动画规范

### 5.1 Framer Motion 动画模式

**入场动画**：

```jsx
initial = {
{
    opacity: 0, y
:
    30
}
}
whileInView = {
{
    opacity: 1, y
:
    0
}
}
viewport = {
{
    once: true, margin
:
    "-50px"
}
}
transition = {
{
    duration: 0.6, ease
:
    [0.25, 0.46, 0.45, 0.94]
}
}
```

**交错动画 (Stagger)**：

```jsx
transition = {
{
    duration: 0.6, delay
:
    index * 0.15
}
}
```

**悬停效果**：

```jsx
whileHover = {
{
    scale: 1.02, y
:
    -8
}
}
whileTap = {
{
    scale: 0.98
}
}
transition = {
{
    type: "spring", stiffness
:
    400, damping
:
    10
}
}
```

**导航栏入场**：

```jsx
initial = {
{
    y: -100
}
}
animate = {
{
    y: 0
}
}
transition = {
{
    duration: 0.6, ease
:
    [0.25, 0.46, 0.45, 0.94]
}
}
```

### 5.2 CSS 动画

**浮动动画**：

```css
@keyframes float {
    0%, 100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-10px);
    }
}

animation: float

6
s ease-in-out infinite

;
```

**脉冲光晕**：

```css
@keyframes pulse-glow {
    0%, 100% {
        box-shadow: 0 0 20px rgba(59, 130, 246, 0.3);
    }
    50% {
        box-shadow: 0 0 40px rgba(59, 130, 246, 0.5);
    }
}
```

**闪光效果**：

```css
@keyframes shimmer {
    0% {
        background-position: -200% 0;
    }
    100% {
        background-position: 200% 0;
    }
}
```

### 5.3 过渡时间

| 类型        | 时长        | 用途       |
|-----------|-----------|----------|
| Fast      | 150ms     | 微交互、颜色变化 |
| Base      | 300ms     | 悬停效果、按钮  |
| Slow      | 500ms     | 卡片悬停、大元素 |
| Animation | 600-800ms | 入场动画     |

---

## 6. 背景与装饰

### 6.1 渐变光晕

```jsx
{/* 蓝色渐变光晕 */
}
<div className="absolute -top-40 -left-20 w-96 h-96 bg-blue-400/20 rounded-full blur-3xl"/>
<div className="absolute bottom-20 -right-20 w-96 h-96 bg-blue-600/20 rounded-full blur-3xl"/>
```

### 6.2 网格背景

```css
background:

linear-gradient
(
rgba
(
59
,
130
,
246
,
0.03
)
1
px, transparent

1
px

)
,
linear-gradient
(
90
deg,

rgba
(
59
,
130
,
246
,
0.03
)
1
px, transparent

1
px

)
;
background-size:

60
px

60
px

;
```

### 6.3 浮动装饰元素

```jsx
<motion.div
    animate={{y: [0, -20, 0], rotate: [0, 5, 0]}}
    transition={{duration: 6, repeat: Infinity, ease: "easeInOut"}}
    className="absolute top-32 right-[15%] w-16 h-16 rounded-2xl bg-gradient-to-br from-blue-400/30 to-blue-600/30 backdrop-blur-sm border border-blue-300/30"
/>
```

---

## 7. 图标规范

### 7.1 图标尺寸

| 用途   | 尺寸   | Tailwind 类 |
|------|------|------------|
| 按钮图标 | 20px | `w-5 h-5`  |
| 卡片图标 | 28px | `w-7 h-7`  |
| 导航图标 | 24px | `w-6 h-6`  |
| Logo | 24px | `w-6 h-6`  |
| 社交图标 | 20px | `w-5 h-5`  |

### 7.2 图标风格

- 使用 Heroicons 风格（描边式）
- 线宽：`strokeWidth={1.5}` 或 `strokeWidth={2}`
- 颜色：继承父元素 `currentColor`

### 7.3 常用图标

**灯泡/创意**：

```jsx
<svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
          d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
</svg>
```

**用户组**：

```jsx
<svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
          d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
</svg>
```

**书本**：

```jsx
<svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
          d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
</svg>
```

**搜索/放大镜**：

```jsx
<svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
          d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM10 7v3m0 0v3m0-3h3m-3 0H7"/>
</svg>
```

---

## 8. 布局规范

### 8.1 页面结构

```
┌─────────────────────────────────────┐
│              Header                 │  fixed, h-20
├─────────────────────────────────────┤
│                                     │
│              Hero                   │  min-h-screen
│         (大标题 + CTA)               │
│                                     │
├─────────────────────────────────────┤
│                                     │
│            Features                 │  py-32
│      (4个 FeatureCard 网格)          │
│                                     │
├─────────────────────────────────────┤
│                                     │
│               CTA                   │  py-32, 蓝色背景
│        (行动号召区块)                │
│                                     │
├─────────────────────────────────────┤
│              Footer                 │  bg-slate-900
└─────────────────────────────────────┘
```

### 8.2 响应式断点

| 断点  | 宽度     | 列数   | 说明   |
|-----|--------|------|------|
| sm  | 640px  | 1-2列 | 手机横屏 |
| md  | 768px  | 2列   | 平板   |
| lg  | 1024px | 4列   | 小型桌面 |
| xl  | 1280px | 4列   | 标准桌面 |
| 2xl | 1536px | 4列   | 大屏   |

### 8.3 网格系统

**4 列 Feature 网格**：

```jsx
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
```

**3 列统计网格**：

```jsx
<div className="grid grid-cols-3 gap-8 max-w-lg mx-auto">
```

**Footer 链接网格**：

```jsx
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-12">
```

---

## 9. 特殊效果

### 9.1 玻璃态 (Glassmorphism)

```css
.glass {
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
}
```

### 9.2 渐变文字

```css
.gradient-text {
    background: linear-gradient(135deg, #2563eb, #3b82f6);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}
```

### 9.3 渐变边框

```css
.gradient-border {
    position: relative;
}

.gradient-border::before {
    content: '';
    position: absolute;
    inset: 0;
    padding: 1px;
    border-radius: inherit;
    background: linear-gradient(135deg, #60a5fa, #2563eb);
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask-composite: exclude;
}
```

---

## 10. 文件组织

### 10.1 组件目录结构

```
src/
├── components/
│   ├── common/           # 通用组件
│   │   ├── ErrorBoundary.jsx
│   │   └── Loading.jsx
│   ├── layout/           # 布局组件
│   │   ├── Header.jsx
│   │   ├── Footer.jsx
│   │   └── MainLayout.jsx
│   └── ui/               # UI 组件
│       └── FeatureCard.jsx
├── pages/                # 页面组件
│   └── Home/
│       └── Home.jsx
├── styles/
│   └── index.css         # 全局样式
```

### 10.2 样式文件组织

```css
/* index.css 结构 */
1
. Tailwind 导入

2
. CSS 变量定义

3
. 基础样式重置

4
. 排版样式

5
. 工具类

6
. 动画定义

7
. 组件基础样式

8
. 响应式调整
```

---

## 11. 最佳实践

### 11.1 性能优化

- 使用 `will-change` 谨慎，动画结束后移除
- 图片使用 `loading="lazy"`
- 组件使用 `React.memo` 优化重渲染
- 动画使用 CSS 优先，复杂交互使用 Framer Motion

### 11.2 可访问性

- 所有按钮和链接有明确的焦点样式
- 图片有 `alt` 属性
- 颜色对比度符合 WCAG 2.1 AA 标准
- 支持键盘导航

### 11.3 代码规范

- 使用 Tailwind 类名优先
- 复杂样式提取到 CSS 文件
- 组件使用默认导出
- Props 使用解构赋值

---

## 12. 示例代码

### 12.1 完整 Hero 区块

```jsx
<section className="relative min-h-screen flex items-center justify-center overflow-hidden pt-20">
    {/* 背景装饰 */}
    <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -left-20 w-96 h-96 bg-blue-400/20 rounded-full blur-3xl"/>
        <div className="absolute bottom-20 -right-20 w-96 h-96 bg-blue-600/20 rounded-full blur-3xl"/>
        <div
            className="absolute inset-0 bg-[linear-gradient(rgba(59,130,246,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(59,130,246,0.03)_1px,transparent_1px)] bg-[size:60px_60px]"/>
    </div>

    {/* 内容 */}
    <div className="relative z-10 max-w-5xl mx-auto px-4 text-center">
        <motion.h1
            initial={{opacity: 0, y: 30}}
            animate={{opacity: 1, y: 0}}
            className="text-5xl sm:text-6xl md:text-7xl font-bold tracking-tight mb-6"
        >
            <span className="bg-gradient-to-r from-blue-600 via-blue-500 to-blue-700 bg-clip-text text-transparent">
                剧本杀
            </span>
            <br/>
            <span className="text-slate-800">AI 智能推理</span>
        </motion.h1>

        <p className="text-lg text-slate-600 max-w-2xl mx-auto mb-10">
            深入迷雾，解锁真相。与AI共织悬疑，每一场都是智力与勇气的较量。
        </p>

        <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link className="btn-primary">
                开始推理
            </Link>
            <Link className="btn-secondary">
                剧本工坊
            </Link>
        </div>
    </div>
</section>
```

### 12.2 FeatureCard 使用

```jsx
import FeatureCard from '../../components/ui/FeatureCard'

const features = [
        {
            icon: <svg>...</svg>,
            title: 'AI 主持',
            description: '智能 AI 主持人引导游戏流程...'
        },
        // ...
    ]

    < div
className = "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6" >
    {
        features.map((feature, index) => (
            <FeatureCard
                key={feature.title}
                icon={feature.icon}
                title={feature.title}
                description={feature.description}
                index={index}
            />
        ))
    }
</div>
```

---

## 13. 更新日志

| 日期         | 版本    | 更新内容               |
|------------|-------|--------------------|
| 2026-02-12 | 1.0.0 | 初始版本，定义完整的蓝白色系设计规范 |

---

**注意**：本文档应与项目代码同步更新。任何设计变更都应在此文档中记录，以确保所有 Agent 保持风格一致性。
