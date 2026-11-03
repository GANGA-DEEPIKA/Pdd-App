// complaint-status.js — Mirrors ComplaintStatusScreen.kt
import { AppState, goBack } from '../app.js';
import { formatDate, getStatusBadgeClass } from './utils.js';

export function renderComplaintStatus(container) {
  const c = AppState.complaintData || {};

  const statusClass = getStatusBadgeClass(c.status);
  const hasPhoto = c.photoUrl && c.photoUrl.startsWith('base64:');
  const hasProof = c.proofPhotoUrl && c.proofPhotoUrl.startsWith('base64:');
  const hasVoice = c.voiceUrl && c.voiceUrl.startsWith('base64:');

  // Timeline steps
  const steps = [
    { label: 'Submitted', done: true, icon: '📝' },
    { label: 'Assigned to Staff', done: c.status !== 'Pending' && !c.status?.startsWith('Waiting'), icon: '👷' },
    { label: 'In Progress', done: ['In Progress', 'Resolved', 'Escalated'].includes(c.status), icon: '🔧' },
    { label: 'Resolved', done: c.status === 'Resolved', icon: '✅' },
  ];

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar">
      <button class="back-btn" id="cs-back">←</button>
      <span class="app-bar-title">Complaint Status</span>
    </div>
    <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:60px;">
      <!-- Ticket header -->
      <div style="background:linear-gradient(135deg,#1565C0,#1A237E);border-radius:20px;padding:20px;margin-bottom:20px;color:white;">
        <div style="font-size:12px;opacity:0.75;font-weight:600;letter-spacing:1px;">TICKET ID</div>
        <div style="font-size:24px;font-weight:800;margin:4px 0;">${c.ticketId || 'N/A'}</div>
        <div style="margin-top:8px;"><span class="badge ${statusClass}" style="font-size:13px;padding:5px 14px;">${c.status || 'Pending'}</span></div>
      </div>
      <!-- Details card -->
      <div class="card" style="margin-bottom:16px;">
        <h3 style="font-size:15px;font-weight:700;color:#1565C0;margin-bottom:16px;">${c.title || 'Complaint'}</h3>
        ${c.category && c.category !== 'General Maintenance' ? dRow('Category', c.category) : (c.category ? dRow('Category', c.category) : '')}
        ${dRow('Location', c.location)}
        ${dRow('Priority', c.priority)}
        ${dRow('College', c.collegeName)}
        ${dRow('Submitted On', formatDate(c.createdAt))}
        ${c.assignedStaffEmail ? dRow('Assigned Staff', c.assignedStaffEmail) : ''}
        ${c.details ? `<div style="margin-top:12px;"><div style="font-size:12px;color:#757575;margin-bottom:4px;">Description</div><div style="background:#F5F7FA;padding:12px;border-radius:8px;font-size:14px;color:#333;line-height:1.6;">${c.details}</div></div>` : ''}
      </div>
      <!-- Timeline -->
      <div class="card" style="margin-bottom:16px;">
        <h3 style="font-size:14px;font-weight:700;color:#555;margin-bottom:16px;">Progress Timeline</h3>
        ${steps.map((step, i) => `
        <div style="display:flex;align-items:center;gap:12px;margin-bottom:${i<steps.length-1?'0':'0'};">
          <div style="display:flex;flex-direction:column;align-items:center;">
            <div style="width:36px;height:36px;border-radius:50%;background:${step.done?'#1565C0':'#E0E0E0'};display:flex;align-items:center;justify-content:center;font-size:16px;transition:background 0.3s;">${step.done?step.icon:'○'}</div>
            ${i<steps.length-1?`<div style="width:2px;height:24px;background:${step.done&&steps[i+1].done?'#1565C0':'#E0E0E0'};margin:2px 0;"></div>`:''}
          </div>
          <div style="padding:8px 0;">
            <div style="font-size:14px;font-weight:${step.done?'700':'500'};color:${step.done?'#1565C0':'#BDBDBD'};">${step.label}</div>
          </div>
        </div>`).join('')}
      </div>
      <!-- Attachments -->
      ${(hasPhoto || hasVoice || hasProof) ? `
      <div class="card" style="margin-bottom:16px;">
        <h3 style="font-size:14px;font-weight:700;color:#555;margin-bottom:12px;">Attachments</h3>
        ${hasPhoto ? `<div style="margin-bottom:12px;">
          <div style="font-size:12px;color:#757575;margin-bottom:6px;">Complaint Photo</div>
          <img src="data:image/jpeg;base64,${c.photoUrl.replace('base64:','')}" style="width:100%;border-radius:12px;max-height:200px;object-fit:cover;" />
        </div>` : ''}
        ${hasProof ? `<div>
          <div style="font-size:12px;color:#757575;margin-bottom:6px;">Resolution Proof</div>
          <img src="data:image/jpeg;base64,${c.proofPhotoUrl.replace('base64:','')}" style="width:100%;border-radius:12px;max-height:200px;object-fit:cover;" />
        </div>` : ''}
        ${hasVoice ? `<div style="background:#E3F2FD;border-radius:8px;padding:10px;font-size:13px;color:#1565C0;">🎙 Voice recording attached</div>` : ''}
      </div>` : ''}
    </div>
  </div>`;

  function dRow(label, value) {
    if (!value) return '';
    const isCategory = label === 'Category';
    const isDept = label === 'Department';
    const valDisplay = isCategory
      ? `<span style="background:#E3F2FD;color:#1565C0;padding:4px 12px;border-radius:8px;font-weight:700;font-size:12px;">🏷️ ${value}</span>`
      : isDept
      ? `<span style="background:#EDE7F6;color:#512DA8;padding:4px 12px;border-radius:8px;font-weight:700;font-size:12px;">🏢 ${value}</span>`
      : `<span style="font-size:13px;font-weight:600;color:#212121;max-width:60%;text-align:right;">${value}</span>`;
    return `<div style="display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid #F5F5F5;">
      <span style="font-size:13px;color:#757575;">${label}</span>
      ${valDisplay}
    </div>`;
  }

  document.getElementById('cs-back').addEventListener('click', () => {
    AppState.complaintData = {};
    goBack();
  });
}
