package pers.ocean.timewheel.error.timer;

/**
 * @Description 定时器
 * @Author ocean_wll
 * @Date 2021/10/9 10:13 上午
 */
public interface Timer {

    /**
     * 添加一个新任务
     *
     * @param timerTask TimerTask
     */
    void add(TimerTask timerTask);

    /**
     * 推动指针
     *
     * @param timeout 过期时间
     */
    void advanceClock(Long timeout);

    /**
     * 等待执行的任务数
     *
     * @return 等待执行的任务
     */
    Integer size();

    /**
     * 关闭服务，剩下的无法被执行
     */
    void shutdown();
}
