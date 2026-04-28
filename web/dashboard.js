import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js";
import { getAuth, onAuthStateChanged, signOut } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";
import { getFirestore, collection, onSnapshot, query, orderBy, doc, getDoc, updateDoc, deleteDoc, addDoc, serverTimestamp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";

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

// REPORT GENERATION
window.generateReport = async () => {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();
    const timestamp = new Date().toLocaleString();

    // Header
    doc.setFontSize(22);
    doc.setTextColor(30, 60, 114); // Primary Navy
    doc.text("i-Cleaner | Business Report", 14, 22);

    doc.setFontSize(10);
    doc.setTextColor(100);
    doc.text(`Generated on: ${timestamp}`, 14, 30);
    doc.text("Premium Laundry Management Ecosystem", 14, 35);

    // Business Summary Section
    doc.setFontSize(16);
    doc.setTextColor(0);
    doc.text("Operational Summary", 14, 50);

    const totalOrders = document.getElementById('total-orders').textContent;
    const pendingOrders = document.getElementById('pending-orders').textContent;
    const revenue = document.getElementById('today-revenue').textContent;

    doc.setFontSize(12);
    doc.text(`Total Orders: ${totalOrders}`, 14, 60);
    doc.text(`Pending Work: ${pendingOrders}`, 14, 67);
    doc.text(`Gross Revenue: ${revenue}`, 14, 74);

    // Order Table
    doc.setFontSize(16);
    doc.text("Live Order Details", 14, 90);

    const rows = [];
    const tableRows = document.querySelectorAll("#orders-table-body tr");
    tableRows.forEach(tr => {
        const cols = tr.querySelectorAll("td");
        rows.push([
            cols[0].innerText, // ID
            cols[1].innerText, // Customer
            cols[2].innerText, // Items
            cols[3].innerText.split('\n')[0], // Price (clean)
            cols[4].innerText  // Status
        ]);
    });

    doc.autoTable({
        startY: 95,
        head: [['Order ID', 'Customer', 'Items', 'Amount', 'Status']],
        body: rows,
        theme: 'striped',
        headStyles: { fillColor: [30, 60, 114] },
        styles: { fontSize: 10 }
    });

    // Footer
    const finalY = doc.lastAutoTable.finalY || 150;
    doc.setFontSize(10);
    doc.setTextColor(150);
    doc.text("End of Business Report. Confirmed by i-Cleaner System.", 14, finalY + 20);

    doc.save(`i-Cleaner_Report_${new Date().toISOString().split('T')[0]}.pdf`);
};

document.getElementById('downloadReportBtn')?.addEventListener('click', window.generateReport);

// MISSION CONTROL: Sound Effects
const notificationSound = new Audio('https://assets.mixkit.co/active_storage/sfx/2358/2358-preview.mp3');

// THEME MANAGEMENT
const toggleTheme = () => {
    const isDark = document.body.classList.toggle('dark-mode');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');

    // Update Chart colors if they exist
    if (myChart) {
        const textColor = isDark ? '#e2e8f0' : '#1e293b';
        myChart.options.scales.x.ticks.color = textColor;
        myChart.options.scales.y.ticks.color = textColor;
        myChart.update();
    }
    if (myPieChart) {
        myPieChart.options.plugins.legend.labels.color = isDark ? '#e2e8f0' : '#1e293b';
        myPieChart.update();
    }
};

document.getElementById('themeToggle')?.addEventListener('click', toggleTheme);

if (localStorage.getItem('theme') === 'dark') {
    document.body.classList.add('dark-mode');
}

// ATTACH TO WINDOW FOR TABLE ACTIONS
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

window.sendWhatsApp = (phone, name, amount, status) => {
    if (!phone) {
        alert("No phone number found for this customer.");
        return;
    }
    // Format phone: remove spaces/dashes, ensure 255 prefix for TZ
    let cleanPhone = phone.replace(/\D/g, '');
    if (cleanPhone.startsWith('0')) cleanPhone = '255' + cleanPhone.substring(1);
    if (!cleanPhone.startsWith('255')) cleanPhone = '255' + cleanPhone;

    const message = `Habari ${name}! Nguo zako zipo tayari hapa i-Cleaner 🧼.\n\nMalipo yafanyike kwa:\nLipa Number: 222555738 (Mix by Yas)\nKumbukumbu No (Control Number): ${controlNumber || 'Angalia Risiti'}\n\nJumla ya Malipo: TSH ${amount.toLocaleString()}\n\nBaada ya kulipa, mfumo utatambua malipo yako moja kwa moja. Karibu tena! ✨`;
    const url = `https://wa.me/${cleanPhone}?text=${encodeURIComponent(message)}`;
    window.open(url, '_blank');
};

// Modal Logic for Admin
document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('orderModal');
    const addBtn = document.getElementById('addOrderBtn');
    const manualForm = document.getElementById('manualOrderForm');

    if (addBtn) {
        addBtn.onclick = () => modal.style.display = 'flex';
    }

    if (manualForm) {
        manualForm.onsubmit = async (e) => {
            e.preventDefault();
            const btn = e.target.querySelector('button[type="submit"]');
            btn.disabled = true;

            // Generate professional Control Number (Reference)
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
                alert("Error: " + err.message);
            } finally {
                btn.disabled = false;
            }
        };
    }
});

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
                    loadUsers();
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
    let isInitialLoad = true;

    onSnapshot(q, (snapshot) => {
        const tableBody = document.getElementById('orders-table-body');
        if (!tableBody) return;

        // Play sound for new orders (not on first load)
        if (!isInitialLoad && snapshot.docChanges().some(change => change.type === "added")) {
            notificationSound.play().catch(e => console.log("Sound blocked by browser"));
        }
        isInitialLoad = false;

        tableBody.innerHTML = '';
        let totalRevenue = 0;
        let pendingCount = 0;
        let totalCount = 0;

        snapshot.forEach((doc) => {
            const order = doc.data();
            totalCount++;

            if (order.status?.toLowerCase() === 'pending') pendingCount++;

            // Handle both camelCase (Web) and snake_case (Android)
            const price = parseFloat(order.totalAmount || order.total_amount || order.totalPrice || order.totalBill || 0);
            const customer = order.customerName || order.customer_name || 'Customer';
            const oNumber = order.orderNumber || order.order_number || '#' + doc.id.substring(0,6).toUpperCase();
            const gCount = order.garmentCount || order.garment_count || 0;
            const payStatus = order.paymentStatus || order.payment_status || "Unpaid";

            totalRevenue += price;

            const row = `
                <tr>
                    <td>${oNumber}</td>
                    <td>${customer}</td>
                    <td>${gCount} items</td>
                    <td>
                        TSH ${price.toLocaleString(undefined, {minimumFractionDigits: 2})}
                        <br>
                        <small class="status-badge ${payStatus === 'Paid' ? 'status-completed' : 'status-cancelled'}" style="margin-top:4px; display:inline-block">
                            ${payStatus}
                        </small>
                    </td>
                    <td><span class="status-badge status-${(order.status || 'pending').toLowerCase()}">${order.status || 'Pending'}</span></td>
                    <td>
                        <div class="user-actions" style="flex-direction: column; align-items: stretch; gap: 4px;">
                            <select onchange="updateStatus('${doc.id}', this.value)" class="action-select">
                                <option value="" disabled selected>Update Status</option>
                                <option value="Processing">Processing</option>
                                <option value="Completed">Completed</option>
                            </select>
                            <div style="display:flex; gap:4px;">
                                <button class="action-btn btn-success" onclick="verifyPayment('${doc.id}')">Pay</button>
                                <button class="action-btn" onclick="sendWhatsApp('${order.customerPhone || order.customer_phone || ''}', '${customer}', ${price}, '${order.status}')" title="WhatsApp Customer">💬 WA</button>
                                <button class="action-btn" onclick="showQR('${doc.id}', '${oNumber}')" title="Generate QR">🔲 QR</button>
                                <button class="action-btn btn-danger" onclick="revokeOrder('${doc.id}')">Del</button>
                            </div>
                        </div>
                    </td>
                </tr>
            `;
            tableBody.innerHTML += row;
        });

        updateMachineStatus(snapshot);
        updateActivityLog(snapshot);
        updateRevenueChart(snapshot);
        updateCategoryChart(snapshot);
        updateLoyalCustomers(snapshot);

        document.getElementById('total-orders').textContent = totalCount;
        document.getElementById('pending-orders').textContent = pendingCount;
        document.getElementById('today-revenue').textContent = `TSH ${totalRevenue.toLocaleString(undefined, {minimumFractionDigits: 2})}`;
    }, (error) => {
        console.error("Real-time update error:", error);
    });
}

