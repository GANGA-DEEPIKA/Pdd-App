// performance-summary.js — Mirrors PerformanceSummaryScreen.kt
import { AppState, goBack } from '../app.js';

export function renderPerformanceSummary(container) {
  const list = AppState.complaintList;
  const total = list.length;
  const resolved = list.filter(c => c.status === 'Resolved').length;
  const pending = list.filter(c => c.status === 'Pending' || c.status === 'Assigned' || (c.status||'').startsWith('Waiting')).length;
  const inProgress = list.filter(c => c.status === 'In Progress').length;
  const resolveRate = total > 0 ? Math.round(resolved / total * 100) : 0;

  // Category breakdown
  const catMap = {};
  list.forEach(c => {
    const cat = c.category || 'General Maintenance';
    if (!catMap[cat]) catMap[cat] = { total: 0, resolved: 0 };
    catMap[cat].total++;
    if (c.status === 'Resolved') catMap[cat].resolved++;
  });

  const categories = Object.entries(catMap).sort((a, b) => b[1].total - a[1].total);
  const maxCat = categories[0]?.[1]?.total || 1;

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar app-bar-dark">
      <button class="back-btn back-btn-white" id="ps-back">←</button>
      <span class="app-bar-title" style="color:white;">Performance Summary</span>
    </div>
    <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:40px;">
      <!-- Overview cards -->
      <h3 style="font-size:15px;font-weight:700;color:#555;margin-bottom:12px;">Overview</h3>
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:20px;">
        <div style="background:linear-gradient(135deg,#1565C0,#0D47A1);border-radius:20px;padding:16px;color:white;text-align:center;">
          <div style="font-size:32px;font-weight:800;">${total}</div>
          <div style="font-size:12px;opacity:0.8;font-weight:600;">Total Complaints</div>
        </div>
        <div style="background:linear-gradient(135deg,#4CAF50,#2E7D32);border-radius:20px;padding:16px;color:white;text-align:center;">
          <div style="font-size:32px;font-weight:800;">${resolved}</div>
          <div style="font-size:12px;opacity:0.8;font-weight:600;">Resolved</div>
        </div>
        <div style="background:linear-gradient(135deg,#FF9800,#E65100);border-radius:20px;padding:16px;color:white;text-align:center;">
          <div style="font-size:32px;font-weight:800;">${pending}</div>
          <div style="font-size:12px;opacity:0.8;font-weight:600;">Pending</div>
        </div>
        <div style="background:linear-gradient(135deg,#7B1FA2,#4A148C);border-radius:20px;padding:16px;color:white;text-align:center;">
          <div style="font-size:32px;font-weight:800;">${inProgress}</div>
          <div style="font-size:12px;opacity:0.8;font-weight:600;">In Progress</div>
        </div>
      </div>
      <!-- Resolution rate -->
      <div class="card" style="margin-bottom:20px;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
          <span style="font-size:15px;font-weight:700;">Resolution Rate</span>
          <span style="font-size:22px;font-weight:800;color:${resolveRate>=70?'#4CAF50':resolveRate>=40?'#FF9800':'#F44336'};">${resolveRate}%</span>
        </div>
        <div style="background:#F5F5F5;border-radius:8px;height:12px;overflow:hidden;">
          <div style="background:linear-gradient(90deg,#4CAF50,#81C784);height:100%;border-radius:8px;width:${resolveRate}%;transition:width 0.8s ease;"></div>
        </div>
        <p style="font-size:12px;color:#757575;margin-top:8px;">${resolveRate >= 70 ? '🌟 Excellent performance!' : resolveRate >= 40 ? '📈 Good progress, keep going!' : '⚠️ Needs improvement'}</p>
      </div>
      <!-- Category breakdown -->
      ${categories.length > 0 ? `
      <div class="card">
        <h3 style="font-size:15px;font-weight:700;margin-bottom:16px;">Category Breakdown</h3>
        ${categories.map(([cat, data]) => `
        <div style="margin-bottom:14px;">
          <div style="display:flex;justify-content:space-between;margin-bottom:4px;">
            <span style="font-size:13px;font-weight:600;">${cat}</span>
            <span style="font-size:12px;color:#757575;">${data.resolved}/${data.total} resolved</span>
          </div>
          <div style="background:#F5F5F5;border-radius:8px;height:8px;">
            <div style="background:#1565C0;border-radius:8px;height:8px;width:${Math.round(data.total/maxCat*100)}%;"></div>
          </div>
        </div>`).join('')}
      </div>` : ''}
    </div>
  </div>`;

  document.getElementById('ps-back').addEventListener('click', goBack);
}
