package com.subhan_nadeem.sandcastle.features.maps;

import com.subhan_nadeem.sandcastle.features.chat_room.ChatFragment;

/**
 * Created by Subhan Nadeem on 2017-10-14.
 * Listener implemented by activity holding MapsFragment
 */

public interface OpenChatroomListener {
    void onChatroomClicked(ChatFragment chatFragment, String roomName);
}
