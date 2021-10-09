package pers.ocean.timewheel.error.timer;

import java.util.concurrent.DelayQueue;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description 时间轮主数据结构
 * @Author ocean_wll
 * @Date 2021/10/9 11:12 上午
 */
@Slf4j
public class TimeWheel {

    /**
     * 一个槽的时间间隔(时间轮最小刻度)
     */
    private final Long tickMs;

    /**
     * 时间轮大小(槽的个数)
     */
    private final Integer wheelSize;

    /**
     * 一轮的时间跨度
     */
    private final Long interval;

    /**
     * 当前时间
     */
    private Long currentTime;

    /**
     * 槽
     */
    private final TimerTaskList[] buckets;

    /**
     * 上层时间轮
     */
    private volatile TimeWheel overflowWheel;

    /**
     * 延迟队列，一个timer只有一个delayQueue
     */
    private final DelayQueue<TimerTaskList> delayQueue;

    public TimeWheel(Long tickMs, Integer wheelSize, Long currentTime, DelayQueue<TimerTaskList> delayQueue) {
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.interval = tickMs * wheelSize;
        this.buckets = new TimerTaskList[wheelSize];
        this.currentTime = currentTime - (currentTime % tickMs);
        this.delayQueue = delayQueue;
        for (int i = 0; i < wheelSize; i++) {
            buckets[i] = new TimerTaskList();
        }
    }

    /**
     * 增加任务
     *
     * @param entry 任务包装类
     * @return true or false
     */
    public boolean add(TimerTaskEntry entry) {
        Long expiration = entry.getExpireMs();
        if (expiration < tickMs + currentTime) {
            // 到期了
            return false;
        } else if (expiration < currentTime + interval) {
            // 扔进当前时间轮的某个槽里，只有时间大于某个槽，才会放进去
            long virtualId = expiration / tickMs;
            int index = (int)(virtualId % wheelSize);
            TimerTaskList bucket = buckets[index];
            bucket.addTask(entry);
            // 设置bucket过期时间
            if (bucket.setExpiration(virtualId * tickMs)) {
                // 设置好过期时间的bucket需要入队
                delayQueue.offer(bucket);
                return true;
            }
        } else {
            // 当前时间轮不能满足，需要扔到上一轮
            TimeWheel timeWheel = getOverFlowWheel();
            return timeWheel.add(entry);
        }
        return false;
    }

    /**
     * 获取上一轮时间轮
     *
     * @return TimeWheel
     */
    private TimeWheel getOverFlowWheel() {
        if (overflowWheel == null) {
            synchronized (this) {
                if (overflowWheel == null) {
                    overflowWheel = new TimeWheel(interval, wheelSize, currentTime, delayQueue);
                }
            }
        }
        return overflowWheel;
    }

    /**
     * 推进指针
     *
     * @param timestamp 时间戳
     */
    public void advanceLock(Long timestamp) {
        if (timestamp > currentTime + tickMs) {
            currentTime = timestamp - timestamp % tickMs;
            if (overflowWheel != null) {
                this.getOverFlowWheel().advanceLock(timestamp);
            }
        }
    }
}
