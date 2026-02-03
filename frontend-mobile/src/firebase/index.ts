import { browserLocalPersistence, getAuth, setPersistence } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
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



export const db = getFirestore(app); 
export default app;