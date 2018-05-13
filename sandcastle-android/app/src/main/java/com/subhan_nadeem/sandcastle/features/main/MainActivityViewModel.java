package com.subhan_nadeem.sandcastle.features.main;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.ObservableBoolean;
import android.support.annotation.NonNull;

import com.subhan_nadeem.sandcastle.utils.ProfilePhotoManager;

import java.io.File;

public class MainActivityViewModel extends AndroidViewModel {

    private final ProfilePhotoManager mProfilePhotoManager;
    private MutableLiveData<Boolean> mUserHasPhoto = new MutableLiveData<>();
    private MutableLiveData<Boolean> mAvatarChangeUploaded = new MutableLiveData<>();
    private ObservableBoolean mAvatarUploading = new ObservableBoolean(false);

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        mProfilePhotoManager = new ProfilePhotoManager(application);

        mUserHasPhoto.setValue(mProfilePhotoManager.hasAvatar());
    }

    /**
     * Changed if avatar upload change is in progress
     */
    public ObservableBoolean getAvatarUploading() {
        return mAvatarUploading;
    }

    /**
     * Set true if change upload is successful, false otherwise
     */
    MutableLiveData<Boolean> getAvatarChangeUploaded() {
        return mAvatarChangeUploaded;
    }


    /**
     * Is set true if user has a profile picture, false otherwise
     */
    MutableLiveData<Boolean> getUserHasPhoto() {
        return mUserHasPhoto;
    }

    void uploadAvatar(String path) {
        mAvatarUploading.set(true);

        mProfilePhotoManager.uploadAvatar(path, new ProfilePhotoManager.OnAvatarUploadListener() {
            @Override
            public void onSuccess(File file) {
                mUserHasPhoto.setValue(true);
                mAvatarChangeUploaded.setValue(true);
                mAvatarUploading.set(false);
            }

            @Override
            public void onUploadFailure(String error) {
                mAvatarUploading.set(false);
                mAvatarChangeUploaded.setValue(false);
            }
        });
    }

    void deleteAvatar() {
        mAvatarUploading.set(true);
        mProfilePhotoManager.deleteUserAvatarFile(new ProfilePhotoManager.OnAvatarDeleteListener() {
            @Override
            public void onDeleteSuccessful() {
                mUserHasPhoto.setValue(false);
                mAvatarChangeUploaded.setValue(true);
                mAvatarUploading.set(false);
            }

            @Override
            public void onDeleteFailure() {
                mAvatarUploading.set(false);
                mAvatarChangeUploaded.setValue(false);
            }
        });
    }


    File getUserAvatarFile() {
        return mProfilePhotoManager.getUserAvatarFile();
    }
}
