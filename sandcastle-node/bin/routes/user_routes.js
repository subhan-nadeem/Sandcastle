/**
 * Holds routes related to user data
 * POST /v1/user/register
 * POST /v1/user/authenticate
 * POST /v1/user/update_fcm
 */
const multer = require('multer');
const upload = multer();
const AWS = require('aws-sdk');
const uid = require('uid-safe'); // Used to generate refresh tokens
const crypto = require('crypto'); // Used to generate SHA256 hashes for refresh tokens
const AVATAR_BUCKET_NAME = 'sandcastle-avatars';
const avatarBucket = new AWS.S3({params: {Bucket: AVATAR_BUCKET_NAME}});


/**
 * Returns SHA256 hash for any given string
 */
function getSHA256Hash(string) {
    return crypto.createHash('sha256').update(string).digest('base64');
}


/**
 * Returns generated path for user avatar
 */
function getAvatarPath(userID) {
    return `files_user_${userID}/avatar.jpg`
}


async function generateTokens(username, user_id, db) {
    const JWTData = {
        name: username,
        id: user_id,
    };

    const auth_token = await db.getAuthToken(JWTData);

    const refresh_token = await uid(18);

    const refresh_hash = getSHA256Hash(refresh_token);
    return {auth_token, refresh_token, refresh_hash};
}

module.exports = function (app, db) {

    // Register API
    app.post('/v1/user/register', async (req, res) => {

        const userExists = await db.userExists(req.body.username);

        if (userExists) {
            return res.status(409).send({
                message: 'User already exists'
            });
        }

        // Hash password
        const passHash = await db.hash(req.body.password);

        await db.addNewUser(req.body.username, passHash);

        res.status(201).send({
            message: 'User successfully registered'
        });
    });

    // Login API
    app.post('/v1/user/authenticate', async (req, res) => {
        try {
            const user = await db.getUserFromName(req.body.username);

            if (user === null || !await db.isValidPassword(req.body.password, user.password_hash)) {
                // Send 403 because client interprets 401 as a log out action
                res.status(403).send({
                    message: 'Invalid credentials'
                });
                return;
            }

            await db.updateFCMKey(req.body.fcm_key, user.user_id);

            const {auth_token, refresh_token, refresh_hash}
                = await generateTokens(user.name, user.user_id, db);

            await db.storeRefreshToken(user.user_id, refresh_hash, req.body.device_name);

            // Don't send out password hash
            delete user.password_hash;

            res.send({
                message: 'Successfully authenticated',
                auth_token: auth_token,
                refresh_token: refresh_token,
                user: user
            });
        }
        catch (err) {
            console.error(err);
            res.status(500).send({
                message: 'Server error'
            })
        }
    });


    /**
     * OAuth refresh token call
     * Refreshes token if entry exists
     * Client should store updated tokens after receiving them
     */
    app.post('/v1/user/refresh_token', async (req, res) => {
        try {
            const authEntry = await db.getRefreshTokenEntry(getSHA256Hash(req.body.refresh_token));

            // The refresh token never existed/was revoked
            if (authEntry === null || authEntry === undefined) {
                res.status(400).send({
                    message: 'Refresh token does not exist'
                });
            }
            else {

                // Generate brand new tokens
                const {auth_token, refresh_token, refresh_hash}
                    = await generateTokens(authEntry.name, authEntry.user_id, db);

                // Update refresh hash entry with new refresh hash
                await db.updateRefreshTokenEntry(authEntry.auth_id, refresh_hash);

                res.status(201).send({
                    message: 'Tokens successfully refreshed',
                    auth_token: auth_token,
                    refresh_token: refresh_token
                })
            }
        }
        catch (err) {
            console.error(err);
            res.status(500).send({
                message: 'Server error'
            })
        }
    });

    app.post('/v1/user/logout', async (req, res) => {
        try {
            await db.removeRefreshToken(getSHA256Hash(req.body.refresh_token));
        }
        catch(err) {
            return res.status(400).send({message: "Refresh token doesn't exist or is invalid"});
        }

        res.status(200).send({
            message: 'Logged out successfully'
        })
    });

    // Update FCM key API
    app.post('/v1/user/update_fcm', async (req, res) => {

        await db.updateFCMKey(req.body.fcm_key, req.decoded.id);

        res.status(201).send({message: 'FCM key updated successfully'});
    });

    // Update user's location
    app.post('/v1/user/update_location', async (req, res) => {

        await db.updateUserLocation(req.body.lat, req.body.lng, req.decoded.id);

        res.status(201).send({message: 'Location updated successfully'});
    });

    app.post('/v1/user/upload_avatar', upload.single('avatar'), async (req, res) => {

        const filePath = getAvatarPath(req.decoded.id);

        const uploadData = {Key: filePath, Body: req.file.buffer};

        avatarBucket.putObject(uploadData, async function (err, data) {
            if (err) {
                console.error(err, err.stack);
                return res.status(500).send({message: 'Error uploading avatar'});
            } // an error occurred

            await db.updateUserAvatarPath(req.decoded.id, filePath);

            res.status(201).send({message: 'Avatar uploaded successfully'});
        });

    });


    app.delete('/v1/user/delete_avatar', async (req, res) => {

        const params = {
            Key: getAvatarPath(req.decoded.id)
        };

        avatarBucket.deleteObject(params, function (err, data) {
            if (err) console.error(err, err.stack); // an error occurred
            else console.log(data);           // successful response
        });

        await db.deleteUserAvatarPath(req.decoded.id);

        res.send({message: 'Avatar successfully deleted'});
    });
};