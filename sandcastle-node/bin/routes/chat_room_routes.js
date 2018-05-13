/**
 * Holds routes related to chatroom data
 * GET /v1/chat_rooms
 * GET /v1/chat_rooms/:id
 * GET /v1/chat_rooms/:id/users
 * POST /v1/chat_rooms/create
 */
const firebase = require('../firebase');
const CHAT_ROOM_MAX_RADIUS = 5000;


module.exports = function (app, db) {

    // Get all chat rooms in defined radius API
    app.get('/v1/chat_rooms', async (req, res) => {

        // $1 = user lat, $2 = user lng, $3 = radius
        // Query retrieves chat room & all who are at currently at the chatroom
        const rooms = await db.getAllRoomsWithinDistance(req.query.lat, req.query.lng, CHAT_ROOM_MAX_RADIUS);

        res.status(200).send({chat_rooms: rooms});
    });

    // Access specific chat room API
    app.get('/v1/chat_rooms/:id', async (req, res) => {
        const room_info = await db.getChatroom(req.params.id);

        const messages = await db.getChatroomMessages(req.params.id);

        res.send({
            success: true,
            room: room_info,
            messages: messages
        });
    });

    // Create new chat room API
    app.post('/v1/chat_rooms/create', async (req, res) => {

        const pushNewRoomToUsers = async function (chat_room) {
            let registrationTokens = [];

            // Get all users within the defined radius of the chatroom & send them a push
            // $1 = user lat, $2 = user lng, $3 = radius
            const {rows} = await db.query("SELECT fcm_key\n" +
                "FROM users \n" +
                "WHERE earth_box(ll_to_earth($1,$2), $3) @> ll_to_earth(lat, lng);",
                [chat_room.lat, chat_room.lng, CHAT_ROOM_MAX_RADIUS]);

            console.log(rows);

            await firebase.sendToDevice(firebase.ID_NEW_ROOM, registrationTokens, chat_room);
        };

        const chat_room = await db.createChatroom(req.body.lat, req.body.lng, req.body.name);

        res.status(201).send({message: "Chat created successfully", chat_room: chat_room});

        pushNewRoomToUsers(chat_room);
    });

};