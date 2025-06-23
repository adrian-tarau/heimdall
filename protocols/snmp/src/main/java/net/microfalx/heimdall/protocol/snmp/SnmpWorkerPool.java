package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.threadpool.ThreadPool;
import org.snmp4j.util.WorkerPool;
import org.snmp4j.util.WorkerTask;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class SnmpWorkerPool implements WorkerPool {

    private final ThreadPool threadPool;

    SnmpWorkerPool(ThreadPool executor) {
        requireNonNull(executor);
        this.threadPool = executor;
    }

    @Override
    public void execute(WorkerTask task) {
        threadPool.execute(task);
    }

    @Override
    public boolean tryToExecute(WorkerTask task) {
        try {
            threadPool.execute(task);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isIdle() {
        return false;
    }
}
