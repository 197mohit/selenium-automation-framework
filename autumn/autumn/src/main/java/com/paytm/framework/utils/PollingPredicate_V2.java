package com.paytm.framework.utils;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * @param <T>
 * @author ankuragarwal
 * <p>
 * PollingPredicate_V2 work for achieving async task and polling
 */
public class PollingPredicate_V2<T> {
    private Callable<T> tCallable;
    private Predicate<T> tPredicate;
    private Duration MAX_TIME;
    private Duration POLL_TIME;

    public PollingPredicate_V2(Callable<T> tCallable) {
        this.MAX_TIME = Duration.ofSeconds(1L);
        this.POLL_TIME = Duration.ofMillis(500L);
        this.tCallable = tCallable;
        this.tPredicate = null;
    }

    public PollingPredicate_V2(Callable<T> tCallable, Predicate<T> tPredicate) {
        this.MAX_TIME = Duration.ofSeconds(1L);
        this.POLL_TIME = Duration.ofMillis(500L);
        this.tCallable = tCallable;
        this.tPredicate = tPredicate;
    }

    public PollingPredicate_V2(Callable<T> tCallable, Duration MAX_TIME, Duration POLL_TIME) {
        this(tCallable);
        this.MAX_TIME = MAX_TIME;
        this.POLL_TIME = POLL_TIME;
    }

    public PollingPredicate_V2(Callable<T> tCallable, Predicate<T> tPredicate, Duration MAX_TIME, Duration POLL_TIME) {
        this(tCallable, tPredicate);
        this.MAX_TIME = MAX_TIME;
        this.POLL_TIME = POLL_TIME;
    }

    public T evaluate() {
        long maxToGo = this.MAX_TIME.toMillis() + System.currentTimeMillis();

        do {
            try {
                T resp = this.tCallable.call();
                if (null == this.tPredicate) {
                    if ((Boolean) resp) {
                        return resp;
                    }
                } else if (this.tPredicate.test(resp)) {
                    return resp;
                }

            } catch (Exception var5) { }
            try {
                Thread.sleep(this.POLL_TIME.toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (maxToGo > System.currentTimeMillis());

        return null;
    }
}
