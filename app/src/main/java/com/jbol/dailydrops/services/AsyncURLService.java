package com.jbol.dailydrops.services;

import android.graphics.Bitmap;
import android.os.AsyncTask;

// https://stackoverflow.com/a/12575319/11391965
public class AsyncURLService extends AsyncTask<String, Void, Bitmap> {
    public interface AsyncResponse {
        void processFinish(Bitmap output);
    }

    public AsyncResponse delegate;

    public AsyncURLService(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        delegate.processFinish(bitmap);
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        return ImageService.getImageFromURL(strings[0]);
    }
}
