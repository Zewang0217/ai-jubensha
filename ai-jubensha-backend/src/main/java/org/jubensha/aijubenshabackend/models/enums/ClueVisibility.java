package org.jubensha.aijubenshabackend.models.enums;

/**
 * 线索可见性, 所谓:
 * 1. 公开线索
 * 2. 个人线索：玩家可自主选择公开 / 隐藏，或用于玩家间交换线索
 * 3. 深入线索 or 触发式线索：玩家在游戏过程中，根据线索的获取条件，可以查看该线索
 */
public enum ClueVisibility {
    PUBLIC,
    PRIVATE,
    DISCOVERED
}