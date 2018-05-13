package com.subhan_nadeem.sandcastle.features.auth;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import com.subhan_nadeem.sandcastle.data.RealmHelper;
import com.subhan_nadeem.sandcastle.models.User;
import com.subhan_nadeem.sandcastle.utils.ProfilePhotoManager;

import java.io.File;


public class ProfilePhotoViewModel extends AndroidViewModel {
    private final ProfilePhotoManager mProfilePhotoManager;
    private ObservableField<String> mUsername = new ObservableField<>();
    private MutableLiveData<File> mAvatarFile = new MutableLiveData<>();
    private MutableLiveData<Boolean> mAvatarUploaded = new MutableLiveData<>();

    public ProfilePhotoViewModel(@NonNull Application application) {
        super(application);
        RealmHelper realmHelper = new RealmHelper();
        User user = realmHelper.getUser();
        mUsername.set(user.getName());
        mProfilePhotoManager = new ProfilePhotoManager(application);
    }

    MutableLiveData<File> getAvatarFile() {
        return mAvatarFile;
    }

    MutableLiveData<Boolean> getAvatarUploaded() {
        return mAvatarUploaded;
    }

    void uploadAvatar(String imagePath) {

        mProfilePhotoManager.uploadAvatar(imagePath,
                new ProfilePhotoManager.OnAvatarUploadListener() {
                    @Override
                    public void onSuccess(File file) {
                        mAvatarFile.setValue(file);
                        mAvatarUploaded.setValue(true);
                    }

                    @Override
                    public void onUploadFailure(String error) {

                        mAvatarUploaded.setValue(false);
                    }
                });
    }

    public ObservableField<String> getUsername() {
        return mUsername;
    }
}
