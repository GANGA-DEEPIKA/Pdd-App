// manage-students.js — Mirrors ManageStudentsScreen.kt
import { AppState, goBack } from '../app.js';
import { getStudentsByCollege, removeUser } from './firestore-service.js';
import { showToast } from './utils.js';

export async function renderManageStudents(container) {
  const college = AppState.currentUserData?.collegeName || AppState.collegeName;

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar app-bar-dark">
      <button class="back-btn back-btn-white" id="ms-back">←</button>
      <span class="app-bar-title" style="color:white;">Manage Students</span>
    </div>
    <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:40px;" id="ms-body">
      <div class="loading-inline"><div class="spinner-sm"></div></div>
    </div>
  </div>`;

  document.getElementById('ms-back').addEventListener('click', goBack);

  try {
    const students = await getStudentsByCollege(college);
    const body = document.getElementById('ms-body');

    if (students.length === 0) {
      body.innerHTML = `<div class="empty-state"><div class="empty-state-icon">👥</div><div class="empty-state-title">No Students Found</div><div class="empty-state-sub">No registered students for ${college}.</div></div>`;
      return;
    }

    body.innerHTML = `
    <p style="font-size:14px;color:#757575;margin-bottom:16px;"><strong>${students.length}</strong> registered students at ${college}</p>
    <div style="margin-bottom:12px;">
      <input type="text" id="ms-search" class="form-input" placeholder="🔍 Search by name or email…" />
    </div>
    <div id="ms-list">
      ${renderStudentList(students, students)}
    </div>`;

    document.getElementById('ms-search').addEventListener('input', e => {
      const q = e.target.value.toLowerCase();
      const filtered = students.filter(s =>
        (s.name||'').toLowerCase().includes(q) || (s.email||'').toLowerCase().includes(q)
      );
      document.getElementById('ms-list').innerHTML = renderStudentList(students, filtered);
      attachRemoveListeners(students);
    });

    attachRemoveListeners(students);

    function renderStudentList(all, filtered) {
      if (!filtered.length) return `<div class="empty-state"><div class="empty-state-icon">🔍</div><div class="empty-state-title">No results</div></div>`;
      return filtered.map(s => `
      <div class="card card-sm" style="margin-bottom:12px;display:flex;align-items:center;gap:12px;" id="student-card-${s.uid}">
        <div style="width:44px;height:44px;background:#1565C0;border-radius:50%;display:flex;align-items:center;justify-content:center;color:white;font-weight:700;font-size:18px;flex-shrink:0;">${(s.name||'S')[0].toUpperCase()}</div>
        <div style="flex:1;min-width:0;">
          <div style="font-weight:700;font-size:14px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${s.name}</div>
          <div style="font-size:12px;color:#757575;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${s.email}</div>
          ${s.regNo ? `<div style="font-size:11px;color:#9E9E9E;">${s.regNo} • ${s.year||''}</div>` : ''}
        </div>
        <button class="btn btn-danger btn-sm remove-student-btn" data-uid="${s.uid}" data-name="${s.name}" style="flex-shrink:0;font-size:12px;padding:0 10px;height:34px;">Remove</button>
      </div>`).join('');
    }

    function attachRemoveListeners(all) {
      document.querySelectorAll('.remove-student-btn').forEach(btn => {
        btn.addEventListener('click', async () => {
          const uid = btn.dataset.uid;
          const name = btn.dataset.name;
          if (!confirm(`Remove ${name} from the system?`)) return;
          btn.textContent = '…'; btn.disabled = true;
          try {
            await removeUser(uid);
            showToast(`${name} removed`, 'success');
            document.getElementById(`student-card-${uid}`)?.remove();
          } catch (e) {
            showToast(e.message || 'Remove failed', 'error');
            btn.textContent = 'Remove'; btn.disabled = false;
          }
        });
      });
    }
  } catch (e) {
    document.getElementById('ms-body').innerHTML = `<div class="alert alert-error">Failed to load students: ${e.message}</div>`;
  }
}