let myChart = null;
let myPieChart = null;

function updateCategoryChart(snapshot) {
    const ctx = document.getElementById('categoryChart');
    if (!ctx) return;

    const stats = { 'Wash & Fold': 0, 'Dry Cleaning': 0, 'Ironing': 0, 'Other': 0 };

    snapshot.forEach(doc => {
        const order = doc.data();
        const service = order.serviceType || 'Wash & Fold';
        if (stats.hasOwnProperty(service)) stats[service]++;
        else stats['Other']++;
    });

    if (myPieChart) myPieChart.destroy();

    const isDark = document.body.classList.contains('dark-mode');
    const textColor = isDark ? '#e2e8f0' : '#1e293b';

    myPieChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: Object.keys(stats),
            datasets: [{
                data: Object.values(stats),
                backgroundColor: ['#1E3C72', '#2A5298', '#4F46E5', '#94A3B8'],
                hoverOffset: 10
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { color: textColor }
                }
            }
        }
    });
}

function updateLoyalCustomers(snapshot) {
    const list = document.getElementById('top-customers-list');
    if (!list) return;

    const customers = {};
    snapshot.forEach(doc => {
        const order = doc.data();
        const name = order.customerName || order.customer_name || 'Anonymous';
        customers[name] = (customers[name] || 0) + 1;
    });

    const sorted = Object.entries(customers)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 5);

    list.innerHTML = sorted.map(([name, count]) => `
        <div class="customer-item">
            <span>${name}</span>
            <span class="customer-badge">${count} Orders</span>
        </div>
    `).join('');
}

