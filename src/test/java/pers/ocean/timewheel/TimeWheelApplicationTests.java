package pers.ocean.timewheel;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import pers.ocean.timewheel.timer.SystemTimer;
import pers.ocean.timewheel.timer.TimerTask;

@SpringBootTest
class TimeWheelApplicationTests {

    @Test
    public void timeWheelTest() throws InterruptedException {
        SystemTimer systemTimer = new SystemTimer();

        for (int i = 0; i < 1; i++) {
            TimerTask timerTask = new TimerTask("这是第" + i + "个任务", i * 1000L);
            systemTimer.add(timerTask);
        }

        TimeUnit.SECONDS.sleep(100);
    }

}
