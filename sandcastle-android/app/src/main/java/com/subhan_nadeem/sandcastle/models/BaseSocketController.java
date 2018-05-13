package com.subhan_nadeem.sandcastle.models;

import android.databinding.Observable;
import android.databinding.ObservableField;

import io.socket.client.Socket;

/**
 * Created by Subhan Nadeem on 2017-10-28.
 * All socket controllers in the app extend here
 * Holds base functionality such as emitting state
 */

public abstract class BaseSocketController {
    private static final String KEY_STATE_UPDATE = "appState";
    protected final ObservableField<Socket> mSocket;
    private final String STATE;
    private boolean isResumed;

    protected BaseSocketController(ObservableField<Socket> socket, String state) {
        mSocket = socket;
        STATE = state;

        mSocket.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                // Socket was initialized
                if (mSocket.get() != null) {
                    if (isResumed) {
                        onResume();
                    }
                    else
                        onPause();
                }
            }
        });
    }

    public void onResume() {
        isResumed = true;
        if (mSocket.get() == null) return;
        mSocket.get().emit(KEY_STATE_UPDATE, STATE);
        onResumeChild();
    }

    protected abstract void onResumeChild();

    protected abstract void onPauseChild();

    public void onPause() {
        isResumed = false;
        if (mSocket.get() == null) return;
        mSocket.get().emit(KEY_STATE_UPDATE, (Object) null);
        onPauseChild();
    }

    public ObservableField<Socket> getSocket() {
        return mSocket;
    }
}
