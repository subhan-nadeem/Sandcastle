package com.subhan_nadeem.sandcastle.features.new_room;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableField;

import com.google.android.gms.maps.model.LatLng;
import com.subhan_nadeem.sandcastle.models.ChatRoom;

public class NewRoomViewModel extends ViewModel {
    private ObservableField<String> mNameError = new ObservableField<>();
    private ObservableField<String> mName = new ObservableField<>();
    private MutableLiveData<Boolean> mCreatingNewRoom = new MutableLiveData<>();
    private MutableLiveData<LatLng> mUserLatLng = new MutableLiveData<>();
    private MutableLiveData<ChatRoom> newChatroom = new MutableLiveData<>();

    MutableLiveData<ChatRoom> getNewChatroom() {
        return newChatroom;
    }

    MutableLiveData<Boolean> getCreatingNewRoom() {
        return mCreatingNewRoom;
    }

    public ObservableField<String> getName() {
        return mName;
    }

    public void setName(ObservableField<String> name) {
        mName = name;
    }

    public ObservableField<String> getNameError() {
        return mNameError;
    }

    public void validateName() {
    }

    private void createNewRoom() {

    }

    void setUserLatLng(LatLng userLatLng) {
        mUserLatLng.setValue(userLatLng);
    }
}
