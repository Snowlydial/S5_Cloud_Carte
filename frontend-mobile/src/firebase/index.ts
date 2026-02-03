import { browserLocalPersistence, getAuth, setPersistence } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getMessaging, getToken, onMessage } from "firebase/messaging";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries




// Miharisoa
const firebaseConfig = {
  apiKey: "AIzaSyA_uqtf0wYmmtnGuIxnQqolBUVKK2q6_MQ",
  authDomain: "map-project-a1787.firebaseapp.com",
  projectId: "map-project-a1787",
  storageBucket: "map-project-a1787.firebasestorage.app",
  messagingSenderId: "333124874397",
  appId: "1:333124874397:web:48bbf493d60bd3e798e474",
  measurementId: "G-ZDFPKDYRM1"
};



// nanavo
// const firebaseConfig = {
//   apiKey: "AIzaSyCxI35eK7y9e67YExBNHukzsI9RlcD0WeI",
//   authDomain: "cloud-cart-944df.firebaseapp.com",
//   projectId: "cloud-cart-944df",
//   storageBucket: "cloud-cart-944df.firebasestorage.app",
//   messagingSenderId: "617454116326",
//   appId: "1:617454116326:web:cde47508c4b29e49658a27",
//   measurementId: "G-LPXYWLJ75H"
// };
// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);




async function initFirebaseAuth() {
  await setPersistence(auth, browserLocalPersistence);
}
// await setPersistence(auth, browserLocalPersistence);
export const initAuth = initFirebaseAuth();

// Firebase Messaging
const messaging = getMessaging(app);


// Demander la permission notifications et récupérer token
export async function requestNotificationPermission() {
  if (!("Notification" in window)) {
    alert("Ce navigateur ne supporte pas les notifications.");
    return;
  }

  const permission = await Notification.requestPermission();
  if (permission === "granted") {
    console.log("Notification autorisée !");
    try {
      const token = await getToken(messaging, { vapidKey: "BKczifT1eLLql4ZVLGUdIlFgboONLIOtuDVgGFdHoik78OIg0S8EYjsui1gtW012P3fdX5hI-i3t69emqmnPGRY" });
      console.log("FCM Token:", token);
      return token;
    } catch (err) {
      console.error("Erreur lors de la récupération du token:", err);
    }
  } else {
    console.warn("Permission notifications refusée");
  }
}

// Notifications en premier plan
onMessage(messaging, (payload) => {
  console.log("Message reçu en premier plan:", payload);

  const title = payload.notification?.title || 'Notification';
  const body = payload.notification?.body || '';

  new Notification(title, { body, icon: '/favicon.png' });
});

if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/firebase-messaging-sw.js')
    .then((registration) => {
      console.log('Service Worker enregistré avec succès:', registration);
    })
    .catch((err) => {
      console.error('Erreur d’enregistrement du Service Worker:', err);
    });
}

export const db = getFirestore(app);
export default app;


