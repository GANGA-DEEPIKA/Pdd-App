// Shared utility functions
import { detectAICategory } from './ai-voice-assistant.js';

export function showToast(message, type = '') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(16px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

export function formatDate(ts) {
  if (!ts) return 'N/A';
  const d = ts.toDate ? ts.toDate() : new Date(ts);
  return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

export function getStatusBadgeClass(status = '') {
  if (status === 'Resolved') return 'badge-resolved';
  if (status === 'In Progress') return 'badge-progress';
  if (status === 'Assigned') return 'badge-assigned';
  if (status.startsWith('Waiting')) return 'badge-waiting';
  return 'badge-pending';
}

export function getComplaintBorderClass(status = '') {
  if (status === 'Resolved') return 'status-resolved';
  if (status === 'In Progress') return 'status-progress';
  if (status === 'Assigned') return 'status-assigned';
  return 'status-pending';
}

export function suggestCategory(text) {
  if (!text || !text.trim()) return '';
  return detectAICategory(text).category || '';
}

export function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      // Strip data URL prefix, keep only base64 data
      const base64 = reader.result.split(',')[1];
      resolve(base64);
    };
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

export function resizeImageToBase64(file, maxPx = 800, quality = 0.7) {
  return new Promise((resolve) => {
    const img = new Image();
    const url = URL.createObjectURL(file);
    img.onload = () => {
      const scale = Math.min(1, maxPx / Math.max(img.width, img.height));
      const canvas = document.createElement('canvas');
      canvas.width = Math.round(img.width * scale);
      canvas.height = Math.round(img.height * scale);
      canvas.getContext('2d').drawImage(img, 0, 0, canvas.width, canvas.height);
      URL.revokeObjectURL(url);
      const base64 = canvas.toDataURL('image/jpeg', quality).split(',')[1];
      resolve('base64:' + base64);
    };
    img.src = url;
  });
}

export function openAttachment(url, type) {
  if (!url) return;
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('blob:')) {
    window.open(url, '_blank');
    return;
  }
  let mime = 'image/jpeg';
  if (type === 'video') mime = 'video/mp4';
  if (type === 'audio') mime = 'audio/webm';
  let b64 = url;
  if (url.startsWith('base64:')) {
    b64 = url.substring(7);
  } else if (url.startsWith('data:')) {
    b64 = url.split(',')[1];
    mime = url.substring(5, url.indexOf(';'));
  }
  try {
    const byteCharacters = atob(b64);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i);
    }
    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], {type: mime});
    const blobUrl = URL.createObjectURL(blob);
    window.open(blobUrl, '_blank');
  } catch(e) {
    console.error("Error opening attachment", e);
    alert("Could not open attachment.");
  }
}
