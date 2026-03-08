/**
 * @fileoverview SummaryPreview 组件 - 真相揭晓预览页面
 * @description 用于独立预览 Summary 组件的调试页面，使用 mock 数据展示
 */

import {memo} from 'react'
import {useNavigate} from 'react-router-dom'
import Summary from '../GameRoom/phases/Summary'

// =============================================================================
// Mock 数据配置 - 使用用户提供的完整评分数据
// =============================================================================

/**
 * Mock 游戏数据
 * @type {Object}
 */
const MOCK_GAME_DATA = {
    gameId: 'preview-game-001',
    scriptId: 1,
    scriptName: '测试剧本 - 时隙学苑谜案',
    phase: 'SUMMARY',
    status: 'FINISHED',
    players: [
        {playerId: 1352, name: '轮回老师', characterName: '轮回老师', role: '时间感知者', isRealPlayer: true},
        {playerId: 1353, name: '凝时', characterName: '凝时', role: '时间感知紊乱者', isRealPlayer: false},
        {playerId: 1354, name: '溯光医生', characterName: '溯光医生', role: '时间修复师', isRealPlayer: false},
        {playerId: 1355, name: '断流保安', characterName: '断流保安', role: '前时间刑警', isRealPlayer: true},
        {playerId: 1356, name: '瞬影', characterName: '瞬影', role: '记忆被篡改者', isRealPlayer: false},
        {playerId: 1357, name: '隙月', characterName: '隙月', role: '时间干扰者', isRealPlayer: false},
        {playerId: 1358, name: '玩家1358', characterName: '玩家1358', role: '时间观测者', isRealPlayer: true},
    ],
    result: {
        // 真相内容
        ending: `案发当晚，月光如水洒在时隙学苑。19:50，断流在钟楼东侧感受到时间黏滞的异常波动——那是隙月调整时间干扰器参数的痕迹。

20:00，时痕提前来到钟楼底层密室检查逆流之钟，他预感到了什么。

20:30，瞬影愤怒地冲进办公室，却发现空无一人，桌上银色凝胶泛着冷光。

与此同时，在钟楼深处，隙月面对养育自己多年的叔叔，手中时间终结剂微微颤抖。

"为了父母，"她轻声说，声音在时间凝固的密室中回荡。

时痕没有反抗，只是平静地看着她，眼中闪过一丝解脱。逆流之钟发出短促三响，像被扼住的呜咽，宣告着时间守护者的终结，也开启了新的时间螺旋。`,
        // DM 总结
        summary: '本次剧本杀玩家整体表现良好，多数玩家投入角色扮演，推理质量较高。断流保安（1355）和凝时（1353）的推理最为严谨完整，隙月（1357）的自我辩护也很出色。瞬影（1356）的角色扮演感人至深，轮回老师（1352）的诗意回答很有特色。溯光医生（1354）保持了角色神秘感，但推理不足。玩家1358需要更多参与和推理。',
        // 凶手信息
        culprit: {
            id: 1357,
            name: '隙月',
            characterName: '时间干扰者'
        },
        // 玩家答案
        playerAnswers: {
            '1352': '我认为凶手是...（轮回老师的诗意回答）时间螺旋在转动，真相隐藏在逆流之钟的回响中。每一个时间碎片都指向那个被遗忘的角落...',
            '1353': '经过详细分析，我认为凶手是隙月。动机：为父母复仇；手法：利用时间干扰器制造时间停滞，使用时间终结剂；关键线索：时间凝胶、逆流之钟的异常、钟楼密室的时间波动。作为时间感知紊乱者，我能感受到案发时的时间涟漪。',
            '1354': '（溯光医生的神秘回答）作为时间修复师，我看到了太多不该看到的东西。真相...或许不是你们想象的那样。有些秘密应该永远被封存。',
            '1355': '作为前时间刑警，我通过时间线分析和线索对比，确定凶手是隙月。详细推理：1.时间线矛盾 2.动机充分（父母之仇）3.作案手法专业 4.现场遗留的时间凝胶痕迹。建议立即逮捕！',
            '1356': '我的记忆被篡改了，但我记得那个声音...那个在时间缝隙中呼唤我的声音。我不知道谁是凶手，但我知道我们都是时间的囚徒。对时间凝胶的分析显示...',
            '1357': '我是被冤枉的！真正的凶手是溯光医生！理由：1.他有机会接触时间终结剂 2.他的时间修复师身份可以掩盖作案痕迹 3.他有动机（争夺时间控制权）。请相信我！',
            '1358': '我觉得是凝时。'
        },
        // 玩家评分 - 使用用户提供的完整数据
        scores: [
            {
                playerId: 1352,
                score: 65,
                breakdown: {
                    motive: 12,
                    method: 10,
                    clues: 13,
                    accuracy: 30
                },
                comment: '角色扮演出色，展现了轮回老师追寻真相的执着。答案富有诗意但缺乏明确的推理结论，更像是在探索可能性而非做出判断。对时间螺旋和逆流之钟的解读很有深度。'
            },
            {
                playerId: 1353,
                score: 88,
                breakdown: {
                    motive: 18,
                    method: 18,
                    clues: 18,
                    accuracy: 34
                },
                comment: '推理逻辑清晰，明确指出凶手是隙月，提供了详细的作案动机、手法和关键线索分析。作为时间感知紊乱者，利用自身能力进行推理很有说服力。答案结构完整，论证有力。'
            },
            {
                playerId: 1354,
                score: 55,
                breakdown: {
                    motive: 10,
                    method: 8,
                    clues: 12,
                    accuracy: 25
                },
                comment: '角色扮演到位，展现了溯光医生的冷静和专业。但没有给出明确的答案，更像是在隐藏信息或保护秘密。作为关键嫌疑人，这种回答方式符合角色设定但缺乏推理深度。'
            },
            {
                playerId: 1355,
                score: 92,
                breakdown: {
                    motive: 19,
                    method: 19,
                    clues: 19,
                    accuracy: 35
                },
                comment: '作为前时间刑警，推理最为严谨。明确指出凶手是隙月，提供了极其详细的作案过程分析，时间线梳理清晰。对各个线索的解读专业，对其他玩家的怀疑理由分析到位。答案结构完整，论证有力。'
            },
            {
                playerId: 1356,
                score: 70,
                breakdown: {
                    motive: 14,
                    method: 14,
                    clues: 14,
                    accuracy: 28
                },
                comment: '角色扮演非常出色，完美展现了记忆被篡改者的困惑和挣扎。虽然没有明确指认凶手，但对自身记忆的质疑和对真相的追寻很感人。对时间凝胶使用、记忆干扰器的分析很有深度。'
            },
            {
                playerId: 1357,
                score: 85,
                breakdown: {
                    motive: 17,
                    method: 17,
                    clues: 17,
                    accuracy: 34
                },
                comment: '为自己辩护有力，反指控溯光医生是凶手，推理逻辑清晰。作为被指控者，这种反击策略很聪明。提供了详细的作案过程推测，对时间线矛盾的分析到位。虽然可能是凶手在辩解，但推理质量很高。'
            },
            {
                playerId: 1358,
                score: 20,
                breakdown: {
                    motive: 4,
                    method: 4,
                    clues: 4,
                    accuracy: 8
                },
                comment: '答案过于简单，仅凭直觉指认凝时，缺乏任何推理过程或证据支持。没有分析动机、手法或线索，不符合剧本杀答题要求。'
            }
        ]
    }
}

