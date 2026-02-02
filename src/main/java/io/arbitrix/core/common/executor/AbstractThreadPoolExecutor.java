package io.arbitrix.core.common.executor;

import io.arbitrix.core.utils.executor.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * Abstract base class for creating customized thread pool executors
 */
@Slf4j
public abstract class AbstractThreadPoolExecutor extends ThreadPoolExecutor {

    public AbstractThreadPoolExecutor(int corePoolSize,
                                      int maximumPoolSize,
                                      long keepAliveTime,
                                      TimeUnit unit,
                                      BlockingQueue<Runnable> workQueue,
                                      String poolName) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                new NamedThreadFactory(poolName, true));
    }

    public AbstractThreadPoolExecutor(int corePoolSize,
                                      int maximumPoolSize,
                                      long keepAliveTime,
                                      TimeUnit unit,
                                      BlockingQueue<Runnable> workQueue,
                                      String poolName,
                                      RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                new NamedThreadFactory(poolName, true), handler);
    }

    /**
     * Get a DiscardOldestPolicy that logs when tasks are discarded
     */
    public static RejectedExecutionHandler getDiscardOldestPolicy() {
        return (r, executor) -> {
            if (!executor.isShutdown()) {
                Runnable discarded = executor.getQueue().poll();
                if (discarded != null) {
                    log.warn("Discarding oldest task due to queue full: {}", discarded);
                }
                executor.execute(r);
            }
        };
    }

    /**
     * Get a CallerRunsPolicy
     */
    public static RejectedExecutionHandler getCallerRunsPolicy() {
        return new CallerRunsPolicy();
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t != null) {
            log.error("Task execution failed", t);
        }
    }
}
