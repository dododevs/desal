package revolver.desal.util.logic;

import android.os.AsyncTask;

public class Async<T> {

    private final Task<T> mTask;
    private Callback<T> mCallback;
    private Worker<T> mWorker;

    private boolean mAllowNullResult = true;

    private Async(Task<T> task) {
        mTask = task;
    }

    public Async<T> andThen(Callback<T> callback) {
        mCallback = callback;
        return this;
    }

    public Async<T> orThrow() {
        mCallback = new Callback<T>() {
            @Override
            public void done(T obj) {
                mCallback.done(obj);
            }

            @Override
            public void error(Exception e) {
                throw new RuntimeException(e);
            }
        };
        return this;
    }

    public Async<T> allowNullResult(boolean whether) {
        mAllowNullResult = whether;
        return this;
    }

    public void start() {
        mWorker = new Worker<>(mTask, mCallback, mAllowNullResult);
        mWorker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public boolean cancel() {
        return mWorker.cancel(true);
    }

    public static <T> Async<T> run(Task<T> task) {
        return new Async<>(task);
    }

    public interface Task<T> {
        T run() throws Exception;
    }
    public interface Callback<T> {
        void done(T obj);
        void error(Exception e);
    }

    private static class Worker<T> extends AsyncTask<Void, Void, T> {
        private final Task<T> mTask;
        private final Callback<T> mCallback;
        private final boolean mAllowNull;
        private Exception mException;

        Worker(Task<T> task, Callback<T> callback, boolean allowNull) {
            super();
            mTask = task;
            mCallback = callback;
            mAllowNull = allowNull;
        }

        @Override
        protected T doInBackground(Void... params) {
            try {
                return mTask.run();
            } catch (Exception e) {
                mException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(T result) {
            if (mCallback == null)
                return;
            if (result != null) {
                mCallback.done(result);
            } else {
                if (mException == null) {
                    if (mAllowNull)
                        mCallback.done(null);
                    else
                        mCallback.error(new NullPointerException());
                } else {
                    mCallback.error(mException);
                }
            }
        }
    }
}