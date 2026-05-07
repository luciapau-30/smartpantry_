const _AUTH_API = 'http://localhost:8080/smartpantry';

const authModal = document.getElementById('authModal');
const loginBtn  = document.getElementById('loginBtn');
const userMenu  = document.getElementById('userMenu');

function setLoggedIn(username) {
  loginBtn?.classList.add('hidden');
  // Support both id="userMenu" (index.html) and id="userNav" (pantry/recipes)
  (userMenu || document.getElementById('userNav'))?.classList.remove('hidden');
  // Support both id="navUsername" and id="usernameDisplay"
  const nameEl = document.getElementById('navUsername') || document.getElementById('usernameDisplay');
  if (nameEl) nameEl.textContent = username || 'Account';
}

function setLoggedOut() {
  loginBtn?.classList.remove('hidden');
  (userMenu || document.getElementById('userNav'))?.classList.add('hidden');
}

function toggleModal() { authModal?.classList.toggle('hidden'); }
function closeModal()  { authModal?.classList.add('hidden'); }

if (authModal) {
  authModal.addEventListener('click', e => { if (e.target === authModal) closeModal(); });
}

function showLoginTab() {
  document.getElementById('loginForm')?.classList.remove('hidden');
  document.getElementById('registerForm')?.classList.add('hidden');
  document.getElementById('tabLogin')?.classList.add('border-purple-500','text-white');
  document.getElementById('tabLogin')?.classList.remove('border-transparent','text-gray-400');
  document.getElementById('tabRegister')?.classList.remove('border-purple-500','text-white');
  document.getElementById('tabRegister')?.classList.add('border-transparent','text-gray-400');
  document.getElementById('authError')?.classList.add('hidden');
}

function showRegisterTab() {
  document.getElementById('registerForm')?.classList.remove('hidden');
  document.getElementById('loginForm')?.classList.add('hidden');
  document.getElementById('tabRegister')?.classList.add('border-purple-500','text-white');
  document.getElementById('tabRegister')?.classList.remove('border-transparent','text-gray-400');
  document.getElementById('tabLogin')?.classList.remove('border-purple-500','text-white');
  document.getElementById('tabLogin')?.classList.add('border-transparent','text-gray-400');
  document.getElementById('authError')?.classList.add('hidden');
}

function showAuthError(msg) {
  const el = document.getElementById('authError');
  if (el) { el.textContent = msg; el.classList.remove('hidden'); }
  else alert(msg);
}

async function submitLogin(e) {
  e.preventDefault();
  const login    = document.getElementById('loginIdentifier').value.trim();
  const password = document.getElementById('loginPassword').value;
  try {
    const res  = await fetch(`${_AUTH_API}/api/auth/login`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ login, password })
    });
    const json = await res.json();
    if (json.success) {
      const username = json.data?.user?.username || json.data?.username || login;
      setLoggedIn(username);
      closeModal();
      if (typeof onAuthLogin === 'function') onAuthLogin(json.data);
      else location.reload();
    } else {
      showAuthError(json.error || 'Invalid credentials.');
    }
  } catch (_) { showAuthError('Could not reach the server.'); }
}

async function submitRegister(e) {
  e.preventDefault();
  const username = document.getElementById('regUsername').value.trim();
  const email    = document.getElementById('regEmail').value.trim();
  const password = document.getElementById('regPassword').value;
  try {
    const res  = await fetch(`${_AUTH_API}/api/auth/register`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password, accountType: 'member' })
    });
    const json = await res.json();
    if (json.success) {
      setLoggedIn(username);
      closeModal();
      if (typeof onAuthLogin === 'function') onAuthLogin(json.data);
      else location.reload();
    } else {
      showAuthError(json.error || 'Registration failed.');
    }
  } catch (_) { showAuthError('Could not reach the server.'); }
}

async function logout() {
  await fetch(`${_AUTH_API}/api/auth/logout`, { method: 'POST', credentials: 'include' }).catch(() => {});
  setLoggedOut();
  location.reload();
}

// On every page load: check server session to determine guest vs. member view.
// No localStorage — state comes entirely from the JSESSIONID cookie.
(async () => {
  try {
    const res  = await fetch(`${_AUTH_API}/api/auth/session`, { credentials: 'include' });
    const json = await res.json();
    if (json.success && json.data?.loggedIn) {
      setLoggedIn(json.data.username);
      if (typeof onAuthLogin === 'function') onAuthLogin(json.data);
    } else {
      setLoggedOut();
      if (typeof onAuthLogout === 'function') onAuthLogout();
    }
  } catch (_) {
    setLoggedOut();
    if (typeof onAuthLogout === 'function') onAuthLogout();
  }
})();
