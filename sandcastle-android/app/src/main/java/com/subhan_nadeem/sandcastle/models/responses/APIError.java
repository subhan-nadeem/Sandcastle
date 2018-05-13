package com.subhan_nadeem.sandcastle.models.responses;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;

import okhttp3.ResponseBody;

/**
 * Created by Subhan Nadeem on 2017-10-12.
 * Error model
 **/

public class APIError {

    public static final String API_ERROR_TOKEN_EXPIRED = "TokenExpiredError";
    private static final String TAG = "APIError";

    @SerializedName("message")
    private String mMessage;

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public static APIError parseError(ResponseBody errorBody) {
        try {
            APIError error = new Gson().fromJson(errorBody.string(), APIError.class);
            Log.e(TAG, error.getMessage());
            return error;
        } catch (IOException e) {
            return new APIError();
        }
    }
}