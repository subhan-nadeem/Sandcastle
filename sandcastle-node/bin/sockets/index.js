/**
 * Holds socket related events
 * Sockets are primarly used for realtime map and chat updates
 *
 */

// TODO MODULARIZE INTO DIFFERENT FILES

const firebase = require('../firebase');
const EVENT_CONNECT = 'connection';// Client emits when connected
const EVENT_DISCONNECT = 'disconnect'; // Client emits when disconnected
const EVENT_STATE_UPDATE = 'appState'; // Client emits when app state is changed
const EVENT_LOCATION_UPDATE = 'locationUpdate'; // Client emits when user's location updates
const EVENT_NEARBY_USERS_UPDATE = 'nearbyUsersUpdate'; // Server emits when nearby users update
const EVENT_JOIN_ROOM = 'joinRoom'; // Client emits when joining a room
const EVENT_LEAVE_ROOM = 'leaveRoom'; // Client emits when leaving a room
const EVENT_MESSAGES = 'chatMessages';  // Server sends messages of a chatroom to client
const EVENT_SEND_MESSAGE = "sendMessage"; // Client emits single message to server
const EVENT_SEND_POST = 'sendPost'; // Client emits single post to server
const EVENT_EMIT_POST = 'sendPost'; // Server emits single post to client
const EVENT_EMIT_MESSAGE = "emitMessage"; // Server emits single message to client
const STATE_MAP = 'stateMap'; // When user is viewing map
const STATE_CHAT = 'stateChat'; // When user is in chat
const NEARBY_RADIUS = 500; // A user is considered nearby at this distance
const TRIGGER_RADIUS = 2000; // The nearby user trigger is fired at this distance


function authenticationMiddleware(io, db) {
// Authentication middleware
    io.use(async function (socket, next) {
        try {
            socket.decoded = await db.verifyAuthToken(socket.handshake.query.token);
            console.log("Socket accepted connection");
            next();
        }
        catch (err) {
            console.error("Socket error validating JWT: " + err);
            next(new Error(err.name));
        }
    });
}


/**
 * Creates pubsub listener for specified owner's notification channel
 */
function initNearbyUsersUpdateListener(pubsub, notificationChannel, usersNearby, socket) {
// Enable pubsub on channel payload
    pubsub.addChannel(notificationChannel, function (updatedUser) {

        console.log(usersNearby);
        console.log(updatedUser);
        let nearbyUsersUpdated;

        // If the updated user is close and user was not previously close, so add to list
        if (updatedUser.is_close && !usersNearby.hasOwnProperty(updatedUser.user_id)) {
            usersNearby[updatedUser.user_id] = updatedUser.name;
            nearbyUsersUpdated = true;
        }

        // Updated user is not close and was previously close, so delete from list
        else if (!updatedUser.is_close && usersNearby.hasOwnProperty(updatedUser.user_id)) {
            delete  usersNearby[updatedUser.user_id];
            nearbyUsersUpdated = true;
        }

        if (nearbyUsersUpdated)
            socket.emit(EVENT_NEARBY_USERS_UPDATE, usersNearby);

    });
}


/**
 * Delete the user's trigger and remove the corresponding pubsub listener
 */
async function deleteUserTrigger(db, pubsub, notificationChannel) {
    try {
        await db.deleteUserLocationTrigger(notificationChannel);
        pubsub.removeChannel(notificationChannel);
        console.log('Trigger successfully deleted');
        return true;
    }
    catch (err) {
        console.error("Error deleting user trigger: " + err);
        return false;
    }
}

async function sendMessagePushNotification(db, message) {
    let registrationTokens;

    // Get all users that belong to the specified chat
    const {rows} = await db.query("SELECT json_agg(fcm_key) as keys\n" +
        "FROM users\n" +
        "WHERE chat_room_id = $1\n" +
        "AND fcm_key IS NOT NULL\n" +
        "AND user_id != $2;",
        [message.chat_room_id, message.user_id]);

    registrationTokens = rows[0].keys;

    await firebase.sendToDevice(firebase.ID_NEW_MESSAGE, registrationTokens, message);
}

