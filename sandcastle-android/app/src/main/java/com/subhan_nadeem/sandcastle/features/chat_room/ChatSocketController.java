package com.subhan_nadeem.sandcastle.features.chat_room;

import android.databinding.ObservableField;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.subhan_nadeem.sandcastle.models.BaseSocketController;
import com.subhan_nadeem.sandcastle.models.Message;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Subhan Nadeem on 2017-10-29.
 * Controls chat socket interactions
 * i.e. sending/receiving messages
 */

@SuppressWarnings("ConstantConditions")
class ChatSocketController extends BaseSocketController {

    private static final String STATE_CHAT = "stateChat"; // When user is in chat
    private static final String EVENT_MESSAGES = "chatMessages";
    private static final String EVENT_EMIT_MESSAGE = "emitMessage";
    private static final String EVENT_JOIN_ROOM = "joinRoom";
    private static final String EVENT_SEND_MESSAGE = "sendMessage";
    private final MessageReceivedListener mMessageCallback;
    private final Emitter.Listener mMessagesListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONArray jsonArray = (JSONArray) args[0];
            Type messageListType = new TypeToken<ArrayList<Message>>() {
            }.getType();

            List<Message> messages = new Gson().fromJson(jsonArray.toString(), messageListType);

            mMessageCallback.onMessagesReceived(messages);
        }
    };
    private final Emitter.Listener mNewMessageListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];

            Message message = new Gson().fromJson(jsonObject.toString(), Message.class);

            mMessageCallback.onNewMessageReceived(message);
        }
    };
    private long mChatroomID;

    ChatSocketController(ObservableField<Socket> socket, MessageReceivedListener listener) {
        super(socket, STATE_CHAT);
        mMessageCallback = listener;
    }

    void setChatroomID(long chatroomID) {
        mChatroomID = chatroomID;
    }

    @Override
    protected void onResumeChild() {
        mSocket.get().emit(EVENT_JOIN_ROOM, mChatroomID); // Notify server about joining
        mSocket.get().on(EVENT_EMIT_MESSAGE, mNewMessageListener);
        mSocket.get().on(EVENT_MESSAGES, mMessagesListener);
    }

    @Override
    protected void onPauseChild() {
        mSocket.get().off(EVENT_EMIT_MESSAGE);
        mSocket.get().off(EVENT_MESSAGES);
    }

    void sendMessage(String messageText, String uploadConfirmationEvent) {
        String KEY_MESSAGE = "message";
        String KEY_UPLOAD_CONFIRAMTION_EVENT = "upload_confirmation_event";

        JsonObject messageObj = new JsonObject();

        messageObj.addProperty(KEY_MESSAGE, messageText);
        messageObj.addProperty(KEY_UPLOAD_CONFIRAMTION_EVENT, uploadConfirmationEvent);

        mSocket.get().emit(EVENT_SEND_MESSAGE, messageObj.toString());
    }
}
