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