package io.arbitrix;

import org.junit.jupiter.api.Test;
import io.arbitrix.core.utils.timewindow.CallLimiter;

public class CallLimiterTest {

    private CallLimiter callLimiter = new CallLimiter();

    @Test
    public void test() throws InterruptedException {
        callLimiter.increment();
        callLimiter.increment();
        callLimiter.increment();
        System.out.println(callLimiter.getSum());
        callLimiter.add(30);
        System.out.println(callLimiter.getSum());

        Thread.sleep(500);
        callLimiter.getSum();
        System.out.println(callLimiter.getSum());

        Thread.sleep(500);
        callLimiter.increment();
        System.out.println(callLimiter.getSum());
    }

}
