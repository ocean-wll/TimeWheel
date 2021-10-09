package pers.ocean.timewheel.timer;

/**
 * @Description
 * @Author ocean_wll
 * @Date 2021/10/9 3:58 下午
 */
public interface ITimerWheelTask {

    /**
     * 获取执行时间
     *
     * @return
     */
    long getExecutingTime();

    /**
     * 执行任务
     */
    void executeTask();
}
