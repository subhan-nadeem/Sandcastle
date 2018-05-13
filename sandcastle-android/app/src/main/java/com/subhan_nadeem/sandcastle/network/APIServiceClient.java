package com.subhan_nadeem.sandcastle.network;

import android.support.annotation.NonNull;

import com.subhan_nadeem.sandcastle.App;
import com.subhan_nadeem.sandcastle.data.RealmHelper;
import com.subhan_nadeem.sandcastle.models.AuthTokenHolder;
import com.subhan_nadeem.sandcastle.models.responses.APIError;
import com.subhan_nadeem.sandcastle.models.responses.RefreshTokenResponse;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.subhan_nadeem.sandcastle.models.responses.APIError.API_ERROR_TOKEN_EXPIRED;

/**
 * Instance holder of APIService
 */

public abstract class APIServiceClient {

    private static final String TAG = "APIServiceClient";
    private static final String KEY_AUTHORIZATION = "authorization";
    private static Retrofit mInstance = null;
    private static Interceptor authHeaderInterceptor = new Interceptor() {
        @Override
        public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
            Request originalRequest = chain.request();

            // Retrieve auth token
            String authToken = new RealmHelper().getUserAuthToken();

            Request newRequest = getAuthHeaderRequest(originalRequest, authToken);

            return getErrorCheckedResponse(newRequest, chain);
        }
    };


    static Retrofit getClient(final String baseUrl) {

        OkHttpClient okHttpClient = getAuthTokenHeaderClient();

        if (mInstance == null) {
            mInstance = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return mInstance;
    }

    /**
     * Checks the given response from a given request for any errors,
     * and handles them accordingly
     *
     * @param request Given request to be sent out for a response
     * @param chain   Given network chain
     * @return A network response from a network request
     */
    private static Response getErrorCheckedResponse(Request request, Interceptor.Chain chain)
            throws IOException {

        Response response = chain.proceed(request);

        // Intercept any unauthorized responses
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            APIError error = APIError.parseError(response.body());

            // Auth token expired, refresh it
            if (error.getMessage().equals(API_ERROR_TOKEN_EXPIRED)) {

                refreshAuthToken();

                AuthTokenHolder refreshedTokens = new RealmHelper().getTokenHolder();

                // Use the updated auth token to create a new request check
                    Request updatedTokenRequest
                            = getAuthHeaderRequest(request, refreshedTokens.getAuthToken());

                    response = getErrorCheckedResponse(updatedTokenRequest, chain);

            } else {
                // Unauthorized access does not fall into special case, so log user out
                App.logUserOut();
            }
        }

        return response;
    }


    @NonNull
    private static OkHttpClient getAuthTokenHeaderClient() {

        return new OkHttpClient().newBuilder().addInterceptor(authHeaderInterceptor).build();
    }

    private static Request getAuthHeaderRequest(Request originalRequest, String authToken) {
        Request.Builder builder = originalRequest.newBuilder()
                .header(KEY_AUTHORIZATION, authToken);

        return builder.build();
    }

    /**
     * Refreshes a user's auth token
     * SYNCHRONOUS Executed in an existing client's background worker thread
     */
    public static synchronized void refreshAuthToken() throws IOException {
        RealmHelper realmHelper = new RealmHelper();

        // Retrieving existing API service
        APIService apiService = APIUtils.getAPIService();

        retrofit2.Response<RefreshTokenResponse> refreshResponse =
                apiService.refreshToken(realmHelper.getRefreshToken()).execute();

        if (refreshResponse.isSuccessful()) {

            AuthTokenHolder authTokenHolder = new AuthTokenHolder(
                    refreshResponse.body().getAuthToken(),
                    refreshResponse.body().getRefreshToken());

            realmHelper.storeTokens(authTokenHolder);

        }
    }
}