function updateRevenueChart(snapshot) {
    const ctx = document.getElementById('revenueChart');
    if (!ctx) return;

    // Process data for the last 7 days
    const revenueData = {};
    const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

    // Initialize last 7 days with 0
    for(let i=6; i>=0; i--) {
        const d = new Date();
        d.setDate(d.getDate() - i);
        revenueData[days[d.getDay()]] = 0;
    }

    snapshot.forEach(doc => {
        const order = doc.data();
        if (order.createdAt) {
            let seconds;
            if (typeof order.createdAt === 'string') {
                seconds = parseInt(order.createdAt) / 1000;
            } else {
                seconds = order.createdAt.seconds;
            }
            const date = new Date(seconds * 1000);
            const dayName = days[date.getDay()];
            if (revenueData.hasOwnProperty(dayName)) {
                revenueData[dayName] += parseFloat(order.totalAmount || order.total_amount || 0);
            }
        }
    });

    const labels = Object.keys(revenueData);
    const values = Object.values(revenueData);

    if (myChart) {
        myChart.destroy();
    }

    const isDark = document.body.classList.contains('dark-mode');
    const textColor = isDark ? '#e2e8f0' : '#1e293b';

    myChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Revenue (TSH)',
                data: values,
                borderColor: '#1E3C72',
                backgroundColor: 'rgba(30, 60, 114, 0.1)',
                borderWidth: 3,
                fill: true,
                tension: 0.4,
                pointRadius: 5,
                pointBackgroundColor: '#2A5298'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { display: false },
                    ticks: {
                        color: textColor,
                        callback: function(value) {
                            return value >= 1000 ? value/1000 + 'k' : value;
                        }
                    }
                },
                x: {
                    grid: { display: false },
                    ticks: { color: textColor }
                }
            }
        }
    });
}

