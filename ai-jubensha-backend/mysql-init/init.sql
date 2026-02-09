-- AI剧本杀项目初始化SQL

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS ai_jubensha CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_jubensha;

-- 1. 剧本表
CREATE TABLE IF NOT EXISTS scripts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL COMMENT '剧本名称',
    description TEXT COMMENT '剧本描述',
    author VARCHAR(100) COMMENT '作者',
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM' COMMENT '难度等级',
    duration INT COMMENT '预计时长（分钟）',
    player_count INT COMMENT '玩家数量',
    cover_image VARCHAR(255) COMMENT '封面图片',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 角色表
CREATE TABLE IF NOT EXISTS characters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    script_id BIGINT NOT NULL COMMENT '所属剧本ID',
    name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description TEXT COMMENT '角色描述',
    background_story TEXT COMMENT '背景故事',
    secret TEXT COMMENT '角色秘密',
    avatar VARCHAR(255) COMMENT '角色头像',
    is_ai BOOLEAN DEFAULT FALSE COMMENT '是否为AI角色',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (script_id) REFERENCES scripts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 玩家表
CREATE TABLE IF NOT EXISTS players (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    nickname VARCHAR(100) NOT NULL COMMENT '昵称',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(255) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像',
    role VARCHAR(30) DEFAULT 'USER' COMMENT '角色',
    status ENUM('ONLINE', 'OFFLINE') DEFAULT 'OFFLINE' COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 游戏表
CREATE TABLE IF NOT EXISTS games (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    script_id BIGINT NOT NULL COMMENT '剧本ID',
    game_code VARCHAR(20) NOT NULL UNIQUE COMMENT '游戏码',
    status ENUM('CREATED', 'STARTED', 'PAUSED', 'ENDED') DEFAULT 'CREATED' COMMENT '游戏状态',
    start_time TIMESTAMP COMMENT '开始时间',
    end_time TIMESTAMP COMMENT '结束时间',
    current_phase ENUM('INTRODUCTION', 'SEARCH', 'DISCUSSION', 'VOTING', 'ENDING') DEFAULT 'INTRODUCTION' COMMENT '当前阶段',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (script_id) REFERENCES scripts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 游戏玩家表
CREATE TABLE IF NOT EXISTS game_players (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_id BIGINT NOT NULL COMMENT '游戏ID',
    player_id BIGINT NOT NULL COMMENT '玩家ID',
    character_id BIGINT NOT NULL COMMENT '角色ID',
    is_dm BOOLEAN DEFAULT FALSE COMMENT '是否为DM',
    status ENUM('READY', 'PLAYING', 'LEFT') DEFAULT 'READY' COMMENT '状态',
    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 线索表
CREATE TABLE IF NOT EXISTS clues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    script_id BIGINT NOT NULL COMMENT '所属剧本ID',
    name VARCHAR(255) NOT NULL COMMENT '线索名称',
    description TEXT NOT NULL COMMENT '线索描述',
    type ENUM('PHYSICAL', 'TESTIMONY', 'DOCUMENT', 'OTHER') DEFAULT 'OTHER' COMMENT '线索类型',
    visibility ENUM('PUBLIC', 'DISCOVERED', 'PRIVATE') DEFAULT 'DISCOVERED' COMMENT '可见性',
    scene VARCHAR(100) COMMENT '发现场景',
    importance INT DEFAULT 5 COMMENT '重要性（1-10）',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (script_id) REFERENCES scripts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 玩家线索表（记录每个玩家已发现的线索）
CREATE TABLE IF NOT EXISTS player_clues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_id BIGINT NOT NULL COMMENT '游戏ID',
    player_id BIGINT NOT NULL COMMENT '玩家ID',
    clue_id BIGINT NOT NULL COMMENT '线索ID',
    discovered_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发现时间',
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    FOREIGN KEY (clue_id) REFERENCES clues(id) ON DELETE CASCADE,
    UNIQUE KEY uk_game_player_clue (game_id, player_id, clue_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 对话表
CREATE TABLE IF NOT EXISTS dialogues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_id BIGINT NOT NULL COMMENT '游戏ID',
    player_id BIGINT NOT NULL COMMENT '发言玩家ID',
    character_id BIGINT NOT NULL COMMENT '发言角色ID',
    content TEXT NOT NULL COMMENT '对话内容',
    type ENUM('CHAT', 'ACTION', 'SYSTEM') DEFAULT 'CHAT' COMMENT '对话类型',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发言时间',
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. 内心独白表
CREATE TABLE IF NOT EXISTS inner_thoughts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_id BIGINT NOT NULL COMMENT '游戏ID',
    player_id BIGINT NOT NULL COMMENT '玩家ID',
    character_id BIGINT NOT NULL COMMENT '角色ID',
    content TEXT NOT NULL COMMENT '内心独白内容',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. 投票表
CREATE TABLE IF NOT EXISTS votes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_id BIGINT NOT NULL COMMENT '游戏ID',
    voter_id BIGINT NOT NULL COMMENT '投票玩家ID',
    suspect_id BIGINT NOT NULL COMMENT '被投票角色ID',
    phase INT COMMENT '投票轮次',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (voter_id) REFERENCES players(id) ON DELETE CASCADE,
    FOREIGN KEY (suspect_id) REFERENCES characters(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. 场景表
CREATE TABLE IF NOT EXISTS scenes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    script_id BIGINT NOT NULL COMMENT '所属剧本ID',
    name VARCHAR(100) NOT NULL COMMENT '场景名称',
    description TEXT NOT NULL COMMENT '场景描述',
    image VARCHAR(255) COMMENT '场景图片',
    available_actions TEXT COMMENT '可用动作',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (script_id) REFERENCES scripts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. 场景线索关联表
CREATE TABLE IF NOT EXISTS scene_clues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene_id BIGINT NOT NULL COMMENT '场景ID',
    clue_id BIGINT NOT NULL COMMENT '线索ID',
    discovery_condition TEXT COMMENT '发现条件',
    FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE CASCADE,
    FOREIGN KEY (clue_id) REFERENCES clues(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. 线索关联表
CREATE TABLE IF NOT EXISTS clue_relations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    clue_id1 BIGINT NOT NULL COMMENT '线索1 ID',
    clue_id2 BIGINT NOT NULL COMMENT '线索2 ID',
    strength INT DEFAULT 5 COMMENT '关联强度（1-10）',
    description TEXT COMMENT '关联描述',
    FOREIGN KEY (clue_id1) REFERENCES clues(id) ON DELETE CASCADE,
    FOREIGN KEY (clue_id2) REFERENCES clues(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 14. 系统设置表
CREATE TABLE IF NOT EXISTS system_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    key_name VARCHAR(100) NOT NULL UNIQUE COMMENT '设置键',
    value VARCHAR(255) NOT NULL COMMENT '设置值',
    description TEXT COMMENT '描述',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入初始系统设置
INSERT IGNORE INTO system_settings (key_name, value, description) VALUES
('SYSTEM_VERSION', '1.0.0', '系统版本'),
('MAX_PLAYERS_PER_GAME', '8', '每局游戏最大玩家数'),
('DEFAULT_GAME_DURATION', '120', '默认游戏时长（分钟）'),
('SEARCH_PHASE_DURATION', '30', '搜证阶段时长（分钟）'),
('DISCUSSION_PHASE_DURATION', '60', '讨论阶段时长（分钟）');

-- 插入示例剧本
INSERT IGNORE INTO scripts (name, description, author, difficulty, duration, player_count) VALUES
('神秘的书房', '一个富商在自己的书房中被发现死亡，所有在场的人都有嫌疑。', '系统', 'MEDIUM', 120, 6),
('校园谜案', '校园里发生了一起离奇的失踪案，学生们需要找出真相。', '系统', 'EASY', 90, 4);

-- 插入示例场景
INSERT IGNORE INTO scenes (script_id, name, description) VALUES
(1, '书房', '一个豪华的书房，书架上摆满了各种书籍，书桌上有一台笔记本电脑，角落里有一个保险箱。'),
(1, '客厅', '宽敞的客厅，摆放着舒适的沙发和茶几，墙上挂着一幅昂贵的油画。'),
(1, '花园', '美丽的花园，种满了各种鲜花，角落里有一个小亭子。'),
(2, '教室', '整洁的教室，摆放着课桌和椅子，黑板上还留着上次课的内容。'),
(2, '图书馆', '安静的图书馆，书架上摆满了各种书籍，有几个学生在学习。'),
(2, '操场', '宽阔的操场，有跑道和足球场，一些学生在运动。');

-- 插入示例线索
INSERT IGNORE INTO clues (script_id, name, description, type, scene, importance) VALUES
(1, '研究报告', '一份未完成的研究报告，报告中提到了一种新型毒药。', 'DOCUMENT', '书房', 8),
(1, '保险箱划痕', '保险箱上有一些划痕，似乎是尝试输入密码时留下的。', 'PHYSICAL', '书房', 5),
(1, '油画背面', '油画背面发现了一张纸条，上面写着一串数字。', 'PHYSICAL', '客厅', 7),
(1, '花园脚印', '花园里发现了一串脚印，通向小亭子。', 'PHYSICAL', '花园', 4),
(2, '学生证', '一张学生证，上面有失踪学生的照片和信息。', 'PHYSICAL', '教室', 9),
(2, '借书记录', '图书馆的借书记录显示，失踪学生最近借了一本关于密码学的书。', 'DOCUMENT', '图书馆', 6),
(2, '运动服', '操场上发现了一件运动服，上面有一些污渍。', 'PHYSICAL', '操场', 5);

-- 插入场景线索关联
INSERT IGNORE INTO scene_clues (scene_id, clue_id) VALUES
(1, 1),
(1, 2),
(2, 3),
(3, 4),
(4, 5),
(5, 6),
(6, 7);

-- 插入线索关联
INSERT IGNORE INTO clue_relations (clue_id1, clue_id2, strength, description) VALUES
(1, 2, 7, '研究报告中提到的毒药可能与保险箱有关'),
(2, 3, 6, '保险箱的密码可能与油画背面的数字有关'),
(5, 6, 8, '失踪学生的学生证和借书记录可能有关联'),
(6, 7, 5, '密码学书籍可能与运动服上的污渍有关');

COMMIT;
