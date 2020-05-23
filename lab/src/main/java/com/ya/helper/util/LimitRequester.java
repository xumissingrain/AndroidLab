package com.ya.helper.util;

import android.os.Handler;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 包装Handler，实现请求控制
 * 机制：前一次请求尚未执行完成，则仅保留最新一次请求
 * 注意：同一LimitRequester对象提交的所有Runnable视为同一类可合并请求
 *
 * @author miss
 */
public class LimitRequester {
    private static final String TAG = "LimitRequester";
    private Handler mHandler;
    private AutoTimeRunnable aRun;
    private int mTime;

    /**
     * 创建限制刷新的Handler
     * 上个请求尚未执行完，则仅保留最新一次请求
     *
     * @param handler 实际使用的线程句柄 Handler
     */
    public LimitRequester(Handler handler) {
        mHandler = handler;
        aRun = new AutoTimeRunnable();
    }

    /**
     * 创建间隔刷新的Handler
     * 两次请求提交间隔不能超过interval，否则丢弃仅执行最新一次
     *
     * @param handler
     * @param interval 毫秒间隔
     */
    public LimitRequester(Handler handler, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("LimitRequester interval must > 0");
        }
        mHandler = handler;
        mTime = interval;
        aRun = new AutoTimeRunnable();
    }

    /**
     * 提交请求，默认为同一类别
     * 若Handler处于非空闲状态，则仅执行最新一次提交
     *
     * @param runnable
     */
    public void postLimit(Runnable runnable) {
        if (null == runnable) {
            return;
        }
        aRun.post(runnable);
    }

    private class AutoTimeRunnable implements Runnable {
        private Runnable toRun;
        //标识是否空闲，默认是空闲状态
        private final AtomicBoolean isIdle = new AtomicBoolean(true);
        private long lastExecuteTime = 0L;

        public void post(Runnable toRun) {
            //若空闲，则执行，不空闲，则仅保存最新一次Runnable
            this.toRun = toRun;
            if (isIdle.compareAndSet(true, false)) {
                execute();
            }
        }

        /**
         * 执行最新保存的Runnable
         * 执行完成后，若发现有新请求，则执行
         * 若没有新请求，则置为空闲状态
         */
        @Override
        public void run() {
            Runnable r = toRun;
            r.run();
            if (r != toRun) {
                execute();
            } else {
                isIdle.set(true);
            }
        }

        /**
         * 具体执行逻辑
         * 若无间隔，则立刻执行
         * 若有间隔，两次执行间隔超过要求间隔，立刻执行
         * 两次执行间隔不及要求间隔，则延迟间隔时间后执行
         */
        private void execute() {
            if (mTime > 0) {
                long now = System.currentTimeMillis();
                long interval = now - lastExecuteTime;
                if (interval >= mTime) {
                    lastExecuteTime = now;
                    mHandler.post(this);
                } else {
                    lastExecuteTime = now + mTime;
                    mHandler.postDelayed(this, mTime);
                }
            } else {
                mHandler.post(this);
            }
        }
    }
}