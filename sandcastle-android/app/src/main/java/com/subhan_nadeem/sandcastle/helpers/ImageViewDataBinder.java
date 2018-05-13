package com.subhan_nadeem.sandcastle.helpers;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.subhan_nadeem.sandcastle.GlideApp;
import com.subhan_nadeem.sandcastle.R;

/**
 * Created by Subhan Nadeem on 2017-11-18.
 * Handles binding adapters pertaining to imageviews
 */

public class ImageViewDataBinder {
    @BindingAdapter("imageURL")
    public static void setImageUrl(ImageView imageView, String avatarURL) {

        if (avatarURL == null) {
            GlideApp.with(imageView.getContext())
                    .load(R.drawable.placeholder_profile)
                    .into(imageView);
        } else {
            String BUCKET_PREFIX = "https://sandcastle-avatars.s3.amazonaws.com/";
            String url = BUCKET_PREFIX + avatarURL;

            GlideApp.with(imageView.getContext())
                    .load(url)
                    .placeholder(R.drawable.placeholder_profile)
                    .error(R.drawable.placeholder_profile)
                    .into(imageView);
        }
    }
}
