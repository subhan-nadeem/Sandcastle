/**
 * Module that handles database & encryption
 */

// pg
const {Pool} = require('pg');
const pool = new Pool(
    {
        user: process.env.DB_USER,
        host: process.env.DB_HOST,
        database: process.env.DB_NAME,
        password: process.env.DB_PASSWORD,
        port: process.env.DB_PORT
    }
);
const escape = require('pg-escape');

// bcrypt
const bcrypt = require('bcrypt');
const saltRounds = 10;

// jwt
const jwt = require('jsonwebtoken');

module.exports = {
    query: (text, params) => pool.query(text, params),

    /**
     * Checks to see if a given user exists in the database
     * @param username Given username
     * @returns {Promise.<boolean>}
     */
    userExists: async function (username) {
        const {rows} = await pool.query("SELECT 1 FROM users WHERE name = $1 LIMIT 1;", [username]);
        return rows.length > 0;
    },


    addNewUser: (username, hash) => pool.query("INSERT INTO users(name, password_hash) values($1, $2)",
        [username, hash]),

    /**
     * Returns a user object retrieved from the database
     * @param username Name to query
     */
    getUserFromName: async function (username) {
        const {rows} = await pool.query("SELECT name, password_hash, user_id FROM users WHERE name = $1 LIMIT 1;",
            [username]);

        return rows.length === 0 ? null : rows[0];
    },

    updateUserLocation: (lat, lng, user_id) => pool.query("UPDATE users SET lat=$1, lng=$2 WHERE user_id=$3;",
        [lat, lng, user_id]),


    getUsersNearby: (lat, lng, specified_radius, owner_id) => pool.query(
        // Return empty json object instead of null if there are no results
        "SELECT coalesce(json_object_agg(user_id, users.name), '{}'::json) as users\n" +
        "FROM users\n" +
        "WHERE earth_box(ll_to_earth($1, $2), $3) @> ll_to_earth(lat, lng)\n" +
        "AND user_id != $4;",
        [lat, lng, specified_radius, owner_id]),

    addUserToChatroom: (chatRoomID, userID) =>
        pool.query("UPDATE users SET chat_room_id = $1 WHERE user_id = $2;",
            [chatRoomID, userID]),

    removeUserFromChatroom: (userID) =>
        pool.query("UPDATE users SET chat_room_id = NULL WHERE user_id = $1;",
            [userID]),

    deleteUserLocationTrigger: (trigger_name) => pool.query(`DROP TRIGGER IF EXISTS ${trigger_name} on users;`),

    /**
     * SQL transaction creating/updating trigger function for the user
     */
    updateUserLocationTrigger: async function (notification_channel, owner_id, owner_lat, owner_lng,
                                               trigger_radius, nearby_radius) {
        const client = await pool.connect();

        try {
            await client.query('BEGIN');

            // Drop any existing triggers
            await client.query("DROP TRIGGER IF EXISTS " + notification_channel + " ON users;");

            const triggerString = escape('CREATE TRIGGER %s\n' +
                'AFTER INSERT OR UPDATE OR DELETE\n' +
                'ON users\n' +
                'FOR EACH ROW\n' +
                'EXECUTE PROCEDURE notify_on_user_location_update(\'%s\', \'%s\', \'%s\', \'%s\', \'%s\',\'%s\');',
                notification_channel, notification_channel, owner_id, owner_lat, owner_lng, trigger_radius, nearby_radius);

            // Create a trigger that is fired every time a user is updated
            await client.query(triggerString);

            await client.query('COMMIT')
        } catch (e) {
            await client.query('ROLLBACK');
            throw e
        } finally {
            client.release()
        }
    },

    updateFCMKey: (fcm_key, user_id) => pool.query("UPDATE users SET fcm_key=$1 WHERE user_id=$2;",
        [fcm_key, user_id]),

    getAuthToken: (dataToBeEncrypted) => jwt.sign(dataToBeEncrypted, process.env.JWT_SECRET, {
        expiresIn: '30 minutes'
    }),
    verifyAuthToken: (token) => jwt.verify(token, process.env.JWT_SECRET),
    hash: (password) => bcrypt.hash(password, saltRounds),
    isValidPassword: (password, hash) => bcrypt.compare(password, hash),
    storeRefreshToken: async function (user_id, refresh_hash, device_name) {
        await pool.query("INSERT INTO auth (user_id, refresh_hash, device_name) values($1, $2, $3);",
            [user_id, refresh_hash, device_name]);
    },
    getRefreshTokenEntry: async function (refresh_hash) {
        const {rows} = await pool.query("SELECT auth.*, users.name" +
            " FROM auth JOIN users USING (user_id) WHERE refresh_hash = $1;",
            [refresh_hash]);

        return rows[0];
    },
    updateRefreshTokenEntry: async function (auth_id, refresh_hash) {
        await pool.query("UPDATE auth SET refresh_hash=$1, created_at = NOW() WHERE auth_id=$2;",
            [refresh_hash, auth_id]);
    },
    removeRefreshToken: async function (refresh_hash) {
        await pool.query("DELETE FROM auth WHERE refresh_hash = $1", [refresh_hash]);
    },
    updateUserAvatarPath: async function (user_id, file_path) {
        await pool.query("UPDATE users SET avatar_url=$1 WHERE user_id=$2", [file_path, user_id]);
    },
    deleteUserAvatarPath: async function (user_id) {
        await pool.query("UPDATE users SET avatar_url=NULL WHERE user_id=$1", [user_id]);
    },
    getUserAvatarPath: async function(user_id) {
      const {rows} =   await pool.query("SELECT avatar_url FROM users WHERE user_id=$1", [user_id]);

      return rows[0];
    },


    // Chat room queries
    getAllRoomsWithinDistance: async function (lat, lng, distance) {

        // First delete all nearby rooms that are expired
        await pool.query("SELECT delete_expired_nearby_chats($1,$2,$3);", [lat, lng, distance]);

        // Get nearby chatrooms
        const {rows} = await pool.query("SELECT \n" +
            "\t*, \n" +
            "\tearth_distance(ll_to_earth($1,$2), ll_to_earth(lat, lng)) \n" +
            "    as distance_metres\n" +
            "FROM RoomsWithUsers\n" +
            "WHERE earth_box(ll_to_earth($1,$2), $3) @> \n" +
            "            ll_to_earth(lat, lng)\n" +
            "ORDER by distance_metres;",
            [lat, lng, distance]);

        return rows;
    },


    /**
     * Retrieves room information of the given room
     * @param room_id
     * @returns {Promise.<*>}
     */
    getChatroom: async function (room_id) {
        const {rows} = await pool.query("SELECT * FROM chat_rooms WHERE chat_room_id = $1 LIMIT 1;",
            [room_id]);
        return rows[0];
    },

    /**
     * Queries database for all messages of the given room and returns array of messages
     * @param room_id
     * @returns {Promise.<*>}
     */
    getChatroomMessages: async function (room_id) {
        const {rows} = await pool.query("SELECT cr.chat_room_id, cr.name as room_name," +
            " cr.created_at as room_created_at, u.name as username, u.avatar_url, m.* " +
            "FROM chat_rooms cr " +
            // Inner join ensures empty array is returned if no messages
            "INNER JOIN messages m ON m.chat_room_id = cr.chat_room_id " +
            "LEFT JOIN users u ON u.user_id = m.user_id " +
            "WHERE cr.chat_room_id = $1", [room_id]);
        return rows;
    },


    /**
     * Adds new message to database and returns the message object
     * @param chat_room_id
     * @param user_id
     * @param message
     * @return The newly added message object
     */
    addMessage: async function (chat_room_id, user_id, message) {
        const {rows} = await pool.query("INSERT INTO messages " +
            "(chat_room_id, user_id, message) values($1, $2, $3) RETURNING *;",
            [chat_room_id, user_id, message]);
        return rows[0];
    },

    addPost: async function (chat_room_id, user_id, message) {
        const {rows} = await pool.query("INSERT INTO posts " +
            "(chat_room_id, user_id, message) values($1, $2, $3) RETURNING *;",
            [chat_room_id, user_id, message]);
        return rows[0];
    },

    /**
     * Creates a new chatroom at specified lat, lng, with given name and returns chat room object
     * @param lat
     * @param lng
     * @param name
     * @return {Promise.<*>}
     */
    createChatroom: async function (lat, lng, name) {
        const {rows} = await pool.query("INSERT INTO chat_rooms (lat, lng, name) " +
            "values ($1, $2, $3) RETURNING *;",
            [lat, lng, name]);

        return rows[0];
    }


}
;

