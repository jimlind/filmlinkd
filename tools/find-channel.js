#!/usr/bin/env node
"use strict";

const FirestoreConnection = require("../services/google/firestore/firestore-connection");
const firestoreCollection = new FirestoreConnection().getCollection();

const channel = {channelId: '819404842816110603', guildId: '819385323343446036'};
const query = firestoreCollection.where('channelList', 'array-contains', channel);

query.get().then((querySnapshot) => {
    querySnapshot.forEach((documentSnapshot) => {
        const userData = documentSnapshot.data();
        
        console.log([userData.id, userData.userName]);
    });
});


