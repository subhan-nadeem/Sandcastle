package com.subhan_nadeem.sandcastle.features.chat_room;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.subhan_nadeem.sandcastle.databinding.ItemChatOtherBinding;
import com.subhan_nadeem.sandcastle.databinding.ItemChatSelfBinding;
import com.subhan_nadeem.sandcastle.models.Message;

import java.util.Collections;
import java.util.List;

/**
 * Created by Subhan Nadeem on 2017-10-15.
 * RecyclerView adapter handling messages
 */
class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private List<Message> mMessageList = Collections.emptyList();

    void setMessages(List<Message> messages) {
        mMessageList = messages;
        notifyDataSetChanged();
    }


    public int getItemViewType(int position) {
        Message message = mMessageList.get(position);

        if (message.isSelf()) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            ItemChatSelfBinding itemBinding =
                    ItemChatSelfBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent, false);
            return new SentMessageHolder(itemBinding);
        } else {
            ItemChatOtherBinding itemBinding =
                    ItemChatOtherBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent, false);
            return new ReceivedMessageHolder(itemBinding);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).mBinding.setMessage(message);
                ((SentMessageHolder) holder).mBinding.executePendingBindings();
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).mBinding.setMessage(message);
                ((ReceivedMessageHolder) holder).mBinding.executePendingBindings();
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    class SentMessageHolder extends RecyclerView.ViewHolder {

        private final ItemChatSelfBinding mBinding;

        SentMessageHolder(ItemChatSelfBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }

    class ReceivedMessageHolder extends RecyclerView.ViewHolder {

        private final ItemChatOtherBinding mBinding;

        ReceivedMessageHolder(ItemChatOtherBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }
}
