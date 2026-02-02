package io.arbitrix.core.utils.executor;

import io.arbitrix.core.common.executor.AbstractThreadPoolExecutor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jonathan.ji
 */
public class CancelOrderExecutor extends AbstractThreadPoolExecutor {
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new CancelOrderExecutor();

    private CancelOrderExecutor() {
        super(1
                , 1
                , 60000L, TimeUnit.DAYS
                , new LinkedBlockingQueue<>(200000)
                , "CancelOrderExecutor"
                , getDiscardOldestPolicy()
        );
    }

    public static ThreadPoolExecutor getInstance() {
        return THREAD_POOL_EXECUTOR;
    }
}
