
package com.subhan_nadeem.sandcastle.models.responses;

import com.google.gson.annotations.SerializedName;
import com.subhan_nadeem.sandcastle.models.ChatRoom;
import com.subhan_nadeem.sandcastle.models.Message;

import java.util.List;

@SuppressWarnings("unused")
public class ChatRoomResponse {

    @SerializedName("messages")
    private List<Message> mMessages;
    @SerializedName("room")
    private ChatRoom mRoom;

    public List<Message> getMessages() {
        return mMessages;
    }

    public void setMessages(List<Message> messages) {
        mMessages = messages;
    }

    public ChatRoom getRoom() {
        return mRoom;
    }

    public void setRoom(ChatRoom room) {
        mRoom = room;
    }

}
