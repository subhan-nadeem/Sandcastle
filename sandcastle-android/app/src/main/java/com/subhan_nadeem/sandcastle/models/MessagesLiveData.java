package com.subhan_nadeem.sandcastle.models;

import android.arch.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Subhan Nadeem on 2017-10-15.
 */

public class MessagesLiveData extends LiveData<Message> {

    @Override
    protected void onActive() {
        super.onActive();
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
    }
}
