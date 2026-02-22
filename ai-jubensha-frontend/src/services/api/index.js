import apiClient from './client'

// API 方法封装
export const api = {
    // GET 请求
    get: (url, params = {}) => apiClient.get(url, {params}),

    // POST 请求
    post: (url, data = {}) => apiClient.post(url, data),

    // PUT 请求
    put: (url, data = {}) => apiClient.put(url, data),

    // DELETE 请求
    delete: (url) => apiClient.delete(url),

    // PATCH 请求
    patch: (url, data = {}) => apiClient.patch(url, data),
}

// 游戏相关 API
const USE_MOCK = true // 开启 Mock 数据模式

// Mock 数据
const mockGameData = {
    1: {
        id: 1,
        name: '午夜庄园谋杀案',
        scriptName: '午夜庄园',
        status: 'playing',
        currentPlayers: 4,
        maxPlayers: 6,
        players: [
            {
                id: 1,
                name: '玩家A',
                characterName: '管家',
                backgroundStory: '在庄园工作了20年的老管家，对庄园的一切了如指掌',
                isHost: true
            },
            {
                id: 2,
                name: '玩家B',
                characterName: '医生',
                backgroundStory: '镇上的知名医生，与死者有旧交情',
                isHost: false
            },
            {
                id: 3,
                name: '玩家C',
                characterName: '律师',
                backgroundStory: '负责死者遗嘱的律师，动机不明',
                isHost: false
            },
            {
                id: 4,
                name: '玩家D',
                characterName: '厨师',
                backgroundStory: '庄园的新厨师，最近刚入职',
                isHost: false
            }
        ]
    }
}

export const gameApi = {
    // 获取游戏列表
    getGames: (params) => api.get('/games', params),

    // 获取游戏详情
    getGame: (id) => {
        if (USE_MOCK) {
            return Promise.resolve({
                data: mockGameData[id] || {
                    id: parseInt(id),
                    name: `游戏房间 #${id}`,
                    scriptName: '未选择剧本',
                    status: 'waiting',
                    currentPlayers: 0,
                    maxPlayers: 8,
                    players: []
                }
            })
        }
        return api.get(`/games/${id}`)
    },

    // 创建游戏
    createGame: (data) => api.post('/games', data),

    // 更新游戏
    updateGame: (id, data) => api.put(`/games/${id}`, data),

    // 删除游戏
    deleteGame: (id) => api.delete(`/games/${id}`),

    // 加入游戏
    joinGame: (id, data) => api.post(`/games/${id}/join`, data),

    // 离开游戏
    leaveGame: (id) => api.post(`/games/${id}/leave`),
}

// 剧本相关 API
export const scriptApi = {
    // 获取剧本列表
    getScripts: (params) => api.get('/scripts', params),

    // 获取剧本详情
    getScript: (id) => api.get(`/scripts/${id}`),

    // 创建剧本
    createScript: (data) => api.post('/scripts', data),

    // 生成剧本
    generateScript: (data) => api.post('/scripts/generate', data),
}

// 角色相关 API
export const characterApi = {
    // 获取角色列表
    getCharacters: (params) => api.get('/characters', params),

    // 获取角色详情
    getCharacter: (id) => api.get(`/characters/${id}`),

    // 创建角色
    createCharacter: (data) => api.post('/characters', data),
}

// 线索相关 API
export const clueApi = {
    // 获取线索列表
    getClues: (params) => api.get('/clues', params),

    // 获取线索详情
    getClue: (id) => api.get(`/clues/${id}`),

    // 创建线索
    createClue: (data) => api.post('/clues', data),

    // 搜证
    searchClue: (data) => api.post('/clues/search', data),
}

// 场景相关 API
export const sceneApi = {
    // 获取场景列表
    getScenes: (params) => api.get('/scenes', params),

    // 获取场景详情
    getScene: (id) => api.get(`/scenes/${id}`),

    // 创建场景
    createScene: (data) => api.post('/scenes', data),
}

// 导出新的模块化 API
export {
    getScripts,
    getScriptById,
    createScript as createScriptNew,
    updateScript,
    deleteScript,
    generateScript as generateScriptNew,
} from './script'

export {default as apiClient} from './client'
export {default} from './client'
