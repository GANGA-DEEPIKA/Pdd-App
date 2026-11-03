// new-complaint.js — Mirrors NewComplaintScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';
import { auth } from '../firebase-config.js';
import { suggestCategory } from './utils.js';
import { parseVoiceComplaint, createSpeechRecognizer, detectAICategory } from './ai-voice-assistant.js';

export function renderNewComplaint(container) {
  let selectedPhotoFile = null;
  let selectedVideoFile = null;
  let audioBlob = null;
  let mediaRecorder = null;
  let audioChunks = [];
  let isRecording = false;

  let activeSpeechRecognizer = null;
  let currentExtractedData = null;

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

      <!-- AI Voice Assistant Quick Action Banner -->
      <div class="voice-assistant-banner" id="nc-voice-btn" style="margin-bottom:18px;">
        <div style="display:flex;align-items:center;gap:12px;">
          <div style="width:42px;height:42px;border-radius:12px;background:rgba(255,255,255,0.22);display:flex;align-items:center;justify-content:center;font-size:22px;">🎙️</div>
          <div>
            <div style="font-size:15px;font-weight:700;">AI Voice Campus Assistant</div>
            <div style="font-size:12px;opacity:0.88;">Speak in English to auto-detect title, category & location</div>
          </div>
        </div>
        <span style="font-size:12px;background:rgba(255,255,255,0.25);padding:6px 12px;border-radius:14px;font-weight:700;">Tap to Speak 🎤</span>
      </div>

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
        <!-- AI-detected Category Label (Matches Android Design - identified after giving complaint) -->
        <div id="nc-category-container" style="display:none;margin-top:10px;margin-bottom:16px;">
          <div id="nc-category-tag" class="category-tag" style="display:inline-flex;align-items:center;background:#E3F2FD;color:#1565C0;padding:6px 14px;border-radius:8px;font-size:12px;font-weight:700;"></div>
        </div>
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

    <!-- AI Voice Assistant Modal Dialog -->
    <div id="voice-assistant-modal" class="modal-overlay center" style="display:none;padding:16px;">
      <div class="modal center-modal" style="box-shadow:0 12px 36px rgba(0,0,0,0.25);border:1px solid #BBDEFB;max-width:440px;">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;">
          <div style="display:flex;align-items:center;gap:8px;">
            <span style="font-size:24px;">🎙️</span>
            <div>
              <div style="font-size:16px;font-weight:800;color:#1565C0;">AI Campus Assistant</div>
              <div style="font-size:11px;color:#757575;">English Speech-to-Complaint Engine</div>
            </div>
          </div>
          <button id="va-close-btn" style="background:none;border:none;font-size:20px;cursor:pointer;color:#757575;">✕</button>
        </div>

        <div style="text-align:center;padding:10px 0 6px;">
          <span id="va-status-pill" class="mic-status-pill idle">● Tap Microphone</span>
          
          <div style="margin:18px 0 10px;">
            <button id="va-mic-btn" class="mic-pulse-btn" title="Tap to Speak">🎙️</button>
          </div>

          <div class="soundwave-container" id="va-soundwave">
            <div class="soundwave-bar"></div>
            <div class="soundwave-bar"></div>
            <div class="soundwave-bar"></div>
            <div class="soundwave-bar"></div>
            <div class="soundwave-bar"></div>
          </div>

          <p id="va-prompt" style="font-size:13px;color:#555;margin:4px 0 12px;line-height:1.4;">
            Tap the microphone and speak your complaint naturally in English.<br/>
            <span style="font-size:11px;color:#888;">e.g., "The fan in Room 101 Block A is vibrating and not spinning"</span>
          </p>

          <!-- Fallback text input if mic permission is denied or unsupported -->
          <div id="va-text-fallback" style="display:none;margin-bottom:12px;text-align:left;">
            <textarea id="va-manual-input" class="form-textarea" placeholder="Or type your complaint here in English..." style="min-height:60px;font-size:13px;"></textarea>
            <button id="va-analyze-text-btn" class="btn btn-secondary btn-sm" style="margin-top:6px;width:100%;font-weight:600;">🤖 Analyze Text with AI</button>
          </div>

          <!-- Speech Transcript Box -->
          <div id="va-transcript-box" style="display:none;background:#F5F7FA;border:1px solid #E0E0E0;border-radius:10px;padding:10px 12px;font-size:13px;color:#333;text-align:left;margin-bottom:12px;max-height:75px;overflow-y:auto;font-style:italic;">
          </div>

          <!-- AI Extracted Preview Card -->
          <div id="va-result-card" style="display:none;background:#E3F2FD;border:1px solid #90CAF9;border-radius:14px;padding:14px;text-align:left;margin-bottom:16px;">
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;">
              <span style="font-size:11px;font-weight:800;color:#1565C0;letter-spacing:0.5px;">🤖 AI EXTRACTED DETAILS</span>
              <span id="va-conf-badge" style="font-size:10px;background:#1565C0;color:white;padding:2px 8px;border-radius:10px;font-weight:700;">95% Match</span>
            </div>
            <div style="font-size:13px;margin-bottom:3px;"><strong>📌 Subject:</strong> <span id="va-res-title"></span></div>
            <div style="font-size:13px;margin-bottom:3px;"><strong>🏷️ Category:</strong> <span id="va-res-cat"></span></div>
            <div style="font-size:13px;margin-bottom:6px;"><strong>📍 Location:</strong> <span id="va-res-loc"></span></div>
            <div style="font-size:12px;color:#444;background:white;padding:8px 10px;border-radius:8px;border:1px solid #BBDEFB;line-height:1.4;" id="va-res-desc"></div>
          </div>

          <!-- Actions -->
          <div style="display:flex;flex-direction:column;gap:8px;">
            <button id="va-apply-btn" class="btn btn-primary btn-lg" style="display:none;font-weight:700;">
              ✅ Apply &amp; Review Details
            </button>
            <button id="va-cancel-btn" class="btn btn-secondary" style="font-weight:600;">
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>`;

  const titleInput = document.getElementById('nc-title');
  const detailsInput = document.getElementById('nc-details');
  const locationInput = document.getElementById('nc-location');
  const catTag = document.getElementById('nc-category-tag');
  const catContainer = document.getElementById('nc-category-container');
  const submitBtn = document.getElementById('nc-submit');
  const attachStatus = document.getElementById('attach-status');

  // Auto-detect category using AI logic (matching Android design)
  function updateCategory(overrideCategory) {
    const text = `${titleInput.value} ${detailsInput.value}`.trim();
    let cat = '';
    if (overrideCategory && overrideCategory !== 'General Maintenance') {
      cat = overrideCategory;
    } else if (text) {
      const aiResult = detectAICategory(text);
      if (aiResult.category && aiResult.category !== 'General Maintenance') {
        cat = aiResult.category;
      }
    }

    AppState._ncCategory = cat;

    if (catTag && catContainer) {
      if (cat) {
        catTag.textContent = `Category: ${cat}`;
        catContainer.style.display = 'block';
      } else {
        catTag.textContent = '';
        catContainer.style.display = 'none';
      }
    }

    submitBtn.disabled = !(titleInput.value.trim() && locationInput.value.trim());
  }

  titleInput.addEventListener('input', () => {
    updateCategory();
  });
  detailsInput.addEventListener('input', () => {
    updateCategory();
  });
  locationInput.addEventListener('input', () => {
    submitBtn.disabled = !(titleInput.value.trim() && locationInput.value.trim());
  });

  // Check for pre-filled data from voice assistant (if launched from Dashboard)
  if (AppState._voicePrefill) {
    titleInput.value = AppState._voicePrefill.title || '';
    detailsInput.value = AppState._voicePrefill.description || '';
    locationInput.value = AppState._voicePrefill.location || '';
    updateCategory(AppState._voicePrefill.category);
    AppState._voicePrefill = null;
  } else {
    updateCategory();
  }

  // --- Voice Assistant Modal Logic ---
  const vaModal = document.getElementById('voice-assistant-modal');
  const vaMicBtn = document.getElementById('va-mic-btn');
  const vaStatusPill = document.getElementById('va-status-pill');
  const vaSoundwave = document.getElementById('va-soundwave');
  const vaPrompt = document.getElementById('va-prompt');
  const vaTranscriptBox = document.getElementById('va-transcript-box');
  const vaResultCard = document.getElementById('va-result-card');
  const vaConfBadge = document.getElementById('va-conf-badge');
  const vaResTitle = document.getElementById('va-res-title');
  const vaResCat = document.getElementById('va-res-cat');
  const vaResDept = document.getElementById('va-res-dept');
  const vaResLoc = document.getElementById('va-res-loc');
  const vaResDesc = document.getElementById('va-res-desc');
  const vaApplyBtn = document.getElementById('va-apply-btn');
  const vaCancelBtn = document.getElementById('va-cancel-btn');
  const vaCloseBtn = document.getElementById('va-close-btn');
  const vaTextFallback = document.getElementById('va-text-fallback');
  const vaManualInput = document.getElementById('va-manual-input');
  const vaAnalyzeTextBtn = document.getElementById('va-analyze-text-btn');

  let isListening = false;

  function setMicStatus(status, text) {
    vaStatusPill.className = `mic-status-pill ${status}`;
    vaStatusPill.textContent = text;

    const bars = vaSoundwave.querySelectorAll('.soundwave-bar');
    if (status === 'listening') {
      vaMicBtn.classList.add('listening');
      bars.forEach(b => b.classList.add('active'));
    } else {
      vaMicBtn.classList.remove('listening');
      bars.forEach(b => b.classList.remove('active'));
    }
  }

  function handleVoiceInputText(transcript) {
    setMicStatus('processing', '🤖 AI Analyzing…');
    vaTranscriptBox.textContent = `"${transcript}"`;
    vaTranscriptBox.style.display = 'block';

    setTimeout(() => {
      currentExtractedData = parseVoiceComplaint(transcript);
      vaResTitle.textContent = currentExtractedData.title;
      vaResCat.textContent = currentExtractedData.category || 'General';
      if (vaResDept) vaResDept.textContent = currentExtractedData.department;
      vaResLoc.textContent = currentExtractedData.location;
      vaResDesc.textContent = currentExtractedData.description;
      vaConfBadge.textContent = `${currentExtractedData.confidence}% Confidence`;

      vaResultCard.style.display = 'block';
      vaApplyBtn.style.display = 'block';
      setMicStatus('success', '✔ Extracted Successfully');
      vaPrompt.innerHTML = 'Review the extracted details above. Tap <strong>Apply & Review Details</strong> to populate the complaint form.';
    }, 450);
  }

  function startVoiceListening() {
    if (isListening) {
      if (activeSpeechRecognizer) activeSpeechRecognizer.stop();
      isListening = false;
      setMicStatus('idle', '● Tap Microphone');
      return;
    }

    activeSpeechRecognizer = createSpeechRecognizer({
      onStart: () => {
        isListening = true;
        setMicStatus('listening', '● Listening in English…');
        vaPrompt.textContent = 'Listening... Speak your complaint naturally (what is the issue and where?)';
        vaResultCard.style.display = 'none';
        vaApplyBtn.style.display = 'none';
      },
      onResult: (transcript) => {
        isListening = false;
        handleVoiceInputText(transcript);
      },
      onError: (err) => {
        isListening = false;
        setMicStatus('error', '⚠️ Error');
        vaTextFallback.style.display = 'block';
        vaPrompt.innerHTML = `<span style="color:#D32F2F;">Speech error (${err}). You can speak again or type in the box below:</span>`;
      },
      onEnd: () => {
        isListening = false;
        if (vaStatusPill.classList.contains('listening')) {
          setMicStatus('idle', '● Tap Microphone');
        }
      }
    });

    if (!activeSpeechRecognizer) {
      // Browser does not support speech recognition — show helpful fallback input
      setMicStatus('idle', 'Speech API Unavailable');
      vaTextFallback.style.display = 'block';
      vaPrompt.textContent = 'Voice recognition is not supported in this browser. Enter your description below for AI analysis:';
    } else {
      try {
        activeSpeechRecognizer.start();
      } catch (e) {
        setMicStatus('error', 'Mic Busy / Blocked');
        vaTextFallback.style.display = 'block';
      }
    }
  }

  function openVoiceModal() {
    vaModal.style.display = 'flex';
    setMicStatus('idle', '● Tap Microphone');
    vaTranscriptBox.style.display = 'none';
    vaResultCard.style.display = 'none';
    vaApplyBtn.style.display = 'none';
    vaTextFallback.style.display = 'none';
    vaPrompt.innerHTML = 'Tap the microphone and speak your complaint naturally in English.<br/><span style="font-size:11px;color:#888;">e.g., "The fan in Room 101 Block A is vibrating and not spinning"</span>';
  }

  function closeVoiceModal() {
    if (isListening && activeSpeechRecognizer) {
      try { activeSpeechRecognizer.stop(); } catch {}
    }
    isListening = false;
    vaModal.style.display = 'none';
  }

  document.getElementById('nc-voice-btn').addEventListener('click', openVoiceModal);
  vaCloseBtn.addEventListener('click', closeVoiceModal);
  vaCancelBtn.addEventListener('click', closeVoiceModal);
  vaMicBtn.addEventListener('click', startVoiceListening);

  vaAnalyzeTextBtn.addEventListener('click', () => {
    const text = vaManualInput.value.trim();
    if (text) handleVoiceInputText(text);
  });

  vaApplyBtn.addEventListener('click', () => {
    if (currentExtractedData) {
      titleInput.value = currentExtractedData.title;
      detailsInput.value = currentExtractedData.description;
      locationInput.value = currentExtractedData.location;
      const voiceCat = currentExtractedData.category !== 'General Maintenance' ? currentExtractedData.category : '';
      AppState._ncCategory = voiceCat;
      updateCategory(voiceCat);
      closeVoiceModal();

      // Highlight fields briefly
      titleInput.style.borderColor = '#4CAF50';
      locationInput.style.borderColor = '#4CAF50';
      detailsInput.style.borderColor = '#4CAF50';
      setTimeout(() => {
        titleInput.style.borderColor = '';
        locationInput.style.borderColor = '';
        detailsInput.style.borderColor = '';
      }, 1500);
    }
  });

  // Open automatically if launched with flag
  if (AppState._openVoiceModal) {
    AppState._openVoiceModal = false;
    setTimeout(openVoiceModal, 150);
  }

  // --- Attachments Logic ---
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
      mediaRecorder?.stop();
    } else if (audioBlob) {
      showManageDialog('voice');
    } else {
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
    const text = `${titleInput.value} ${detailsInput.value}`.trim();
    const detected = detectAICategory(text);
    const category = AppState._ncCategory || (detected.category !== 'General Maintenance' ? detected.category : '') || 'General';

    AppState.complaintData = {
      title: titleInput.value.trim(),
      details: detailsInput.value.trim(),
      location: locationInput.value.trim(),
      category: category,
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
