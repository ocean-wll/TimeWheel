package pers.ocean.timewheel.error.timer;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description 定时器实现
 * @Author ocean_wll
 * @Date 2021/10/9 11:32 上午
 */
@Slf4j
public class SystemTimer implements Timer {

    /**
     * 底层时间轮
     */
    private TimeWheel timeWheel;

    /**
     * 一个Timer只有一个延时队列
     */
    private final DelayQueue<TimerTaskList> delayQueue = new DelayQueue<>();

    /**
     * 过期任务执行线程
     */
    private final ExecutorService workerThreadPool;

    /**
     * 轮询delayQueue获取过期任务线程
     */
    private final ExecutorService bossThreadPool;

    public SystemTimer() {
        this.timeWheel = new TimeWheel(1L, 20, System.currentTimeMillis(), delayQueue);
        this.workerThreadPool = Executors.newFixedThreadPool(100);
        this.bossThreadPool = Executors.newFixedThreadPool(1);
        this.bossThreadPool.submit(() -> {
            // 20ms推动一次时间轮运转
            while (true) {
                this.advanceClock(20L);
            }
        });
    }

    /**
     * 增加任务
     *
     * @param entry 任务实体
     */
    private void addTimerTaskEntry(TimerTaskEntry entry) {
        if (!timeWheel.add(entry)) {
            // 已经过期
            TimerTask timerTask = entry.getTimerTask();
            log.info("-----任务:{} 已到期，准备执行-----", timerTask.getDesc());
            workerThreadPool.submit(timerTask);
        }
    }

    @Override
    public void add(TimerTask timerTask) {
        log.info("-----添加任务开始-----task:{}", timerTask.getDesc());
        TimerTaskEntry entry = new TimerTaskEntry(timerTask, timerTask.getDelayMs() + System.currentTimeMillis());
        timerTask.setTimerTaskEntry(entry);
        addTimerTaskEntry(entry);
    }

    @Override
    public synchronized void advanceClock(Long timeout) {
        try {
            TimerTaskList bucket = delayQueue.poll(timeout, TimeUnit.MILLISECONDS);
            if (bucket != null) {
                // 推进时间
                timeWheel.advanceLock(bucket.getExpiration());
                // 执行过期任务(包含降级)
                bucket.clear(this::addTimerTaskEntry);
            }
        } catch (InterruptedException e) {
            log.error("advanceClock error");
        }
    }

    @Override
    public Integer size() {
        // todo ocean_wll 这里的size不准确
        return delayQueue.size();
    }

    @Override
    public void shutdown() {
        this.bossThreadPool.shutdown();
        this.workerThreadPool.shutdown();
        this.timeWheel = null;
    }
}
