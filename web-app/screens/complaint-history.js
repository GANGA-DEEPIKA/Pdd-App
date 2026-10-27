// complaint-history.js — Mirrors ComplaintHistoryScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';
import { formatDate, getStatusBadgeClass, getComplaintBorderClass, showToast } from './utils.js';
import { db } from '../firebase-config.js';
import { doc, deleteDoc } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js';

export function renderComplaintHistory(container) {
  const filter = AppState.filterType || 'all';
  const allList = AppState.complaintList || [];

  let title, filteredList;
  if (filter === 'Resolved') { title = 'Resolved Complaints'; filteredList = allList.filter(c => c.status === 'Resolved'); }
  else if (filter === 'active') { title = 'Active Complaints'; filteredList = allList.filter(c => c.status !== 'Resolved'); }
  else { title = 'All Complaints'; filteredList = allList; }

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar">
      <button class="back-btn" id="ch-back">←</button>
      <span class="app-bar-title">${title}</span>
    </div>
    <div style="padding:16px;overflow-y:auto;flex:1;padding-bottom:40px;" id="history-list">
      ${filteredList.length === 0 ? `
      <div class="empty-state">
        <div class="empty-state-icon">📋</div>
        <div class="empty-state-title">No complaints found</div>
        <div class="empty-state-sub">${filter === 'Resolved' ? 'No resolved complaints yet.' : 'No complaints to show.'}</div>
      </div>` :
      filteredList.map(c => `
      <div class="complaint-item ${getComplaintBorderClass(c.status)}" data-ticket="${c.ticketId}">
        <div class="complaint-item-header">
          <div>
            <div class="complaint-title">${c.title || 'Untitled'}</div>
            <div class="complaint-category">${c.category || ''}</div>
          </div>
          <span class="badge ${getStatusBadgeClass(c.status)}">${c.status || 'Pending'}</span>
        </div>
        <div style="display:flex;justify-content:space-between;align-items:center;margin-top:10px;">
          <div class="complaint-meta" style="margin-top:0;">📍 ${c.location || 'N/A'} &nbsp;•&nbsp; ${formatDate(c.createdAt)} &nbsp;•&nbsp; 🖊️ ${c.ticketId}</div>
          <button class="delete-complaint-btn" data-ticket="${c.ticketId}" style="background:#FFEBEE;color:#DC2626;border:none;border-radius:8px;padding:6px 14px;font-size:12px;font-weight:700;cursor:pointer;display:inline-flex;align-items:center;gap:4px;transition:background 0.2s;">
            🗑️ Delete
          </button>
        </div>
      </div>`).join('')}
    </div>
  </div>
  <style>
    .delete-complaint-btn:hover {
      background: #FFCDD2 !important;
      transform: scale(1.02);
    }
  </style>`;

  document.getElementById('ch-back').addEventListener('click', goBack);

  document.querySelectorAll('.complaint-item').forEach(item => {
    item.addEventListener('click', () => {
      const ticket = item.dataset.ticket;
      const complaint = filteredList.find(c => c.ticketId === ticket);
      if (complaint) {
        AppState.complaintData = complaint;
        navigateTo('complaintStatus');
      }
    });
  });

  document.querySelectorAll('.delete-complaint-btn').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      e.stopPropagation(); // Stop opening complaint status details
      const ticket = btn.dataset.ticket;
      if (!confirm(`Are you sure you want to delete complaint ticket ${ticket}?`)) return;

      btn.disabled = true;
      btn.textContent = 'Deleting…';

      try {
        if (ticket) {
          await deleteDoc(doc(db, 'all_complaints', ticket));
        }
        showToast('Complaint deleted successfully! 🗑️', 'success');
        AppState.complaintList = AppState.complaintList.filter(c => c.ticketId !== ticket);

        if (AppState.currentUserData?.uid) {
          try {
            const cacheKey = `campusfix_complaints_${AppState.currentUserData.uid}`;
            const cached = JSON.parse(localStorage.getItem(cacheKey) || '[]');
            const updated = cached.filter(c => c.ticketId !== ticket);
            localStorage.setItem(cacheKey, JSON.stringify(updated));
          } catch {}
        }

        renderComplaintHistory(container);
      } catch (err) {
        console.error('Delete complaint error:', err);
        showToast('Failed to delete complaint', 'error');
        btn.disabled = false;
        btn.innerHTML = '🗑️ Delete';
      }
    });
  });
}
