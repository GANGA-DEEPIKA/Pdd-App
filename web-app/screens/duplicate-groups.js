// duplicate-groups.js — AI Duplicate Complaint Detection and Grouping for Staff Dashboard
import { AppState, navigateTo, goBack } from '../app.js';
import { groupComplaintsWithAI } from './ai-duplicate-engine.js';
import {
  confirmDuplicateGroup,
  separateDuplicateComplaint,
  separateAllInGroup,
  updateComplaintStatus
} from './firestore-service.js';
import { showToast } from './utils.js';

export function renderDuplicateGroups(container) {
  let activeFilter = 'all'; // 'all', 'suggested', 'confirmed'
  let isProcessing = false;

  function render() {
    const list = AppState.complaintList || [];
    const allGroups = groupComplaintsWithAI(list);

    let filteredGroups = allGroups;
    if (activeFilter === 'suggested') {
      filteredGroups = allGroups.filter(g => !g.isConfirmed);
    } else if (activeFilter === 'confirmed') {
      filteredGroups = allGroups.filter(g => g.isConfirmed);
    }

    const totalGroups = allGroups.length;
    const totalAffected = allGroups.reduce((acc, g) => acc + g.totalStudentsAffected, 0);
    const confirmedCount = allGroups.filter(g => g.isConfirmed).length;
    const suggestedCount = totalGroups - confirmedCount;

    container.innerHTML = `
    <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
      <!-- App Bar -->
      <div class="app-bar" style="background:#fff;box-shadow:0 2px 8px rgba(0,0,0,0.08);display:flex;align-items:center;padding:16px 20px;position:sticky;top:0;z-index:100;">
        <button class="back-btn" id="dg-back" style="background:none;border:none;font-size:22px;cursor:pointer;color:#333;margin-right:12px;">←</button>
        <div>
          <h1 style="font-size:18px;font-weight:800;color:#1565C0;margin:0;">AI Duplicate Groups</h1>
          <p style="font-size:11px;color:#757575;margin:2px 0 0;">Automatic similarity detection & student impact</p>
        </div>
      </div>

      <!-- Content -->
      <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:50px;">
        
        <!-- Summary Stats Card -->
        <div style="background:linear-gradient(135deg,#1565C0,#0D47A1);border-radius:18px;padding:20px;color:white;margin-bottom:20px;box-shadow:0 4px 14px rgba(21,101,192,0.25);">
          <div style="display:flex;align-items:center;gap:12px;margin-bottom:16px;">
            <div style="width:42px;height:42px;border-radius:12px;background:rgba(255,255,255,0.2);display:flex;align-items:center;justify-content:center;font-size:22px;">👥</div>
            <div>
              <div style="font-size:16px;font-weight:800;">Duplicate Complaints Overview</div>
              <div style="font-size:12px;opacity:0.85;">Grouped by AI based on Title, Category, Location & Details</div>
            </div>
          </div>

          <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:10px;">
            <div style="background:rgba(255,255,255,0.15);border-radius:12px;padding:12px 10px;text-align:center;">
              <div style="font-size:22px;font-weight:800;">${totalGroups}</div>
              <div style="font-size:11px;opacity:0.85;margin-top:2px;">Groups Detected</div>
            </div>
            <div style="background:rgba(255,255,255,0.15);border-radius:12px;padding:12px 10px;text-align:center;">
              <div style="font-size:22px;font-weight:800;">${totalAffected}</div>
              <div style="font-size:11px;opacity:0.85;margin-top:2px;">Students Affected</div>
            </div>
            <div style="background:rgba(255,255,255,0.15);border-radius:12px;padding:12px 10px;text-align:center;">
              <div style="font-size:22px;font-weight:800;">${confirmedCount}</div>
              <div style="font-size:11px;opacity:0.85;margin-top:2px;">Confirmed</div>
            </div>
          </div>
        </div>

        <!-- Filter Tabs -->
        <div style="display:flex;gap:8px;margin-bottom:20px;overflow-x:auto;padding-bottom:4px;">
          <button class="dg-filter-btn ${activeFilter === 'all' ? 'active' : ''}" data-filter="all" style="padding:8px 16px;border-radius:20px;border:none;background:${activeFilter === 'all' ? '#1565C0' : '#E0E0E0'};color:${activeFilter === 'all' ? '#fff' : '#555'};font-size:12px;font-weight:700;cursor:pointer;white-space:nowrap;">
            All Groups (${totalGroups})
          </button>
          <button class="dg-filter-btn ${activeFilter === 'suggested' ? 'active' : ''}" data-filter="suggested" style="padding:8px 16px;border-radius:20px;border:none;background:${activeFilter === 'suggested' ? '#1565C0' : '#E0E0E0'};color:${activeFilter === 'suggested' ? '#fff' : '#555'};font-size:12px;font-weight:700;cursor:pointer;white-space:nowrap;">
            🤖 AI Suggested (${suggestedCount})
          </button>
          <button class="dg-filter-btn ${activeFilter === 'confirmed' ? 'active' : ''}" data-filter="confirmed" style="padding:8px 16px;border-radius:20px;border:none;background:${activeFilter === 'confirmed' ? '#1565C0' : '#E0E0E0'};color:${activeFilter === 'confirmed' ? '#fff' : '#555'};font-size:12px;font-weight:700;cursor:pointer;white-space:nowrap;">
            ✔ Confirmed (${confirmedCount})
          </button>
        </div>

        <!-- Groups List -->
        ${filteredGroups.length === 0 ? `
          <div style="background:white;border-radius:16px;padding:40px 20px;text-align:center;box-shadow:0 1px 4px rgba(0,0,0,0.06);margin-top:10px;">
            <div style="font-size:42px;margin-bottom:12px;">🎉</div>
            <div style="font-size:16px;font-weight:700;color:#333;">No Duplicate Complaints Found</div>
            <div style="font-size:13px;color:#757575;max-width:320px;margin:6px auto 0;line-height:1.5;">
              All current complaints in your assigned categories are unique. The AI automatically scans all incoming complaints in real-time.
            </div>
          </div>
        ` : filteredGroups.map((g, gIdx) => {
          const main = g.mainComplaint;
          return `
          <div class="card dg-group-card" style="margin-bottom:24px;border-radius:18px;overflow:hidden;box-shadow:0 3px 12px rgba(0,0,0,0.07);border:1px solid #E0E0E0;background:white;" data-gid="${g.groupId}">
            
            <!-- Group Header -->
            <div style="background:#F0F7FF;padding:16px;border-bottom:1px solid #D6E8FA;display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:10px;">
              <div style="display:flex;align-items:center;gap:10px;">
                <span style="background:#1565C0;color:white;font-weight:800;font-size:12px;padding:5px 12px;border-radius:14px;display:inline-flex;align-items:center;gap:5px;">
                  👥 ${g.totalStudentsAffected} Students Affected
                </span>
                ${g.isConfirmed ? `
                  <span style="background:#E8F5E9;color:#2E7D32;font-size:11px;font-weight:700;padding:4px 10px;border-radius:12px;border:1px solid #C8E6C9;">
                    ✔ Confirmed Group
                  </span>
                ` : `
                  <span style="background:#FFF3E0;color:#E65100;font-size:11px;font-weight:700;padding:4px 10px;border-radius:12px;border:1px solid #FFE0B2;">
                    🤖 AI Suggested (${g.confidence}% Match)
                  </span>
                `}
              </div>
              <div style="font-size:11px;color:#666;font-weight:600;">
                Group ID: <strong style="color:#1565C0;">${g.groupId}</strong>
              </div>
            </div>

            <!-- AI Reason Pill -->
            <div style="padding:10px 16px;background:#FAFAFA;border-bottom:1px solid #EEEEEE;font-size:12px;color:#555;display:flex;align-items:center;gap:8px;">
              <span style="font-size:14px;">💡</span>
              <span><strong>AI Detection:</strong> ${g.matchReason}</span>
            </div>

            <div style="padding:16px;">
              <!-- Main Complaint Details -->
              <div style="background:#F9FBFF;border-left:4px solid #1565C0;border-radius:8px;padding:14px;margin-bottom:16px;">
                <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;flex-wrap:wrap;gap:6px;">
                  <div style="display:flex;align-items:center;gap:8px;">
                    <span style="background:#1565C0;color:white;font-size:10px;font-weight:800;padding:2px 8px;border-radius:6px;letter-spacing:0.5px;">PRIMARY TICKET</span>
                    <strong style="color:#1565C0;font-size:13px;">${main.ticketId}</strong>
                  </div>
                  <span class="badge ${main.status === 'Resolved' ? 'badge-resolved' : main.status === 'In Progress' ? 'badge-progress' : 'badge-assigned'}" style="font-size:11px;">
                    ${main.status || 'Pending'}
                  </span>
                </div>
                
                <div style="font-size:15px;font-weight:700;color:#212121;margin-bottom:6px;">${main.title || 'Untitled Issue'}</div>
                <div style="font-size:13px;color:#444;line-height:1.4;margin-bottom:10px;">${main.details || main.description || 'No description provided.'}</div>

                <div style="display:flex;align-items:center;gap:8px;font-size:12px;color:#555;flex-wrap:wrap;">
                  <span style="background:#E3F2FD;color:#1565C0;padding:3px 10px;border-radius:8px;font-weight:700;">🏷️ ${main.category || 'General'}</span>
                  <span>📍 <strong>${main.location || 'Campus'}</strong></span>
                  ${main.studentEmail ? `<span style="color:#757575;">👤 ${main.studentEmail}</span>` : ''}
                </div>
              </div>

              <!-- Linked Complaints Section -->
              <div style="margin-bottom:16px;">
                <div style="font-size:13px;font-weight:700;color:#444;margin-bottom:10px;display:flex;align-items:center;gap:6px;">
                  <span>🔗 Linked Duplicate Complaints (${g.linkedComplaints.length})</span>
                </div>

                <div style="display:flex;flex-direction:column;gap:10px;">
                  ${g.linkedComplaints.map(linked => `
                    <div style="background:#F5F7FA;border-radius:12px;padding:12px 14px;border:1px solid #E0E0E0;display:flex;align-items:flex-start;justify-content:space-between;gap:12px;">
                      <div style="flex:1;">
                        <div style="display:flex;align-items:center;gap:8px;margin-bottom:4px;flex-wrap:wrap;">
                          <span style="font-weight:700;color:#1565C0;font-size:12px;">${linked.ticketId}</span>
                          <span class="badge ${linked.status === 'Resolved' ? 'badge-resolved' : linked.status === 'In Progress' ? 'badge-progress' : 'badge-assigned'}" style="font-size:10px;padding:2px 8px;">
                            ${linked.status || 'Pending'}
                          </span>
                          ${linked.studentEmail ? `<span style="font-size:11px;color:#757575;">👤 ${linked.studentEmail}</span>` : ''}
                        </div>
                        <div style="font-size:13px;font-weight:600;color:#333;margin-bottom:4px;">${linked.title || 'Untitled'}</div>
                        <div style="font-size:12px;color:#666;line-height:1.4;">${(linked.details || linked.description || '').substring(0, 120)}${(linked.details || '').length > 120 ? '…' : ''}</div>
                        <div style="font-size:11px;color:#888;margin-top:6px;">📍 ${linked.location || main.location} • 🏷️ ${linked.category || main.category}</div>
                      </div>

                      <button class="dg-separate-btn" data-main="${main.ticketId}" data-linked="${linked.ticketId}" title="Separate this complaint from the group" style="background:#FFF0F0;border:1px solid #FFCDD2;color:#D32F2F;padding:6px 10px;border-radius:8px;font-size:11px;font-weight:700;cursor:pointer;white-space:nowrap;flex-shrink:0;">
                        ✂️ Separate
                      </button>
                    </div>
                  `).join('')}
                </div>
              </div>

              <!-- Group Management Actions Bar -->
              <div style="background:#FAFAFA;border-radius:12px;padding:14px;border:1px solid #EAEAEA;display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:10px;">
                
                <!-- Quick Status Cascader -->
                <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
                  <span style="font-size:12px;font-weight:700;color:#555;">Update Group Status:</span>
                  <button class="dg-status-btn" data-tid="${main.ticketId}" data-status="In Progress" style="background:#FFF8E1;border:1px solid #FFE082;color:#F57F17;padding:6px 12px;border-radius:8px;font-size:11px;font-weight:700;cursor:pointer;">
                    🔄 In Progress
                  </button>
                  <button class="dg-status-btn" data-tid="${main.ticketId}" data-status="Resolved" style="background:#E8F5E9;border:1px solid #A5D6A7;color:#2E7D32;padding:6px 12px;border-radius:8px;font-size:11px;font-weight:700;cursor:pointer;">
                    ✅ Resolved
                  </button>
                </div>

                <!-- Group Confirmation & Separation Actions -->
                <div style="display:flex;align-items:center;gap:8px;">
                  ${!g.isConfirmed ? `
                    <button class="dg-confirm-btn" data-main="${main.ticketId}" data-linked="${g.linkedComplaints.map(l => l.ticketId).join(',')}" data-reason="${g.matchReason}" style="background:#1565C0;color:white;border:none;padding:8px 14px;border-radius:8px;font-size:12px;font-weight:700;cursor:pointer;display:inline-flex;align-items:center;gap:4px;">
                      ✔ Confirm Group
                    </button>
                  ` : ''}

                  <button class="dg-separate-all-btn" data-main="${main.ticketId}" data-linked="${g.linkedComplaints.map(l => l.ticketId).join(',')}" style="background:#FAFAFA;border:1px solid #BDBDBD;color:#616161;padding:8px 12px;border-radius:8px;font-size:12px;font-weight:600;cursor:pointer;">
                    Separate All
                  </button>
                </div>

              </div>

            </div>
          </div>`;
        }).join('')}

      </div>
    </div>`;

    // Event listeners
    document.getElementById('dg-back').addEventListener('click', goBack);

    // Filter tabs
    document.querySelectorAll('.dg-filter-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        activeFilter = btn.dataset.filter;
        render();
      });
    });

    // Confirm Group
    document.querySelectorAll('.dg-confirm-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        if (isProcessing) return;
        const mainTicketId = btn.dataset.main;
        const linkedTicketIds = btn.dataset.linked.split(',').filter(Boolean);
        const reason = btn.dataset.reason;

        isProcessing = true;
        btn.textContent = 'Confirming…';
        btn.disabled = true;

        try {
          await confirmDuplicateGroup(mainTicketId, linkedTicketIds, reason);
          showToast(`Group ${mainTicketId} confirmed with ${linkedTicketIds.length} linked complaints! ✅`, 'success');
          // Update local state
          const mainC = list.find(c => c.ticketId === mainTicketId);
          if (mainC) {
            mainC.isGroupMaster = true;
            mainC.linkedTicketIds = linkedTicketIds;
            mainC.duplicateConfirmed = true;
          }
          linkedTicketIds.forEach(id => {
            const lc = list.find(c => c.ticketId === id);
            if (lc) {
              lc.parentTicketId = mainTicketId;
              lc.duplicateConfirmed = true;
            }
          });
          render();
        } catch (e) {
          showToast('Failed to confirm group: ' + (e.message || e), 'error');
        } finally {
          isProcessing = false;
        }
      });
    });

    // Separate Single Complaint
    document.querySelectorAll('.dg-separate-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        if (isProcessing) return;
        const mainTicketId = btn.dataset.main;
        const linkedTicketId = btn.dataset.linked;

        isProcessing = true;
        btn.textContent = 'Separating…';
        btn.disabled = true;

        try {
          await separateDuplicateComplaint(mainTicketId, linkedTicketId);
          showToast(`Complaint ${linkedTicketId} separated from group! ✅`, 'success');
          // Update local state
          const mainC = list.find(c => c.ticketId === mainTicketId);
          if (mainC && Array.isArray(mainC.linkedTicketIds)) {
            mainC.linkedTicketIds = mainC.linkedTicketIds.filter(id => id !== linkedTicketId);
            if (mainC.linkedTicketIds.length === 0) mainC.isGroupMaster = false;
          }
          const lc = list.find(c => c.ticketId === linkedTicketId);
          if (lc) {
            lc.parentTicketId = '';
            lc.duplicateConfirmed = false;
          }
          render();
        } catch (e) {
          showToast('Failed to separate complaint: ' + (e.message || e), 'error');
        } finally {
          isProcessing = false;
        }
      });
    });

    // Separate All in Group
    document.querySelectorAll('.dg-separate-all-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        if (isProcessing) return;
        const mainTicketId = btn.dataset.main;
        const linkedTicketIds = btn.dataset.linked.split(',').filter(Boolean);

        if (!confirm(`Are you sure you want to separate all ${linkedTicketIds.length + 1} complaints back into standalone tickets?`)) {
          return;
        }

        isProcessing = true;
        btn.textContent = 'Separating…';
        btn.disabled = true;

        try {
          await separateAllInGroup(mainTicketId, linkedTicketIds);
          showToast(`All complaints separated into individual tickets! ✅`, 'success');
          // Update local state
          const mainC = list.find(c => c.ticketId === mainTicketId);
          if (mainC) {
            mainC.isGroupMaster = false;
            mainC.linkedTicketIds = [];
            mainC.duplicateConfirmed = false;
          }
          linkedTicketIds.forEach(id => {
            const lc = list.find(c => c.ticketId === id);
            if (lc) {
              lc.parentTicketId = '';
              lc.duplicateConfirmed = false;
            }
          });
          render();
        } catch (e) {
          showToast('Failed to separate group: ' + (e.message || e), 'error');
        } finally {
          isProcessing = false;
        }
      });
    });

    // Update Status across entire group
    document.querySelectorAll('.dg-status-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        if (isProcessing) return;
        const mainTicketId = btn.dataset.tid;
        const newStatus = btn.dataset.status;

        isProcessing = true;
        const origText = btn.textContent;
        btn.textContent = 'Updating…';
        btn.disabled = true;

        try {
          await updateComplaintStatus(mainTicketId, newStatus);
          showToast(`Group status updated to "${newStatus}" for all linked complaints! ✅`, 'success');
          
          // Update local state for main and all linked
          const mainC = list.find(c => c.ticketId === mainTicketId);
          if (mainC) {
            mainC.status = newStatus;
            const linkedIds = Array.isArray(mainC.linkedTicketIds) ? mainC.linkedTicketIds : [];
            linkedIds.forEach(id => {
              const lc = list.find(c => c.ticketId === id);
              if (lc) lc.status = newStatus;
            });
          }
          render();
        } catch (e) {
          showToast('Status update failed: ' + (e.message || e), 'error');
        } finally {
          isProcessing = false;
          btn.textContent = origText;
          btn.disabled = false;
        }
      });
    });
  }

  render();
}
