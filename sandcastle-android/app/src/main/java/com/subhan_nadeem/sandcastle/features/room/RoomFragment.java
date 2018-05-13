package com.subhan_nadeem.sandcastle.features.room;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.subhan_nadeem.sandcastle.R;

/**
 * Created by Subhan Nadeem on 2018-01-13.
 * Handles posts &
 */

public class RoomFragment extends Fragment{


    private FragmentRoomBinding mBinding;

    public static RoomFragment newInstance() {
        return new RoomFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_room, container, false);

        return mBinding.getRoot();
    }
}
