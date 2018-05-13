package com.subhan_nadeem.sandcastle.network;

import com.google.gson.JsonObject;
import com.subhan_nadeem.sandcastle.models.responses.AuthenticateResponse;
import com.subhan_nadeem.sandcastle.models.responses.ChatRoomResponse;
import com.subhan_nadeem.sandcastle.models.responses.NewMessageResponse;
import com.subhan_nadeem.sandcastle.models.responses.NewRoomResponse;
import com.subhan_nadeem.sandcastle.models.responses.RefreshTokenResponse;
import com.subhan_nadeem.sandcastle.models.responses.RoomsResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Subhan Nadeem on 2017-10-10.
 * Access interface to backend
 */

public interface APIService {
    @FormUrlEncoded
    @POST("/v1/user/register")
    Call<JsonObject> registerUser(@Field("username") String username,
                                  @Field("password") String password);

    @FormUrlEncoded
    @POST("/v1/user/authenticate")
    Call<AuthenticateResponse> authenticateUser(@Field("username") String username,
                                                @Field("password") String password,
                                                @Field("fcm_key") String fcmKey,
                                                @Field("device_name") String deviceName);

    @FormUrlEncoded
    @POST("/v1/user/update_fcm")
    Call<JsonObject> updateFCMKey(@Field("fcm_key") String fcmKey);


    @GET("/v1/chat_rooms")
    Call<RoomsResponse> getChatrooms(@Query("lat") double latitude,
                                     @Query("lng") double longitude);


    @FormUrlEncoded
    @POST("/v1/chat_rooms/create")
    Call<NewRoomResponse> createChatRoom(@Field("lat") double latitude,
                                         @Field("lng") double longitude,
                                         @Field("name") String name);

    @FormUrlEncoded
    @POST("/v1/user/refresh_token")
    Call<RefreshTokenResponse> refreshToken(@Field("refresh_token") String refreshToken);

    @Multipart
    @POST("/v1/user/upload_avatar")
    Call<JsonObject> uploadAvatar(@Part MultipartBody.Part image);

    @DELETE("/v1/user/delete_avatar")
    Call<JsonObject> deleteAvatar();

    @FormUrlEncoded
    @POST("/v1/user/logout")
    Call<JsonObject> logout(@Field("refresh_token") String refreshToken);

    @FormUrlEncoded
    @POST("/v1/user/update_location")
    Call<JsonObject> updateLocation(@Field("lat") double latitude,
                                    @Field("lng") double longitude);

    @GET("/v1/chat_rooms/{id}")
    Call<ChatRoomResponse> getChatRoom(@Path("id") long id);

    @FormUrlEncoded
    @POST("/v1/chat_rooms/{id}/message")
    Call<NewMessageResponse> sendMessage(@Path("id") long chatRoomID,
                                         @Field("message") String message);
}