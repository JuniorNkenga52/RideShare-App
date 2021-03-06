package com.app.rideWhiz.activity;

import android.content.Context;
import android.os.AsyncTask;
import com.app.rideWhiz.api.RideShareApi;

public class LongOperation extends AsyncTask<String, Void, String> {

    Context context;
    public LongOperation(Context context) {
        this.context=context;
    }

    @Override
    protected String doInBackground(String... params) {
        return RideShareApi.postApiCall(params[0], params[1],context);
    }
}