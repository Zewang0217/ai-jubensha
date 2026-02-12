// 游戏状态常量
export const GAME_STATUS = {
    WAITING: 'waiting',
    PLAYING: 'playing',
    FINISHED: 'finished',
}

// 游戏状态文本
export const GAME_STATUS_TEXT = {
    [GAME_STATUS.WAITING]: '等待中',
    [GAME_STATUS.PLAYING]: '游戏中',
    [GAME_STATUS.FINISHED]: '已结束',
}

// 游戏状态颜色
export const GAME_STATUS_COLOR = {
    [GAME_STATUS.WAITING]: 'bg-[var(--color-warning)]',
    [GAME_STATUS.PLAYING]: 'bg-[var(--color-success)]',
    [GAME_STATUS.FINISHED]: 'bg-[var(--color-secondary-400)]',
}

// 线索类型常量
export const CLUE_TYPE = {
    PHYSICAL: 'physical',
    TESTIMONY: 'testimony',
    DOCUMENT: 'document',
    OTHER: 'other',
}

// 线索类型文本
export const CLUE_TYPE_TEXT = {
    [CLUE_TYPE.PHYSICAL]: '物证',
    [CLUE_TYPE.TESTIMONY]: '证词',
    [CLUE_TYPE.DOCUMENT]: '文件',
    [CLUE_TYPE.OTHER]: '其他',
}

// 线索类型颜色
export const CLUE_TYPE_COLOR = {
    [CLUE_TYPE.PHYSICAL]: 'bg-[var(--color-primary-100)] text-[var(--color-primary-700)]',
    [CLUE_TYPE.TESTIMONY]: 'bg-[var(--color-accent-100)] text-[var(--color-accent-700)]',
    [CLUE_TYPE.DOCUMENT]: 'bg-[var(--color-success)]/20 text-[var(--color-success)]',
    [CLUE_TYPE.OTHER]: 'bg-[var(--color-secondary-200)] text-[var(--color-secondary-700)]',
}

// WebSocket 消息类型
export const WS_MESSAGE_TYPE = {
    // 连接相关
    CONNECT: 'connect',
    DISCONNECT: 'disconnect',

    // 游戏相关
    GAME_JOIN: 'game_join',
    GAME_LEAVE: 'game_leave',
    GAME_START: 'game_start',
    GAME_END: 'game_end',
    GAME_UPDATE: 'game_update',

    // 玩家相关
    PLAYER_JOIN: 'player_join',
    PLAYER_LEAVE: 'player_leave',
    PLAYER_READY: 'player_ready',
    PLAYER_ACTION: 'player_action',

    // 搜证相关
    CLUE_SEARCH: 'clue_search',
    CLUE_FOUND: 'clue_found',
    CLUE_SHARE: 'clue_share',

    // 对话相关
    CHAT_MESSAGE: 'chat_message',
    SYSTEM_MESSAGE: 'system_message',

    // 阶段相关
    PHASE_CHANGE: 'phase_change',
    TURN_CHANGE: 'turn_change',

    // 投票相关
    VOTE_START: 'vote_start',
    VOTE_CAST: 'vote_cast',
    VOTE_END: 'vote_end',

    // 错误
    ERROR: 'error',
}

// 本地存储键名
export const STORAGE_KEYS = {
    TOKEN: 'token',
    USER: 'user',
    SETTINGS: 'gameSettings',
    LAST_GAME: 'lastGame',
}

// 路由路径
export const ROUTES = {
    HOME: '/',
    GAMES: '/games',
    GAME: '/game/:id',
    SCENE: '/scene/:id',
    CHARACTER: '/character/:id',
    CLUE: '/clue/:id',
    SETTINGS: '/settings',
    NOT_FOUND: '/404',
}

// API 错误码
export const API_ERROR_CODE = {
    UNAUTHORIZED: 401,
    FORBIDDEN: 403,
    NOT_FOUND: 404,
    INTERNAL_ERROR: 500,
}

// 默认分页配置
export const PAGINATION = {
    DEFAULT_PAGE: 1,
    DEFAULT_SIZE: 10,
    SIZE_OPTIONS: [10, 20, 50, 100],
}

// 动画配置
export const ANIMATION = {
    DURATION: {
        FAST: 0.2,
        NORMAL: 0.3,
        SLOW: 0.5,
    },
    EASE: {
        DEFAULT: [0.25, 0.1, 0.25, 1],
        BOUNCE: [0.68, -0.55, 0.265, 1.55],
    },
}
