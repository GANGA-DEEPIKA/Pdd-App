// register.js — Mirrors RegisterScreen.kt
import { auth } from '../firebase-config.js';
import { createUserWithEmailAndPassword } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js';
import { db } from '../firebase-config.js';
import { doc, setDoc, serverTimestamp } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js';
import { AppState } from '../app.js';
import { createPendingStaffRequest } from './firestore-service.js';
import { showToast } from './utils.js';

export function renderRegister(container, title, emailHint, onBackClick) {
  const isStaff = title.includes('Staff');
  const isAdmin = title.includes('Admin');
  const isStudent = title.includes('Student');
  const role = isStudent ? 'student' : isStaff ? 'staff' : 'admin';
  const rolePrefix = role;

  container.innerHTML = `
  <div class="screen active bg-primary" style="min-height:100vh;display:flex;flex-direction:column;overflow-y:auto;">
    <div style="padding:16px 24px;display:flex;align-items:center;gap:8px;">
      <button class="back-btn back-btn-white" id="reg-back">←</button>
      <h1 style="color:white;font-size:22px;font-weight:800;">${title}</h1>
    </div>
    <div style="padding:0 24px 40px;display:flex;flex-direction:column;align-items:center;">
      <div class="card card-lg" style="width:100%;max-width:480px;">
        <p style="color:#1565C0;font-size:14px;font-weight:600;margin-bottom:16px;">${AppState.collegeName}</p>
        <div class="form-group">
          <label class="form-label">Full Name</label>
          <input type="text" id="reg-name" class="form-input" placeholder="Your full name" autocomplete="name" />
        </div>
        <div class="form-group">
          <label class="form-label">${emailHint}</label>
          <input type="email" id="reg-email" class="form-input" placeholder="${emailHint}" autocomplete="email" />
        </div>
        <div class="form-group">
          <label class="form-label">Mobile Number</label>
          <input type="tel" id="reg-phone" class="form-input" placeholder="10-digit phone number" autocomplete="tel" />
        </div>
        ${isStudent ? `
        <div class="form-group">
          <label class="form-label">Registration Number</label>
          <input type="text" id="reg-regno" class="form-input" placeholder="e.g. 21CS001" />
        </div>
        <div class="form-group">
          <label class="form-label">Department</label>
          <input type="text" id="reg-dept" class="form-input" placeholder="e.g. Computer Science" />
        </div>
        <div class="form-group">
          <label class="form-label">Year (e.g. 3rd Year)</label>
          <input type="text" id="reg-year" class="form-input" placeholder="e.g. 3rd Year" />
        </div>` : ''}
        <div class="form-group">
          <label class="form-label">Password</label>
          <div style="position:relative;">
            <input type="password" id="reg-password" class="form-input" placeholder="Min 6 characters" style="padding-right:48px;" />
            <button id="reg-toggle-pass" style="position:absolute;right:12px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;font-size:18px;">👁️</button>
          </div>
        </div>
        <div id="reg-error" class="alert alert-error alert-hidden"></div>
        <div id="reg-success" class="alert alert-success alert-hidden"></div>
        <button id="reg-btn" class="btn btn-primary btn-lg" style="margin-top:8px;">Create Account</button>
      </div>
    </div>
  </div>`;

  document.getElementById('reg-back').addEventListener('click', onBackClick);

  // Password toggle
  const passInput = document.getElementById('reg-password');
  document.getElementById('reg-toggle-pass').addEventListener('click', () => {
    passInput.type = passInput.type === 'password' ? 'text' : 'password';
    document.getElementById('reg-toggle-pass').textContent = passInput.type === 'password' ? '👁️' : '🙈';
  });

  document.getElementById('reg-btn').addEventListener('click', async () => {
    const name = document.getElementById('reg-name').value.trim();
    const email = document.getElementById('reg-email').value.trim().toLowerCase();
    const phone = document.getElementById('reg-phone').value.trim();
    const password = document.getElementById('reg-password').value.trim();
    
    const department = isStudent ? (document.getElementById('reg-dept')?.value.trim() || '') : '';
    const regNo = isStudent ? (document.getElementById('reg-regno')?.value.trim() || '') : '';
    const year = isStudent ? (document.getElementById('reg-year')?.value.trim() || '') : '';
    
    const errEl = document.getElementById('reg-error');
    const succEl = document.getElementById('reg-success');
    const btn = document.getElementById('reg-btn');

    errEl.classList.add('alert-hidden');
    succEl.classList.add('alert-hidden');

    if (!name || !email || !phone || !password || (isStudent && (!department || !regNo))) { 
        showError('Please fill all fields'); 
        return; 
    }
    
    if (!email.startsWith(rolePrefix)) {
        showError(`Use your college ${rolePrefix} email`);
        return;
    }

    if (password.length < 6) { 
        showError('Password must be at least 6 characters'); 
        return; 
    }

    btn.textContent = 'Creating…'; btn.disabled = true;

    try {
      const cred = await createUserWithEmailAndPassword(auth, email, password);
      const uid = cred.user.uid;

      const userData = {
        uid, 
        name, 
        email, 
        phone,
        collegeName: AppState.collegeName.trim(),
        role: rolePrefix,
        department,
        regNo,
        year,
        isApproved: (rolePrefix !== "staff"),
        approved: true // used in some places in web app fallback
      };
      
      // To exactly match Android structure:
      if (rolePrefix !== "staff") {
          userData.college = AppState.collegeName.trim();
      }

      if (isStaff) {
        await createPendingStaffRequest(userData);
        auth.signOut();
        succEl.textContent = '✅ Account created! Awaiting admin approval.';
        succEl.classList.remove('alert-hidden');
        btn.textContent = 'Create Account'; btn.disabled = false;
      } else {
        await setDoc(doc(db, 'users', uid), userData);
        showToast('Account created successfully! ✅', 'success');
        onBackClick();
      }
    } catch (e) {
      btn.textContent = 'Create Account'; btn.disabled = false;
      const msg = e.code === 'auth/email-already-in-use'
        ? 'Account exists. Please use Forgot Password.'
        : e.message || 'Registration failed';
      showError(msg);
    }

    function showError(msg) {
      errEl.textContent = msg;
      errEl.classList.remove('alert-hidden');
    }
  });
}

