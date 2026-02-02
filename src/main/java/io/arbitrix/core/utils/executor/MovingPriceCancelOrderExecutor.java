package io.arbitrix.core.utils.executor;

import io.arbitrix.core.common.executor.AbstractThreadPoolExecutor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MovingPriceCancelOrderExecutor extends AbstractThreadPoolExecutor {
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new MovingPriceCancelOrderExecutor();

    private MovingPriceCancelOrderExecutor() {
        super(20
                , 20
                , 60000L, TimeUnit.DAYS
                , new LinkedBlockingQueue<>(10)
                , "MovingPriceCancelOrderExecutor"
                , getDiscardOldestPolicy()
        );
    }

    public static ThreadPoolExecutor getInstance() {
        return THREAD_POOL_EXECUTOR;
    }
}
