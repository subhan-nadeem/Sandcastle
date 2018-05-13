package com.subhan_nadeem.sandcastle.features.auth;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.subhan_nadeem.sandcastle.R;
import com.subhan_nadeem.sandcastle.databinding.FragmentProfilePhotoBinding;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

/**
 * Created by Subhan Nadeem on 2017-11-01.
 * Handles profile picture upload after registration
 */

public class ProfilePhotoFragment extends BaseAuthenticationFragment {

    private static final int MY_REQUEST_IMAGES = 1002;
    private FragmentProfilePhotoBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_photo, container,
                false);

        mBinding.setView(this);

        mBinding.setViewModel(ViewModelProviders.of(this).get(ProfilePhotoViewModel.class));

        return mBinding.getRoot();
    }

    public static android.support.v4.app.Fragment newInstance() {
        return new ProfilePhotoFragment();
    }

    public void onSkipClick() {
        mAuthenticationChangeListener.onLoggedIn();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_REQUEST_IMAGES: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startCropActivity();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    public void onYesClick() {
        if (hasImagePermissions()) {
            startCropActivity();
        }
    }

    private void startCropActivity() {
        CropImage.activity(null)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(getContext(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (result == null) return;

            animateLoading();

            ViewModelProviders.of(this).get(ProfilePhotoViewModel.class)
                    .uploadAvatar(result.getUri().getPath());
        }
    }

    private void animateLoading() {
        mBinding.profilePictureLottie.setAnimation("snap_loader_white.json");
        mBinding.profilePictureLottie.loop(true);
        mBinding.profilePictureLottie.playAnimation();
        mBinding.profilePictureYes.setVisibility(View.GONE);
        mBinding.profilePictureSkip.setVisibility(View.GONE);

        mBinding.uploadPictureTextview.setText(R.string.uploading_picture);
    }

    private boolean hasImagePermissions() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_REQUEST_IMAGES);
            return false;
        }
        return true;
    }


    @Override
    void subscribeUI() {
        ProfilePhotoViewModel viewModel =
                ViewModelProviders.of(this).get(ProfilePhotoViewModel.class);

        viewModel.getAvatarUploaded().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isUploaded) {
                if (isUploaded)
                    animateDone();
            }
        });

        viewModel.getAvatarFile().observe(this, new Observer<File>() {
            @Override
            public void onChanged(@Nullable File file) {
                Glide.with(getContext())
                        .load(file)
                        .apply(new RequestOptions()
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE))
                        .into(mBinding.profileImageView);
            }
        });
    }

    private void animateDone() {
        Animation slideOutAnim = AnimationUtils.loadAnimation(getContext(), R.anim.exit_to_right);
        slideOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mBinding.profilePictureLottie.setVisibility(View.GONE);
                int delayToLoginMS = 1000;
                mBinding.profilePictureLottie.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAuthenticationChangeListener.onLoggedIn();
                    }
                }, delayToLoginMS);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBinding.profileImageView.setVisibility(View.VISIBLE);
        mBinding.profileImageView
                .setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.enter_from_left));
        mBinding.uploadPictureTextview.setText(R.string.profile_picture_upload_successful);
        mBinding.profilePictureLottie.setAnimation(slideOutAnim);
    }

    @Override
    public void onSwitchModeClick() {
// Nothing here, no switch button
    }
}
