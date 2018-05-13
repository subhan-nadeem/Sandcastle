
package com.subhan_nadeem.sandcastle.models.responses;

import com.google.gson.annotations.SerializedName;
import com.subhan_nadeem.sandcastle.models.ChatRoom;

@SuppressWarnings("unused")
public class NewRoomResponse extends BaseResponse {

    @SerializedName("chat_room")
    private ChatRoom mChatRoom;
    @SerializedName("message")

    public ChatRoom getChatRoom() {
        return mChatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        mChatRoom = chatRoom;
    }

}
