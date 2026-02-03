import admin from "firebase-admin";
import fs from "fs";

// ⚡ Lire le fichier JSON
const serviceAccount = JSON.parse(
  fs.readFileSync(new URL("./serviceAccountKey.json", import.meta.url))
);

// Initialiser Firebase Admin
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// Ton token FCM
const fcmToken = "eDXOGjxBPo1o11jPlQiw35:APA91bHUmJq-Zifbg881zcnLxvbGPNTCAbl0S3QslaSLZIPpAb3vrJFvWIUU68omj3y8NB1StsiuQozzhhC5VKGLteP4O2E6BfrYiuAwRFWUmgyrcQO-5JM";

// Message de test
const message = {
  token: fcmToken,
  notification: {
    title: "Notification Test 🚀",
    body: "Ceci est un test depuis Node.js"
  },
  webpush: {
    notification: {
      icon: '/favicon.png'
    }
  }
};

// Envoyer le message
try {
  const response = await admin.messaging().send(message);
  console.log("✅ Message envoyé avec succès :", response);
} catch (error) {
  console.error("❌ Erreur lors de l'envoi du message :", error);
}
