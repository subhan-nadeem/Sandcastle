package com.subhan_nadeem.sandcastle.features.chat_room;

import com.subhan_nadeem.sandcastle.models.Message;

import java.util.List;

/**
 * Created by Subhan Nadeem on 2017-10-29.
 * Handles received messages from backend
 */

public interface MessageReceivedListener {
    void onMessagesReceived(List<Message> messageList);
    void onNewMessageReceived(Message message);
}
