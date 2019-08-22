package com.evosus.loupos;

import android.os.Handler;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppThreadPool {

    private final ScheduledExecutorService threadPool;
    private final Handler mainThreadHandler;

    private static class LazyHolder {
        private static final AppThreadPool INSTANCE = new AppThreadPool();

    }

    public static final AppThreadPool getInstance() {
        return LazyHolder.INSTANCE;
    }

    private AppThreadPool() {
        threadPool = Executors.newSingleThreadScheduledExecutor();
        mainThreadHandler = new Handler();
    }

    public <T> void postTask(final Callable<T> callable, final FinishInMainThreadCallback<T> callback) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Object result = callable.call();
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onFinish((T) result);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public <T> void postTask(final Callable<T> callable, long timeInMilliSec, final FinishInMainThreadCallback<T> callback) {
        threadPool.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    final Object result = callable.call();
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onFinish((T) result);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, timeInMilliSec, TimeUnit.MILLISECONDS);
    }

    public void runOnUiThread(final Runnable runnable) {
        if (mainThreadHandler == null) {
            throw new IllegalThreadStateException("Your POSLink does not init on main thread.");
        }
        mainThreadHandler.post(runnable);
    }

    public void runOnUiThreadDelay(final Runnable runnable, int timeout) {
        mainThreadHandler.postDelayed(runnable, timeout);
    }

    public void runInBackground(final Runnable runnable) {
        threadPool.submit(runnable);
    }

    public interface FinishInMainThreadCallback<T> {
        void onFinish(T result);
    }
}
