package org.jubensha.aijubenshabackend.ai.service;


/**
 * 时间管理服务接口
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-05 16:20
 * @since 2026
 */
public interface TimerService {

    /**
     * 启动计时器
     *
     * @param phase     阶段名称
     * @param duration  持续时间（秒）
     * @param callback  回调函数
     */
    void startTimer(String phase, Long duration, Runnable callback);

    /**
     * 取消计时器
     *
     * @param phase 阶段名称
     */
    void cancelTimer(String phase);

    /**
     * 获取剩余时间
     *
     * @param phase 阶段名称
     * @return 剩余时间（秒）
     */
    long getRemainingTime(String phase);

    /**
     * 暂停计时器
     *
     * @param phase 阶段名称
     */
    void pauseTimer(String phase);

    /**
     * 恢复计时器
     *
     * @param phase 阶段名称
     */
    void resumeTimer(String phase);
}
