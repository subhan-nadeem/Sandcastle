package com.subhan_nadeem.sandcastle.models.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Subhan Nadeem on 2017-10-11.
 *
 * All API responses should inherit from this
 */

abstract class BaseResponse {
    @SerializedName("message")
    private String mMessage;

    public String getMessage() {
        return mMessage;
    }
}
