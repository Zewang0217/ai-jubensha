package org.jubensha.aijubenshabackend.models.enums;

public enum GameStatus {
    /* 创建 */
    CREATED,

    /* 开始 */
    STARTED,

    /* 暂停 */
    PAUSED,

    /* 结束 */
    ENDED,

    /* 取消 */
    CANCELED;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}