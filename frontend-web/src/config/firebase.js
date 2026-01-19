//?=== FIREBASE CONFIGURATION
const firebaseConfig = {
    apiKey: "MOCK_API_KEY",
    authDomain: "mock-project.firebaseapp.com",
    projectId: "mock-project",
    storageBucket: "mock-project.appspot.com",
    messagingSenderId: "123456789",
    appId: "1:123456789:web:abcdef"
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