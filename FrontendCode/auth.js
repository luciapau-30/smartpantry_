// Shared auth logic for all pages.
// Update API_BASE to match your Tomcat deployment URL.
const API_BASE = 'http://localhost:8080/smartpantry';

// ── Session check ────────────────────────────────────────────────────────────

async function checkSession() {
    try {
        const res = await fetch(`${API_BASE}/api/auth/me`, { credentials: 'include' });
        const json = await res.json();
        if (json.data && json.data.user) {
            onLoggedIn(json.data.user);
        } else {
            onLoggedOut();
        }
    } catch (e) {
        onLoggedOut();
    }
}

function onLoggedIn(user) {
    const loginBtn  = document.getElementById('loginBtn');
    const userNav   = document.getElementById('userNav');
    const userLabel = document.getElementById('usernameDisplay');
    if (loginBtn)  loginBtn.classList.add('hidden');
    if (userNav)   userNav.classList.remove('hidden');
    if (userLabel) userLabel.textContent = user.username;
    window.currentUser = user;
    connectAlertSocket();
    if (typeof window.onAuthLogin === 'function') window.onAuthLogin(user);
}

function onLoggedOut() {
    const loginBtn = document.getElementById('loginBtn');
    const userNav  = document.getElementById('userNav');
    if (loginBtn) loginBtn.classList.remove('hidden');
    if (userNav)  userNav.classList.add('hidden');
    window.currentUser = null;
    disconnectAlertSocket();
    if (typeof window.onAuthLogout === 'function') window.onAuthLogout();
}

// ── Modal open/close ─────────────────────────────────────────────────────────

function toggleModal() {
    const modal = document.getElementById('authModal');
    if (modal) modal.classList.toggle('hidden');
    clearAuthError();
}

function closeModal() {
    const modal = document.getElementById('authModal');
    if (modal) modal.classList.add('hidden');
    clearAuthError();
}

function showLoginTab() {
    document.getElementById('loginForm').classList.remove('hidden');
    document.getElementById('registerForm').classList.add('hidden');
    document.getElementById('tabLogin').classList.add('border-purple-500', 'text-white');
    document.getElementById('tabLogin').classList.remove('border-transparent', 'text-gray-400');
    document.getElementById('tabRegister').classList.add('border-transparent', 'text-gray-400');
    document.getElementById('tabRegister').classList.remove('border-purple-500', 'text-white');
    clearAuthError();
}

function showRegisterTab() {
    document.getElementById('loginForm').classList.add('hidden');
    document.getElementById('registerForm').classList.remove('hidden');
    document.getElementById('tabRegister').classList.add('border-purple-500', 'text-white');
    document.getElementById('tabRegister').classList.remove('border-transparent', 'text-gray-400');
    document.getElementById('tabLogin').classList.add('border-transparent', 'text-gray-400');
    document.getElementById('tabLogin').classList.remove('border-purple-500', 'text-white');
    clearAuthError();
}

function clearAuthError() {
    const el = document.getElementById('authError');
    if (el) { el.textContent = ''; el.classList.add('hidden'); }
}

function showAuthError(msg) {
    const el = document.getElementById('authError');
    if (el) { el.textContent = msg; el.classList.remove('hidden'); }
}

// ── Login ────────────────────────────────────────────────────────────────────

async function submitLogin(e) {
    e.preventDefault();
    clearAuthError();
    const login    = document.getElementById('loginIdentifier').value.trim();
    const password = document.getElementById('loginPassword').value;

    if (!login || !password) {
        showAuthError('Email/username and password are required.');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/api/auth/login`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ login, password })
        });
        const json = await res.json();
        if (res.ok && json.data && json.data.user) {
            onLoggedIn(json.data.user);
            closeModal();
        } else {
            showAuthError(json.error || 'Invalid credentials.');
        }
    } catch (err) {
        showAuthError('Could not reach the server. Is Tomcat running?');
    }
}

// ── Register ─────────────────────────────────────────────────────────────────

async function submitRegister(e) {
    e.preventDefault();
    clearAuthError();
    const username = document.getElementById('regUsername').value.trim();
    const email    = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPassword').value;

    if (!username || !email || !password) {
        showAuthError('All fields are required.');
        return;
    }
    if (password.length < 6) {
        showAuthError('Password must be at least 6 characters.');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/api/auth/register`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password, accountType: 'member' })
        });
        const json = await res.json();
        if (res.ok) {
            // Auto-login after register
            const loginRes = await fetch(`${API_BASE}/api/auth/login`, {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ login: username, password })
            });
            const loginJson = await loginRes.json();
            if (loginRes.ok && loginJson.data && loginJson.data.user) {
                onLoggedIn(loginJson.data.user);
                closeModal();
            } else {
                showAuthError('Registered! Please log in.');
                showLoginTab();
            }
        } else {
            showAuthError(json.error || 'Registration failed.');
        }
    } catch (err) {
        showAuthError('Could not reach the server. Is Tomcat running?');
    }
}

// ── Logout ───────────────────────────────────────────────────────────────────

async function logout() {
    try {
        await fetch(`${API_BASE}/api/auth/logout`, {
            method: 'POST',
            credentials: 'include'
        });
    } catch (e) { /* ignore network errors on logout */ }
    onLoggedOut();
}

// ── Close modal on outside click ─────────────────────────────────────────────

window.addEventListener('click', function (e) {
    const modal = document.getElementById('authModal');
    if (modal && e.target === modal) closeModal();
});

// ── WebSocket alert connection ────────────────────────────────────────────────

function connectAlertSocket() {
    const wsUrl = API_BASE.replace(/^http/, 'ws') + '/ws/alerts';
    const ws = new WebSocket(wsUrl);

    ws.onmessage = function (event) {
        try {
            const data = JSON.parse(event.data);
            if (data.type === 'expiry_alert' && data.items && data.items.length > 0) {
                showExpiryToast(data.items);
            }
        } catch (e) { /* ignore malformed messages */ }
    };

    ws.onclose = function () {
        // Reconnect after 10 seconds if still logged in
        if (window.currentUser) setTimeout(connectAlertSocket, 10000);
    };

    window._alertWs = ws;
}

function disconnectAlertSocket() {
    if (window._alertWs) {
        window._alertWs.onclose = null; // prevent auto-reconnect on logout
        window._alertWs.close();
        window._alertWs = null;
    }
}

function showExpiryToast(items) {
    let toast = document.getElementById('_expiryToast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = '_expiryToast';
        toast.style.cssText = [
            'position:fixed', 'bottom:1.5rem', 'right:1.5rem', 'z-index:9999',
            'background:#7c3aed', 'color:#fff', 'padding:0.75rem 1.25rem',
            'border-radius:0.5rem', 'box-shadow:0 4px 12px rgba(0,0,0,0.4)',
            'font-size:0.875rem', 'max-width:320px', 'display:none'
        ].join(';');
        document.body.appendChild(toast);
    }
    const count = items.length;
    const soonest = items.reduce((min, i) => (i.daysLeft < min ? i.daysLeft : min), Infinity);
    toast.textContent = count === 1
        ? `1 pantry item expires in ${soonest} day${soonest !== 1 ? 's' : ''}!`
        : `${count} pantry items expiring soon — soonest in ${soonest} day${soonest !== 1 ? 's' : ''}!`;
    toast.style.display = 'block';
    clearTimeout(toast._timer);
    toast._timer = setTimeout(() => { toast.style.display = 'none'; }, 8000);
}

// ── On load: check existing session ─────────────────────────────────────────

document.addEventListener('DOMContentLoaded', checkSession);
