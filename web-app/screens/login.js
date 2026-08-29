// login.js — Mirrors LoginScreen.kt
import { auth } from '../firebase-config.js';
import { signInWithEmailAndPassword } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js';
import { AppState } from '../app.js';
import { getUserData } from './firestore-service.js';
import { showToast } from './utils.js';

export function renderLogin(container, title, emailHint, onLogin, onForgotPassword, onBackClick, onRegisterClick) {
  const rolePrefix = title.includes('Student') ? 'student' : title.includes('Staff') ? 'staff' : 'admin';
  const savedKey = `campusfix_saved_${rolePrefix}`;
  let savedAccounts = [];
  try { savedAccounts = JSON.parse(localStorage.getItem(savedKey) || '[]'); } catch { savedAccounts = []; }

  container.innerHTML = `
  <div class="screen active bg-primary" style="min-height:100vh;display:flex;flex-direction:column;padding:24px;overflow-y:auto;">
    <div style="display:flex;align-items:center;">
      <button class="back-btn back-btn-white" id="login-back">←</button>
    </div>
    <div style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;padding:16px 0;">
      <p style="color:rgba(255,255,255,0.9);font-size:17px;font-weight:500;text-align:center;margin-bottom:4px;">${AppState.collegeName}</p>
      <h1 style="color:white;font-size:32px;font-weight:800;margin-bottom:32px;text-align:center;">${title}</h1>
      <div class="card card-lg" style="width:100%;max-width:420px;">
        <div class="form-group" style="position:relative;">
          <label class="form-label">Email</label>
          <input type="email" id="login-email" class="form-input" placeholder="${emailHint}" autocomplete="email" />
          <div id="saved-accounts-dropdown" class="saved-accounts" style="display:none;position:absolute;width:100%;top:100%;left:0;z-index:100;"></div>
        </div>
        <div class="form-group">
          <label class="form-label">Password</label>
          <div style="position:relative;">
            <input type="password" id="login-password" class="form-input" placeholder="Password" autocomplete="current-password" style="padding-right:48px;" />
            <button id="toggle-pass" style="position:absolute;right:12px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;font-size:18px;">👁️</button>
          </div>
        </div>
        <div class="checkbox-row" style="margin-bottom:24px;">
          <input type="checkbox" id="remember-me" checked />
          <label for="remember-me">Remember me</label>
        </div>
        <div id="login-error" class="alert alert-error alert-hidden"></div>
        <button id="login-btn" class="btn btn-primary btn-lg">Login</button>
      </div>
      <button class="btn-text text-white mt-12" id="forgot-btn" style="color:white;background:none;border:none;cursor:pointer;font-size:15px;padding:12px;">Forgot Password?</button>
      <div style="display:flex;align-items:center;gap:4px;margin-top:4px;">
        <span style="color:rgba(255,255,255,0.8);font-size:14px;">Don't have an account?</span>
        <button style="background:none;border:none;color:white;font-weight:700;font-size:14px;cursor:pointer;padding:8px 4px;" id="register-btn">Register Now</button>
      </div>
    </div>
  </div>`;

  const emailInput = document.getElementById('login-email');
  const passwordInput = document.getElementById('login-password');
  const dropdown = document.getElementById('saved-accounts-dropdown');
  const errEl = document.getElementById('login-error');

  // Saved accounts dropdown
  if (savedAccounts.length > 0) {
    emailInput.addEventListener('focus', () => {
      dropdown.innerHTML = `<div class="saved-accounts-header">Saved Accounts</div>` +
        savedAccounts.map(a => `<div class="saved-account-item" data-email="${a.email}" data-pass="${a.password}">👤 ${a.email}</div>`).join('') +
        `<div class="saved-account-item" style="color:#757575;" id="dismiss-saved">Dismiss</div>`;
      dropdown.style.display = 'block';
      dropdown.querySelectorAll('[data-email]').forEach(el => {
        el.addEventListener('click', () => {
          emailInput.value = el.dataset.email;
          passwordInput.value = el.dataset.pass;
          dropdown.style.display = 'none';
        });
      });
      document.getElementById('dismiss-saved')?.addEventListener('click', () => dropdown.style.display = 'none');
    });
  }

  document.getElementById('toggle-pass').addEventListener('click', () => {
    const isPass = passwordInput.type === 'password';
    passwordInput.type = isPass ? 'text' : 'password';
    document.getElementById('toggle-pass').textContent = isPass ? '🙈' : '👁️';
  });

  document.getElementById('login-back').addEventListener('click', onBackClick);
  document.getElementById('forgot-btn').addEventListener('click', onForgotPassword);
  document.getElementById('register-btn').addEventListener('click', onRegisterClick);

  document.getElementById('login-btn').addEventListener('click', async () => {
    const email = emailInput.value.trim().toLowerCase();
    const password = passwordInput.value.trim();
    if (!email || !password) {
      showError('Required fields missing'); return;
    }

    // College email validation
    const collegeLower = AppState.collegeName.toLowerCase();
    const domainKeywords = ['saveetha', 'srm', 'vit', 'annauniv', 'iit', 'nit'];
    const noiseWords = ['school','engineering','college','university','institute','technology','clg','univ','of','and','the','for'];
    let isCorrect = domainKeywords.some(k => collegeLower.includes(k) && email.includes(k));
    if (!isCorrect) {
      const keywords = collegeLower.split(/[\s,.\-]/).filter(w => w.length > 2 && !noiseWords.includes(w));
      isCorrect = keywords.some(k => email.includes(k)) || keywords.length === 0;
    }
    if (!isCorrect && AppState.collegeName) {
      showError(`Use only ${AppState.collegeName} email`); return;
    }

    const btn = document.getElementById('login-btn');
    btn.textContent = 'Signing in…'; btn.disabled = true;
    errEl.classList.add('alert-hidden');

    try {
      const cred = await signInWithEmailAndPassword(auth, email, password);
      const user = await getUserData(cred.user.uid);
      if (!user) {
        auth.signOut();
        showError('User profile not found. Please register.'); btn.textContent = 'Login'; btn.disabled = false; return;
      }
      if (user.role === 'staff' && !user.isApproved) {
        auth.signOut();
        showError('Access Denied. Waiting for Admin Approval.'); btn.textContent = 'Login'; btn.disabled = false; return;
      }

      // Save credentials
      if (document.getElementById('remember-me').checked) {
        const existing = JSON.parse(localStorage.getItem(savedKey) || '[]');
        const updated = existing.filter(a => a.email !== email);
        updated.unshift({ email, password });
        localStorage.setItem(savedKey, JSON.stringify(updated.slice(0, 5)));
      }

      showToast('Login successful ✅', 'success');
      onLogin();
    } catch (e) {
      btn.textContent = 'Login'; btn.disabled = false;
      const msg = e.code === 'auth/network-request-failed'
        ? 'Network Error: Check your internet connection.'
        : e.message || 'Login failed';
      showError(msg);
    }
  });

  function showError(msg) {
    errEl.textContent = msg;
    errEl.classList.remove('alert-hidden');
  }
}
