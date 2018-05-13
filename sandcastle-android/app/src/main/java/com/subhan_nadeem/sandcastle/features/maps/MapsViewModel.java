package com.subhan_nadeem.sandcastle.features.maps;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;
import android.support.annotation.NonNull;

import com.subhan_nadeem.sandcastle.App;
import com.subhan_nadeem.sandcastle.models.ChatRoom;
import com.subhan_nadeem.sandcastle.models.responses.NewRoomResponse;
import com.subhan_nadeem.sandcastle.models.responses.RoomsResponse;
import com.subhan_nadeem.sandcastle.network.APIService;
import com.subhan_nadeem.sandcastle.network.APIUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Subhan Nadeem on 2017-10-10.
 * ViewModel pertaining to Maps fragment
 */

public class MapsViewModel extends AndroidViewModel implements OnNearbyUsersUpdateListener {

    // If user is within this range of the chatroom, don't let them make a new room
    static final int CHATROOM_RANGE_METRES = 100;
    private MutableLiveData<Integer> mNumUsersNearby = new MutableLiveData<>();
    private final APIService mAPIService;
    private final MutableLiveData<ChatRoom> mClosestChatroom = new MutableLiveData<>();
    private MutableLiveData<Location> mLatestLocation = new MutableLiveData<>();
    private MutableLiveData<List<ChatRoom>> mChatRooms = new MutableLiveData<>();

    MutableLiveData<ChatRoom> getNewChatroom() {
        return newChatroom;
    }

    private MutableLiveData<ChatRoom> newChatroom = new MutableLiveData<>();
    private final MapsSocketController mSocketController;

    public MapsViewModel(Application application) {
        super(application);

        mSocketController = new MapsSocketController(((App) application).getConnectedSocket(),
                this);
        mAPIService = APIUtils.getAPIService();
    }

    MutableLiveData<List<ChatRoom>> getChatRooms() {
        return mChatRooms;
    }

    void setLatestLocation(final Location location) {
        mLatestLocation.setValue(location);

        mSocketController.emitLocation(location.getLatitude(), location.getLongitude());

        getNearestChatrooms();
    }

    private void getNearestChatrooms() {
        mAPIService.getChatrooms(mLatestLocation.getValue().getLatitude(),
                mLatestLocation.getValue().getLongitude()).enqueue(new Callback<RoomsResponse>() {
            @Override
            public void onResponse(@NonNull Call<RoomsResponse> call,
                                   @NonNull Response<RoomsResponse> response) {
                if (response.isSuccessful()) {
                    RoomsResponse roomsResponse = response.body();
                    ChatRoom closestChatroom = roomsResponse.getChatRooms().size() == 0 ? null :
                            roomsResponse.getChatRooms().get(0);

                    mClosestChatroom.setValue(closestChatroom);

                    addChatRooms(response.body().getChatRooms());
                }
            }

            @Override
            public void onFailure(Call<RoomsResponse> call, Throwable t) {

            }
        });
    }

    private void addChatRooms(List<ChatRoom> chatRooms) {
        mChatRooms.setValue(chatRooms);
    }

    /**
     * @return true if user is within the defined chat room radius of another chat, false otherwise
     */
    boolean isInRangeOfChat() {

        if (mClosestChatroom.getValue() != null && mLatestLocation.getValue() != null) {

            Location chatroomLocation = new Location("");
            chatroomLocation.setLatitude(mClosestChatroom.getValue().getLat());
            chatroomLocation.setLongitude(mClosestChatroom.getValue().getLng());

            return mLatestLocation.getValue().distanceTo(chatroomLocation) <= CHATROOM_RANGE_METRES;
        }

        // There is no closest chatroom, so not in range of any chat
        return false;
    }


    MutableLiveData<Integer> getNumUsersNearby() {
        return mNumUsersNearby;
    }

    void onResume() {
        mSocketController.onResume();
    }

    void onPause() {
        mSocketController.onPause();
    }

    @Override
    public void onNearbyUsersUpdated(int numUsersNearby) {
        mNumUsersNearby.postValue(numUsersNearby);
    }

    boolean userLocationRetrieved() {
        return !(mLatestLocation.getValue() == null);
    }

    void createNewRoom(String roomName) {

        mAPIService.createChatRoom(
                mLatestLocation.getValue().getLatitude(),
                mLatestLocation.getValue().getLongitude(),
                roomName)
                .enqueue(new Callback<NewRoomResponse>() {
                    @Override
                    public void onResponse(Call<NewRoomResponse> call,
                                           Response<NewRoomResponse> response) {
                        if (response.isSuccessful()) {
                            newChatroom.setValue(response.body().getChatRoom());
                        }
                    }

                    @Override
                    public void onFailure(Call<NewRoomResponse> call, Throwable t) {

                    }
                });
    }
}
