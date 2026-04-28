import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js";
import { getAuth, signInWithEmailAndPassword, onAuthStateChanged, signOut } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";
import { getFirestore, doc, getDoc } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";

const firebaseConfig = {
  apiKey: "AIzaSyBzPt6LDpHw1c7NWZPtfWOkOj6GEHGlSH4",
  authDomain: "i-cleaner.firebaseapp.com",
  projectId: "i-cleaner",
  storageBucket: "i-cleaner.firebasestorage.app",
  messagingSenderId: "836866272458",
  appId: "1:836866272458:web:a51811cf53c52abadb1833"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);

// If already logged in, redirect to dashboard automatically
onAuthStateChanged(auth, async (user) => {
    if (user) {
        console.log("User logged in:", user.email);
        const userDoc = await getDoc(doc(db, "users", user.uid));
        if (userDoc.exists()) {
            const role = userDoc.data().role?.toLowerCase();
            console.log("Detected role:", role);
            if (role === "admin") {
                window.location.href = "dashboard.html";
            } else if (role === "employee" || role === "staff") {
                window.location.href = "employee.html";
            } else {
                console.warn("Unauthorized role:", role);
            }
        } else {
            console.error("No Firestore document for UID:", user.uid);
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

            if (userDoc.exists()) {
                const role = userDoc.data().role?.toLowerCase();
                if (role === "admin") {
                    window.location.href = "dashboard.html";
                } else if (role === "employee" || role === "staff") {
                    window.location.href = "employee.html";
                } else {
                    errorMessage.textContent = "Access Denied: Only Admin/Staff can use this portal.";
                    await signOut(auth);
                    loginBtn.disabled = false;
                    loginBtn.textContent = "Authenticate Access";
                }
            } else {
                errorMessage.textContent = "User profile not found.";
                await signOut(auth);
                loginBtn.disabled = false;
                loginBtn.textContent = "Authenticate Access";
            }
        } catch (error) {
            errorMessage.textContent = "Invalid credentials. Please try again.";
            loginBtn.disabled = false;
            loginBtn.textContent = "Authenticate Access";
        }
    });
}
