import {useEffect, useState} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
import {AnimatePresence, motion} from 'framer-motion'
import {gameApi} from '../../services/api'
import Loading from '../../components/common/Loading'
import {GAME_PHASE, GAME_PHASE_TEXT, WS_MESSAGE_TYPE} from '../../utils/constants'
import {useWebSocket} from '../../hooks/useWebSocket'
import {Bug} from 'lucide-react'

// 阶段组件导入
import PhaseIntroduction from './components/PhaseIntroduction'
import PhaseSearch from './components/PhaseSearch'
import PhaseDiscussion from './components/PhaseDiscussion'
import PhaseVoting from './components/PhaseVoting'
import PhaseEnding from './components/PhaseEnding'

// 可复用组件导入
import PhaseStepper from './components/PhaseStepper'
import ActionBar from './components/ActionBar'

/**
 * 阶段组件映射表 - 策略模式
 */
const PHASE_COMPONENTS = {
    [GAME_PHASE.INTRODUCTION]: PhaseIntroduction,
    [GAME_PHASE.SEARCH]: PhaseSearch,
    [GAME_PHASE.DISCUSSION]: PhaseDiscussion,
    [GAME_PHASE.VOTING]: PhaseVoting,
    [GAME_PHASE.ENDING]: PhaseEnding,
}

/**
 * 游戏房间主组件
 * 全屏布局，无滚动，使用阶段条展示游戏进度
 */
