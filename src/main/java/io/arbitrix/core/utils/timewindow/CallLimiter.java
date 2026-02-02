package io.arbitrix.core.utils.timewindow;

import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author jonathan.ji
 */
public class CallLimiter {

    private final LeapArray<LongAdder> data;

    public CallLimiter() {
        this(new UnaryLeapArray(20, 1000));
    }

    public CallLimiter(int sampleCount, int intervalInMs) {
        this(new UnaryLeapArray(sampleCount, intervalInMs));
    }

    CallLimiter(LeapArray<LongAdder> data) {
        this.data = data;
    }

    public void increment() {
        data.currentWindow().value().increment();
    }

    public void add(int x) {
        data.currentWindow().value().add(x);
    }

    public long getSum() {
        data.currentWindow();
        long success = 0;

        List<LongAdder> list = data.values();
        for (LongAdder window : list) {
            success += window.sum();
        }
        return success;
    }
}
