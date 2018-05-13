#!/usr/bin/env node

// TODO replace with ENV
const config = require('./sensitive_config');
const express = require('express');
const db = require('./database');
const app = express(); // Express instance
const bodyParser = require('body-parser');
const port = 3000;
const PGPubSub = require('pg-pubsub');
const pubsubInstance = new PGPubSub(config.connectionString);

// DEBUG
const name = 'sandcastle';
const debug = require('debug')('sc');
debug('booting %s', name);


app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));
app.set('superSecret', config.secret);

// Init routes
require('./routes')(app, db);

// Enable server
const server = app.listen(port, () => {
    console.log('We are live on ' + port);
});

const io = require('socket.io')(server);
io.secret = config.secret;

// Init sockets
require('./sockets')(io, db, pubsubInstance);