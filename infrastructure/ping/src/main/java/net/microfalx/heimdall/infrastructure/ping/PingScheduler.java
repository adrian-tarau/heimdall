package net.microfalx.heimdall.infrastructure.ping;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which handles scheduling pings.
 */
@Component
class PingScheduler implements Runnable {

    private final PingCache cache;
    private final AsyncTaskExecutor taskExecutor;

    PingScheduler(PingCache cache, AsyncTaskExecutor taskExecutor) {
        requireNonNull(cache);
        requireNonNull(taskExecutor);
        this.cache = cache;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void run() {

    }
}