function updateActivityLog(snapshot) {
    const activityLog = document.getElementById('activity-log');
    if (!activityLog) return;

    activityLog.innerHTML = '';
    const recentOrders = [];
    snapshot.forEach(doc => recentOrders.push(doc.data()));

    // Show last 5 activities
    recentOrders.slice(0, 5).forEach(order => {
        const item = document.createElement('div');
        item.className = 'activity-item';

        let icon = '📦';
        let action = 'placed an order';
        if (order.status?.toLowerCase() === 'processing') { icon = '⚡'; action = 'is being washed'; }
        if (order.status?.toLowerCase() === 'completed') { icon = '✅'; action = 'completed'; }

        let timeStr = "Just now";
        if (order.createdAt) {
            let seconds;
            if (typeof order.createdAt === 'string') {
                seconds = parseInt(order.createdAt) / 1000;
            } else {
                seconds = order.createdAt.seconds;
            }
            timeStr = new Date(seconds * 1000).toLocaleTimeString();
        }

        item.innerHTML = `
            <div class="activity-icon">${icon}</div>
            <div class="activity-info">
                <p><strong>${order.customerName || order.customer_name || 'Customer'}</strong> ${action}</p>
                <small style="color: #64748b">${timeStr}</small>
            </div>
        `;
        activityLog.appendChild(item);
    });
}

// GLOBAL STATE FOR HARDWARE
let machineInventory = [
    { id: "Washer 01", icon: "🧺", type: "washer", status: "active" },
    { id: "Washer 02", icon: "🧺", type: "washer", status: "active" },
    { id: "Dryer 01", icon: "💨", type: "dryer", status: "active" }
];

// Load inventory from local storage if exists
const savedInventory = localStorage.getItem('machine_inventory');
if (savedInventory) {
    machineInventory = JSON.parse(savedInventory);
}

window.toggleMachineService = (index) => {
    const machine = machineInventory[index];
    machine.status = machine.status === 'service' ? 'active' : 'service';
    localStorage.setItem('machine_inventory', JSON.stringify(machineInventory));
    loadOrders(); // Refresh UI
};

window.removeMachine = (index) => {
    if (confirm(`Remove ${machineInventory[index].id} from system?`)) {
        machineInventory.splice(index, 1);
        localStorage.setItem('machine_inventory', JSON.stringify(machineInventory));
        loadOrders();
    }
};

function updateMachineStatus(snapshot) {
    const machineGrid = document.querySelector('.machine-grid');
    if (!machineGrid) return;

    // 1. Get all orders currently being processed
    let processingOrders = [];
    snapshot.forEach(doc => {
        const order = doc.data();
        if (order.status?.toLowerCase() === 'processing') {
            let seconds;
            if (typeof order.createdAt === 'string') {
                seconds = parseInt(order.createdAt) / 1000;
            } else {
                seconds = order.createdAt?.seconds || Date.now()/1000;
            }
            processingOrders.push({
                id: doc.id,
                name: order.customerName || order.customer_name,
                startTime: seconds
            });
        }
    });

    let html = '';
    let orderIndex = 0;

    // 3. Automate Assignment based on Inventory
    machineInventory.forEach((machine, index) => {
        const isUnderService = machine.status === 'service';
        let assignedOrder = null;

        if (!isUnderService && orderIndex < processingOrders.length) {
            assignedOrder = processingOrders[orderIndex];
            orderIndex++;
        }

        const isActive = !!assignedOrder;

        // Calculate a fake progress based on time (capped at 95% until finished)
        let progress = 0;
        if (isActive) {
            const now = Date.now() / 1000;
            const elapsed = now - assignedOrder.startTime;
            progress = Math.min(Math.floor((elapsed / 1800) * 100), 95); // Assuming 30 min cycle
        }

        html += `
            <div class="machine-card ${isUnderService ? 'status-cancelled' : (isActive ? 'status-active' : 'status-idle')}"
                 style="${isUnderService ? 'border-left-color: var(--danger); opacity: 0.8;' : ''}">
                <div class="machine-icon">${isUnderService ? '🛠️' : machine.icon}</div>
                <div class="machine-info">
                    <div style="display:flex; justify-content:space-between; align-items:start;">
                        <h4>${machine.id}</h4>
                        <div style="display:flex; gap:5px;">
                            <button onclick="toggleMachineService(${index})" class="action-btn" style="padding:2px 5px; font-size:0.6rem;" title="Service Mode">🛠️</button>
                            <button onclick="removeMachine(${index})" class="action-btn btn-danger" style="padding:2px 5px; font-size:0.6rem;">🗑️</button>
                        </div>
                    </div>
                    <p>${isUnderService ? '<strong style="color:var(--danger)">UNDER SERVICE</strong>' : (isActive ? `Working: <strong>${assignedOrder.name}</strong>` : 'Ready for Load')}</p>
                    <div class="progress-bar">
                        <div class="progress" style="width: ${isUnderService ? '0' : progress}%"></div>
                    </div>
                    ${isActive && !isUnderService ? `<small style="font-size:0.7rem; color:var(--text-muted)">Estimated: ${progress}% complete</small>` : ''}
                </div>
            </div>
        `;
    });

    // 4. Handle Overflow (Queue)
    if (orderIndex < processingOrders.length) {
        const overflow = processingOrders.length - orderIndex;
        html += `
            <div class="machine-card status-pending" style="border-left-color: var(--warning)">
                <div class="machine-icon">⏳</div>
                <div class="machine-info">
                    <h4>Queue Overflow</h4>
                    <p><strong>${overflow} Orders</strong> waiting for machine</p>
                    <small>Auto-starts when a machine finishes or comes back from service</small>
                </div>
            </div>
        `;
    }

    machineGrid.innerHTML = html;
}

