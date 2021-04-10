#!/usr/bin/env node
"use strict";

const FirestoreConnection = require("../services/google/firestore/firestore-connection");
const firestoreCollection = new FirestoreConnection().getCollection();

firestoreCollection.get().then((querySnapshot) => {
    querySnapshot.forEach((documentSnapshot) => {
        const userData = documentSnapshot.data();
        if (userData.checked) return;

        const updatedUserData = {
            ...userData,
            checked: Date.now(),
        };

        documentSnapshot.ref.update(updatedUserData).then(() => {
            console.log(`Updated ${userData.userName}`);
        });
    });
});
