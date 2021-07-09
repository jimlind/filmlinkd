#!/usr/bin/env node

const Axios = require('axios');
const diaryEntryPublisher = require('./models/diaryEntryPublisher');
const DiscordJS = require('discord.js');
const DotEnv = require('dotenv');
const { Firestore } = require('@google-cloud/firestore');
const FirestoreDao = require('./models/FirestoreDao');
const { letterboxdDiary, letterboxdProfile } = require('letterboxd');
const MessageFactory = require('./models/MessageFactory');

// Load values from the .env file to the process.env object
DotEnv.config();

// TODO 'users to constants'
const firestoreCollection = new Firestore().collection('users');
const firestoreDAO = new FirestoreDao(firestoreCollection);
const messageFactory = new MessageFactory();

const discordClient = new DiscordJS.Client();
discordClient.login(process.env.DISCORD_BOT_TOKEN).then(() => {
    // This is the thing that handles posting to Discord
    const diaryEntryPublisher = new diaryEntryPublisher(
        firestoreDAO,
        discordClient,
        messageFactory,
    );
    diaryEntryPublisher.postAllEntries();
});
