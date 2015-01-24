package com.discover.step.async;

import android.os.AsyncTask;
import android.util.Pair;

public abstract class SafeAsyncTask<TParams, TProgress, TResult> extends AsyncTask<TParams, TProgress, Pair<TResult, Exception>> {

    @Override
    protected final Pair<TResult, Exception> doInBackground(TParams... arg0) {
        TResult result = null;
        Exception exception = null;

        try {
            result = doWorkInBackground(arg0);
        } catch (Exception e) {
            exception = e;
        }
        return new Pair<TResult, Exception>(result, exception);
    }

    @Override
    protected final void onPostExecute(Pair<TResult, Exception> result) {
        if (!isCancelled()) {
            onFinished();
            if (result.second != null) {
                onException(result.second);
            } else {
                onSuccess(result.first);
            }
        }
    }

    protected abstract TResult doWorkInBackground(TParams... params) throws Exception;

    /**
     * Called when an exception happens during background processing
     *
     * @param ex The exception
     */
    protected void onException(Exception ex) {
    }

    /**
     * Called when the background task completes without error
     *
     * @param result The result
     */
    protected void onSuccess(TResult result) {
    }

    /**
     * Called BEFORE either onException or onSuccess
     */
    protected void onFinished() {
    }
}
