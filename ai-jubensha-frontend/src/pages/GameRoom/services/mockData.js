/**
 * @fileoverview GameRoom 调试数据服务
 * @description 提供模拟数据用于前端离线调试，无需后端连接
 */

// =============================================================================
// 游戏基础数据
// =============================================================================

export const mockGameData = {
    id: '1',
    name: '迷雾山庄谋杀案',
    scriptId: 'script-001',
    scriptName: '迷雾山庄',
    scriptDescription: '一场突如其来的暴风雨将六位陌生人困在了偏僻的山庄中，而当夜，山庄主人被发现死在了书房...',
    status: 'playing',
    currentPlayers: 4,
    maxPlayers: 6,
    createdAt: new Date().toISOString(),
}

// =============================================================================
// 玩家数据
// =============================================================================

export const mockPlayerData = {
    id: 'player-001',
    name: '侦探林',
    characterId: 'char-001',
    isHost: true,
}

// =============================================================================
// 角色数据
// =============================================================================

export const mockCharacters = [
    {
        id: 'char-001',
        name: '林侦探',
        description: '一位经验丰富的侦探，受邀来到山庄调查一起旧案。',
        background: '曾在警队服役十年，退役后成为自由侦探。以敏锐的观察力和推理能力闻名。',
        goal: '找出真凶，揭开山庄的秘密。',
        secret: '你其实与山庄主人有旧怨，但你发誓与此案无关。',
        isPlayer: true,
        avatar: '',
    },
    {
        id: 'char-002',
        name: '苏医生',
        description: '山庄的家庭医生，性格温和。',
        background: '在山庄服务多年，与主人关系密切。',
        goal: '保护自己的病人隐私。',
        secret: '知道主人患有什么疾病，有机会接触毒药。',
        isPlayer: false,
        avatar: '',
    },
    {
        id: 'char-003',
        name: '陈管家',
        description: '山庄的管家，一丝不苟。',
        background: '管理山庄日常事务十五年，知道所有秘密通道。',
        goal: '维护山庄的秩序。',
        secret: '知道山庄的密室位置，当晚曾进入书房。',
        isPlayer: false,
        avatar: '',
    },
    {
        id: 'char-004',
        name: '赵律师',
        description: '山庄主人的私人律师。',
        background: '负责处理山庄主人的法律事务多年。',
        goal: '确保遗嘱的执行。',
        secret: '知道遗嘱的内容，可能会因此受益。',
        isPlayer: false,
        avatar: '',
    },
]

// =============================================================================
// 剧本章节数据
// =============================================================================

export const mockScriptChapters = [
    {
        id: 'chapter-001',
        title: '序章：暴风雨夜',
        time: '第一幕',
        location: '山庄大厅',
        content: `
      <p>暴风雨肆虐的夜晚，六位素不相识的客人齐聚迷雾山庄。山庄主人秦先生热情地招待了大家，然而每个人心中都有自己的秘密。</p>
      <br/>
      <p>晚餐后，秦先生邀请大家到书房品茶。他神秘地宣布，今晚将有一件重要的事情要公布。然而，就在他准备开口的那一刻，灯光突然熄灭...</p>
      <br/>
      <p>当灯光再次亮起时，秦先生已经倒在了血泊中。</p>
    `,
    },
    {
        id: 'chapter-002',
        title: '第一章：你是谁',
        time: '角色背景',
        location: '各角色回忆',
        content: `
      <p>作为受邀的客人之一，你在这个风雨交加的夜晚来到了迷雾山庄。每位客人都有自己不为人知的故事...</p>
      <br/>
      <p>你的任务是在这场死亡游戏中存活下来，找出真凶。但要小心，每个人都可能是凶手，包括你自己。</p>
      <br/>
      <p>记住：不要相信任何人，但也需要与他人合作才能获得线索。</p>
    `,
    },
    {
        id: 'chapter-003',
        title: '第二章：线索浮现',
        time: '事件经过',
        location: '案发现场',
        content: `
      <p>案发现场在书房。秦先生倒在书桌旁，胸口插着一把古董匕首。书房的窗户紧闭，门也从内部锁上了。</p>
      <br/>
      <p>这是一个经典的密室杀人案件。凶手是如何在黑暗中行凶后离开现场的？</p>
      <br/>
      <p>你需要仔细搜查每一个房间，寻找可能被遗漏的线索。</p>
    `,
    },
    {
        id: 'chapter-004',
        title: '幕间：任务提示',
        time: '游戏指引',
        location: '系统提示',
        content: `
      <p><strong>你的主要任务：</strong></p>
      <ul style="margin-left: 1.5rem; margin-top: 0.5rem;">
        <li>搜集线索，还原案发经过</li>
        <li>与其他玩家交流，交换信息</li>
        <li>找出真凶，但不要暴露自己的秘密</li>
      </ul>
      <br/>
      <p><strong>游戏提示：</strong></p>
      <ul style="margin-left: 1.5rem; margin-top: 0.5rem;">
        <li>每个场景都有隐藏的线索</li>
        <li>注意观察其他玩家的言行</li>
        <li>适时分享或隐瞒信息</li>
      </ul>
    `,
    },
]

