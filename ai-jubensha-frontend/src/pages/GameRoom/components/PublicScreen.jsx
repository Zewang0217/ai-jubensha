/**
 * @fileoverview PublicScreen 组件 - AI Agent 操作公屏
 * @description 实时显示 AI Agent 的各种操作，包括搜证、公开线索、发言、投票等
 */

import React, {memo, useEffect, useRef} from 'react';
import {AnimatePresence, motion} from 'framer-motion';
import {Eye, EyeOff, MessageCircle, Search, Sparkles, Terminal, Vote} from 'lucide-react';

/**
 * AI Agent 动作类型
 */
const AGENT_ACTION_TYPE = {
    INVESTIGATE: 'INVESTIGATE',     // 搜证
    REVEAL_CLUE: 'REVEAL_CLUE',     // 公开线索
    HIDE_CLUE: 'HIDE_CLUE',         // 不公开线索
    SPEAK: 'SPEAK',                 // 发言
    VOTE: 'VOTE',                   // 投票
    SYSTEM: 'SYSTEM',               // 系统消息
};

/**
 * 动作类型配置
 */
const ACTION_CONFIG = {
    [AGENT_ACTION_TYPE.INVESTIGATE]: {
        icon: Search,
        color: 'text-blue-500',
        bgColor: 'bg-blue-500/10',
        borderColor: 'border-blue-500/20',
        label: '搜证',
    },
    [AGENT_ACTION_TYPE.REVEAL_CLUE]: {
        icon: Eye,
        color: 'text-green-500',
        bgColor: 'bg-green-500/10',
        borderColor: 'border-green-500/20',
        label: '公开线索',
    },
    [AGENT_ACTION_TYPE.HIDE_CLUE]: {
        icon: EyeOff,
        color: 'text-amber-500',
        bgColor: 'bg-amber-500/10',
        borderColor: 'border-amber-500/20',
        label: '隐藏线索',
    },
    [AGENT_ACTION_TYPE.SPEAK]: {
        icon: MessageCircle,
        color: 'text-purple-500',
        bgColor: 'bg-purple-500/10',
        borderColor: 'border-purple-500/20',
        label: '发言',
    },
    [AGENT_ACTION_TYPE.VOTE]: {
        icon: Vote,
        color: 'text-rose-500',
        bgColor: 'bg-rose-500/10',
        borderColor: 'border-rose-500/20',
        label: '投票',
    },
    [AGENT_ACTION_TYPE.SYSTEM]: {
        icon: Terminal,
        color: 'text-slate-500',
        bgColor: 'bg-slate-500/10',
        borderColor: 'border-slate-500/20',
        label: '系统',
    },
};

/**
 * 格式化时间戳
 * @param {string} timestamp - ISO 格式时间戳
 * @returns {string} 格式化后的时间字符串
 */
const formatTime = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
    });
};

/**
 * 单条消息组件
 */
const ActionMessage = memo(({action}) => {
    const config = ACTION_CONFIG[action.actionType] || ACTION_CONFIG[AGENT_ACTION_TYPE.SYSTEM];
    const Icon = config.icon;

    return (
        <motion.div
            initial={{opacity: 0, x: 20, scale: 0.95}}
            animate={{opacity: 1, x: 0, scale: 1}}
            exit={{opacity: 0, x: -20, scale: 0.95}}
            transition={{duration: 0.3, ease: [0.25, 0.1, 0.25, 1]}}
            className={`
        relative px-3 py-2.5 rounded-lg border mb-2
        ${config.bgColor} ${config.borderColor}
        backdrop-blur-sm
        hover:shadow-md transition-shadow duration-200
      `}
        >
            <div className="flex items-start gap-2.5">
                {/* 图标 */}
                <div className={`
          flex-shrink-0 w-7 h-7 rounded-md flex items-center justify-center
          ${config.bgColor}
        `}>
                    <Icon className={`w-4 h-4 ${config.color}`}/>
                </div>

                {/* 内容 */}
                <div className="flex-1 min-w-0">
                    {/* 头部：角色名 + 动作类型 */}
                    <div className="flex items-center gap-1.5 mb-0.5">
            <span className="text-xs font-medium text-slate-700 dark:text-slate-200 truncate">
              {action.agentName}
            </span>
                        <span className={`
              text-[10px] px-1.5 py-0.5 rounded-full font-medium
              ${config.bgColor} ${config.color}
            `}>
              {config.label}
            </span>
                    </div>

                    {/* 消息内容 */}
                    <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed break-words">
                        {action.message}
                    </p>

                    {/* 目标（线索/场景名称） */}
                    {action.targetName && (
                        <p className="text-[11px] text-slate-500 dark:text-slate-500 mt-1 italic truncate">
                            「{action.targetName}」
                        </p>
                    )}
                </div>

                {/* 时间戳 */}
                <span className="flex-shrink-0 text-[10px] text-slate-400 dark:text-slate-500 tabular-nums">
          {formatTime(action.timestamp)}
        </span>
            </div>
        </motion.div>
    );
});

