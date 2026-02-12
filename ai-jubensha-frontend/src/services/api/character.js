import apiClient from './client'

/**
 * 角色 API 模块
 * 提供角色相关的所有 HTTP 请求接口
 */

// Mock 数据开关 - 开发环境使用
const USE_MOCK = true

// Mock 角色数据
const mockCharacters = [
    {
        id: 1,
        scriptId: 1,
        name: '柳如烟',
        description: '江南水乡戏班的花旦，母亲曾在黄泉客栈遭遇不测，来此调查真相。',
        backgroundStory: `我叫柳如烟，是江南水乡一个戏班的花旦。我的前半生，就像戏台上那些悲欢离合的折子戏，看似光鲜，内里却满是疮痍。我生在戏班，长在戏班，母亲曾是红极一时的青衣，却在最风光时被一个负心汉骗走了全部积蓄，郁郁而终。
母亲死后，我跟着戏班四处漂泊。十三岁登台，凭着一副好嗓子和母亲留下的几分姿色，渐渐有了些名气。可我忘不了母亲临终前抓着我的手，眼睛瞪得老大，反复念叨着："烟儿，记住……黄泉客栈……钱……"话没说完就断了气。黄泉客栈，成了我心头一根刺。
案发那天，暴雨如注。我们戏班原本要去邻镇唱堂会，却被这场雨困在了半路。车夫说前面有家客栈可避雨，我抬头一看匾额——"黄泉客栈"，心头猛地一颤。这就是母亲念叨的地方。`,
        secret: `1. 我母亲留下的遗物中，有一张当票，典当的物品是一对翡翠镯子，当铺的印章模糊可辨是"黄泉镇"。我怀疑母亲当年在黄泉客栈附近典当了最后的财物，而经手人可能就是钱掌柜。
2. 我这次随戏班出行，真正的目的地就是黄泉镇。我谎称去邻镇唱堂会，其实是想来查清母亲当年的遭遇。
3. 晚膳时钱掌柜碰到我的手，我袖中藏着一根磨尖的银簪。那一瞬间，我几乎要刺出去。但我忍住了，因为我要的不是他痛快地死，而是要他身败名裂。`,
        avatarUrl: null,
        createTime: '2026-02-12T22:04:46.87068',
    },
    {
        id: 2,
        scriptId: 1,
        name: '白薇',
        description: '上海《申报》的女记者，为调查母亲失踪案来到黄泉客栈。',
        backgroundStory: `我叫白薇，从上海来。别人都说我是个不要命的女记者，专往危险的地方钻。其实他们不懂，我只是想找到真相——就像我一直在找母亲失踪的真相一样。
我的前半生没什么好说的。父亲早逝，母亲在我七岁那年去了苏州探亲，就再也没回来。巡捕房的人说可能是遇上了土匪，可我不信。母亲临走前那晚，抱着我哭了很久，她说："薇薇，如果妈妈回不来，你就去上海，永远别再回这个镇子。"
我在上海租界长大，进了报馆。这次来江南，表面上是采风写游记，其实我是循着母亲当年最后那封信的邮戳找来的——那封信盖着"黄泉镇"的邮戳。`,
        secret: `1. 我来黄泉镇的真实目的，是调查母亲二十年前的失踪案。母亲最后来信提到的"黄泉客栈掌柜"，很可能就是钱掌柜。
2. 我随身携带了母亲的照片和那封信。晚饭时我故意把照片"不小心"掉在地上，钱掌柜捡起来时脸色瞬间惨白——他认出了我母亲。
3. 案发前，我曾偷偷潜入账房，在沈墨的抽屉里发现了一本旧账本，里面夹着一张泛黄的女人照片。`,
        avatarUrl: null,
        createTime: '2026-02-12T22:04:46.890546',
    },
    {
        id: 3,
        scriptId: 1,
        name: '陈老九',
        description: '走江湖的郎中，与钱掌柜有不可告人的交易。',
        backgroundStory: `我叫陈老九，是个走江湖的郎中。这年头兵荒马乱的，能混口饭吃不容易。我爹就是个赤脚医生，小时候跟着他走村串巷，看尽了人间冷暖。十二岁那年，爹染了瘟疫走了，我就接过他那套行头，开始了江湖生涯。
这些年我见过太多生死，也学会了见人说人话、见鬼说鬼话的本事。药箱里除了草药，还常备着几瓶用井水兑的"神仙水"，专治那些想求个心理安慰的愚夫愚妇。`,
        secret: `1. 我药箱最底层藏着一本真正的古医书，记载着几种失传的毒药配方，其中一种的症状与钱掌柜的死状有相似之处。
2. 钱掌柜死前三天曾托我配一种剧毒鼠药，说客栈闹鼠患。我配好了，但一直犹豫要不要给他——因为那剂量足够毒死一个人。
3. 我其实认得那把青铜钥匙——三年前第一次来客栈时，曾在钱掌柜的账本里见过它的草图。`,
        avatarUrl: null,
        createTime: '2026-02-12T22:04:46.903172',
    },
    {
        id: 4,
        scriptId: 1,
        name: '钱掌柜',
        description: '黄泉客栈的掌柜，一个充满秘密和罪恶的男人。',
        backgroundStory: `我叫钱有财，是这黄泉客栈的掌柜。我这一生啊，就像这客栈的名字一样，半截身子已经埋进了黄泉。
我生在光绪年间，家里穷得叮当响。十二岁那年，爹娘把我送到镇上的绸缎庄当学徒，掌柜的姓沈，是个斯文人。沈掌柜待我不薄，教我识字算账，我把他当再生父母。`,
        secret: `1. 我本名钱二狗，钱有财是后来改的。沈家那口井……其实沈夫人投井那晚，我就在井边。
2. 黄泉客栈的天字一号房，墙里真的埋着东西——不是金银，是沈夫人的一支银簪和一本染血的《牡丹亭》唱本。
3. 我怀疑沈墨是沈家的后人。三年前他来的那天，也是月圆。他腰间挂着一枚玉佩，那成色、那雕工……和沈掌柜当年随身戴的那块，一模一样。`,
        avatarUrl: null,
        createTime: '2026-02-12T22:04:46.920306',
    },
    {
        id: 5,
        scriptId: 1,
        name: '赵四海',
        description: '古董商人，与钱掌柜有二十年前的血债。',
        backgroundStory: `我叫赵四海，在古董行当里摸爬滚打了二十年。我爹是个当铺朝奉，从小我就跟着他在那些散发着霉味的旧物堆里打转。他常说："四海啊，这世上最值钱的不是金子，是人心里的贪念。"
我十二岁那年，爹收了一件青铜酒爵，说是战国的东西。没过三天，家里就闯进来几个黑衣人，把我爹拖到后院活活打死了。`,
        secret: `1. 我认出钱掌柜就是二十年前带人打死我爹的凶手，虎口的黑痣和记忆完全吻合。
2. 我今晚来客栈的真正目的是购买钱掌柜手中的战国青铜钥匙，交易价是五百大洋，已随身携带。
3. 我原本计划在交易完成后，用掺了砒霜的茶水毒死钱掌柜，既报仇又不用付钱。`,
        avatarUrl: null,
        createTime: '2026-02-12T22:04:46.933139',
    },
    {
        id: 6,
        scriptId: 1,
        name: '沈墨',
        description: '黄泉客栈的账房先生，沈家后人，隐忍二十年只为复仇。',
        backgroundStory: `我叫沈墨，这个名字是我自己取的。墨色最浓时，能掩盖一切痕迹，就像我这些年的人生。
我真正的名字，应该随那个男人姓钱。但我宁愿自己从未出生。母亲死的那年我七岁，她躺在破庙的草席上，咳出的血染红了胸前那枚青铜钥匙——那是沈家祖传的库房钥匙。`,
        secret: `1. 我是钱掌柜的亲生儿子，本名钱继祖（但我憎恶这个名字）。
2. 母亲临终前告诉我，钱掌柜为夺家产，在外公的茶里下了慢性毒药，制造了"失足落水"的假象。
3. 我花了三年时间，偷偷复制了客栈所有地契和房契的原件，藏在客栈地窖的暗格里——那些本都该姓沈。
4. 墙上的血字是我三天前就写好的，用的朱砂混鱼血。`,
        avatarUrl: null,
        createTime: '2026-02-12T22:04:46.946141',
    },
]

