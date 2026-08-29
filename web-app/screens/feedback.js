// feedback.js — Mirrors FeedbackScreen.kt
import { AppState, goBack } from '../app.js';
import { submitFeedback } from './firestore-service.js';
import { showToast } from './utils.js';

export function renderFeedback(container) {
  let rating = 0;
  const collegeName = AppState.currentUserData?.collegeName || AppState.collegeName;

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar">
      <button class="back-btn" id="fb-back">←</button>
      <span class="app-bar-title">Feedback</span>
    </div>
    <div style="padding:24px;overflow-y:auto;flex:1;padding-bottom:60px;display:flex;flex-direction:column;align-items:center;">
      <div class="card" style="width:100%;max-width:480px;">
        <div style="text-align:center;margin-bottom:24px;">
          <div style="font-size:52px;">⭐</div>
          <h2 style="font-size:22px;font-weight:800;color:#1565C0;margin-top:8px;">Rate Your Experience</h2>
          <p style="font-size:14px;color:#757575;margin-top:4px;">Your feedback helps us improve CampusFix</p>
        </div>
        <!-- Star rating -->
        <div style="display:flex;justify-content:center;gap:10px;margin-bottom:8px;" id="star-row">
          ${[1,2,3,4,5].map(i => `<button class="star-btn" id="star-${i}" data-val="${i}" style="font-size:40px;background:none;border:none;cursor:pointer;transition:transform 0.15s;filter:grayscale(1) opacity(0.35);">⭐</button>`).join('')}
        </div>
        <p id="star-label" style="text-align:center;font-size:14px;color:#757575;margin-bottom:20px;">Tap a star to rate</p>
        <div class="form-group">
          <label class="form-label">Comments (Optional)</label>
          <textarea id="fb-comments" class="form-textarea" placeholder="Tell us about your experience…" rows="5"></textarea>
        </div>
        <div id="fb-error" class="alert alert-error alert-hidden"></div>
        <button id="fb-submit" class="btn btn-primary btn-lg">Submit Feedback</button>
      </div>
    </div>
  </div>`;

  const labels = ['','Poor','Fair','Good','Very Good','Excellent'];

  document.getElementById('fb-back').addEventListener('click', goBack);

  [1,2,3,4,5].forEach(i => {
    document.getElementById(`star-${i}`).addEventListener('click', () => {
      rating = i;
      [1,2,3,4,5].forEach(j => {
        const btn = document.getElementById(`star-${j}`);
        btn.style.filter = j <= i ? 'none' : 'grayscale(1) opacity(0.35)';
        btn.style.transform = j === i ? 'scale(1.2)' : 'scale(1)';
      });
      document.getElementById('star-label').textContent = labels[i];
      document.getElementById('star-label').style.color = '#FF9800';
      document.getElementById('star-label').style.fontWeight = '700';
    });
  });

  document.getElementById('fb-submit').addEventListener('click', async () => {
    const comments = document.getElementById('fb-comments').value.trim();
    const errEl = document.getElementById('fb-error');
    if (rating === 0) { errEl.textContent = 'Please select a rating.'; errEl.classList.remove('alert-hidden'); return; }
    const btn = document.getElementById('fb-submit');
    btn.textContent = 'Submitting…'; btn.disabled = true;
    errEl.classList.add('alert-hidden');
    try {
      await submitFeedback(rating, comments, collegeName);
      showToast('Thank you for your feedback! ⭐', 'success');
      goBack();
    } catch (e) {
      errEl.textContent = e.message || 'Submission failed.';
      errEl.classList.remove('alert-hidden');
      btn.textContent = 'Submit Feedback'; btn.disabled = false;
    }
  });
}
