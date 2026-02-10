import { browserLocalPersistence, getAuth, setPersistence } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { Capacitor } from "@capacitor/core";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries



import { PushNotifications } from '@capacitor/push-notifications';
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
// const messaging = getMessaging(app);




// plateforme
const isWeb = Capacitor.getPlatform() === "web";

/* =========================
   PUSH NOTIFICATIONS
   ========================= */

export async function initPushNotifications(): Promise<string | null> {

  // 📱 MOBILE (Android / iOS)
  if (!isWeb) {
    // 1. Demander les permissions
    const permResult = await PushNotifications.requestPermissions();

    if (permResult.receive === 'granted') {
      console.log('✅ Permission accordée');

      // 2. Enregistrer pour recevoir les notifications
      await PushNotifications.register();

      // 3. Listener pour obtenir le token
      return new Promise((resolve) => {
        PushNotifications.addListener("registration", (token) => {
          console.log("📱 FCM MOBILE TOKEN:", token.value);
          resolve(token.value);
        });

        // Listener pour les erreurs d'enregistrement
        PushNotifications.addListener("registrationError", (error) => {
          console.error("❌ Erreur d'enregistrement:", error);
          resolve(null);
        });

        // 4. Listener pour recevoir les notifications (app ouverte)
        PushNotifications.addListener("pushNotificationReceived", (notification) => {
          console.log("🔔 Notification reçue:", notification);
          // Afficher une alerte ou un toast
          alert(`Nouvelle notification: ${notification.title}\n${notification.body}`);
        });

        // 5. Listener pour les actions sur les notifications (app fermée)
        PushNotifications.addListener("pushNotificationActionPerformed", (notification) => {
          console.log("👆 Notification cliquée:", notification);
          // Naviguer vers une page spécifique si nécessaire
        });
      });
    } else {
      console.warn('⚠️ Permission refusée');
      alert('Veuillez autoriser les notifications dans les paramètres de l\'app');
      return null;
    }
  }

  // 🌐 WEB
  try {
    const { getMessaging, getToken, onMessage } = await import("firebase/messaging");
    const messaging = getMessaging(app);

    // Enregistrer le service worker
    if ("serviceWorker" in navigator) {
      await navigator.serviceWorker.register("/firebase-messaging-sw.js");
    }

    // Demander la permission pour les notifications web
    const permission = await Notification.requestPermission();

    if (permission === 'granted') {
      console.log('✅ Permission web accordée');

      const token = await getToken(messaging, {
        vapidKey: "BKczifT1eLLql4ZVLGUdIlFgboONLIOtuDVgGFdHoik78OIg0S8EYjsui1gtW012P3fdX5hI-i3t69emqmnPGRY",
      });

      console.log("🌐 FCM WEB TOKEN:", token);

      // Écouter les messages quand l'app est ouverte
      onMessage(messaging, (payload) => {
        console.log("🔔 Message reçu (web):", payload);
        new Notification(
          payload.notification?.title ?? "Notification",
          {
            body: payload.notification?.body,
            icon: payload.notification?.icon || '/icon.png'
          }
        );
      });

      return token;
    } else {
      console.warn('⚠️ Permission web refusée');
      return null;
    }
  } catch (error) {
    console.error("❌ Erreur initialisation FCM Web:", error);
    return null;
  }
}

export const db = getFirestore(app);
export default app;