/**
 * @typedef {Object} ListCharacterResponseDTO
 * @property {number} id - 角色ID
 * @property {number} scriptId - 剧本ID
 * @property {string} name - 角色名称
 * @property {string} description - 角色描述
 * @property {string} backgroundStory - 背景故事
 * @property {string} secret - 角色秘密
 * @property {string} avatarUrl - 头像URL
 * @property {string} createTime - 创建时间
 */

/**
 * 根据剧本ID查询角色列表
 * @param {number} scriptId - 剧本ID
 * @returns {Promise<ListCharacterResponseDTO[]>} 角色列表
 */
export const getCharactersByScriptId = (scriptId) => {
    if (USE_MOCK) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const characters = mockCharacters.filter(
                    (c) => c.scriptId === Number(scriptId)
                )
                resolve(characters)
            }, 600)
        })
    }
    return apiClient.get(`/characters/script/${scriptId}`)
}

/**
 * 根据角色ID查询角色详情
 * @param {number} characterId - 角色ID
 * @returns {Promise<ListCharacterResponseDTO>} 角色详情
 */
export const getCharacterById = (characterId) => {
    if (USE_MOCK) {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const character = mockCharacters.find((c) => c.id === Number(characterId))
                if (!character) {
                    reject(new Error('角色不存在'))
                } else {
                    resolve(character)
                }
            }, 400)
        })
    }
    return apiClient.get(`/characters/${characterId}`)
}

/**
 * 创建角色
 * @param {Object} data - 角色数据
 * @returns {Promise<ListCharacterResponseDTO>} 创建的角色
 */
export const createCharacter = (data) => apiClient.post('/characters', data)

/**
 * 更新角色
 * @param {number} characterId - 角色ID
 * @param {Object} data - 角色数据
 * @returns {Promise<ListCharacterResponseDTO>} 更新后的角色
 */
export const updateCharacter = (characterId, data) =>
    apiClient.put(`/characters/${characterId}`, data)

/**
 * 删除角色
 * @param {number} characterId - 角色ID
 * @returns {Promise<void>}
 */
export const deleteCharacter = (characterId) =>
    apiClient.delete(`/characters/${characterId}`)