// =============================================================================
// 场景数据
// =============================================================================

export const mockScenes = [
    {
        id: 'scene-001',
        name: '书房',
        description: '案发现场，秦先生被发现的地方。房间内有书桌、书架和一张沙发。',
        imageUrl: '',
        isLocked: false,
        clueCount: 3,
        clues: [
            {
                id: 'clue-001',
                name: '血迹匕首',
                description: '插在受害者胸口的古董匕首，刀柄上有奇怪的指纹。看起来是凶器。',
                type: '物证',
                imageUrl: '',
            },
            {
                id: 'clue-002',
                name: '破碎的茶杯',
                description: '书桌上的茶杯被打翻，茶水洒在地毯上，还有一些白色粉末。',
                type: '物证',
                imageUrl: '',
            },
            {
                id: 'clue-003',
                name: '未完成的遗嘱',
                description: '书桌上有一份未签字的遗嘱草稿，部分内容被涂抹。',
                type: '文件',
                imageUrl: '',
            },
        ],
    },
    {
        id: 'scene-002',
        name: '客厅',
        description: '客人们聚集的地方，有一个大壁炉和几张沙发。',
        imageUrl: '',
        isLocked: false,
        clueCount: 2,
        clues: [
            {
                id: 'clue-004',
                name: '停电记录',
                description: '管家记录了停电的确切时间：晚上8点15分，持续约5分钟。',
                type: '证词',
                imageUrl: '',
            },
            {
                id: 'clue-005',
                name: '窗户痕迹',
                description: '客厅的窗户有被撬过的痕迹，但窗外是悬崖，不可能从这里进出。',
                type: '物证',
                imageUrl: '',
            },
        ],
    },
    {
        id: 'scene-003',
        name: '厨房',
        description: '准备晚餐的地方，有各种厨具和储藏室。',
        imageUrl: '',
        isLocked: false,
        clueCount: 1,
        clues: [
            {
                id: 'clue-006',
                name: '毒药瓶',
                description: '垃圾桶里发现一个小药瓶，标签被撕掉了，残留少量白色粉末。',
                type: '物证',
                imageUrl: '',
            },
        ],
    },
    {
        id: 'scene-004',
        name: '地下室',
        description: '存放杂物的地方，有一个神秘的铁门。',
        imageUrl: '',
        isLocked: true,
        clueCount: 0,
        clues: [],
    },
    {
        id: 'scene-005',
        name: '卧室',
        description: '秦先生的私人卧室，可能藏有秘密。',
        imageUrl: '',
        isLocked: false,
        clueCount: 2,
        clues: [
            {
                id: 'clue-007',
                name: '日记本',
                description: '床头柜里发现一本日记，记录了最近与某人的争吵。',
                type: '文件',
                imageUrl: '',
            },
            {
                id: 'clue-008',
                name: '保险箱',
                description: '墙角有一个小型保险箱，需要密码才能打开。',
                type: '物证',
                imageUrl: '',
            },
        ],
    },
]