function GameRoom() {
    const {id} = useParams()
    const navigate = useNavigate()

    // 当前阶段状态（预留：后续从 API/WebSocket 获取）
    const [currentPhase, setCurrentPhase] = useState(GAME_PHASE.INTRODUCTION)

    // 阶段信息列表（预留：从 API 获取）
    const [phaseInfo, setPhaseInfo] = useState(null)

    // 调试面板状态
    const [showDebugPanel, setShowDebugPanel] = useState(false)

    // WebSocket 连接
    const {isConnected, sendMessage, onMessage, offMessage} = useWebSocket(`ws://localhost:8088/ws`)

    // 获取游戏数据
    const {data: game, isLoading, error} = useQuery({
        queryKey: ['game', id],
        queryFn: () => gameApi.getGame(id),
    })

    // 预留：从 API 获取阶段信息
    useEffect(() => {
        // TODO: 调用 API 获取阶段信息
        // const fetchPhaseInfo = async () => {
        //     const response = await gameApi.getPhaseInfo(id)
        //     setPhaseInfo(response.data)
        //     setCurrentPhase(response.data.currentPhase)
        // }
        // fetchPhaseInfo()
    }, [id])

    // WebSocket 消息监听
    useEffect(() => {
        // 监听阶段变化消息
        onMessage(WS_MESSAGE_TYPE.PHASE_CHANGE, (data) => {
            console.log('收到阶段变化消息:', data)
            setCurrentPhase(data.phase)
        })

        // 监听游戏更新消息
        onMessage(WS_MESSAGE_TYPE.GAME_UPDATE, (data) => {
            console.log('收到游戏更新消息:', data)
        })

        return () => {
            offMessage(WS_MESSAGE_TYPE.PHASE_CHANGE)
            offMessage(WS_MESSAGE_TYPE.GAME_UPDATE)
        }
    }, [onMessage, offMessage])

    // 发送消息
    const handleSendMessage = (message) => {
        if (isConnected) {
            sendMessage(message)
        }
    }

    // 调试：手动切换阶段
    const handleDebugPhaseChange = (phase) => {
        setCurrentPhase(phase)
        console.log(`[Debug] 切换到阶段: ${GAME_PHASE_TEXT[phase]}`)
    }

    // 处理操作按钮点击
    const handleAction = (actionId) => {
        switch (actionId) {
            case 'exit':
                if (confirm('确定要退出游戏吗？')) {
                    // TODO: 调用 API 退出游戏
                    navigate('/games')
                }
                break
            case 'script':
                console.log('查看剧本')
                break
            case 'players':
                console.log('查看玩家')
                break
            case 'chat':
                console.log('打开聊天')
                break
            case 'settings':
                console.log('打开设置')
                break
            default:
                console.log('未知操作:', actionId)
        }
    }

    if (isLoading) {
        return <Loading fullScreen text="加载游戏房间..."/>
    }

    if (error) {
        return (
            <div className="h-screen w-screen flex items-center justify-center bg-slate-900">
                <div className="text-center">
                    <h3 className="text-2xl font-bold text-slate-200 mb-2">加载失败</h3>
                    <p className="text-slate-500">无法获取游戏信息，请稍后重试</p>
                </div>
            </div>
        )
    }

    const gameData = game?.data
    const CurrentPhaseComponent = PHASE_COMPONENTS[currentPhase] || PhaseIntroduction

    return (
        <div className="h-screen w-screen bg-slate-900 flex flex-col overflow-hidden">
            {/* 背景装饰 */}
            <div className="fixed inset-0 pointer-events-none overflow-hidden">
                <div
                    className="absolute top-0 left-0 w-full h-full bg-[linear-gradient(rgba(30,41,59,0.8)_1px,transparent_1px),linear-gradient(90deg,rgba(30,41,59,0.8)_1px,transparent_1px)] bg-[size:60px_60px]"/>
                <div className="absolute top-20 right-20 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl"/>
                <div className="absolute bottom-20 left-20 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl"/>
            </div>

            {/* 顶部栏 */}
            <header
                className="relative z-10 flex-none px-6 py-4 border-b border-slate-800/50 bg-slate-900/80 backdrop-blur-xl">
                <div className="flex items-center justify-between">
                    {/* 左侧：游戏信息 */}
                    <div className="flex items-center gap-4">
                        <div
                            className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white font-bold">
                            {gameData?.name?.[0] || 'G'}
                        </div>
                        <div>
                            <h1 className="text-lg font-bold text-white">
                                {gameData?.name || `游戏房间 #${id}`}
                            </h1>
                            <p className="text-xs text-slate-300">
                                {gameData?.scriptName || '未选择剧本'} · {gameData?.currentPlayers || 0}/{gameData?.maxPlayers || 8}人
                            </p>
                        </div>
                    </div>

                    {/* 中间：阶段条 */}
                    <div className="flex-1 max-w-2xl mx-8">
                        <PhaseStepper
                            currentPhase={currentPhase}
                            phases={phaseInfo?.phases}
                        />
                    </div>

                    {/* 右侧：操作按钮 */}
                    <ActionBar onAction={handleAction}/>
                </div>
            </header>

            {/* 主内容区 */}
            <main className="relative z-10 flex-1 p-6 min-h-0">
                <AnimatePresence mode="wait">
                    <motion.div
                        key={currentPhase}
                        initial={{opacity: 0, y: 20}}
                        animate={{opacity: 1, y: 0}}
                        exit={{opacity: 0, y: -20}}
                        transition={{duration: 0.3}}
                        className="h-full glass rounded-2xl border border-slate-700/50 overflow-hidden"
                    >
                        <CurrentPhaseComponent
                            gameData={gameData}
                            sendMessage={handleSendMessage}
                        />
                    </motion.div>
                </AnimatePresence>
            </main>

            {/* 底部状态栏 */}
            <footer
                className="relative z-10 flex-none px-6 py-3 border-t border-slate-800/50 bg-slate-900/80 backdrop-blur-xl">
                <div className="flex items-center justify-between text-sm">
                    <div className="flex items-center gap-6">
                        {/* 当前阶段文本 */}
                        <div className="flex items-center gap-2">
                            <span className="text-slate-500">当前阶段:</span>
                            <span className="font-medium text-blue-400">
                                {GAME_PHASE_TEXT[currentPhase]}
                            </span>
                        </div>

                        {/* WebSocket 状态 */}
                        <div className="flex items-center gap-2">
                            <div
                                className={`w-2 h-2 rounded-full ${isConnected ? 'bg-emerald-500 animate-pulse' : 'bg-red-500'}`}/>
                            <span className={`${isConnected ? 'text-emerald-400' : 'text-red-400'}`}>
                                {isConnected ? '已连接' : '未连接'}
                            </span>
                        </div>
                    </div>

                    {/* 游戏状态 */}
                    <div className="flex items-center gap-2">
                        <span className="text-slate-500">游戏状态:</span>
                        <span className={`font-medium ${
                            gameData?.status === 'waiting' ? 'text-amber-400' :
                                gameData?.status === 'playing' ? 'text-emerald-400' :
                                    'text-slate-400'
                        }`}>
                            {gameData?.status === 'waiting' ? '等待中' :
                                gameData?.status === 'playing' ? '游戏中' : '已结束'}
                        </span>
                    </div>
                </div>
            </footer>

            {/* 调试面板 */}
            <motion.div
                initial={false}
                animate={{y: showDebugPanel ? 0 : 'calc(100% - 40px)'}}
                className="fixed bottom-0 right-6 z-50 w-80"
            >
                <div
                    className="bg-slate-800/95 backdrop-blur-xl border border-slate-700/50 rounded-t-xl shadow-2xl overflow-hidden">
                    {/* 调试面板头部 */}
                    <button
                        onClick={() => setShowDebugPanel(!showDebugPanel)}
                        className="w-full px-4 py-2 flex items-center justify-between bg-slate-700/50 hover:bg-slate-700/80 transition-colors"
                    >
                        <div className="flex items-center gap-2">
                            <Bug className="w-4 h-4 text-amber-400"/>
                            <span className="text-sm font-medium text-slate-200">阶段调试</span>
                        </div>
                        <motion.div
                            animate={{rotate: showDebugPanel ? 180 : 0}}
                            className="text-slate-400"
                        >
                            ▼
                        </motion.div>
                    </button>

                    {/* 调试面板内容 */}
                    <div className="p-4 space-y-3">
                        <p className="text-xs text-slate-500">点击按钮切换阶段：</p>
                        <div className="grid grid-cols-2 gap-2">
                            {Object.values(GAME_PHASE).map((phase) => (
                                <button
                                    key={phase}
                                    onClick={() => handleDebugPhaseChange(phase)}
                                    className={`px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${
                                        currentPhase === phase
                                            ? 'bg-blue-500 text-white shadow-lg shadow-blue-500/30'
                                            : 'bg-slate-700/50 text-slate-400 hover:bg-slate-700 hover:text-slate-200'
                                    }`}
                                >
                                    {GAME_PHASE_TEXT[phase]}
                                </button>
                            ))}
                        </div>
                        <p className="text-xs text-slate-600 text-center pt-2 border-t border-slate-700/50">
                            仅用于开发调试
                        </p>
                    </div>
                </div>
            </motion.div>
        </div>
    )
}

export default GameRoom