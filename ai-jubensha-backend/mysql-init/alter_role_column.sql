-- 修改players表的role字段长度
USE ai_jubensha;

-- 查看当前表结构
DESCRIBE players;

-- 修改role字段长度为20
ALTER TABLE players MODIFY COLUMN role VARCHAR(20) DEFAULT 'USER' COMMENT '角色';

-- 再次查看表结构，确认修改成功
DESCRIBE players;