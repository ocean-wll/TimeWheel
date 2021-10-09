package pers.ocean.timewheel.timer;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import lombok.Data;

/**
 * @Description 存储任务的环形链表
 * @Author ocean_wll
 * @Date 2021/10/9 10:17 上午
 */
@Data
public class TimerTaskList implements Delayed {

    /**
     * TimerTaskList环形链表使用一个虚拟根节点root
     */
    private final TimerTaskEntry root = new TimerTaskEntry(null, -1L);

    {
        root.setNext(root);
        root.setPrev(root);
    }

    /**
     * bucket的过期时间
     */
    private AtomicLong expiration = new AtomicLong(-1L);

    public Long getExpiration() {
        return expiration.get();
    }

    /**
     * 设置bucket的过期时间，设置成功返回true
     *
     * @param expirationMs 过期时间
     * @return true设置成功，false设置失败
     */
    public Boolean setExpiration(Long expirationMs) {
        return expiration.getAndSet(expirationMs) != expirationMs;
    }

    /**
     * 增加任务
     *
     * @param entry 任务实体
     * @return true增加成功，false增加失败
     */
    public Boolean addTask(TimerTaskEntry entry) {
        boolean done = false;
        while (!done) {
            // 如果TimerTaskEntry已经在别的list中就先移除，同步代码块外面移除，避免死锁，一直到成功为止
            entry.remove();
            synchronized (this) {
                if (entry.getTimerTaskList() == null) {
                    // 加到链表的末尾
                    entry.setTimerTaskList(this);
                    TimerTaskEntry tail = root.getPrev();
                    entry.setPrev(tail);
                    entry.setNext(root);
                    tail.setNext(entry);
                    root.setPrev(entry);
                    done = true;
                }
            }
        }
        return true;
    }

    /**
     * 从TimerTaskList中移除指定的timerTaskEntry
     *
     * @param entry 任务对象
     */
    public void remove(TimerTaskEntry entry) {
        synchronized (this) {
            if (entry.getTimerTaskList().equals(this)) {
                entry.getNext().setPrev(entry.getPrev());
                entry.getPrev().setNext(entry.getNext());
                entry.setNext(null);
                entry.setPrev(null);
                entry.setTimerTaskList(null);
            }
        }
    }

    /**
     * 移除所有
     *
     * @param entry 任务对象
     */
    public synchronized void clear(Consumer<TimerTaskEntry> entry) {
        TimerTaskEntry head = root.getNext();
        while (!head.equals(root)) {
            remove(head);
            entry.accept(head);
            head = root.getNext();
        }
        expiration.set(-1L);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return Math.max(0, unit.convert(expiration.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public int compareTo(Delayed o) {
        if (o instanceof TimerTaskList) {
            return Long.compare(expiration.get(), ((TimerTaskList)o).getExpiration());
        }
        return 0;
    }
}
