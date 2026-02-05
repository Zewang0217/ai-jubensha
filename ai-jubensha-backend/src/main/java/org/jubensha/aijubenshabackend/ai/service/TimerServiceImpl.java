package org.jubensha.aijubenshabackend.ai.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 时间管理服务实现
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-05 16:30
 * @since 2026
 */
@Slf4j
@Service
public class TimerServiceImpl implements TimerService {

    // 线程池
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

    // 计时器状态
    private final Map<String, TimerInfo> timerInfos = new ConcurrentHashMap<>();

    @Override
    public void startTimer(String phase, Long duration, Runnable callback) {
        log.info("启动计时器，阶段: {}, 持续时间: {}秒", phase, duration);

        // 取消之前的计时器
        cancelTimer(phase);

        // 创建计时器信息
        TimerInfo timerInfo = new TimerInfo();
        timerInfo.setPhase(phase);
        timerInfo.setDuration(duration);
        timerInfo.setStartTime(System.currentTimeMillis());
        timerInfo.setRemainingTime(duration);
        timerInfo.setRunning(true);

        // 调度任务
        ScheduledFuture<?> future = executorService.schedule(() -> {
            try {
                log.info("计时器结束，阶段: {}", phase);
                callback.run();
            } catch (Exception e) {
                log.error("计时器回调执行失败: {}", e.getMessage(), e);
            } finally {
                timerInfos.remove(phase);
            }
        }, duration, TimeUnit.SECONDS);

        timerInfo.setFuture(future);
        timerInfos.put(phase, timerInfo);

        // 启动定时器线程，更新剩余时间
        executorService.scheduleAtFixedRate(() -> {
            TimerInfo info = timerInfos.get(phase);
            if (info != null && info.isRunning()) {
                long elapsed = (System.currentTimeMillis() - info.getStartTime()) / 1000;
                long remaining = Math.max(0, info.getDuration() - elapsed);
                info.setRemainingTime(remaining);

                // 发送时间提醒
                if (remaining <= 60 && remaining > 0 && remaining % 10 == 0) {
                    log.info("时间提醒，阶段: {}, 剩余时间: {}秒", phase, remaining);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void cancelTimer(String phase) {
        TimerInfo timerInfo = timerInfos.get(phase);
        if (timerInfo != null) {
            log.info("取消计时器，阶段: {}", phase);
            ScheduledFuture<?> future = timerInfo.getFuture();
            if (future != null && !future.isCancelled() && !future.isDone()) {
                future.cancel(false);
            }
            timerInfos.remove(phase);
        }
    }

    @Override
    public long getRemainingTime(String phase) {
        TimerInfo timerInfo = timerInfos.get(phase);
        if (timerInfo != null && timerInfo.isRunning()) {
            return timerInfo.getRemainingTime();
        }
        return 0;
    }

    @Override
    public void pauseTimer(String phase) {
        TimerInfo timerInfo = timerInfos.get(phase);
        if (timerInfo != null && timerInfo.isRunning()) {
            log.info("暂停计时器，阶段: {}", phase);
            timerInfo.setRunning(false);
            ScheduledFuture<?> future = timerInfo.getFuture();
            if (future != null && !future.isCancelled() && !future.isDone()) {
                future.cancel(false);
            }
            // 计算剩余时间
            long elapsed = (System.currentTimeMillis() - timerInfo.getStartTime()) / 1000;
            long remaining = Math.max(0, timerInfo.getDuration() - elapsed);
            timerInfo.setRemainingTime(remaining);
        }
    }

    @Override
    public void resumeTimer(String phase) {
        TimerInfo timerInfo = timerInfos.get(phase);
        if (timerInfo != null && !timerInfo.isRunning()) {
            long remaining = timerInfo.getRemainingTime();
            if (remaining > 0) {
                log.info("恢复计时器，阶段: {}, 剩余时间: {}秒", phase, remaining);
                // 重新启动计时器
                startTimer(phase, remaining, () -> {
                    log.info("计时器结束，阶段: {}", phase);
                    timerInfos.remove(phase);
                });
            }
        }
    }

    /**
     * 计时器信息
     */
    private static class TimerInfo {
        private String phase;
        private Long duration;
        private long startTime;
        private long remainingTime;
        private boolean running;
        private ScheduledFuture<?> future;

        public String getPhase() {
            return phase;
        }

        public void setPhase(String phase) {
            this.phase = phase;
        }

        public Long getDuration() {
            return duration;
        }

        public void setDuration(Long duration) {
            this.duration = duration;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getRemainingTime() {
            return remainingTime;
        }

        public void setRemainingTime(long remainingTime) {
            this.remainingTime = remainingTime;
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public ScheduledFuture<?> getFuture() {
            return future;
        }

        public void setFuture(ScheduledFuture<?> future) {
            this.future = future;
        }
    }
}
