// firebase-messaging-sw.js

importScripts('https://www.gstatic.com/firebasejs/10.5.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.5.0/firebase-messaging-compat.js');

const firebaseConfig = {
  apiKey: "AIzaSyA_uqtf0wYmmtnGuIxnQqolBUVKK2q6_MQ",
  authDomain: "map-project-a1787.firebaseapp.com",
  projectId: "map-project-a1787",
  storageBucket: "map-project-a1787.firebasestorage.app",
  messagingSenderId: "333124874397",
  appId: "1:333124874397:web:48bbf493d60bd3e798e474",
  measurementId: "G-ZDFPKDYRM1"
};

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

// Notifications en arrière-plan
messaging.onBackgroundMessage((payload) => {
  console.log('[Service Worker] Message background:', payload);

  const notificationTitle = payload.notification?.title || 'Notification';
  const notificationOptions = {
    body: payload.notification?.body || '',
    icon: '/favicon.png'
  };

  self.registration.showNotification(notificationTitle, notificationOptions);
});
