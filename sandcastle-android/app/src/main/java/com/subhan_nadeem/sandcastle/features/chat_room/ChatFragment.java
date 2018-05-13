package com.subhan_nadeem.sandcastle.features.chat_room;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.subhan_nadeem.sandcastle.R;
import com.subhan_nadeem.sandcastle.data.RealmHelper;
import com.subhan_nadeem.sandcastle.databinding.FragmentChatRoomBinding;
import com.subhan_nadeem.sandcastle.models.ChatRoom;
import com.subhan_nadeem.sandcastle.models.Message;

import java.util.List;


public class ChatFragment extends Fragment {

    private final static String KEY_CHATROOM = "KEY_CHATROOM";
    private final static String KEY_NEWLY_JOINED = "KEY_NEWLY_JOINED";
    private FragmentChatRoomBinding mBinding;
    private ChatMessageAdapter mMessageAdapter;
    private boolean mNewlyJoined;

    public static ChatFragment newInstance(ChatRoom chatRoom) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(KEY_CHATROOM, new Gson().toJson(chatRoom));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_maps, menu);
    }

    @Override
    public void onPause() {
        super.onPause();

        ChatViewModel viewModel = ViewModelProviders.of(this).get(ChatViewModel.class);

        viewModel.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        ChatViewModel viewModel = ViewModelProviders.of(this).get(ChatViewModel.class);

        viewModel.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        ChatRoom chatRoom
                = new Gson().fromJson(getArguments().getString(KEY_CHATROOM), ChatRoom.class);

        long chatRoomID = chatRoom.getChatRoomId();

        ChatViewModel viewModel = ViewModelProviders.of(this).get(ChatViewModel.class);

        viewModel.setChatroomID(chatRoomID);

        RealmHelper repository = new RealmHelper();

        Long userChatroom = repository.getUser().getChatroomID();

        if (userChatroom == null || userChatroom != chatRoomID) {
            mNewlyJoined = true;
            repository.setUserChatroomID(chatRoomID);
        }

        subscribeUI(viewModel);
    }

    private void subscribeUI(ChatViewModel viewModel) {
        viewModel.getMessages().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> messages) {
                mMessageAdapter.setMessages(messages);
            }
        });

        viewModel.getNewMessageAdded().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean newMessageAdded) {
                mMessageAdapter.notifyItemInserted(mMessageAdapter.getItemCount() - 1);
                scrollToBottomOfChat();
            }
        });

        viewModel.getMessageSent().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean messageChanged) {
                mBinding.contentChat.editTextMessage.getText().clear();
            }
        });
    }


    /**
     * Scrolls to the bottom of the chat recyclerview
     */
    private void scrollToBottomOfChat() {
        mBinding.contentChat.chatRecyclerview.smoothScrollToPosition(mMessageAdapter.getItemCount() - 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_room, container, false);

        mBinding.setView(this);

        mBinding.setViewModel(ViewModelProviders.of(this).get(ChatViewModel.class));

        mMessageAdapter = new ChatMessageAdapter();
        mBinding.contentChat.chatRecyclerview.setAdapter(mMessageAdapter);

        if (mNewlyJoined) {
            Snackbar.make(mBinding.coordinatorChat,
                    "You will now get notifications from this sandcastle " +
                    "until you leave or are no longer nearby", Snackbar.LENGTH_LONG).show();
            mNewlyJoined = false;
        }
        return mBinding.getRoot();
    }
}
