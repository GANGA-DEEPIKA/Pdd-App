// new-complaint.js — Mirrors NewComplaintScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';
import { auth } from '../firebase-config.js';
import { suggestCategory } from './utils.js';

export function renderNewComplaint(container) {
  let selectedPhotoFile = null;
  let selectedVideoFile = null;
  let audioBlob = null;
  let mediaRecorder = null;
  let audioChunks = [];
  let isRecording = false;

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <!-- App bar -->
    <div class="app-bar" style="box-shadow:0 2px 8px rgba(0,0,0,0.1);">
      <button class="back-btn" id="nc-back">←</button>
      <span class="app-bar-title">New Complaint</span>
    </div>
    <!-- Recording banner (hidden) -->
    <div id="recording-banner" class="recording-banner" style="display:none;">
      <span>● Recording Audio…</span>
      <span style="font-size:12px;opacity:0.8;">Tap stop icon to save</span>
    </div>
    <!-- Body -->
    <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:60px;">
      <!-- Problem details card -->
      <div class="card" style="margin-bottom:20px;">
        <h3 style="font-size:15px;font-weight:700;color:#1565C0;margin-bottom:16px;">Problem Details</h3>
        <div class="form-group">
          <label class="form-label">Subject *</label>
          <input type="text" id="nc-title" class="form-input" placeholder="Brief title of the issue" />
        </div>
        <div class="form-group">
          <label class="form-label">Description</label>
          <textarea id="nc-details" class="form-textarea" placeholder="Describe the problem in detail…"></textarea>
        </div>
        <!-- Auto category -->
        <div id="nc-category-tag" class="category-tag" style="display:none;margin-bottom:16px;"></div>
        <div class="form-group">
          <label class="form-label">Location *</label>
          <input type="text" id="nc-location" class="form-input" placeholder="e.g. Block A, Room 101" />
        </div>
      </div>
      <!-- Attachments -->
      <h3 style="font-size:15px;font-weight:700;color:#555;margin-bottom:12px;">Attachments</h3>
      <div class="attachment-row">
        <div class="attachment-box" id="attach-photo">
          <span id="attach-photo-icon">📸</span>
          <span class="attachment-label" id="attach-photo-label">Photo</span>
        </div>
        <div class="attachment-box" id="attach-video">
          <span id="attach-video-icon">🎥</span>
          <span class="attachment-label" id="attach-video-label">Video</span>
        </div>
        <div class="attachment-box" id="attach-voice">
          <span id="attach-voice-icon">🎙</span>
          <span class="attachment-label" id="attach-voice-label">Voice</span>
        </div>
      </div>
      <!-- Attachment status -->
      <div id="attach-status" style="display:none;background:#E8F5E9;border-radius:12px;padding:12px;margin-top:12px;"></div>
      <!-- Hidden file inputs -->
      <input type="file" id="photo-file-input" accept="image/*" style="display:none;" />
      <input type="file" id="video-file-input" accept="video/*" style="display:none;" />
      <!-- Submit button -->
      <button id="nc-submit" class="btn btn-primary btn-lg" style="margin-top:24px;" disabled>Review &amp; Submit</button>
    </div>
    
    <!-- Manage Dialog (hidden) -->
    <div id="manage-dialog-overlay" style="display:none;position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.5);align-items:center;justify-content:center;z-index:1000;">
      <div style="background:white;border-radius:24px;padding:24px;width:300px;text-align:center;box-shadow:0 10px 30px rgba(0,0,0,0.2);">
        <h3 id="manage-dialog-title" style="margin-bottom:8px;font-size:18px;font-weight:500;">Manage Photo</h3>
        <p style="color:#666;font-size:14px;margin-bottom:24px;">What would you like to do with this attachment?</p>
        <div style="display:flex;justify-content:space-evenly;">
          <button id="manage-remove-btn" style="background:none;border:none;color:#D32F2F;font-size:16px;font-weight:600;cursor:pointer;">Remove</button>
          <button id="manage-view-btn" style="background:none;border:none;color:#5E35B1;font-size:16px;font-weight:600;cursor:pointer;">View</button>
        </div>
      </div>
    </div>
  </div>`;

  const titleInput = document.getElementById('nc-title');
  const detailsInput = document.getElementById('nc-details');
  const locationInput = document.getElementById('nc-location');
  const catTag = document.getElementById('nc-category-tag');
  const submitBtn = document.getElementById('nc-submit');
  const attachStatus = document.getElementById('attach-status');

  // Auto-suggest category
  function updateCategory() {
    const text = `${titleInput.value} ${detailsInput.value}`.trim();
    if (text) {
      const cat = suggestCategory(text);
      catTag.textContent = `Category: ${cat}`;
      catTag.style.display = 'inline-block';
      AppState._ncCategory = cat;
    } else {
      catTag.style.display = 'none';
      AppState._ncCategory = 'General Maintenance';
    }
    submitBtn.disabled = !(titleInput.value.trim() && locationInput.value.trim());
  }

  titleInput.addEventListener('input', updateCategory);
  detailsInput.addEventListener('input', updateCategory);
  locationInput.addEventListener('input', () => {
    submitBtn.disabled = !(titleInput.value.trim() && locationInput.value.trim());
  });

  // Dialog logic
  let currentManageType = null;
  function showManageDialog(type) {
    currentManageType = type;
    document.getElementById('manage-dialog-title').textContent = `Manage ${type.charAt(0).toUpperCase() + type.slice(1)}`;
    document.getElementById('manage-dialog-overlay').style.display = 'flex';
  }

  document.getElementById('manage-remove-btn').addEventListener('click', () => {
    if (currentManageType === 'photo') selectedPhotoFile = null;
    if (currentManageType === 'video') selectedVideoFile = null;
    if (currentManageType === 'voice') audioBlob = null;
    document.getElementById('manage-dialog-overlay').style.display = 'none';
    updateAttachUI();
  });

  document.getElementById('manage-view-btn').addEventListener('click', () => {
    let file = null;
    if (currentManageType === 'photo') file = selectedPhotoFile;
    if (currentManageType === 'video') file = selectedVideoFile;
    if (currentManageType === 'voice') file = audioBlob;
    if (file) {
      window.open(URL.createObjectURL(file), '_blank');
    }
    document.getElementById('manage-dialog-overlay').style.display = 'none';
  });

  document.getElementById('manage-dialog-overlay').addEventListener('click', e => {
    if (e.target.id === 'manage-dialog-overlay') {
      e.target.style.display = 'none';
    }
  });

  // Photo attachment
  const photoInput = document.getElementById('photo-file-input');
  document.getElementById('attach-photo').addEventListener('click', () => {
    if (selectedPhotoFile) {
      showManageDialog('photo');
    } else {
      photoInput.click();
    }
  });
  photoInput.addEventListener('change', e => {
    if (e.target.files[0]) {
      selectedPhotoFile = e.target.files[0];
      updateAttachUI();
    }
  });

  // Video attachment
  const videoInput = document.getElementById('video-file-input');
  document.getElementById('attach-video').addEventListener('click', () => {
    if (selectedVideoFile) {
      showManageDialog('video');
    } else {
      videoInput.click();
    }
  });
  videoInput.addEventListener('change', e => {
    if (e.target.files[0]) {
      selectedVideoFile = e.target.files[0];
      updateAttachUI();
    }
  });

  // Voice recording
  document.getElementById('attach-voice').addEventListener('click', async () => {
    if (isRecording) {
      // Stop recording
      mediaRecorder?.stop();
    } else if (audioBlob) {
      showManageDialog('voice');
    } else {
      // Start recording
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        audioChunks = [];
        mediaRecorder = new MediaRecorder(stream);
        mediaRecorder.ondataavailable = e => audioChunks.push(e.data);
        mediaRecorder.onstop = () => {
          audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
          stream.getTracks().forEach(t => t.stop());
          isRecording = false;
          document.getElementById('recording-banner').style.display = 'none';
          updateAttachUI();
        };
        mediaRecorder.start();
        isRecording = true;
        document.getElementById('recording-banner').style.display = 'flex';
        updateAttachUI();
      } catch {
        alert('Microphone access denied or not available.');
      }
    }
  });

  function updateAttachUI() {
    const photoBox = document.getElementById('attach-photo');
    const videoBox = document.getElementById('attach-video');
    const voiceBox = document.getElementById('attach-voice');

    if (selectedPhotoFile) {
      photoBox.classList.add('has-file');
      document.getElementById('attach-photo-label').textContent = '✓ Photo';
    } else {
      photoBox.classList.remove('has-file');
      document.getElementById('attach-photo-label').textContent = 'Photo';
    }

    if (selectedVideoFile) {
      videoBox.classList.add('has-file');
      document.getElementById('attach-video-label').textContent = '✓ Video';
    } else {
      videoBox.classList.remove('has-file');
      document.getElementById('attach-video-label').textContent = 'Video';
    }

    if (isRecording) {
      voiceBox.style.borderColor = 'red';
      document.getElementById('attach-voice-icon').textContent = '⏹';
      document.getElementById('attach-voice-label').textContent = 'Stop';
    } else if (audioBlob) {
      voiceBox.classList.add('has-file');
      document.getElementById('attach-voice-icon').textContent = '🎙';
      document.getElementById('attach-voice-label').textContent = '✓ Voice';
    } else {
      voiceBox.classList.remove('has-file');
      voiceBox.style.borderColor = '';
      document.getElementById('attach-voice-icon').textContent = '🎙';
      document.getElementById('attach-voice-label').textContent = 'Voice';
    }

    const lines = [];
    if (selectedPhotoFile) lines.push(`✅ Photo attached`);
    if (selectedVideoFile) lines.push(`✅ Video attached`);
    if (audioBlob) lines.push(`✅ Voice attached`);
    if (lines.length) {
      attachStatus.innerHTML = lines.map(l => `<div style="font-size:13px;color:#2E7D32;font-weight:500;display:flex;align-items:center;gap:4px;margin-bottom:2px;">${l}</div>`).join('');
      attachStatus.style.display = 'block';
    } else {
      attachStatus.style.display = 'none';
    }
  }

  document.getElementById('nc-back').addEventListener('click', goBack);

  submitBtn.addEventListener('click', () => {
    AppState.complaintData = {
      title: titleInput.value.trim(),
      details: detailsInput.value.trim(),
      location: locationInput.value.trim(),
      category: AppState._ncCategory || 'General Maintenance',
      priority: 'Medium',
      status: 'Pending',
      collegeName: AppState.currentUserData?.collegeName || AppState.collegeName,
      _photoFile: selectedPhotoFile,
      _videoFile: selectedVideoFile,
      _audioBlob: audioBlob,
    };
    navigateTo('reviewComplaint');
  });
}