/**
 * Mock 玩家数据
 * @type {Array<Object>}
 */
const MOCK_PLAYER_DATA = [
    {playerId: 1352, playerName: '轮回老师', playerRole: 'REAL', characterName: '轮回老师', isReady: true},
    {playerId: 1353, playerName: '凝时', playerRole: 'AI', characterName: '凝时', isReady: true},
    {playerId: 1354, playerName: '溯光医生', playerRole: 'AI', characterName: '溯光医生', isReady: true},
    {playerId: 1355, playerName: '断流保安', playerRole: 'REAL', characterName: '断流保安', isReady: true},
    {playerId: 1356, playerName: '瞬影', playerRole: 'AI', characterName: '瞬影', isReady: true},
    {playerId: 1357, playerName: '隙月', playerRole: 'AI', characterName: '隙月', isReady: true},
    {playerId: 1358, playerName: '玩家1358', playerRole: 'REAL', characterName: '玩家1358', isReady: true},
]

// =============================================================================
// 主要组件
// =============================================================================

/**
 * SummaryPreview 页面组件
 * @description 用于独立预览 Summary 组件的调试页面
 * @returns {JSX.Element} 预览页面
 */
function SummaryPreview() {
    const navigate = useNavigate()

    /**
     * 处理动作回调
     * @param {string} action - 动作类型
     */
    const handleAction = (action) => {
        switch (action) {
            case 'return_home':
                navigate('/')
                break
            case 'play_again':
                window.location.reload()
                break
            default:
                console.log('[SummaryPreview] Unknown action:', action)
        }
    }

    return (
        <div className="h-screen w-screen bg-[#EEF1F6] dark:bg-[#1A1D26] overflow-hidden">
            {/* 调试信息横幅 */}
            <div className="absolute top-0 left-0 right-0 z-50 bg-gradient-to-r from-[#7C8CD6] to-[#A78BFA] text-white px-4 py-2 text-sm flex items-center justify-between">
                <div className="flex items-center gap-2">
                    <span className="font-bold">🎮 Summary Preview Mode</span>
                    <span className="text-white/70">|</span>
                    <span className="text-white/90">真相揭晓预览 - 使用 Mock 数据</span>
                </div>
                <div className="flex items-center gap-3">
                    <span className="text-xs text-white/70">路径: /summary-preview</span>
                    <button
                        onClick={() => navigate('/')}
                        className="px-3 py-1 bg-white/20 hover:bg-white/30 rounded text-xs transition-colors"
                    >
                        返回首页
                    </button>
                </div>
            </div>

            {/* Summary 组件容器 */}
            <div className="h-full pt-10">
                <Summary
                    _config={{}}
                    gameData={MOCK_GAME_DATA}
                    currentPlayerId={1355}  // 模拟当前玩家是断流保安（得分最高的真人玩家）
                    playerData={MOCK_PLAYER_DATA}
                    onAction={handleAction}
                    isObserverMode={false}
                />
            </div>
        </div>
    )
}

SummaryPreview.displayName = 'SummaryPreview'

export default memo(SummaryPreview)
