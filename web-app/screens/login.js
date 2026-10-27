// login.js — Mirrors LoginScreen.kt
import { auth } from '../firebase-config.js';
import { signInWithEmailAndPassword } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js';
import { AppState, subscribeToComplaints } from '../app.js';
import { getUserData } from './firestore-service.js';
import { showToast } from './utils.js';

export function renderLogin(container, title, emailHint, onLogin, onForgotPassword, onBackClick, onRegisterClick) {
  const rolePrefix = title.includes('Student') ? 'student' : title.includes('Staff') ? 'staff' : 'admin';
  const savedKey = `campusfix_saved_${rolePrefix}`;

  // Helper: check if an account strictly belongs to this role
  function isAccountForRole(account, role) {
    if (!account || !account.email) return false;
    const email = account.email.toLowerCase().trim();
    const localPart = email.split('@')[0];

    // Explicit role mismatch
    if (account.role && account.role.toLowerCase() !== role.toLowerCase()) return false;

    // Check email prefix conventions
    if (role === 'student' && (localPart.startsWith('admin') || localPart.startsWith('staff'))) return false;
    if (role === 'staff' && (localPart.startsWith('admin') || localPart.startsWith('student'))) return false;
    if (role === 'admin' && (localPart.startsWith('student') || localPart.startsWith('staff'))) return false;

    return true;
  }

  // Load and sanitize saved accounts for this specific role
  let savedAccounts = [];
  try {
    const raw = JSON.parse(localStorage.getItem(savedKey) || '[]');
    const cleaned = [];
    raw.forEach(a => {
      if (isAccountForRole(a, rolePrefix)) {
        cleaned.push({ ...a, role: rolePrefix });
      } else {
        // Relocate misplaced accounts to their proper role key
        const emailLower = (a.email || '').toLowerCase().trim();
        const targetRole = emailLower.startsWith('admin') ? 'admin'
          : emailLower.startsWith('staff') ? 'staff'
          : emailLower.startsWith('student') ? 'student' : null;
        if (targetRole) {
          try {
            const tKey = `campusfix_saved_${targetRole}`;
            const tList = JSON.parse(localStorage.getItem(tKey) || '[]');
            if (!tList.some(item => item.email.toLowerCase() === emailLower)) {
              tList.unshift({ ...a, role: targetRole });
              localStorage.setItem(tKey, JSON.stringify(tList.slice(0, 5)));
            }
          } catch {}
        }
      }
    });
    savedAccounts = cleaned;
    // Persist cleaned list back to current role key
    localStorage.setItem(savedKey, JSON.stringify(savedAccounts));
  } catch {
    savedAccounts = [];
  }
  // Sign out conflicting active sessions so different roles cannot leak across login screens
  if (auth.currentUser) {
    const curEmail = (auth.currentUser.email || '').toLowerCase();
    const curRole = (AppState.currentUserData?.role || '').toLowerCase();
    if (rolePrefix === 'student' && (curEmail.startsWith('admin') || curEmail.startsWith('staff') || curRole === 'admin' || curRole === 'staff')) {
      auth.signOut();
      AppState.currentUserData = null;
    } else if (rolePrefix === 'staff' && (curEmail.startsWith('admin') || curEmail.startsWith('student') || curRole === 'admin' || curRole === 'student')) {
      auth.signOut();
      AppState.currentUserData = null;
    } else if (rolePrefix === 'admin' && (curEmail.startsWith('student') || curEmail.startsWith('staff') || curRole === 'student' || curRole === 'staff')) {
      auth.signOut();
      AppState.currentUserData = null;
    }
  }

  container.innerHTML = `
  <div class="screen active bg-primary" style="min-height:100vh;display:flex;flex-direction:column;padding:24px;overflow-y:auto;">
    <div style="display:flex;align-items:center;">
      <button class="back-btn back-btn-white" id="login-back">←</button>
    </div>
    <div style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;padding:16px 0;">
      <p style="color:rgba(255,255,255,0.9);font-size:17px;font-weight:500;text-align:center;margin-bottom:4px;">${AppState.collegeName}</p>
      <h1 style="color:white;font-size:32px;font-weight:800;margin-bottom:32px;text-align:center;">${title}</h1>
      <div class="card card-lg" style="width:100%;max-width:420px;">
        <div class="form-group">
          <label class="form-label">${emailHint || 'Email'}</label>
          <input type="email" id="login-email" class="form-input" placeholder="${emailHint || 'Email'}" autocomplete="email" />
          <div id="saved-accounts-dropdown" class="saved-accounts" style="display:none;margin-top:8px;"></div>
        </div>
        <div class="form-group">
          <label class="form-label">Password</label>
          <div style="position:relative;">
            <input type="password" id="login-password" class="form-input" placeholder="Password" autocomplete="current-password" style="padding-right:48px;" />
            <button id="toggle-pass" type="button" style="position:absolute;right:12px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;font-size:20px;" aria-label="Toggle password visibility">🙈</button>
          </div>
        </div>
        <div class="checkbox-row" style="margin-bottom:24px;">
          <input type="checkbox" id="remember-me" checked />
          <label for="remember-me">Remember me</label>
        </div>
        <button id="login-btn" class="btn btn-primary btn-lg">Login</button>
      </div>
      <button class="btn-text text-white mt-12" id="forgot-btn" style="color:white;background:none;border:none;cursor:pointer;font-size:15px;padding:12px;">Forgot Password?</button>
      <div style="display:flex;align-items:center;gap:4px;margin-top:4px;">
        <span style="color:rgba(255,255,255,0.8);font-size:14px;">Don't have an account?</span>
        <button style="background:none;border:none;color:white;font-weight:700;font-size:14px;cursor:pointer;padding:8px 4px;" id="register-btn">Register Now</button>
      </div>
      <div id="login-error" class="login-role-alert alert-hidden" style="display:none;margin-top:16px;background:#FFEBEE;color:#C62828;padding:12px 20px;border-radius:10px;font-size:14px;font-weight:600;text-align:center;max-width:420px;width:100%;box-shadow:0 2px 8px rgba(0,0,0,0.12);border:1px solid #FFCDD2;"></div>
    </div>
  </div>`;

  const emailInput = document.getElementById('login-email');
  const passwordInput = document.getElementById('login-password');
  const dropdown = document.getElementById('saved-accounts-dropdown');
  const errEl = document.getElementById('login-error');
  const btn = document.getElementById('login-btn');
  const togglePassBtn = document.getElementById('toggle-pass');

  function showError(msg) {
    errEl.textContent = msg;
    errEl.style.display = 'block';
    errEl.classList.remove('alert-hidden');
    errEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }

  function hideError() {
    errEl.style.display = 'none';
    errEl.classList.add('alert-hidden');
  }

  emailInput.addEventListener('input', hideError);
  passwordInput.addEventListener('input', hideError);

  // Saved accounts dropdown — only shows relevant accounts for this role
  if (savedAccounts.length > 0) {
    emailInput.addEventListener('focus', () => {
      dropdown.innerHTML = `<div class="saved-accounts-header">Saved Accounts</div>` +
        savedAccounts.map(a => `<div class="saved-account-item" data-email="${a.email}" data-pass="${a.password}">👤 ${a.email}</div>`).join('') +
        `<div class="saved-account-item" style="color:var(--primary);font-weight:600;" id="dismiss-saved">Dismiss</div>`;
      dropdown.style.display = 'block';
      dropdown.querySelectorAll('[data-email]').forEach(el => {
        el.addEventListener('click', () => {
          emailInput.value = el.dataset.email;
          passwordInput.value = el.dataset.pass;
          dropdown.style.display = 'none';
          hideError();
        });
      });
      document.getElementById('dismiss-saved')?.addEventListener('click', () => dropdown.style.display = 'none');
    });
  }

  togglePassBtn.addEventListener('click', () => {
    const isPass = passwordInput.type === 'password';
    passwordInput.type = isPass ? 'text' : 'password';
    togglePassBtn.textContent = isPass ? '👁️' : '🙈';
  });

  document.getElementById('login-back').addEventListener('click', onBackClick);
  document.getElementById('forgot-btn').addEventListener('click', onForgotPassword);
  document.getElementById('register-btn').addEventListener('click', onRegisterClick);

  btn.addEventListener('click', async () => {
    const email = emailInput.value.trim().toLowerCase();
    const password = passwordInput.value.trim();
    if (!email || !password) {
      showError('Required fields missing');
      showToast('Required fields missing', 'error');
      return;
    }

    // College email validation
    const collegeLower = (AppState.collegeName || '').toLowerCase();
    const domainKeywords = ['saveetha', 'srm', 'vit', 'annauniv', 'iit', 'nit'];
    const noiseWords = ['school','engineering','college','university','institute','technology','clg','univ','of','and','the','for'];
    let isCorrect = domainKeywords.some(k => collegeLower.includes(k) && email.includes(k));
    if (!isCorrect) {
      const keywords = collegeLower.split(/[\s,.\-]/).filter(w => w.length > 2 && !noiseWords.includes(w));
      isCorrect = keywords.some(k => email.includes(k)) || keywords.length === 0;
    }
    if (!isCorrect && AppState.collegeName) {
      const msg = `Use only ${AppState.collegeName} email`;
      showError(msg);
      showToast(msg, 'error');
      return;
    }
    // Upfront Role Mismatch check based on email prefix
    if (rolePrefix === 'student') {
      if (email.startsWith('admin')) {
        const msg = 'Role Mismatch: This account is registered as Admin.';
        showError(msg);
        showToast(msg, 'error');
        return;
      }
      if (email.startsWith('staff')) {
        const msg = 'Role Mismatch: This account is registered as Staff.';
        showError(msg);
        showToast(msg, 'error');
        return;
      }
    } else if (rolePrefix === 'staff') {
      if (email.startsWith('admin')) {
        const msg = 'Role Mismatch: This account is registered as Admin.';
        showError(msg);
        showToast(msg, 'error');
        return;
      }
      if (email.startsWith('student')) {
        const msg = 'Role Mismatch: This account is registered as Student.';
        showError(msg);
        showToast(msg, 'error');
        return;
      }
    } else if (rolePrefix === 'admin') {
      if (email.startsWith('student')) {
        const msg = 'Role Mismatch: This account is registered as Student.';
        showError(msg);
        showToast(msg, 'error');
        return;
      }
      if (email.startsWith('staff')) {
        const msg = 'Role Mismatch: This account is registered as Staff.';
        showError(msg);
        showToast(msg, 'error');
        return;
      }
    }

    btn.textContent = 'Signing in…';
    btn.disabled = true;
    hideError();

    try {
      const cred = await signInWithEmailAndPassword(auth, email, password);
      const user = await getUserData(cred.user.uid);
      if (!user) {
        await auth.signOut();
        showError('User profile not found. Please register.');
        showToast('User profile not found. Please register.', 'error');
        btn.textContent = 'Login';
        btn.disabled = false;
        return;
      }

      // 1. Staff approval verification
      if (user.role === 'staff' && !user.isApproved) {
        await auth.signOut();
        showError('Access Denied. Waiting for Admin Approval.');
        showToast('Access Denied. Waiting for Admin Approval.', 'error');
        btn.textContent = 'Login';
        btn.disabled = false;
        return;
      }

      // 2. Strict Role Mismatch Check (Mirrors LoginScreen.kt)
      const expectedRole = rolePrefix; // 'student', 'staff', 'admin'
      const actualRole = (user.role || '').toLowerCase();
      if (expectedRole && actualRole && actualRole !== expectedRole) {
        await auth.signOut();
        const capitalizedRole = actualRole.charAt(0).toUpperCase() + actualRole.slice(1);
        const mismatchMsg = `Role Mismatch: This account is registered as ${capitalizedRole}.`;
        showError(mismatchMsg);
        showToast(mismatchMsg, 'error');
        btn.textContent = 'Login';
        btn.disabled = false;
        return;
      }

      // 3. Save credentials only if role matched and remember-me is checked
      if (document.getElementById('remember-me').checked) {
        const existing = JSON.parse(localStorage.getItem(savedKey) || '[]');
        const updated = existing.filter(a => isAccountForRole(a, rolePrefix) && a.email.toLowerCase() !== email);
        updated.unshift({ email, password, role: rolePrefix });
        localStorage.setItem(savedKey, JSON.stringify(updated.slice(0, 5)));
      }

      // Pre-set user state and load complaints immediately to eliminate dashboard delay
      AppState.currentUserData = user;
      if (user.collegeName) AppState.collegeName = user.collegeName;
      btn.textContent = 'Loading dashboard…';
      await subscribeToComplaints(user);

      showToast('Login successful ✅', 'success');
      onLogin();
    } catch (e) {
      btn.textContent = 'Login';
      btn.disabled = false;
      const msg = e.code === 'auth/network-request-failed'
        ? 'Network Error: Check your internet connection.'
        : e.code === 'auth/invalid-credential' || e.code === 'auth/wrong-password' || e.code === 'auth/user-not-found'
        ? 'Invalid email or password'
        : e.message || 'Login failed';
      showError(msg);
      showToast(msg, 'error');
    }
  });
}