module.exports = function (io, db, pubsub) {

    authenticationMiddleware(io, db);

    // Called automatically on connection
    io.on(EVENT_CONNECT, function (socket) {
        console.log(`${socket.decoded.id}: connected`);

        let userID = socket.decoded.id;

        /** Map state sockets **/
        /** ===========================================================================**/

            // Variables for use in STATE_MAP
            // Trigger name & channel name
        let notificationChannel = 'notify_user_' + userID;
        let userState, isUserNearbyTriggerSet, usersNearby;

        let currentRoomID, currentRoomNamespace;

        /**
         * Called when the client's state is changed
         * i.e. client is currently viewing a map vs client is in a chat
         */
        socket.on(EVENT_STATE_UPDATE, async function (state) {
            console.log(`${userID}: State changed: ${state}`);

            const previousState = userState;

            userState = state;

            // Switching out of map state
            if (previousState === STATE_MAP && userState !== STATE_MAP) {
                isUserNearbyTriggerSet = await deleteUserTrigger(db, pubsub, notificationChannel);
            }
            // Switching out of chatroom state
            else if (previousState === STATE_CHAT && userState !== STATE_CHAT) {
                socket.leave(currentRoomNamespace);
            }
        });

        /**
         * Called when a client sends a location update
         */
        socket.on(EVENT_LOCATION_UPDATE, async function (location) {
            console.log(`${userID}: location updated: ${location}`);

            try {
                location = JSON.parse(location);
                await db.updateUserLocation(location.lat, location.lng, userID);
            }
            catch (err) {
                console.error("Error updating user location: " + err);
            }

            // If a user trigger isn't set, that means this is the first location update
            // so a query of all nearby users should be conducted and stored
            if (!isUserNearbyTriggerSet) {
                try {
                    const {rows} = await db.getUsersNearby(location.lat, location.lng,
                        NEARBY_RADIUS, userID);

                    usersNearby = rows[0].users;

                    socket.emit(EVENT_NEARBY_USERS_UPDATE, usersNearby);

                    initNearbyUsersUpdateListener(pubsub, notificationChannel, usersNearby, socket);
                }
                catch (err) {
                    console.error("Error getting nearby users:" + err);
                }
            }

            try {
                await db.updateUserLocationTrigger(notificationChannel,
                    userID, location.lat, location.lng, TRIGGER_RADIUS, NEARBY_RADIUS);

                isUserNearbyTriggerSet = true;
            }
            catch (e) {
                console.error("Error updating user trigger: " + e);
            }
        });


        /** Chat state sockets **/
        /** ===========================================================================**/


        /**
         * Called when client joins room
         */
        socket.on(EVENT_JOIN_ROOM, async function (roomID) {
            console.log(`${userID}: joined room ${roomID}`);

            currentRoomID = roomID;

            currentRoomNamespace = `socket_room_${roomID}`;

            socket.join(currentRoomNamespace);

            await db.addUserToChatroom(roomID, userID);

            try {
                const messages = await db.getChatroomMessages(roomID);

                socket.emit(EVENT_MESSAGES, messages);
            }
            catch (err) {
                console.error(`${userID}: Error getting messages: ${err}`)
            }

            // TODO emit user has joined room

            // TODO implement online/offline feature and emit to client user is online
        });


        /**
         * Called when client sends message
         */
        socket.on(EVENT_SEND_MESSAGE, async function (messageObj) {
            try {
                let message = JSON.parse(messageObj);

                // Ping message owner about confirmed message upload
                // This event is unique to every message
                const EVENT_UPLOAD_CONFIRMATION = message.upload_confirmation_event;

                message = await db.addMessage(currentRoomID, userID, message.message);

                message.username = socket.decoded.name;

                message.avatar_url = await db.getUserAvatarPath(socket.decoded.id);

                await sendMessagePushNotification(db, message);

                // Notify client the message was successfully uploaded
                socket.emit(EVENT_UPLOAD_CONFIRMATION, true);

                // Notify room of message
                socket.broadcast.to(currentRoomNamespace).emit(EVENT_EMIT_MESSAGE, message);
            }
            catch (err) {
                console.error(`${userID}: Error sending message: ${err}`);
            }
        });


        /**
         * Called when client sends post
         */
        socket.on(EVENT_SEND_POST, async function (postObj) {
            try {
                let post = JSON.parse(postObj);

                // Ping post owner about confirmed post upload
                // This event is unique to every post
                const EVENT_UPLOAD_CONFIRMATION = post.upload_confirmation_event;

                post = await db.addPost(currentRoomID, userID, post.message);

                post.username = socket.decoded.name;

                post.avatar_url = await db.getUserAvatarPath(socket.decoded.id);

                await sendMessagePushNotification(db, post);

                // Notify client the post was successfully uploaded
                socket.emit(EVENT_UPLOAD_CONFIRMATION, true);

                // Notify room of post
                socket.broadcast.to(currentRoomNamespace).emit(EVENT_EMIT_POST, post);
            }
            catch (err) {
                console.error(`${userID}: Error sending post: ${err}`);
            }
        });


        // Called on client disconnect
        socket.on(EVENT_DISCONNECT, async function () {
            console.log(`${userID}: disconnected`);
            if (isUserNearbyTriggerSet) {
                isUserNearbyTriggerSet = await deleteUserTrigger(db, pubsub, notificationChannel);
            }
        });
    })
};