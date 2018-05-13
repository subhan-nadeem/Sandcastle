package com.subhan_nadeem.sandcastle.features.new_room;

import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.text.TextUtils;
import android.view.View;

/**
 * Created by Subhan Nadeem on 2017-11-04.
 * Handles new room creation
 */

public class NewRoomSheetHandler {

    private static final int MIN_CASTLE_NAME_CHARACTER_LENGTH = 4;
    private final View mFabAdd;
    private final View mFabLocate;
    private final BottomSheetBehavior<View> mBottomSheetBehavior;
    private final NewRoomListener mListener;
    private ObservableField<String> mCastleNameError = new ObservableField<>();
    private ObservableField<String> mCastleName = new ObservableField<>();
    private boolean mIsOpen;


    public NewRoomSheetHandler(View bottomsheetView, View fabAdd, View fabLocate, NewRoomListener listener) {
        mFabAdd = fabAdd;
        mFabLocate = fabLocate;
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomsheetView);
        mListener = listener;

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                // this part hides the button immediately and waits bottom sheet
                // to collapse to show
                if (mIsOpen && BottomSheetBehavior.STATE_SETTLING == newState) {
                    animateFABHide();
                } else if (!mIsOpen && BottomSheetBehavior.STATE_SETTLING == newState) {
                    animateFABShow();
                }
                else if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    mIsOpen = false;
                    animateFABShow();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

    public ObservableField<String> getCastleName() {
        return mCastleName;
    }

    public void setCastleName(ObservableField<String> castleName) {
        mCastleName = castleName;
    }

    private boolean isValidData() {
        if (TextUtils.isEmpty(mCastleName.get())
                || mCastleName.get().length() < MIN_CASTLE_NAME_CHARACTER_LENGTH) {
            mCastleNameError.set("Castle name must be at least " +
                    MIN_CASTLE_NAME_CHARACTER_LENGTH + " characters");
            return false;
        }
        mCastleNameError.set(null);
        return true;
    }


    public void onDoneClicked() {
        if (isValidData()) {
            mListener.onNewRoomRequested(mCastleName.get());
            closeSheet();
        }
    }

    public void onCloseClicked() {
        mCastleName.set(null);
        mCastleNameError.set(null);
        closeSheet();
    }

    private void closeSheet() {
        mIsOpen = false;
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void onOpenClicked() {
        mIsOpen = true;
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void animateFABHide() {
        mFabAdd.animate().scaleX(0).scaleY(0).setDuration(300).start();
        mFabLocate.animate().scaleX(0).scaleY(0).setDuration(300).start();
    }

    private void animateFABShow() {
        mFabAdd.animate().scaleX(1).scaleY(1).setDuration(300).start();
        mFabLocate.animate().scaleX(1).scaleY(1).setDuration(300).start();
    }


    public void setCastleNameError(ObservableField<String> castleNameError) {
        mCastleNameError = castleNameError;
    }

    public ObservableField<String> getCastleNameError() {
        return mCastleNameError;
    }

    public interface NewRoomListener {
        void onNewRoomRequested(String roomName);
    }
}
