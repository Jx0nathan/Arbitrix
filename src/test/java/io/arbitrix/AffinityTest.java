package io.arbitrix;

import lombok.extern.log4j.Log4j2;
import net.openhft.affinity.AffinityLock;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.*;

@SuppressWarnings("unused")
@Log4j2
public class AffinityTest {

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

    private final ExecutorService PLACE_BUY_ORDER_EXECUTOR = new ThreadPoolExecutor(1, 1, 60, TimeUnit.DAYS, new LinkedBlockingQueue<Runnable>(), r -> {
        Thread thread = Executors.defaultThreadFactory().newThread(r);
        thread.setName("PLACE_BUY_ORDER_EXECUTOR-" + thread.getId());
        return thread;
    });
    private final ExecutorService PLACE_SELL_ORDER_EXECUTOR = new ThreadPoolExecutor(1, 1, 60, TimeUnit.DAYS, new LinkedBlockingQueue<Runnable>(), r -> {
        Thread thread = Executors.defaultThreadFactory().newThread(r);
        thread.setName("PLACE_SELL_ORDER_EXECUTOR-" + thread.getId());
        return thread;
    });
    private final ExecutorService MARKER_MAKER_EXECUTOR = new ThreadPoolExecutor(1, 1, 60, TimeUnit.DAYS, new LinkedBlockingQueue<Runnable>(), r -> {
        Thread thread = Executors.defaultThreadFactory().newThread(r);
        thread.setName("MARKER_MAKER_EXECUTOR-" + thread.getId());
        return thread;
    });

    @Test
    public void test() {
        while (true){
            MARKER_MAKER_EXECUTOR.execute(() -> {
                try (final AffinityLock al = AffinityLock.acquireLock(3)) {
                    int sum = new Random().nextInt(100) + new Random().nextInt(100);

                    PLACE_SELL_ORDER_EXECUTOR.submit(() -> {
                        try (final AffinityLock al2 = AffinityLock.acquireLock(4)) {
                            int sum2 = new Random().nextInt(100) + new Random().nextInt(100);
                        }
                    });

                    PLACE_BUY_ORDER_EXECUTOR.submit(() -> {
                        try (final AffinityLock al3 = AffinityLock.acquireLock(4)) {
                            int sum3 = new Random().nextInt(100) + new Random().nextInt(100);
                        }
                    });
                }catch (Exception ex){
                    log.info("ex:" + ex.getMessage());
                }
            });
        }
    }
}
