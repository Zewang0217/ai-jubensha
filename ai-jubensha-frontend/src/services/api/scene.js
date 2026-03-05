import apiClient from './client'

/**
 * 场景 API 模块
 * 提供场景相关的所有 HTTP 请求接口
 */

/**
 * @typedef {Object} SceneResponseDTO
 * @property {number} id - 场景ID
 * @property {number} scriptId - 剧本ID
 * @property {string} scriptName - 关联剧本名称
 * @property {string} name - 场景名称
 * @property {string} description - 场景描述
 * @property {string} imageUrl - 场景图片URL
 * @property {string} availableActions - 可用动作
 * @property {string} createTime - 创建时间
 */

/**
 * 根据剧本ID查询场景列表
 * @param {number} scriptId - 剧本ID
 * @returns {Promise<SceneResponseDTO[]>} 场景列表
 */
export const getScenesByScriptId = (scriptId) => {
    return apiClient.get(`/scenes/script/${scriptId}`)
}

/**
 * 根据场景ID查询场景详情
 * @param {number} sceneId - 场景ID
 * @returns {Promise<SceneResponseDTO>} 场景详情
 */
export const getSceneById = (sceneId) => {
    return apiClient.get(`/scenes/${sceneId}`)
}

/**
 * 获取场景中的线索列表
 * @param {number} sceneId - 场景ID
 * @returns {Promise<Array>} 线索列表
 */
export const getSceneClues = (sceneId) => {
    return apiClient.get(`/scenes/${sceneId}/clues`)
}

/**
 * 创建场景
 * @param {Object} data - 场景数据
 * @returns {Promise<SceneResponseDTO>} 创建的场景
 */
export const createScene = (data) => apiClient.post('/scenes', data)

/**
 * 更新场景
 * @param {number} sceneId - 场景ID
 * @param {Object} data - 场景数据
 * @returns {Promise<SceneResponseDTO>} 更新后的场景
 */
export const updateScene = (sceneId, data) =>
    apiClient.put(`/scenes/${sceneId}`, data)

/**
 * 删除场景
 * @param {number} sceneId - 场景ID
 * @returns {Promise<void>}
 */
export const deleteScene = (sceneId) =>
    apiClient.delete(`/scenes/${sceneId}`)

export default {
    getScenesByScriptId,
    getSceneById,
    getSceneClues,
    createScene,
    updateScene,
    deleteScene
}
