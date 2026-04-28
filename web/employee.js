import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js";
import { getAuth, onAuthStateChanged, signOut } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";
import { getFirestore, collection, onSnapshot, query, orderBy, doc, getDoc, updateDoc, addDoc, serverTimestamp, deleteDoc } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";

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

window.showQR = (id, orderNum) => {
    const qrContainer = document.getElementById("qrcode");
    qrContainer.innerHTML = "";
    document.getElementById("qrOrderInfo").textContent = `Order ${orderNum}`;

    new QRCode(qrContainer, {
        text: `https://i-cleaner.web.app/track?id=${id}`,
        width: 200,
        height: 200,
        colorDark: "#1E3C72",
        colorLight: "#ffffff",
        correctLevel: QRCode.CorrectLevel.H
    });

    document.getElementById("qrModal").style.display = "flex";
};

const notificationSound = new Audio('https://assets.mixkit.co/active_storage/sfx/2358/2358-preview.mp3');

const toggleTheme = () => {
    const isDark = document.body.classList.toggle('dark-mode');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
};

document.getElementById('themeToggle')?.addEventListener('click', toggleTheme);

if (localStorage.getItem('theme') === 'dark') {
    document.body.classList.add('dark-mode');
}

window.updateStatus = async (orderId, status) => {
    try {
        await updateDoc(doc(db, "orders", orderId), { status: status });
    } catch (e) {
        alert("Error: " + e.message);
    }
};

window.revokeOrder = async (id) => {
    if (confirm("Revoke this order?")) {
        try {
            await deleteDoc(doc(db, "orders", id));
        } catch (e) {
            alert("Error: " + e.message);
        }
    }
};

window.verifyPayment = async (id) => {
    try {
        await updateDoc(doc(db, "orders", id), { paymentStatus: "Paid" });
        alert("Payment Verified!");
    } catch (e) {
        alert("Error: " + e.message);
    }
};

onAuthStateChanged(auth, async (user) => {
    if (!user) {
        window.location.href = "index.html";
    } else {
        try {
            const userDoc = await getDoc(doc(db, "users", user.uid));
            if (userDoc.exists()) {
                const data = userDoc.data();
                const role = data.role?.toLowerCase();
                if (role === "employee" || role === "staff") {
                    document.getElementById('staff-name').textContent = data.full_name || data.fullName || "Staff Member";
                    loadOrders();
                } else if (role === "admin") {
                    window.location.href = "dashboard.html";
                } else {
                    alert("Access Denied.");
                    signOut(auth).then(() => window.location.href = "index.html");
                }
            }
        } catch (err) {
            console.error(err);
        }
    }
});

window.sendWhatsApp = (phone, name, amount, status, controlNumber) => {
    if (!phone) {
        alert("No phone number found.");
        return;
    }
    let cleanPhone = phone.replace(/\D/g, '');
    if (cleanPhone.startsWith('0')) cleanPhone = '255' + cleanPhone.substring(1);
    if (!cleanPhone.startsWith('255')) cleanPhone = '255' + cleanPhone;

    const message = `Habari ${name}! Nguo zako zipo tayari hapa i-Cleaner 🧼.\n\nMalipo yafanyike kwa:\nLipa Number: 222555738 (Mix by Yas)\nKumbukumbu No (Control Number): ${controlNumber || 'Angalia Risiti'}\n\nJumla ya Malipo: TSH ${amount.toLocaleString()}\n\nBaada ya kulipa, mfumo utatambua malipo yako moja kwa moja. Karibu tena! ✨`;
    const url = `https://wa.me/${cleanPhone}?text=${encodeURIComponent(message)}`;
    window.open(url, '_blank');
};

document.getElementById('logoutBtn')?.addEventListener('click', () => {
    signOut(auth).then(() => window.location.href = "index.html");
});

document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('orderModal');
    const addBtn = document.getElementById('addOrderBtn');
    const manualForm = document.getElementById('manualOrderForm');

    if (addBtn) addBtn.onclick = () => modal.style.display = 'flex';

    if (manualForm) {
        manualForm.onsubmit = async (e) => {
            e.preventDefault();
            const btn = e.target.querySelector('button[type="submit"]');
            btn.disabled = true;
            const controlNumber = "992" + Math.floor(100000000 + Math.random() * 900000000);

            try {
                await addDoc(collection(db, "orders"), {
                    customerName: document.getElementById('custName').value,
                    customerPhone: document.getElementById('custPhone').value,
                    serviceType: document.getElementById('serviceType').value,
                    garmentCount: parseInt(document.getElementById('gCount').value),
                    totalAmount: parseFloat(document.getElementById('tPrice').value),
                    status: "Pending",
                    paymentStatus: "Unpaid",
                    controlNumber: controlNumber,
                    createdAt: serverTimestamp(),
                    orderNumber: "M-" + Math.floor(1000 + Math.random() * 9000)
                });
                modal.style.display = 'none';
                manualForm.reset();
            } catch (err) {
                alert(err.message);
            } finally {
                btn.disabled = false;
            }
        };
    }
});

