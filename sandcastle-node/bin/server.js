require('dotenv').config();
const express = require('express');
const db = require('./database');
const app = express(); // Express instance
const bodyParser = require('body-parser');
const port = 3000;
const PGPubSub = require('pg-pubsub');
const pubsubInstance = new PGPubSub(`postgres://${process.env.DB_USER}:${process.env.DB_PASSWORD}@${process.env.DB_HOST}:${process.env.DB_PORT}/${process.env.DB_NAME}`);

// DEBUG
const name = 'sandcastle';
const debug = require('debug')('sc');
debug('booting %s', name);

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

// Init routes
require('./routes')(app, db);

// Enable server
const server = app.listen(port, () => {
    console.log('We are live on ' + port);
});

const io = require('socket.io')(server);

// Init sockets
require('./sockets')(io, db, pubsubInstance);