// notification.js — Mirrors NotificationScreen.kt
import { AppState, goBack } from '../app.js';
import { formatDate, getStatusBadgeClass } from './utils.js';

export function renderNotification(container) {
  // Show recent status changes as notifications
  const list = AppState.complaintList.slice(0, 20);

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar">
      <button class="back-btn" id="notif-back">←</button>
      <span class="app-bar-title">Notifications</span>
      ${list.length > 0 ? `<span style="margin-left:auto;background:#F44336;color:white;padding:3px 8px;border-radius:12px;font-size:12px;font-weight:700;">${list.length}</span>` : ''}
    </div>
    <div style="padding:16px;overflow-y:auto;flex:1;padding-bottom:40px;">
      ${list.length === 0 ? `
      <div class="empty-state">
        <div class="empty-state-icon">🔔</div>
        <div class="empty-state-title">No Notifications</div>
        <div class="empty-state-sub">Your complaint updates will appear here.</div>
      </div>` :
      list.map(c => {
        const icon = c.status === 'Resolved' ? '✅' : c.status === 'In Progress' ? '🔧' : c.status === 'Assigned' ? '👷' : '📝';
        const msg = c.status === 'Resolved'
          ? `Your complaint "${c.title}" has been resolved!`
          : c.status === 'In Progress'
          ? `Staff is working on "${c.title}"`
          : c.status === 'Assigned'
          ? `"${c.title}" has been assigned to staff`
          : `"${c.title}" is pending`;
        return `
        <div style="background:white;border-radius:16px;padding:16px;margin-bottom:12px;display:flex;gap:12px;align-items:flex-start;box-shadow:0 1px 4px rgba(0,0,0,0.08);">
          <div style="width:40px;height:40px;border-radius:50%;background:#F5F7FA;display:flex;align-items:center;justify-content:center;font-size:20px;flex-shrink:0;">${icon}</div>
          <div style="flex:1;min-width:0;">
            <div style="font-weight:700;font-size:14px;margin-bottom:2px;">${msg}</div>
            <div style="display:flex;align-items:center;gap:8px;margin-top:6px;">
              <span class="badge ${getStatusBadgeClass(c.status)}">${c.status}</span>
              <span style="font-size:12px;color:#9E9E9E;">${formatDate(c.createdAt)}</span>
            </div>
            <div style="font-size:11px;color:#BDBDBD;margin-top:2px;">🎟 ${c.ticketId}</div>
          </div>
        </div>`;
      }).join('')}
    </div>
  </div>`;

  document.getElementById('notif-back').addEventListener('click', goBack);
}
