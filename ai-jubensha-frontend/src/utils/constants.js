/**
 * @fileoverview 全局常量定义
 * @description 项目中使用的全局常量
 */

// =============================================================================
// WebSocket 消息类型
// =============================================================================

/**
 * @readonly
 * @enum {string}
 * @description WebSocket 消息类型常量（严格按照前端 WebSocket 接入说明书）
 */
export const WS_MESSAGE_TYPE = {
    // 发送/接收 - 聊天消息
    CHAT_MESSAGE: 'CHAT_MESSAGE',

    // 接收 - 剧本生成完成通知
    SCRIPT_READY: 'SCRIPT_READY',

    // 接收 - 开始搜证通知
    START_INVESTIGATION: 'START_INVESTIGATION',

    // 接收 - 公开线索广播
    PUBLIC_CLUE: 'PUBLIC_CLUE',

    // 接收 - 投票结果广播
    VOTE_RESULT: 'VOTE_RESULT',

    // 发送 - 提交投票
    VOTE_SUBMIT: 'VOTE_SUBMIT',
}

// =============================================================================
// 本地存储键名
// =============================================================================

/**
 * @readonly
 * @enum {string}
 * @description localStorage 存储键名常量
 */
export const STORAGE_KEYS = {
    TOKEN: 'token',
    USER: 'user',
    SETTINGS: 'gameSettings',
    LAST_GAME: 'lastGame',
}