// =============================================================================
// 聊天消息数据
// =============================================================================

export const mockChatMessages = [
    {
        id: 'msg-001',
        sender: '系统',
        content: '游戏开始！请大家先阅读剧本，了解各自的角色。',
        time: '20:00',
        isAI: false,
        isSystem: true,
    },
    {
        id: 'msg-002',
        sender: '苏医生',
        content: '秦先生平时身体就不太好，我一直劝他要注意休息。',
        time: '20:05',
        isAI: true,
    },
    {
        id: 'msg-003',
        sender: '陈管家',
        content: '停电的时候我在厨房准备夜宵，听到书房有动静。',
        time: '20:08',
        isAI: true,
    },
    {
        id: 'msg-004',
        sender: '赵律师',
        content: '秦先生今天说要公布重要事情，会不会和遗嘱有关？',
        time: '20:10',
        isAI: true,
    },
]

// =============================================================================
// 游戏结果数据
// =============================================================================

export const mockGameResult = {
    isWin: true,
    correctVote: true,
    culprit: {
        id: 'char-002',
        name: '苏医生',
        role: '医生',
    },
    truth: `
    苏医生与秦先生有着不为人知的恩怨。五年前，秦先生的商业决策导致苏医生家破人亡，
    苏医生发誓要复仇。
    
    他以家庭医生的身份接近秦先生，获得了信任。案发当晚，他在茶中下了安眠药，
    然后在停电时用预先藏好的匕首杀害了秦先生。
    
    他利用对山庄的熟悉，伪造了密室杀人的假象，试图嫁祸给其他人。
  `,
    players: [
        {id: 'char-001', name: '林侦探', role: '侦探', isWinner: true, voteCorrect: true},
        {id: 'char-002', name: '苏医生', role: '医生', isWinner: false, isCulprit: true},
        {id: 'char-003', name: '陈管家', role: '管家', isWinner: false},
        {id: 'char-004', name: '赵律师', role: '律师', isWinner: false},
    ],
}

// =============================================================================
// 完整的游戏数据对象
// =============================================================================

export const getMockGameData = () => ({
    ...mockGameData,
    script: mockGameData,
    characters: mockCharacters,
    scriptChapters: mockScriptChapters,
    scenes: mockScenes,
    players: mockCharacters.map(c => ({
        id: c.id,
        name: c.name,
        role: c.name,
        isAI: !c.isPlayer,
    })),
    result: mockGameResult,
})

// =============================================================================
// 模拟 API 响应
// =============================================================================

export const mockApiResponses = {
    getGame: () => ({
        code: 200,
        data: getMockGameData(),
        message: 'success',
    }),

    getPlayer: () => ({
        code: 200,
        data: mockPlayerData,
        message: 'success',
    }),

    joinGame: () => ({
        code: 200,
        data: {success: true},
        message: '加入成功',
    }),

    leaveGame: () => ({
        code: 200,
        data: {success: true},
        message: '离开成功',
    }),
}

// =============================================================================
// 模拟 WebSocket 消息
// =============================================================================

export const mockWebSocketMessages = {
    // 阶段变更
    phaseChange: (phase) => ({
        type: 'PHASE_CHANGE',
        data: {phase, timestamp: Date.now()},
    }),

    // 游戏更新
    gameUpdate: (update) => ({
        type: 'GAME_UPDATE',
        data: update,
    }),

    // 聊天消息
    chatMessage: (message) => ({
        type: 'CHAT_MESSAGE',
        data: message,
    }),

    // 线索发现
    clueFound: (clue) => ({
        type: 'CLUE_FOUND',
        data: clue,
    }),

    // 投票更新
    voteUpdate: (votes) => ({
        type: 'VOTE_UPDATE',
        data: votes,
    }),
}

export default {
    mockGameData,
    mockPlayerData,
    mockCharacters,
    mockScriptChapters,
    mockScenes,
    mockChatMessages,
    mockGameResult,
    getMockGameData,
    mockApiResponses,
    mockWebSocketMessages,
}
