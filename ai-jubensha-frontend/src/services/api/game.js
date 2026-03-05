import apiClient from './client'

/**
 * 从剧本创建游戏并启动工作流
 * @param {number} scriptId - 剧本ID
 * @param {number} realPlayerCount - 真人玩家数量 (0 或 1)
 * @returns {Promise<{gameId: number, workflowId: string}>}
 */
export const createGameFromScript = async (scriptId, realPlayerCount) => {
    console.log('[createGameFromScript] Starting with params:', { scriptId, realPlayerCount })
    
    // 验证 scriptId
    if (!scriptId) {
        throw new Error('scriptId 不能为空')
    }

    // 步骤1: 创建游戏实体
    console.log('[createGameFromScript] Creating game entity...')
    const createResponse = await apiClient.post('/games', {
        scriptId: scriptId,
        status: 'CREATED',
        currentPhase: 'INTRODUCTION'
    })
    
    console.log('[createGameFromScript] Create response:', createResponse)
    
    // 处理可能的嵌套数据结构
    const gameData = createResponse?.data || createResponse
    const gameId = gameData?.id

    if (!gameId) {
        console.error('[createGameFromScript] Failed to get gameId from response:', createResponse)
        throw new Error('创建游戏失败：未能获取游戏ID')
    }

    console.log('[createGameFromScript] Game created with ID:', gameId)

    // 步骤2: 启动工作流（立即返回，不等待完成）
    console.log('[createGameFromScript] Starting workflow...')
    const workflowResponse = await apiClient.post('/games/start-workflow', {
        originalPrompt: '使用现有剧本开始游戏', // 后端要求 originalPrompt 不能为空
        createNewScript: false,
        scriptId: scriptId,
        gameId: gameId,
        realPlayerCount: realPlayerCount
    })
    
    console.log('[createGameFromScript] Workflow response:', workflowResponse)

    return {
        gameId: gameId,
        workflowId: workflowResponse?.workflowId || workflowResponse?.data?.workflowId
    }
}

/**
 * 获取游戏列表
 * @param {Object} params - 查询参数
 * @returns {Promise<Array>}
 */
export const getGames = (params) => apiClient.get('/games', { params })

/**
 * 获取游戏详情
 * @param {number} gameId - 游戏ID
 * @returns {Promise<Object>}
 */
export const getGameById = (gameId) => apiClient.get(`/games/${gameId}`)

/**
 * 更新游戏
 * @param {number} gameId - 游戏ID
 * @param {Object} data - 更新数据
 * @returns {Promise<Object>}
 */
export const updateGame = (gameId, data) => apiClient.put(`/games/${gameId}`, data)

/**
 * 删除游戏
 * @param {number} gameId - 游戏ID
 * @returns {Promise<void>}
 */
export const deleteGame = (gameId) => apiClient.delete(`/games/${gameId}`)

/**
 * 加入游戏
 * @param {number} gameId - 游戏ID
 * @param {Object} data - 加入数据
 * @returns {Promise<Object>}
 */
export const joinGame = (gameId, data) => apiClient.post(`/games/${gameId}/join`, data)

/**
 * 离开游戏
 * @param {number} gameId - 游戏ID
 * @returns {Promise<void>}
 */
export const leaveGame = (gameId) => apiClient.post(`/games/${gameId}/leave`)

/**
 * 获取游戏玩家列表
 * @param {number} gameId - 游戏ID
 * @returns {Promise<Array>}
 */
export const getGamePlayers = (gameId) => apiClient.get(`/game-players/game/${gameId}`)

/**
 * 获取游戏工作流状态
 * @param {number} gameId - 游戏ID
 * @returns {Promise<Object>}
 */
export const getWorkflowStatus = (gameId) => apiClient.get(`/games/${gameId}/workflow/status`)

/**
 * 启动游戏
 * @param {number} gameId - 游戏ID
 * @returns {Promise<Object>}
 */
export const startGame = (gameId) => apiClient.post(`/games/${gameId}/start`)

/**
 * 结束游戏
 * @param {number} gameId - 游戏ID
 * @returns {Promise<Object>}
 */
export const endGame = (gameId) => apiClient.post(`/games/${gameId}/end`)

/**
 * 更新游戏阶段
 * @param {number} gameId - 游戏ID
 * @param {string} phase - 阶段名称
 * @returns {Promise<Object>}
 */
export const updateGamePhase = (gameId, phase) => apiClient.put(`/games/${gameId}/phase/${phase}`)

export default {
    createGameFromScript,
    getGames,
    getGameById,
    updateGame,
    deleteGame,
    joinGame,
    leaveGame,
    getGamePlayers,
    getWorkflowStatus,
    startGame,
    endGame,
    updateGamePhase
}
