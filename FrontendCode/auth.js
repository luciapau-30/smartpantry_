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
    // Optional hook for page-specific logic (e.g. re-render on index.html)
    if (typeof window.onAuthLogin === 'function') window.onAuthLogin(user);
}

function onLoggedOut() {
    const loginBtn = document.getElementById('loginBtn');
    const userNav  = document.getElementById('userNav');
    if (loginBtn) loginBtn.classList.remove('hidden');
    if (userNav)  userNav.classList.add('hidden');
    window.currentUser = null;
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

// ── On load: check existing session ─────────────────────────────────────────

document.addEventListener('DOMContentLoaded', checkSession);
