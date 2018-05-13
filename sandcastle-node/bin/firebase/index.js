const admin = require("firebase-admin");

const serviceAccount = require("../serviceAccountKey.json");

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://sandcastle-f32be.firebaseio.com"
});


/**
 * FCM requires all values to be strings, so this formats payload accordingly
 * Object MUST be only one level deep
 * @param payload Given payload to be formatted
 */
const formatPayload = function (payload) {
    for (const property in payload) {
        if (payload.hasOwnProperty(property)) {
            payload[property] = payload[property].toString();
        }
    }
};

module.exports = {
    ID_NEW_MESSAGE: '0',
    ID_NEW_ROOM: '1',
    sendToDevice: async function (notification_id, fcm_keys, dataObj) {

        // Messaging throws error if no FCM keys
        if (fcm_keys === null || fcm_keys.length === 0) return;

        let pushData = {
            notification_id: notification_id
        };

        // Merge push data object into notification object
        pushData = Object.assign(pushData, pushData, dataObj);

        console.log(fcm_keys);

        formatPayload(pushData);

        await admin.messaging().sendToDevice(fcm_keys, {data: pushData});
    }
};