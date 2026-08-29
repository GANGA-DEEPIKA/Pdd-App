// Firebase Configuration — same project as Android app (campusfix-d8833)
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";

const firebaseConfig = {
  apiKey: "AIzaSyAglnZfW14bV9ufEVgUbk-3vdwAAVytY9c",
  authDomain: "campusfix-d8833.firebaseapp.com",
  projectId: "campusfix-d8833",
  storageBucket: "campusfix-d8833.firebasestorage.app",
  messagingSenderId: "845071552610",
  appId: "1:845071552610:android:b5edd1f352e352572a813b"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);

export default app;
