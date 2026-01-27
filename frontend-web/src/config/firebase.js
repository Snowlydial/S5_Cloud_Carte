//?=== FIREBASE CONFIGURATION
const firebaseConfig = {
  apiKey: "AIzaSyA_uqtf0wYmmtnGuIxnQqolBUVKK2q6_MQ",
  authDomain: "map-project-a1787.firebaseapp.com",
  projectId: "map-project-a1787",
  storageBucket: "map-project-a1787.firebasestorage.app",
  messagingSenderId: "333124874397",
  appId: "1:333124874397:web:48bbf493d60bd3e798e474",
  measurementId: "G-ZDFPKDYRM1"
};


//*-- Firebase will be initialized when you add real credentials
let app = null;
let auth = null;

// Initialize Firebase only if credentials are real
if (firebaseConfig.apiKey !== "MOCK_API_KEY") {
    const { initializeApp } = require('firebase/app');
    const { getAuth } = require('firebase/auth');
    
    app = initializeApp(firebaseConfig);
    auth = getAuth(app);
}

export { auth, firebaseConfig };
export const isFirebaseConfigured = firebaseConfig.apiKey !== "MOCK_API_KEY";