ActionMessage.displayName = 'ActionMessage';

/**
 * PublicScreen 公屏组件
 * @param {Object} props
 * @param {Array} props.actions - AI Agent 操作消息列表
 * @param {boolean} props.isExpanded - 是否展开
 * @param {Function} props.onToggleExpand - 切换展开状态回调
 */
const PublicScreen = memo(({actions = [], isExpanded = true, onToggleExpand}) => {
    const scrollRef = useRef(null);
    const [displayCount, setDisplayCount] = React.useState(50); // 最大显示条数

    // 自动滚动到底部
    useEffect(() => {
        if (scrollRef.current && isExpanded) {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [actions, isExpanded]);

    // 限制显示数量，避免过多消息影响性能
    const displayedActions = React.useMemo(() => {
        if (actions.length <= displayCount) return actions;
        return actions.slice(actions.length - displayCount);
    }, [actions, displayCount]);

    return (
        <motion.div
            initial={false}
            animate={{
                width: isExpanded ? 320 : 48,
                height: isExpanded ? 'calc(100vh - 140px)' : 48,
            }}
            transition={{duration: 0.3, ease: [0.25, 0.1, 0.25, 1]}}
            className={`
        fixed right-4 top-20 z-40
        rounded-2xl overflow-hidden
        bg-white/80 dark:bg-slate-800/80
        backdrop-blur-xl
        border border-slate-200/50 dark:border-slate-700/50
        shadow-lg shadow-slate-500/10 dark:shadow-black/20
      `}
        >
            {/* 头部 */}
            <div
                onClick={onToggleExpand}
                className={`
          flex items-center gap-2 px-3 h-12
          cursor-pointer select-none
          border-b border-slate-200/50 dark:border-slate-700/50
          hover:bg-slate-50/50 dark:hover:bg-slate-700/30
          transition-colors duration-200
          ${isExpanded ? '' : 'justify-center'}
        `}
            >
                <div className="relative">
                    <Sparkles className="w-5 h-5 text-indigo-500"/>
                    {/* 有新消息时的提示点 */}
                    {actions.length > 0 && !isExpanded && (
                        <span className="absolute -top-0.5 -right-0.5 w-2 h-2 bg-rose-500 rounded-full animate-pulse"/>
                    )}
                </div>
                {isExpanded && (
                    <>
            <span className="flex-1 text-sm font-semibold text-slate-700 dark:text-slate-200">
              AI 操作公屏
            </span>
                        <span className="text-xs text-slate-400 dark:text-slate-500 tabular-nums">
              {actions.length > displayCount ? `${displayCount}+` : actions.length}
            </span>
                    </>
                )}
            </div>

            {/* 消息列表 */}
            <AnimatePresence>
                {isExpanded && (
                    <motion.div
                        initial={{opacity: 0}}
                        animate={{opacity: 1}}
                        exit={{opacity: 0}}
                        transition={{duration: 0.2}}
                        ref={scrollRef}
                        className="h-[calc(100%-48px)] overflow-y-auto scrollbar-thin p-3"
                    >
                        {displayedActions.length === 0 ? (
                            <div className="h-full flex flex-col items-center justify-center text-center p-4">
                                <Terminal className="w-10 h-10 text-slate-300 dark:text-slate-600 mb-3"/>
                                <p className="text-xs text-slate-400 dark:text-slate-500">
                                    暂无 AI 操作记录
                                </p>
                                <p className="text-[11px] text-slate-300 dark:text-slate-600 mt-1">
                                    等待 AI 玩家行动...
                                </p>
                            </div>
                        ) : (
                            <AnimatePresence mode="popLayout">
                                {displayedActions.map((action, index) => (
                                    <ActionMessage
                                        key={`${action.timestamp}-${index}`}
                                        action={action}
                                    />
                                ))}
                            </AnimatePresence>
                        )}
                    </motion.div>
                )}
            </AnimatePresence>
        </motion.div>
    );
});

PublicScreen.displayName = 'PublicScreen';

export default PublicScreen;
export {AGENT_ACTION_TYPE, ACTION_CONFIG};
