package com.thinkerwolf.gamer.common.retry;

import java.util.concurrent.TimeUnit;

/**
 * Retry N times with the given interval.
 *
 * @author wukai
 * @see com.thinkerwolf.gamer.common.retry.IRetryPolicy
 */
public class RetryNTimes implements IRetryPolicy {

    private final int times;
    private final TimeUnit timeUnit;
    private long interval;

    public RetryNTimes(int times, long interval, TimeUnit timeUnit) {
        if (times < 0 || interval < 0 || timeUnit == null) {
            throw new IllegalArgumentException();
        }
        this.times = times;
        this.interval = interval;
        this.timeUnit = timeUnit;
    }


    @Override
    public boolean shouldRetry(int retries, long spend, boolean sleep) {
        if (retries >= times) {
            return false;
        }
        if (sleep) {
            long start = System.nanoTime();
            for (; ; ) {
                long nanos = timeUnit.toNanos(interval) - (System.nanoTime() - start);
                if (nanos <= 0) {
                    break;
                }
                try {
                    TimeUnit.NANOSECONDS.sleep(nanos);
                } catch (InterruptedException e) {
                    start = System.nanoTime();
                }
            }
        }
        return true;
    }
}
