package com.subhan_nadeem.sandcastle.models.responses;

import com.google.gson.annotations.SerializedName;
import com.subhan_nadeem.sandcastle.models.ChatRoom;

import java.util.List;

@SuppressWarnings("unused")
public class RoomsResponse extends BaseResponse {

    @SerializedName("chat_rooms")
    private List<ChatRoom> mChatRooms;

    public List<ChatRoom> getChatRooms() {
        return mChatRooms;
    }

    public void setChatRooms(List<ChatRoom> chatRooms) {
        mChatRooms = chatRooms;
    }

}
