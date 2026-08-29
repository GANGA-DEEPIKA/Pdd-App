// college-selection.js — Mirrors CollegeSelectionScreen.kt
import { AppState, navigateTo } from '../app.js';

export function renderCollegeSelection(container) {
  container.innerHTML = `
  <div class="screen active" style="background:linear-gradient(160deg,#1565C0 0%,#0D47A1 100%);min-height:100vh;display:flex;align-items:center;justify-content:center;padding:24px;">
    <div style="width:100%;max-width:420px;display:flex;flex-direction:column;align-items:center;gap:0;">
      <div style="text-align:center;margin-bottom:32px;">
        <div style="font-size:80px;animation:bounce 1.5s infinite">🎓</div>
        <h1 style="color:white;font-size:42px;font-weight:900;letter-spacing:-1px;margin-top:8px;">CampusFix</h1>
        <p style="color:rgba(255,255,255,0.8);font-size:16px;font-weight:500;margin-top:4px;">Smart Complaint Management</p>
      </div>
      <div class="card card-lg" style="width:100%;border-radius:28px;">
        <h2 style="font-size:24px;font-weight:700;color:#1565C0;text-align:center;">Welcome</h2>
        <p style="font-size:14px;color:#757575;text-align:center;margin-top:6px;margin-bottom:24px;">Please enter your college name to proceed</p>
        <div class="form-group">
          <input type="text" id="college-input" class="form-input" placeholder="e.g. Saveetha University" autocomplete="organization" />
        </div>
        <div id="college-error" class="alert alert-error alert-hidden" style="margin-top:8px;"></div>
        <button id="college-continue-btn" class="btn btn-primary btn-lg" style="margin-top:24px;">Continue</button>
      </div>
    </div>
  </div>`;

  const input = document.getElementById('college-input');
  const btn = document.getElementById('college-continue-btn');
  const errEl = document.getElementById('college-error');

  // Pre-fill if returning
  if (AppState.collegeName) input.value = AppState.collegeName;

  input.addEventListener('keydown', e => { if (e.key === 'Enter') btn.click(); });

  btn.addEventListener('click', () => {
    const name = input.value.trim();
    if (!name) {
      errEl.textContent = 'Please enter your college name.';
      errEl.classList.remove('alert-hidden');
      return;
    }
    AppState.collegeName = name;
    navigateTo('role');
  });
}
