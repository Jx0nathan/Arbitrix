package io.arbitrix.core.utils.executor;

import io.arbitrix.core.common.executor.AbstractThreadPoolExecutor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MovingPricePlaceOrderExecutor extends AbstractThreadPoolExecutor {
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new MovingPricePlaceOrderExecutor();

    private MovingPricePlaceOrderExecutor() {
        super(20
                , 20
                , 60000L, TimeUnit.DAYS
                , new LinkedBlockingQueue<>(10)
                , "MovingPricePlaceOrderExecutor"
                , getDiscardOldestPolicy()
        );
    }

    public static ThreadPoolExecutor getInstance() {
        return THREAD_POOL_EXECUTOR;
    }
}
