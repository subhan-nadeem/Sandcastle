package com.subhan_nadeem.sandcastle.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.JsonObject;
import com.subhan_nadeem.sandcastle.models.responses.APIError;
import com.subhan_nadeem.sandcastle.network.APIService;
import com.subhan_nadeem.sandcastle.network.APIUtils;

import java.io.File;

import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Subhan Nadeem on 2017-11-18.
 * Manages uploading/deleting/changing profile photo
 */

public class ProfilePhotoManager {
    private static final String FILE_NAME_AVATAR = "avatar.jpg";
    private static final String TAG = "ProfilePhotoManager";
    private final Context mContext;
    private final APIService mAPIService;

    public ProfilePhotoManager(Context context) {
        mContext = context;
        mAPIService = APIUtils.getAPIService();
    }

    /**
     * @param imagePath The original path of the image
     * @return The compressed & renamed avatar file
     */
    public void uploadAvatar(String imagePath, final OnAvatarUploadListener fileSavedListener) {

        File originalFile = new File(imagePath);

        new Compressor(mContext)
                .setMaxHeight(200)
                .setMaxWidth(200)
                .compressToFileAsFlowable(originalFile)
                .subscribeOn(Schedulers.io())
                .toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(final File file) {
                        sendImageToServer(file, fileSavedListener);
                    }
                });
    }

    private void sendImageToServer(final File file, final OnAvatarUploadListener fileSavedListener) {
        RequestBody reqFile
                = RequestBody.create(MediaType.parse("image/*"), file);

        MultipartBody.Part imagePart
                = MultipartBody.Part.createFormData("avatar", FILE_NAME_AVATAR,
                reqFile);

        mAPIService.uploadAvatar(imagePart).enqueue(
                new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call,
                                           Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            File avatarFile = saveAvatarFile(file);

                            fileSavedListener.onSuccess(avatarFile);
                        } else {
                            APIError error = APIError.parseError(response.errorBody());
                            fileSavedListener.onUploadFailure(error.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        fileSavedListener.onUploadFailure(t.getMessage());
                    }
                });
    }

    public File getUserAvatarFile() {
        return new File(mContext.getFilesDir(), FILE_NAME_AVATAR);
    }

    public void deleteUserAvatarFile(final OnAvatarDeleteListener listener) {

        mAPIService.deleteAvatar().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    deleteLocalAvatarFile();
                    listener.onDeleteSuccessful();
                } else {
                    listener.onDeleteFailure();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }

    public void deleteLocalAvatarFile() {
        mContext.deleteFile(getUserAvatarFile().getName());
    }

    @NonNull
    private File saveAvatarFile(File file) {
        File avatarFile = new File(mContext.getFilesDir(), FILE_NAME_AVATAR);

        if (avatarFile.exists())
            avatarFile.delete();

        file.renameTo(avatarFile);

        return avatarFile;
    }

    public boolean hasAvatar() {
        File avatarFile = new File(mContext.getFilesDir(), FILE_NAME_AVATAR);
        return avatarFile.exists();
    }

    public interface OnAvatarUploadListener {
        void onSuccess(File file);

        void onUploadFailure(String error);
    }

    public interface OnAvatarDeleteListener {
        void onDeleteSuccessful();

        void onDeleteFailure();
    }
}
