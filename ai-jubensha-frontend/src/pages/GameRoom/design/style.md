# GameRoom UI 设计规范

## 概述

GameRoom 页面采用 **现代扁平化 + 科技简约 + 二次元萌系** 风格。整体视觉舒适且富有活力，兼顾黑白主题。

---

## 色彩系统

### 主色调（低饱和度冷色调）

| 用途   | 浅色主题       | 深色主题         |
|------|------------|--------------|
| 背景主色 | `#F5F7FA`  | `#1A1D26`    |
| 背景次色 | `#EEF1F6`  | `#222631`    |
| 背景三级 | `#E4E8EE`  | `#2A2F3C`    |
| 玻璃态  | `white/80` | `#222631/90` |

### 边框色

| 状态    | 浅色主题      | 深色主题      |
|-------|-----------|-----------|
| 默认    | `#E0E5EE` | `#363D4D` |
| Hover | `#C8D0DD` | `#4A5568` |
| 强调    | `#8B9DC8` | `#5E6B8A` |

### 文字色

| 级别 | 浅色主题      | 深色主题      |
|----|-----------|-----------|
| 主要 | `#2D3748` | `#E8ECF2` |
| 次要 | `#5A6978` | `#9CA8B8` |
| 弱化 | `#8C96A5` | `#6B7788` |
| 强调 | `#7C8CD6` | `#A5B4EC` |

### 强调色（点缀色）

| 用途       | 色值        |
|----------|-----------|
| 主强调（淡紫）  | `#7C8CD6` |
| 次强调（淡紫蓝） | `#A78BFA` |
| 萌系点缀（淡粉） | `#F5A9C9` |
| 成功（淡绿）   | `#5DD9A8` |
| 警告（淡黄）   | `#FCD34D` |
| 错误（淡红）   | `#F87171` |

---

## 按钮样式

### GhostButton（幽灵按钮）

用于 Header 和 Footer 中的操作按钮。

```jsx
import GhostButton from './components/GhostButton'

<
GhostButton
onClick = {handleClick} >
    < span
className = "flex items-center gap-1.5" >
    < Icon
className = "w-3.5 h-3.5" / >
    < span > 文字 < /span>
  </span>
</GhostButton>
```

**样式规范：**

| 状态    | 样式                                                     |
|-------|--------------------------------------------------------|
| 默认    | 无边框，只有文字，颜色 `#5A6978` / `#9CA8B8`                      |
| Hover | 淡蓝色阴影 `shadow-[0_0_12px_rgba(124,140,214,0.3)]`，文字变强调色 |
| 点击    | 缩小 `scale: 0.98`                                       |
| 禁用    | `opacity: 50`, `cursor-not-allowed`                    |

---

## 组件布局

### Header（顶部导航）

**布局结构：**

```
┌─────────────────────────────────────────────────────────────┐
│  Logo + 品牌   |   阶段指示器   |   连接状态   |  设置  退出 │
└─────────────────────────────────────────────────────────────┘
```

**高度：** `py-2`（紧凑型）

**左侧：**

- 品牌徽章（六边形图标 + 星星点缀）
- 阶段指示器（当前阶段 + 阶段序列，仅大屏幕显示）

**右侧：**

- 连接状态（圆点 + 文字）
- 设置按钮（GhostButton）
- 退出按钮（GhostButton）

### Footer（底部状态栏）

**布局结构：**

```
┌─────────────────────────────────────────────────────────────┐
│            当前阶段名称                      连接状态      │
└─────────────────────────────────────────────────────────────┘
```

**高度：** `py-2`（紧凑型）

**左侧：**

- 阶段图标 + 当前阶段名称

**右侧：**

- WebSocket 连接状态图标 + 文字 + 状态点动画

---

## 阶段指示器

### 显示逻辑

| 屏幕宽度    | 显示内容                      |
|---------|---------------------------|
| `< lg`  | 仅显示当前阶段名称                 |
| `>= lg` | 当前阶段名称 + 所有阶段序列（纯文本，不可点击） |

### 阶段列表

```
剧本概览 → 角色分配 → 阅读剧本 → 线索搜证 → 推理讨论 → 真相揭晓
```

---

## 间距系统

| 用途    | Class         |
|-------|---------------|
| 区块间距  | `gap-4`       |
| 元素间距  | `gap-2`       |
| 按钮内边距 | `px-3 py-1.5` |
| 卡片内边距 | `px-4 py-2.5` |

---

## 圆角系统

| 尺寸 | Class        |
|----|--------------|
| 小  | `rounded-md` |
| 中  | `rounded-lg` |
| 大  | `rounded-xl` |

---

## 动画效果

### 按钮 Hover

```javascript
whileHover = {
{
    scale: 1.02,
        boxShadow
:
    'shadow-[0_0_12px_rgba(124,140,214,0.3)]',
}
}
whileTap = {
{
    scale: 0.98
}
}
```

### 状态点脉冲

```javascript
animate = {
{
    scale: [1, 2.5],
        opacity
:
    [0.6, 0]
}
}
transition = {
{
    duration: 1.5,
        repeat
:
    Infinity,
        ease
:
    "easeOut"
}
}
```

### 星星旋转

```javascript
animate = {
{
    rotate: [0, 15, -15, 0],
        scale
:
    [1, 1.1, 1]
}
}
transition = {
{
    duration: 2,
        repeat
:
    Infinity,
        ease
:
    "easeInOut"
}
}
```

---

## 主题检测

使用 `prefers-color-scheme` 媒体查询自动检测系统主题：

```javascript
const useTheme = () => {
    const [isDark, setIsDark] = React.useState(() => {
        if (typeof window !== 'undefined') {
            return window.matchMedia('(prefers-color-scheme: dark)').matches
        }
        return false
    })

    React.useEffect(() => {
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
        const handler = (e) => setIsDark(e.matches)
        mediaQuery.addEventListener('change', handler)
        return () => mediaQuery.removeEventListener('change', handler)
    }, [])

    return isDark
}
```

---

## 文件结构

```
src/pages/GameRoom/
├── components/
│   ├── GameRoomHeader.jsx    # 顶部导航栏
│   ├── GameRoomFooter.jsx    # 底部状态栏
│   └── GameRoomButton.jsx    # 幽灵按钮组件
├── design/
│   └── style.md              # 本设计规范文档
├── phases/                   # 游戏阶段组件
├── hooks/                    # 自定义 Hooks
└── ...
```

---

## 设计原则

1. **简洁优先**：去除不必要的装饰元素
2. **一致性**：所有按钮使用统一的 GhostButton 样式
3. **极简阶段指示**：仅显示必要信息，不作为交互元素
4. **视觉舒适**：低饱和度配色 + 少量亮色点缀
5. **黑白主题**：自动适配系统明暗主题
