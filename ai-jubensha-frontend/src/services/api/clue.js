import apiClient from './client'

/**
 * 线索 API 模块
 * 提供线索相关的所有 HTTP 请求接口
 */

/**
 * @typedef {Object} ClueResponseDTO
 * @property {number} id - 线索ID
 * @property {number} scriptId - 剧本ID
 * @property {number} sceneId - 场景ID
 * @property {string} name - 线索名称
 * @property {string} description - 线索描述
 * @property {string} type - 线索类型
 * @property {string} visibility - 线索可见性
 * @property {string} scene - 场景名称
 * @property {string} imageUrl - 线索图片URL
 * @property {number} importance - 重要程度
 * @property {string} createTime - 创建时间
 * @property {number} playerId - 玩家ID
 */

/**
 * 根据ID查询线索
 * @param {number} clueId - 线索ID
 * @returns {Promise<ClueResponseDTO>} 线索详情
 */
export const getClueById = (clueId) => {
    return apiClient.get(`/clues/${clueId}`)
}

/**
 * 更新线索
 * @param {number} clueId - 线索ID
 * @param {Object} data - 线索数据
 * @returns {Promise<ClueResponseDTO>} 更新后的线索
 */
export const updateClue = (clueId, data) => {
    return apiClient.put(`/clues/${clueId}`, data)
}

/**
 * 公开线索
 * @param {number} clueId - 线索ID
 * @returns {Promise<ClueResponseDTO>} 更新后的线索
 */
export const publicClue = (clueId) => {
    return updateClue(clueId, { visibility: 'PUBLIC' })
}

/**
 * 设置线索为私有
 * @param {number} clueId - 线索ID
 * @returns {Promise<ClueResponseDTO>} 更新后的线索
 */
export const privateClue = (clueId) => {
    return updateClue(clueId, { visibility: 'PRIVATE' })
}

/**
 * 获取公开线索列表
 * @returns {Promise<Array<ClueResponseDTO>>} 公开线索列表
 */
export const getPublicClues = () => {
    return apiClient.get('/clues/visibility/PUBLIC')
}

/**
 * 根据剧本ID获取线索列表
 * @param {number} scriptId - 剧本ID
 * @returns {Promise<Array<ClueResponseDTO>>} 线索列表
 */
export const getCluesByScriptId = (scriptId) => {
    return apiClient.get(`/clues/script/${scriptId}`)
}

export default {
    getClueById,
    updateClue,
    publicClue,
    privateClue,
    getPublicClues,
    getCluesByScriptId
}
