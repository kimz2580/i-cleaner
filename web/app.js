import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js";
import { getAuth, signInWithEmailAndPassword, onAuthStateChanged, signOut } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";
import { getFirestore, doc, getDoc } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";

const firebaseConfig = {
  apiKey: "AIzaSyBzPt6LDpHw1c7NWZPtfWOkOj6GEHGlSH4",
  authDomain: "i-cleaner.firebaseapp.com",
  projectId: "i-cleaner",
  storageBucket: "i-cleaner.firebasestorage.app",
  messagingSenderId: "836866272458",
  appId: "1:836866272458:android:025d673bfd3917badb1833"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);

// If already logged in, redirect to dashboard automatically
onAuthStateChanged(auth, async (user) => {
    if (user) {
        const userDoc = await getDoc(doc(db, "users", user.uid));
        if (userDoc.exists() && (userDoc.data().role === "admin" || userDoc.data().role === "employee")) {
            window.location.href = "dashboard.html";
        }
    }
});

const loginForm = document.getElementById('loginForm');
const errorMessage = document.getElementById('error-message');

if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const loginBtn = document.getElementById('loginBtn');

        loginBtn.disabled = true;
        loginBtn.textContent = "Logging in...";

        try {
            const userCredential = await signInWithEmailAndPassword(auth, email, password);
            const user = userCredential.user;

            const userDoc = await getDoc(doc(db, "users", user.uid));

            if (userDoc.exists() && (userDoc.data().role === "admin" || userDoc.data().role === "employee")) {
                window.location.href = "dashboard.html";
            } else {
                errorMessage.textContent = "Access Denied: Only Admin/Staff can use this portal.";
                await signOut(auth);
                loginBtn.disabled = false;
                loginBtn.textContent = "Login to Dashboard";
            }
        } catch (error) {
            errorMessage.textContent = "Error: " + error.message;
            loginBtn.disabled = false;
            loginBtn.textContent = "Login to Dashboard";
        }
    });
}
