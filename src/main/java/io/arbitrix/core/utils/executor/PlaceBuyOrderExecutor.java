package io.arbitrix.core.utils.executor;

import io.arbitrix.core.common.executor.AbstractThreadPoolExecutor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jonathan.ji
 */
public class PlaceBuyOrderExecutor extends AbstractThreadPoolExecutor {
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new PlaceBuyOrderExecutor();

    /**
     * 阻塞队列设置了10个，如果接口的响应时间为100ms，那么阻塞的时间就得要1s
     * 抛弃策略：抛弃队列中最老的任务
     */
    private PlaceBuyOrderExecutor() {
        super(1
                , 1
                , 60000L, TimeUnit.DAYS
                , new LinkedBlockingQueue<>(10)
                , "PlaceBuyOrderExecutor"
                , getDiscardOldestPolicy()
        );
    }

    public static ThreadPoolExecutor getInstance() {
        return THREAD_POOL_EXECUTOR;
    }
}
