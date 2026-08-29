// forgot-password.js — Mirrors ForgotPasswordScreen.kt
import { auth } from '../firebase-config.js';
import { sendPasswordResetEmail } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js';

export function renderForgotPassword(container, onBackClick) {
  container.innerHTML = `
  <div class="screen active bg-primary" style="min-height:100vh;display:flex;flex-direction:column;padding:24px;">
    <button class="back-btn back-btn-white" id="forgot-back">←</button>
    <div style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;">
      <div class="card card-lg" style="width:100%;max-width:420px;">
        <div style="text-align:center;margin-bottom:24px;">
          <div style="font-size:52px;">🔐</div>
          <h2 style="font-size:22px;font-weight:800;color:#1565C0;margin-top:8px;">Forgot Password?</h2>
          <p style="font-size:14px;color:#757575;margin-top:6px;">Enter your email and we'll send a reset link.</p>
        </div>
        <div class="form-group">
          <label class="form-label">Email Address</label>
          <input type="email" id="forgot-email" class="form-input" placeholder="your@college.edu" autocomplete="email" />
        </div>
        <div id="forgot-error" class="alert alert-error alert-hidden"></div>
        <div id="forgot-success" class="alert alert-success alert-hidden"></div>
        <button id="forgot-send-btn" class="btn btn-primary btn-lg" style="margin-top:8px;">Send Reset Link</button>
      </div>
    </div>
  </div>`;

  document.getElementById('forgot-back').addEventListener('click', onBackClick);
  document.getElementById('forgot-send-btn').addEventListener('click', async () => {
    const email = document.getElementById('forgot-email').value.trim();
    const errEl = document.getElementById('forgot-error');
    const succEl = document.getElementById('forgot-success');
    if (!email) { errEl.textContent = 'Please enter your email.'; errEl.classList.remove('alert-hidden'); return; }
    errEl.classList.add('alert-hidden');
    const btn = document.getElementById('forgot-send-btn');
    btn.textContent = 'Sending…'; btn.disabled = true;
    try {
      await sendPasswordResetEmail(auth, email);
      succEl.textContent = '✅ Reset link sent! Check your inbox.';
      succEl.classList.remove('alert-hidden');
    } catch (e) {
      errEl.textContent = e.message || 'Failed to send reset email.';
      errEl.classList.remove('alert-hidden');
    }
    btn.textContent = 'Send Reset Link'; btn.disabled = false;
  });
}
