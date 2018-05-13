/**
 * Index referencing all route modules
 */

const userRoutes = require('./user_routes');
const chatRoomRoutes = require('./chat_room_routes');


/**
 * Returns authentication middleware function
 * @param app Required express app
 * @param db Required database instance
 * @returns {Function} Middleware function
 */
const authenticationMiddleware = function (app, db) {

    const isUnprotectedAPI = function (url) {

        const unprotectedRoutes = ['/v1/user/register', '/v1/user/authenticate', '/v1/user/refresh_token'];

        return unprotectedRoutes.includes(url);
    };

    return async function (req, res, next) {

        console.log(req.url);

        if (isUnprotectedAPI(req.url))
            return next();

        // check header or url parameters or post parameters for token
        const token = req.body.token || req.query.token || req.headers['authorization'];

        if (!token) {
            return res.status(401).send({
                message: 'No token provided.'
            });
        }

        try {
            req.decoded = await db.verifyAuthToken(token, app.get('superSecret'));
            next();
        }
        catch (error) {

            const ERROR_TOKEN_EXPIRED = "TokenExpiredError";
            if (error.name === ERROR_TOKEN_EXPIRED) {
                return res.status(401).send({
                    message: ERROR_TOKEN_EXPIRED
                });
            }

            return res.status(401).send({
                message: 'Failed to authenticate token.'
            });
        }
    };
};

module.exports = function (app, db) {
    app.use(authenticationMiddleware(app, db));
    userRoutes(app, db);
    chatRoomRoutes(app, db);
};