package pers.ocean.timewheel.timer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 任务包装类
 * @Author ocean_wll
 * @Date 2021/10/9 10:14 上午
 */
@Data
@Slf4j
public class TimerTask implements Runnable {

    /**
     * 延迟时间
     */
    private long delayMs;

    /**
     * 任务所在的entry
     */
    private TimerTaskEntry timerTaskEntry;

    /**
     * 描述
     */
    private String desc;

    public TimerTask(String desc, Long delayMs) {
        this.desc = desc;
        this.delayMs = delayMs;
        this.timerTaskEntry = null;
    }

    /**
     * 设置任务entry
     *
     * @param entry 实例对象
     */
    public synchronized void setTimerTaskEntry(TimerTaskEntry entry) {
        if (timerTaskEntry != null && timerTaskEntry != entry) {
            timerTaskEntry.remove();
        }
        timerTaskEntry = entry;
    }

    /**
     * 获取任务entry
     *
     * @return 实例对象
     */
    public TimerTaskEntry getTimerTaskEntry() {
        return this.timerTaskEntry;
    }

    @Override
    public void run() {
        log.info("--------{} 任务执行", desc);
    }
}