// Attach Machine Modal Logic
document.addEventListener('DOMContentLoaded', () => {
    const mModal = document.getElementById('machineModal');
    const addMBtn = document.getElementById('addMachineBtn');
    const mForm = document.getElementById('addMachineForm');

    if (addMBtn) addMBtn.onclick = () => mModal.style.display = 'flex';

    if (mForm) {
        mForm.onsubmit = (e) => {
            e.preventDefault();
            const name = document.getElementById('mName').value;
            const type = document.getElementById('mType').value;

            machineInventory.push({
                id: name,
                icon: type === 'washer' ? '🧺' : '💨',
                type: type,
                status: 'active'
            });

            localStorage.setItem('machine_inventory', JSON.stringify(machineInventory));
            mModal.style.display = 'none';
            mForm.reset();
            loadOrders(); // Trigger re-render
        };
    }
});

// User Management Logic
window.updateUserRole = async (userId, newRole) => {
    try {
        await updateDoc(doc(db, "users", userId), { role: newRole });
        alert(`User role updated to ${newRole}`);
    } catch (e) {
        alert("Error updating role: " + e.message);
    }
};

window.removeUser = async (userId) => {
    if (confirm("Are you sure you want to remove this user? This cannot be undone.")) {
        try {
            await deleteDoc(doc(db, "users", userId));
            alert("User removed successfully.");
        } catch (e) {
            alert("Error removing user: " + e.message);
        }
    }
};

function loadUsers() {
    const q = query(collection(db, "users"));

    onSnapshot(q, (snapshot) => {
        const tableBody = document.getElementById('users-table-body');
        if (!tableBody) return;

        tableBody.innerHTML = '';
        snapshot.forEach((userDoc) => {
            const user = userDoc.data();
            const role = (user.role || 'user').toLowerCase();
            const userId = userDoc.id;

            const row = `
                <tr>
                    <td>${user.fullName || user.full_name || 'No Name'}</td>
                    <td><span class="status-badge ${role === 'admin' ? 'status-completed' : 'status-pending'}">${role}</span></td>
                    <td>${user.phone || 'N/A'}</td>
                    <td>
                        <div class="user-actions">
                            <select onchange="updateUserRole('${userId}', this.value)" class="action-select">
                                <option value="" disabled selected>Change Role</option>
                                <option value="admin">Admin</option>
                                <option value="employee">Employee</option>
                                <option value="user">User</option>
                            </select>
                            <button class="action-btn btn-danger" onclick="removeUser('${userId}')">Remove</button>
                        </div>
                    </td>
                </tr>
            `;
            tableBody.innerHTML += row;
        });
    }, (error) => {
        console.error("User loading error:", error);
    });
}
