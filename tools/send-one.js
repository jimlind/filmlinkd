#!/usr/bin/env node

const Axios = require('axios');
const DiscordJS = require('discord.js');
const DotEnv = require('dotenv');
const { Firestore } = require('@google-cloud/firestore');
const { letterboxdDiary } = require('letterboxd');
const MessageFactory = require('./models/MessageFactory');

// Load values from the .env file to the process.env object
DotEnv.config();

const discordClient = new DiscordJS.Client();
const firestoreCollection = new Firestore().collection('users');
const documentReference = firestoreCollection.doc('filmlinkd');

discordClient.login(process.env.DISCORD_BOT_TOKEN).then(() => {
     documentReference.get().then((documentSnapshot) => {
          const data = documentSnapshot.data();
          letterboxdDiary.get(data.userName, 1).then((entryList) => {
               const messageFactory = new MessageFactory();
               const message = messageFactory.createDiaryEntryMessage(entryList[0], data);

               for (const channel of data.channelList) {
                    const channelObject = discordClient.channels.cache.find(ch => ch.id === channel.channelId);
                    if (!channelObject) {
                         continue;
                    }

                    channelObject.send(message)
                         .then(() => {
                              console.log(`Posted in ${channelObject.name}@${channelObject.guild.name}`);
                              discordClient.destroy();
                         })
                         .catch(() => {
                              console.log(`Unable to write to ${channelObject.name}@${channelObject.guild.name}`);
                         });
               }
          });
     });
});