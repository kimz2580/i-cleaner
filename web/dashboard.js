import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js";
import { getAuth, onAuthStateChanged, signOut } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";
import { getFirestore, collection, onSnapshot, query, orderBy, doc, getDoc } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";

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

// Authentication Guard
onAuthStateChanged(auth, async (user) => {
    if (!user) {
        window.location.href = "index.html";
    } else {
        try {
            const userDoc = await getDoc(doc(db, "users", user.uid));
            if (userDoc.exists()) {
                const data = userDoc.data();
                const role = data.role?.toLowerCase();

                if (role === "admin" || role === "employee") {
                    document.getElementById('admin-name').textContent = data.full_name || data.fullName || "Staff Member";
                    loadOrders();
                } else {
                    alert("Access Denied: Customers must use the mobile app.");
                    signOut(auth).then(() => window.location.href = "index.html");
                }
            } else {
                console.error("User profile not found in Firestore.");
                signOut(auth).then(() => window.location.href = "index.html");
            }
        } catch (e) {
            console.error("Auth Guard Error:", e);
        }
    }
});

// Logout Handler
document.getElementById('logoutBtn')?.addEventListener('click', () => {
    signOut(auth).then(() => window.location.href = "index.html");
});

// View Order Helper (attached to window so HTML can see it)
window.viewOrder = (id) => {
    alert("Viewing Order: " + id + "\nFeature to edit status coming soon in next deployment!");
};

// Real-Time Orders Loader
function loadOrders() {
    const q = query(collection(db, "orders"), orderBy("createdAt", "desc"));

    onSnapshot(q, (snapshot) => {
        const tableBody = document.getElementById('orders-table-body');
        if (!tableBody) return;

        tableBody.innerHTML = '';
        let totalRevenue = 0;
        let pendingCount = 0;
        let totalCount = 0;

        snapshot.forEach((doc) => {
            const order = doc.data();
            totalCount++;

            if (order.status === 'pending') pendingCount++;

            // Handle different currency/price formats safely
            const price = parseFloat(order.totalPrice || order.totalBill || 0);
            totalRevenue += price;

            const row = `
                <tr>
                    <td>#${doc.id.substring(0,6).toUpperCase()}</td>
                    <td>${order.customerName || 'Customer'}</td>
                    <td>${order.items ? order.items.length : 0} items</td>
                    <td>KSH ${price.toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                    <td><span class="status-badge status-${order.status}">${order.status}</span></td>
                    <td>
                        <button class="action-btn" onclick="viewOrder('${doc.id}')">View</button>
                    </td>
                </tr>
            `;
            tableBody.innerHTML += row;
        });

        document.getElementById('total-orders').textContent = totalCount;
        document.getElementById('pending-orders').textContent = pendingCount;
        document.getElementById('today-revenue').textContent = `KSH ${totalRevenue.toLocaleString(undefined, {minimumFractionDigits: 2})}`;
    }, (error) => {
        console.error("Real-time update error:", error);
    });
}
