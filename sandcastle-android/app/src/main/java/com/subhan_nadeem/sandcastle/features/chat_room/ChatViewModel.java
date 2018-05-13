package com.subhan_nadeem.sandcastle.features.chat_room;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.text.TextUtils;

import com.subhan_nadeem.sandcastle.App;
import com.subhan_nadeem.sandcastle.models.Message;

import org.joda.time.DateTime;

import java.util.List;

public class ChatViewModel extends AndroidViewModel implements MessageReceivedListener {
    private MutableLiveData<List<Message>> mMessages = new MutableLiveData<>();
    private MutableLiveData<Boolean> mNewMessageAdded = new MutableLiveData<>();
    private MutableLiveData<Boolean> mMessageSent = new MutableLiveData<>();
    private ObservableField<String> mMessageText = new ObservableField<>();

    public ObservableBoolean getMessagesLoaded() {
        return mMessagesLoaded;
    }

    private ObservableBoolean mMessagesLoaded = new ObservableBoolean(false);
    private final ChatSocketController mChatSocketController;

    public ChatViewModel(Application application) {
        super(application);
        mChatSocketController = new ChatSocketController(((App) application).getConnectedSocket(), this);
    }

    MutableLiveData<Boolean> getMessageSent() {
        return mMessageSent;
    }

    MutableLiveData<Boolean> getNewMessageAdded() {
        return mNewMessageAdded;
    }

    public ObservableField<String> getMessageText() {
        return mMessageText;
    }

    MutableLiveData<List<Message>> getMessages() {
        return mMessages;
    }

    /**
     * Should only be called once, right after Fragment creation
     */
    void setChatroomID(long chatroomID) {
        mChatSocketController.setChatroomID(chatroomID);
    }

    void onResume() {
        mChatSocketController.onResume();
    }

    public void sendMessage() {
        // Check if string is null, empty, or just whitespace
        if (TextUtils.isEmpty(mMessageText.get()) || TextUtils.isEmpty(mMessageText.get().trim())) {
            return;
        }

        // TODO notify user about lack of connection and/or queue message
        if (mChatSocketController.getSocket() == null) return;

        Message message = new Message(new DateTime().toString(), mMessageText.get());
        message.setUploadConfirmationListener(mChatSocketController.getSocket().get());
        addMessage(message);

        mChatSocketController.sendMessage(message.getMessage(),
                message.getUploadConfirmationEvent());

        mMessageSent.setValue(true);
    }

    @Override
    public void onMessagesReceived(List<Message> messageList) {
        mMessages.postValue(messageList);
        mMessagesLoaded.set(true);
    }

    @Override
    public void onNewMessageReceived(Message message) {
        addMessage(message);
    }

    private void addMessage(Message message) {
        mMessages.getValue().add(message);
        mNewMessageAdded.postValue(true);
    }

    void onPause() {
        mChatSocketController.onPause();
    }
}