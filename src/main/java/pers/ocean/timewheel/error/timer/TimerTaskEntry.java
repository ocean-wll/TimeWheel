package pers.ocean.timewheel.error.timer;

import lombok.Data;

/**
 * @Description 存储任务的容器entry
 * @Author ocean_wll
 * @Date 2021/10/9 10:15 上午
 */
@Data
public class TimerTaskEntry implements Comparable<TimerTaskEntry> {

    /**
     * 任务
     */
    private TimerTask timerTask;

    /**
     * 过期时间
     */
    private Long expireMs;

    /**
     * 任务环形链表
     */
    private volatile TimerTaskList timerTaskList;

    /**
     * 下一节点
     */
    private TimerTaskEntry next;

    /**
     * 上一节点
     */
    private TimerTaskEntry prev;

    public TimerTaskEntry(TimerTask timerTask, Long expireMs) {
        this.timerTask = timerTask;
        this.expireMs = expireMs;
        this.prev = null;
        this.next = null;
    }

    public void remove() {
        TimerTaskList currentList = timerTaskList;
        while (currentList != null){
            currentList.remove(this);
            currentList = timerTaskList;
        }
    }

    @Override
    public int compareTo(TimerTaskEntry o) {
        return (int)(this.expireMs - o.expireMs);
    }
}
