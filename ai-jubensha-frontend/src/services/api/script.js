import apiClient from './client'

/**
 * 剧本 API 模块
 * 提供剧本相关的所有 HTTP 请求接口
 */

// Mock 数据开关 - 开发环境使用
const USE_MOCK = false

// Mock 剧本数据
const mockScripts = [
    {
        id: 1,
        name: '迷雾庄园',
        author: '张三',
        description: '一座古老的庄园，一场神秘的谋杀案。在迷雾笼罩的夜晚，每个人都有秘密，每个人都有嫌疑。作为侦探，你需要通过搜集线索、分析证据，找出真正的凶手。\n\n故事背景设定在1920年代的英国乡村，一位富有的庄园主在他的生日晚宴上被发现死在自己的书房中。现场没有明显的入侵痕迹，所有嫌疑人都是庄园的客人和仆人。你需要仔细审问每个人，找出他们之间的矛盾和秘密，最终揭开真相。',
        playerCount: 6,
        duration: 180,
        difficulty: 'MEDIUM',
        coverImageUrl: '',
        createTime: '2026-01-15T10:30:00Z',
        updateTime: '2026-02-10T14:20:00Z',
    },
    {
        id: 2,
        name: '消失的证据',
        author: '李四',
        description: '一份关键证据在警局证物室神秘消失。所有线索都指向内部人员，但每个人都有完美的不在场证明。',
        playerCount: 4,
        duration: 120,
        difficulty: 'EASY',
        coverImageUrl: '',
        createTime: '2026-01-20T09:00:00Z',
        updateTime: '2026-01-25T16:45:00Z',
    },
    {
        id: 3,
        name: '时间密室',
        author: '王五',
        description: '一个完全密闭的房间，一具尸体，但死亡时间却显示为三天前。这是完美的密室杀人，还是超自然现象？',
        playerCount: 8,
        duration: 240,
        difficulty: 'HARD',
        coverImageUrl: '',
        createTime: '2026-02-01T11:00:00Z',
        updateTime: '2026-02-08T10:30:00Z',
    },
]

/**
 * @typedef {Object} ListScriptResponseDTO
 * @property {number} id - 剧本ID
 * @property {string} name - 剧本名称
 * @property {string} description - 剧本描述
 * @property {string} author - 作者
 * @property {'EASY'|'MEDIUM'|'HARD'} difficulty - 难度等级
 * @property {number} duration - 游戏时长(分钟)
 * @property {number} playerCount - 玩家数量
 * @property {string} coverImageUrl - 封面图片URL
 * @property {string} createTime - 创建时间
 * @property {string} updateTime - 更新时间
 */

/**
 * 获取剧本列表
 * @returns {Promise<ListScriptResponseDTO[]>} 剧本列表
 */
export const getScripts = () => {
    if (USE_MOCK) {
        // 模拟 API 延迟
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    content: mockScripts,
                    totalElements: mockScripts.length,
                    totalPages: 1,
                    number: 0,
                    size: 10,
                })
            }, 500)
        })
    }
    return apiClient.get('/scripts')
}

/**
 * 根据ID获取剧本详情
 * @param {number} id - 剧本ID
 * @returns {Promise<ListScriptResponseDTO>} 剧本详情
 */
export const getScriptById = (id) => {
    if (USE_MOCK) {
        // 模拟 API 延迟
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const script = mockScripts.find((s) => s.id === Number(id))
                if (!script) {
                    reject(new Error('剧本不存在'))
                } else {
                    resolve(script)
                }
            }, 800)
        })
    }
    return apiClient.get(`/scripts/${id}`)
}

/**
 * 创建剧本
 * @param {Object} data - 剧本数据
 * @returns {Promise<ListScriptResponseDTO>} 创建的剧本
 */
export const createScript = (data) => apiClient.post('/scripts', data)

/**
 * 更新剧本
 * @param {number} id - 剧本ID
 * @param {Object} data - 更新的剧本数据
 * @returns {Promise<ListScriptResponseDTO>} 更新后的剧本
 */
export const updateScript = (id, data) => apiClient.put(`/scripts/${id}`, data)

/**
 * 删除剧本
 * @param {number} id - 剧本ID
 * @returns {Promise<void>}
 */
export const deleteScript = (id) => apiClient.delete(`/scripts/${id}`)

/**
 * AI生成剧本
 * @param {Object} params - 生成参数
 * @returns {Promise<ListScriptResponseDTO>} 生成的剧本
 */
export const generateScript = (params) => apiClient.post('/scripts/generate', params)
