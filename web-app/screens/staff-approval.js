// staff-approval.js — Mirrors StaffApprovalScreen.kt
import { AppState, goBack } from '../app.js';
import { getPendingStaffRequests, approveStaffRequest, rejectStaffRequest } from './firestore-service.js';
import { showToast } from './utils.js';

const CATEGORIES = ['Electrical', 'Plumbing', 'Network/WiFi', 'Cleanliness', 'Furniture', 'Hostel', 'Security', 'Transport', 'Laboratory', 'General Maintenance'];

export async function renderStaffApproval(container) {
  const college = AppState.currentUserData?.collegeName || AppState.collegeName;

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar app-bar-dark">
      <button class="back-btn back-btn-white" id="sa-back">←</button>
      <span class="app-bar-title" style="color:white;">Staff Approval</span>
    </div>
    <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:40px;" id="sa-body">
      <div class="loading-inline"><div class="spinner-sm"></div></div>
    </div>
  </div>`;

  document.getElementById('sa-back').addEventListener('click', goBack);

  try {
    const requests = await getPendingStaffRequests(college);
    const body = document.getElementById('sa-body');

    if (requests.length === 0) {
      body.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">✅</div>
        <div class="empty-state-title">No Pending Requests</div>
        <div class="empty-state-sub">All staff requests have been processed.</div>
      </div>`;
      return;
    }

    body.innerHTML = `
    <p style="font-size:14px;color:#757575;margin-bottom:16px;">${requests.length} pending staff request${requests.length>1?'s':''} for <strong>${college}</strong></p>
    ${requests.map(staff => `
    <div class="card" style="margin-bottom:16px;" id="staff-card-${staff.uid}">
      <div style="display:flex;align-items:center;gap:12px;margin-bottom:16px;">
        <div style="width:48px;height:48px;background:#1565C0;border-radius:50%;display:flex;align-items:center;justify-content:center;color:white;font-weight:700;font-size:20px;">${(staff.name||'S')[0].toUpperCase()}</div>
        <div>
          <div style="font-weight:700;font-size:15px;">${staff.name}</div>
          <div style="font-size:13px;color:#757575;">${staff.email}</div>
          <div style="font-size:12px;color:#9E9E9E;">${staff.phone || ''}</div>
        </div>
      </div>
      <div style="margin-bottom:12px;">
        <div style="font-size:13px;font-weight:600;color:#555;margin-bottom:8px;">Assign Categories:</div>
        <div style="display:flex;flex-wrap:wrap;gap:6px;" id="cat-select-${staff.uid}">
          ${CATEGORIES.map(cat => `<div class="cat-chip-sa" data-uid="${staff.uid}" data-cat="${cat}" style="padding:5px 10px;border-radius:16px;border:2px solid #E0E0E0;font-size:12px;cursor:pointer;font-weight:600;background:white;transition:all 0.2s;">${cat}</div>`).join('')}
        </div>
      </div>
      <div style="display:flex;gap:10px;">
        <button class="btn btn-primary btn-sm" id="approve-${staff.uid}" style="flex:1;" data-uid="${staff.uid}">✅ Approve</button>
        <button class="btn btn-danger btn-sm" id="reject-${staff.uid}" style="flex:1;" data-uid="${staff.uid}">❌ Reject</button>
      </div>
    </div>`).join('')}`;

    // Category chip toggle
    const selectedCats = {};
    requests.forEach(s => { selectedCats[s.uid] = []; });

    document.querySelectorAll('.cat-chip-sa').forEach(chip => {
      chip.addEventListener('click', () => {
        const uid = chip.dataset.uid;
        const cat = chip.dataset.cat;
        if (!selectedCats[uid]) selectedCats[uid] = [];
        if (selectedCats[uid].includes(cat)) {
          selectedCats[uid] = selectedCats[uid].filter(c => c !== cat);
          chip.style.background = 'white';
          chip.style.color = '#333';
          chip.style.borderColor = '#E0E0E0';
        } else {
          selectedCats[uid].push(cat);
          chip.style.background = '#1565C0';
          chip.style.color = 'white';
          chip.style.borderColor = '#1565C0';
        }
      });
    });

    // Approve/reject
    requests.forEach(staff => {
      document.getElementById(`approve-${staff.uid}`)?.addEventListener('click', async () => {
        const cats = selectedCats[staff.uid] || [];
        if (cats.length === 0) { alert('Please select at least one category for this staff member.'); return; }
        const btn = document.getElementById(`approve-${staff.uid}`);
        btn.textContent = 'Approving…'; btn.disabled = true;
        try {
          await approveStaffRequest(staff, cats);
          showToast(`${staff.name} approved ✅`, 'success');
          document.getElementById(`staff-card-${staff.uid}`)?.remove();
          if (!document.querySelector('[id^="staff-card-"]')) {
            document.getElementById('sa-body').innerHTML = `<div class="empty-state"><div class="empty-state-icon">✅</div><div class="empty-state-title">All requests processed</div></div>`;
          }
        } catch (e) { showToast(e.message || 'Approval failed', 'error'); btn.textContent = '✅ Approve'; btn.disabled = false; }
      });

      document.getElementById(`reject-${staff.uid}`)?.addEventListener('click', async () => {
        if (!confirm(`Reject ${staff.name}'s request?`)) return;
        const btn = document.getElementById(`reject-${staff.uid}`);
        btn.textContent = 'Rejecting…'; btn.disabled = true;
        try {
          await rejectStaffRequest(staff.uid);
          showToast(`${staff.name} rejected`, 'error');
          document.getElementById(`staff-card-${staff.uid}`)?.remove();
        } catch (e) { showToast(e.message || 'Rejection failed', 'error'); btn.textContent = '❌ Reject'; btn.disabled = false; }
      });
    });
  } catch (e) {
    document.getElementById('sa-body').innerHTML = `<div class="alert alert-error">Failed to load requests: ${e.message}</div>`;
  }
}
