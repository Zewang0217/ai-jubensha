/**
 * @fileoverview 游戏数据适配器
 * @description 将后端API返回的数据转换为前端组件所需的格式
 */

import {getScriptById} from './script'
import {getCharactersByScriptId} from './character'
import {getScenesByScriptId, getSceneClues} from './scene'

/**
 * 将后端游戏数据转换为前端所需格式
 * @param {Object} gameData - 后端返回的游戏数据
 * @param {Array} players - 后端返回的游戏玩家数据
 * @returns {Promise<Object>} 前端所需的游戏数据格式
 */
export const adaptGameData = async (gameData, players) => {
    if (!gameData) {
        console.warn('[adaptGameData] gameData is null or undefined')
        return null
    }

    console.log('[adaptGameData] Input gameData:', gameData)
    console.log('[adaptGameData] Input players:', players)

    const scriptId = gameData.scriptId
    let script = null
    let characters = []
    let scenes = []

    // 获取剧本详情
    if (scriptId) {
        try {
            const scriptResponse = await getScriptById(scriptId)
            script = scriptResponse?.data || scriptResponse
            console.log('[adaptGameData] Fetched script:', script)
        } catch (err) {
            console.error('[adaptGameData] Failed to fetch script:', err)
        }

        // 获取角色列表
        try {
            const charactersResponse = await getCharactersByScriptId(scriptId)
            characters = charactersResponse?.data || charactersResponse || []
            console.log('[adaptGameData] Fetched characters:', characters)
        } catch (err) {
            console.error('[adaptGameData] Failed to fetch characters:', err)
        }

        // 获取场景列表
        try {
            const scenesResponse = await getScenesByScriptId(scriptId)
            scenes = scenesResponse?.data || scenesResponse || []
            console.log('[adaptGameData] Fetched scenes:', scenes)

            // 获取每个场景的线索
            if (Array.isArray(scenes) && scenes.length > 0) {
                const scenesWithClues = await Promise.all(
                    scenes.map(async (scene) => {
                        try {
                            const cluesResponse = await getSceneClues(scene.id)
                            const clues = cluesResponse?.data || cluesResponse || []
                            return {
                                ...scene,
                                clues: Array.isArray(clues) ? clues : [],
                                clueCount: Array.isArray(clues) ? clues.length : 0,
                            }
                        } catch (err) {
                            console.error(`[adaptGameData] Failed to fetch clues for scene ${scene.id}:`, err)
                            return {
                                ...scene,
                                clues: [],
                                clueCount: 0,
                            }
                        }
                    })
                )
                scenes = scenesWithClues
            }
        } catch (err) {
            console.error('[adaptGameData] Failed to fetch scenes:', err)
        }
    }

    // 构建前端所需的数据结构
    const adaptedData = {
        // 游戏基本信息
        id: gameData.id,
        gameId: gameData.id,
        gameCode: gameData.gameCode,
        status: gameData.status,
        currentPhase: gameData.currentPhase,
        startTime: gameData.startTime,
        endTime: gameData.endTime,
        createTime: gameData.createTime,
        updateTime: gameData.updateTime,

        // 剧本信息
        scriptId: scriptId,
        script: script ? {
            id: script.id,
            name: script.name,
            description: script.description,
            author: script.author,
            difficulty: script.difficulty,
            duration: script.duration,
            playerCount: script.playerCount,
            coverImageUrl: script.coverImageUrl,
        } : null,

        // 角色列表
        characters: Array.isArray(characters) ? characters.map((char, index) => ({
            id: char.id,
            name: char.name,
            description: char.description,
            background: char.backgroundStory,
            secret: char.secret,
            avatarUrl: char.avatarUrl,
            isPlayer: players?.some(p => p.characterId === char.id) || false,
        })) : [],

        // 场景列表
        scenes: Array.isArray(scenes) ? scenes.map(scene => ({
            id: scene.id,
            name: scene.name,
            description: scene.description,
            imageUrl: scene.imageUrl,
            isLocked: false,
            clueCount: scene.clueCount || 0,
            clues: scene.clues || [],
        })) : [],

        // 玩家列表
        players: Array.isArray(players) ? players.map(player => ({
            id: player.id,
            playerId: player.playerId,
            characterId: player.characterId,
            isDm: player.isDm,
            status: player.status,
            joinTime: player.joinTime,
        })) : [],
    }

    console.log('[adaptGameData] Adapted data:', adaptedData)
    return adaptedData
}

/**
 * 阶段映射 - 后端阶段到前端阶段
 */
export const PHASE_MAPPING = {
    'INTRODUCTION': 'SCRIPT_OVERVIEW',
    'SEARCH': 'INVESTIGATION',
    'DISCUSSION': 'DISCUSSION',
    'VOTING': 'VOTING',
    'ENDING': 'SUMMARY',
}

/**
 * 将后端阶段转换为前端阶段
 * @param {string} backendPhase - 后端阶段名称
 * @returns {string} 前端阶段名称
 */
export const adaptPhase = (backendPhase) => {
    return PHASE_MAPPING[backendPhase] || backendPhase
}

export default {
    adaptGameData,
    adaptPhase,
}