function loadOrders() {
    const q = query(collection(db, "orders"), orderBy("createdAt", "desc"));
    let isInitialLoad = true;

    onSnapshot(q, (snapshot) => {
        const tableBody = document.getElementById('orders-table-body');
        if (!tableBody) return;
        tableBody.innerHTML = '';

        if (!isInitialLoad && snapshot.docChanges().some(change => change.type === "added")) {
            notificationSound.play().catch(() => {});
        }
        isInitialLoad = false;

        let total = 0;
        let pending = 0;

        snapshot.forEach((docSnap) => {
            const order = docSnap.data();
            const id = docSnap.id;
            total++;
            const status = (order.status || 'Pending').toLowerCase();
            if (status === 'pending') pending++;

            const payStatus = order.paymentStatus || "Unpaid";

            const row = `
                <tr>
                    <td>${order.orderNumber || '#' + id.substring(0,6).toUpperCase()}</td>
                    <td>${order.customerName || 'Customer'}</td>
                    <td>${order.garmentCount || 0} items</td>
                    <td>
                        <span class="status-badge status-${status}">${order.status || 'Pending'}</span>
                        <br>
                        <small class="status-badge ${payStatus === 'Paid' ? 'status-completed' : 'status-cancelled'}" style="margin-top:4px; display:inline-block">
                            ${payStatus}
                        </small>
                    </td>
                    <td>
                        <div class="user-actions" style="flex-direction: column; align-items: stretch;">
                            <select onchange="updateStatus('${id}', this.value)" class="action-select">
                                <option value="" disabled selected>Update Status</option>
                                <option value="Processing">Start Washing</option>
                                <option value="Completed">Ready for Pickup</option>
                            </select>
                            <div style="display:flex; gap:4px; margin-top:4px;">
                                <button class="action-btn" onclick="verifyPayment('${id}')">✅ Pay</button>
                                <button class="action-btn" onclick="sendWhatsApp('${order.customerPhone}', '${order.customerName}', ${order.totalAmount}, '${order.status}', '${order.controlNumber}')">💬 WA</button>
                                <button class="action-btn" onclick="showQR('${id}', '${order.orderNumber || id.substring(0,6)}')">🔲 QR</button>
                                <button class="action-btn btn-danger" onclick="revokeOrder('${id}')">❌</button>
                            </div>
                        </div>
                    </td>
                </tr>
            `;
            tableBody.innerHTML += row;
        });

        document.getElementById('total-orders').textContent = total;
        document.getElementById('pending-orders').textContent = pending;
        updateMachineStatus(snapshot);
        updateActivityLog(snapshot);
    });
}

function updateMachineStatus(snapshot) {
    const machineGrid = document.querySelector('.machine-grid');
    if (!machineGrid) return;

    let processingOrders = [];
    snapshot.forEach(doc => {
        const order = doc.data();
        if (order.status?.toLowerCase() === 'processing') {
            processingOrders.push({ name: order.customerName, startTime: order.createdAt?.seconds || Date.now()/1000 });
        }
    });

    const machines = [
        { id: "Washer 01", icon: "🧺" },
        { id: "Washer 02", icon: "🧺" },
        { id: "Dryer 01", icon: "💨" }
    ];

    let html = '';
    machines.forEach((m, i) => {
        const assigned = processingOrders[i];
        const isActive = !!assigned;
        let progress = 0;
        if (isActive) {
            const elapsed = (Date.now() / 1000) - assigned.startTime;
            progress = Math.min(Math.floor((elapsed / 1800) * 100), 95);
        }

        html += `
            <div class="machine-card ${isActive ? 'status-active' : 'status-idle'}">
                <div class="machine-icon">${m.icon}</div>
                <div class="machine-info">
                    <h4>${m.id}</h4>
                    <p>${isActive ? `Working: ${assigned.name}` : 'Ready'}</p>
                    <div class="progress-bar"><div class="progress" style="width: ${progress}%"></div></div>
                </div>
            </div>
        `;
    });
    machineGrid.innerHTML = html;
}

function updateActivityLog(snapshot) {
    const log = document.getElementById('activity-log');
    if (!log) return;
    log.innerHTML = '';
    snapshot.docs.slice(0, 5).forEach(doc => {
        const order = doc.data();
        log.innerHTML += `<div class="activity-item"><p><strong>${order.customerName}</strong>: ${order.status}</p></div>`;
    });
}
