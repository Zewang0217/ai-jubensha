/**
 * @fileoverview 游戏数据适配器
 * @description 将后端API返回的数据转换为前端组件所需的格式
 */

import {getScriptById} from './script'
import {getCharactersByScriptId} from './character'
import {getScenesByScriptId, getSceneClues} from './scene'
import {getPublicClues} from './clue'

/**
 * 根据场景名称智能分配线索到场景
 * @param {Array} clues - 线索列表
 * @param {Array} scenes - 场景列表
 * @returns {Object} 场景名称到线索列表的映射
 */
const distributeCluesBySceneName = (clues, scenes) => {
    const sceneNameToIdMap = new Map()
    scenes.forEach(scene => {
        sceneNameToIdMap.set(scene.name.toLowerCase(), scene.id)
        sceneNameToIdMap.set(scene.name, scene.id)
    })

    const clueDistribution = {}
    clues.forEach(clue => {
        let assignedSceneId = clue.sceneId

        // 如果线索没有 sceneId，尝试根据 scene 字段匹配
        if (!assignedSceneId && clue.scene) {
            const matchedId = sceneNameToIdMap.get(clue.scene.toLowerCase()) 
                || sceneNameToIdMap.get(clue.scene)
            if (matchedId) {
                assignedSceneId = matchedId
            }
        }

        // 如果仍然没有匹配的，默认分配到第一个场景
        if (!assignedSceneId && scenes.length > 0) {
            assignedSceneId = scenes[0].id
        }

        if (assignedSceneId) {
            const sceneIdStr = String(assignedSceneId)
            if (!clueDistribution[sceneIdStr]) {
                clueDistribution[sceneIdStr] = []
            }
            clueDistribution[sceneIdStr].push({
                ...clue,
                sceneId: assignedSceneId
            })
        }
    })

    return clueDistribution
}

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
                console.log('[adaptGameData] Fetching clues for scenes:', scenes.map(s => s.id))
                const scenesWithClues = await Promise.all(
                    scenes.map(async (scene) => {
                        try {
                            console.log(`[adaptGameData] Fetching clues for scene ${scene.id} (${scene.name})`)
                            const cluesResponse = await getSceneClues(scene.id)
                            let clues = cluesResponse?.data || cluesResponse || []
                            
                            // 过滤掉没有 sceneId 或者 sceneId 匹配的线索
                            clues = clues.filter(clue => {
                                // 如果线索有 sceneId，必须匹配当前场景
                                if (clue.sceneId) {
                                    return Number(clue.sceneId) === Number(scene.id)
                                }
                                // 如果线索没有 sceneId，检查 scene 字段是否匹配场景名称
                                if (clue.scene) {
                                    return clue.scene.toLowerCase() === scene.name.toLowerCase()
                                }
                                // 如果都没有，暂不显示（会被分配到其他场景）
                                return false
                            })
                            
                            console.log(`[adaptGameData] Got ${clues.length} clues for scene ${scene.id}:`, clues.map(c => ({ id: c.id, name: c.name, sceneId: c.sceneId })))
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
                
                // 检查是否有未分配到场景的线索
                const allClues = scenesWithClues.flatMap(s => s.clues)
                const unassignedClues = []
                
                if (allClues.length === 0 && scenes.length > 0) {
                    // 尝试获取所有线索（备用方案）
                    try {
                        const allCluesResponse = await getScenesByScriptId(scriptId)
                        const allScenes = allCluesResponse?.data || allCluesResponse || []
                        
                        // 收集所有线索
                        const allCluesCollected = []
                        for (const s of allScenes) {
                            try {
                                const cluesResp = await getSceneClues(s.id)
                                const c = cluesResp?.data || cluesResp || []
                                allCluesCollected.push(...c.map(cl => ({...cl, _sourceSceneId: s.id})))
                            } catch (e) {
                                // 忽略错误
                            }
                        }
                        
                        if (allCluesCollected.length > 0) {
                            console.log('[adaptGameData] Total clues collected from all scenes:', allCluesCollected.length)
                            
                            // 使用智能分配
                            const clueDistribution = distributeCluesBySceneName(allCluesCollected, scenes)
                            
                            // 更新场景的线索
                            scenesWithClues.forEach(scene => {
                                const sceneIdStr = String(scene.id)
                                if (clueDistribution[sceneIdStr]) {
                                    scene.clues = clueDistribution[sceneIdStr]
                                    scene.clueCount = clueDistribution[sceneIdStr].length
                                }
                            })
                        }
                    } catch (e) {
                        console.error('[adaptGameData] Fallback to collect all clues failed:', e)
                    }
                }
                
                scenes = scenesWithClues
                console.log('[adaptGameData] All scenes with clues:', scenes.map(s => ({ id: s.id, name: s.name, clueCount: s.clueCount })))
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

        // 游戏配置信息（用于刷新后恢复）
        realPlayerCount: gameData.realPlayerCount ?? 1,
        workflowNode: gameData.workflowNode,

        // 搜证次数限制
        remainingChances: gameData.remainingChances ?? 3,
        totalChances: gameData.totalChances ?? 3,

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
        scenes: Array.isArray(scenes) ? scenes.map(scene => {
            const adaptedClues = Array.isArray(scene.clues) ? scene.clues.map(clue => ({
                id: String(clue.id),
                name: clue.name || '',
                description: clue.description || '',
                type: clue.type || 'OTHER',
                visibility: clue.visibility || 'PUBLIC',
                sceneId: clue.sceneId,
                imageUrl: clue.imageUrl,
                importance: clue.importance,
                playerId: clue.playerId,
            })) : []

            return {
                id: String(scene.id),
                name: scene.name,
                description: scene.description,
                imageUrl: scene.imageUrl,
                isLocked: false,
                clueCount: adaptedClues.length,
                clues: adaptedClues,
            }
        }) : [],

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

    // 获取公开线索（用于讨论阶段显示）
    let publicClues = []
    if (scriptId) {
        try {
            const publicCluesResponse = await getPublicClues()
            const allPublicClues = publicCluesResponse?.data || publicCluesResponse || []
            // 过滤出当前剧本的公开线索
            publicClues = allPublicClues.filter(clue => clue.scriptId === scriptId)
            console.log('[adaptGameData] Fetched public clues:', publicClues.length)
        } catch (err) {
            console.error('[adaptGameData] Failed to fetch public clues:', err)
        }
    }
    
    // 将公开线索添加到 adaptedData
    adaptedData.publicClues = publicClues.map(clue => ({
        id: String(clue.id),
        name: clue.name || '',
        description: clue.description || '',
        type: clue.type || 'OTHER',
        visibility: clue.visibility || 'PUBLIC',
        sceneId: clue.sceneId,
        imageUrl: clue.imageUrl,
        importance: clue.importance,
        playerId: clue.playerId,
    }))

    console.log('[adaptGameData] Adapted data:', adaptedData)
    return adaptedData
}

/**
 * 阶段映射 - 后端阶段到前端阶段
 * @description 后端使用大写下划线格式（如 SCRIPT_OVERVIEW），前端使用小写下划线格式（如 script_overview）
 */
export const PHASE_MAPPING = {
    'SCRIPT_OVERVIEW': 'script_overview',
    'CHARACTER_ASSIGNMENT': 'character_assignment',
    'SCRIPT_READING': 'script_reading',
    'INVESTIGATION': 'investigation',
    'DISCUSSION': 'discussion',
    'SUMMARY': 'summary',
}

/**
 * 将后端阶段转换为前端阶段（支持双向映射）
 * @param {string} phase - 阶段名称（后端格式或前端格式）
 * @returns {string} 前端阶段名称（小写下划线格式）
 * @example
 * adaptPhase('SCRIPT_OVERVIEW') // 返回 'script_overview'
 * adaptPhase('script_overview') // 返回 'script_overview'（已经是前端格式，直接返回）
 */
export const adaptPhase = (phase) => {
    // 如果传入的是前端阶段格式（小写下划线），直接返回
    if (phase && phase === phase.toLowerCase()) {
        return phase
    }
    // 否则从映射表中查找转换
    return PHASE_MAPPING[phase] || phase
}

export default {
    adaptGameData,
    adaptPhase,
}
