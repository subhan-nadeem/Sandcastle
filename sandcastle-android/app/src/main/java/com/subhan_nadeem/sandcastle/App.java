package com.subhan_nadeem.sandcastle;

import android.app.Application;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonObject;
import com.subhan_nadeem.sandcastle.data.RealmHelper;
import com.subhan_nadeem.sandcastle.features.auth.AuthenticationActivity;
import com.subhan_nadeem.sandcastle.models.responses.APIError;
import com.subhan_nadeem.sandcastle.network.APIService;
import com.subhan_nadeem.sandcastle.network.APIServiceClient;
import com.subhan_nadeem.sandcastle.network.APIUtils;
import com.subhan_nadeem.sandcastle.utils.ProfilePhotoManager;

import net.danlew.android.joda.JodaTimeAndroid;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import io.realm.Realm;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Subhan Nadeem on 2017-10-11.
 * Application for Realm configuration etc.
 */

public class App extends Application {

    private static final String TAG = "Application";
    // TODO Code smell, learn how to fix in the future
    private static Application mInstance;

    private ObservableField<Socket> mConnectedSocket = new ObservableField<>();

    public static void logUserOut() {
        // Close socket
        Socket socket = ((App) mInstance).getConnectedSocket().get();

        if (socket != null) socket.close();

        new ProfilePhotoManager(mInstance).deleteLocalAvatarFile();

        APIService apiService = APIUtils.getAPIService();
        RealmHelper realmHelper = new RealmHelper();

        // TODO add to job queue
        apiService.logout(realmHelper.getRefreshToken()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });

        // Clear Realm DB
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    realm.deleteAll();
                }
            });
        } finally {
            if (realm != null)
                realm.close();
        }

        mInstance.startActivity(AuthenticationActivity.getIntent(mInstance));

    }

    public ObservableField<Socket> getConnectedSocket() {
        return mConnectedSocket;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(getApplicationContext());
        JodaTimeAndroid.init(this);
        mInstance = this;

        if (new RealmHelper().isUserLoggedIn())
            initSocket();
    }

    public void initSocket() {
        try {
            IO.Options options = new IO.Options();
            options.query = "token=" + new RealmHelper().getUserAuthToken();

            final Socket socket = IO.socket(APIUtils.BASE_URL, options);

            // Generic error handler
            socket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(this.getClass().getName(), Arrays.toString(args));
                    String errorMessage = (String) args[0];
                    if (errorMessage.equals(APIError.API_ERROR_TOKEN_EXPIRED)) {
                        try {
                            APIServiceClient.refreshAuthToken();
                            initSocket();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            // On successful connection, set the connected socket
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    mConnectedSocket.set(socket);
                }
            });

            socket.connect();

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
