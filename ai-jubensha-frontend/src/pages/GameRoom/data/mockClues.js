/**
 * 线索 Mock 数据
 * 用于调试讨论阶段的线索系统
 */

/**
 * 公开线索 - 所有人可见
 */
export const publicClues = [
    {
        id: 'clue-001',
        name: '带血的匕首',
        description: '在书房的书桌抽屉里发现的一把匕首，刀刃上有干涸的血迹。刀柄上刻着复杂的家族徽章，似乎是贵族世家的传家宝。',
        type: 'physical',
        discoveredBy: '侦探A',
        discoveredAt: '22:30',
        location: '书房',
    },
    {
        id: 'clue-002',
        name: '破碎的窗户',
        description: '客厅的大窗户玻璃被击碎，碎片散落在地毯上。从破碎的痕迹来看，似乎是从外部被打破的，但窗外没有脚印或其他痕迹。',
        type: 'physical',
        discoveredBy: '管家B',
        discoveredAt: '22:15',
        location: '客厅',
    },
    {
        id: 'clue-003',
        name: '女仆的证词',
        description: '女仆声称在晚上10点左右听到书房传来争吵声，声音一男一女，但听不清具体内容。大约10分钟后听到了一声巨响。',
        type: 'testimony',
        discoveredBy: '女仆C',
        discoveredAt: '22:45',
        location: '厨房',
    },
    {
        id: 'clue-004',
        name: '遗嘱文件',
        description: '在受害者保险箱中发现的新版遗嘱，签署日期是案发前一天。遗嘱内容显示大部分财产将捐赠给慈善机构，而不是留给家人。',
        type: 'document',
        discoveredBy: '律师D',
        discoveredAt: '23:00',
        location: '书房保险箱',
    },
    {
        id: 'clue-005',
        name: '神秘信笺',
        description: '一张没有署名的威胁信，用剪报拼凑而成。信上写着"你的罪行终将曝光，今晚就是你的末日"。',
        type: 'document',
        discoveredBy: '侦探A',
        discoveredAt: '22:50',
        location: '受害者口袋',
    },
]

/**
 * 私人线索 - 只有自己可见（搜证阶段发现）
 */
export const privateClues = [
    {
        id: 'private-001',
        name: '密室的钥匙',
        description: '在花园的假山后面发现了一把古老的钥匙，上面刻着奇怪的花纹。这把钥匙似乎可以打开宅邸中某个秘密房间。',
        type: 'physical',
        discoveredBy: '我',
        discoveredAt: '22:20',
        location: '花园假山',
    },
    {
        id: 'private-002',
        name: '管家的日记',
        description: '在管家的房间暗格里发现的日记本，记录了受害者对管家的长期虐待和羞辱。最近一页写着"我受够了，该结束了"。',
        type: 'document',
        discoveredBy: '我',
        discoveredAt: '22:40',
        location: '管家房间',
    },
    {
        id: 'private-003',
        name: '隐秘通道',
        description: '在书房的油画后面发现了一个隐藏的暗门，通往宅邸的地下密室。通道内有新鲜的脚印，显示有人最近使用过。',
        type: 'other',
        discoveredBy: '我',
        discoveredAt: '22:55',
        location: '书房暗门',
    },
    {
        id: 'private-004',
        name: '可疑的药瓶',
        description: '在受害者卧室的床头柜里发现了一瓶安眠药，但瓶中的药片数量与标签不符，少了将近一半。药瓶上有细微的指纹。',
        type: 'physical',
        discoveredBy: '我',
        discoveredAt: '23:05',
        location: '卧室',
    },
]

/**
 * 获取所有线索（公开 + 私人）
 */
export const getAllClues = () => ({
    public: publicClues,
    private: privateClues,
})

/**
 * 根据ID获取线索
 */
export const getClueById = (id) => {
    return [...publicClues, ...privateClues].find(clue => clue.id === id)
}

/**
 * 根据类型获取线索
 */
export const getCluesByType = (type) => {
    return [...publicClues, ...privateClues].filter(clue => clue.type === type)
}

export default {
    publicClues,
    privateClues,
    getAllClues,
    getClueById,
    getCluesByType,
}