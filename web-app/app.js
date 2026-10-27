// ============================================================
// app.js — Main SPA Router + Auth State + Real-time Listener
// Mirrors MainActivity.kt's CampusFixApp composable
// ============================================================

import { auth, db } from './firebase-config.js';
import { onAuthStateChanged } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js';
import {
  collection, query, onSnapshot, orderBy, getDocs, where
} from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js';

import { renderCollegeSelection } from './screens/college-selection.js';
import { renderRoleSelection } from './screens/role-selection.js';
import { renderLogin } from './screens/login.js';
import { renderRegister } from './screens/register.js';
import { renderForgotPassword } from './screens/forgot-password.js';
import { renderStudentDashboard } from './screens/student-dashboard.js';
import { renderStaffDashboard } from './screens/staff-dashboard.js';
import { renderAdminDashboard } from './screens/admin-dashboard.js';
import { renderNewComplaint } from './screens/new-complaint.js';
import { renderReviewComplaint } from './screens/review-complaint.js';
import { renderComplaintStatus } from './screens/complaint-status.js';
import { renderComplaintHistory } from './screens/complaint-history.js?v=20261027_02';
import { renderAssignedComplaints } from './screens/assigned-complaints.js';
import { renderUpdateStatus } from './screens/update-status.js';
import { renderUploadProof } from './screens/upload-proof.js';
import { renderStaffApproval } from './screens/staff-approval.js';
import { renderManageStudents } from './screens/manage-students.js';
import { renderPerformanceSummary } from './screens/performance-summary.js';
import { renderFeedback } from './screens/feedback.js';
import { renderEmergencyComplaint } from './screens/emergency-complaint.js';
import { renderAnonymousComplaint } from './screens/anonymous-complaint.js';
import { renderNotification } from './screens/notification.js';
import { renderStudentProfile } from './screens/student-profile.js';
import { renderEditProfile } from './screens/edit-profile.js';
import { renderResolutionNotes } from './screens/resolution-notes.js';
import { renderEscalateComplaint } from './screens/escalate-complaint.js';
import { renderGlobalSettings } from './screens/global-settings.js';
import { getUserData, getCollegeId } from './screens/firestore-service.js';
import { showToast } from './screens/utils.js';

// ---- Global App State ----
export const AppState = {
  screen: 'college',
  navigationStack: [],
  collegeName: '',
  complaintData: {},
  complaintList: [],
  currentUserData: null,
  filterType: 'all',
  unsubscribeComplaints: null,
  complaintsLoaded: false,
};

// ---- Navigation ----
export function navigateTo(newScreen) {
  if (AppState.screen !== newScreen) {
    // Guard: Only verified student accounts can access studentDashboard
    if (newScreen === 'studentDashboard') {
      const u = AppState.currentUserData;
      const email = (auth.currentUser?.email || u?.email || '').toLowerCase().trim();
      if (!u || u.role !== 'student' || email.startsWith('admin') || email.startsWith('staff')) {
        const actualRole = (u?.role === 'admin' || email.startsWith('admin')) ? 'Admin'
          : (u?.role === 'staff' || email.startsWith('staff')) ? 'Staff' : 'User';
        auth.signOut();
        AppState.currentUserData = null;
        showToast(`Role Mismatch: This account is registered as ${actualRole}.`, 'error');
        AppState.screen = 'role';
        renderCurrentScreen();
        return;
      }
    }

    AppState.navigationStack.push(AppState.screen);
    AppState.screen = newScreen;
    renderCurrentScreen();
  }
}

export function goBack() {
  if (AppState.navigationStack.length > 0) {
    AppState.screen = AppState.navigationStack.pop();
  } else {
    AppState.screen = 'college';
  }
  renderCurrentScreen();
}

// ---- Real-time complaints listener ----
export function subscribeToComplaints(userData) {
  // Load from local cache immediately if available
  if (userData?.uid) {
    try {
      const cached = localStorage.getItem(`campusfix_complaints_${userData.uid}`);
      if (cached) {
        AppState.complaintList = JSON.parse(cached);
        AppState.complaintsLoaded = true;
      }
    } catch {}
  }

  // Unsubscribe previous listener
  if (AppState.unsubscribeComplaints) {
    AppState.unsubscribeComplaints();
    AppState.unsubscribeComplaints = null;
  }
  if (!userData || !userData.collegeName) {
    AppState.complaintList = [];
    AppState.complaintsLoaded = true;
    return Promise.resolve([]);
  }

  const collegeId = getCollegeId(userData.collegeName);

  return new Promise((resolve) => {
    let resolved = false;
    const finish = (result) => {
      AppState.complaintsLoaded = true;
      if (!resolved) {
        resolved = true;
        resolve(result);
      }
    };

    // Failsafe timeout for offline/error situations (8 seconds)
    const safetyTimer = setTimeout(() => {
      finish(AppState.complaintList || []);
    }, 8000);

    const handleDocs = (docs) => {
      const all = docs.map(d => ({ id: d.id, ...d.data() }));
      const filtered = all.filter(c => {
        const instMatch =
          getCollegeId(c.collegeName || '') === collegeId ||
          c.collegeId === collegeId;
        if (!instMatch) return false;
        if (userData.role === 'student') return c.studentUid === userData.uid;
        if (userData.role === 'staff') {
          return (userData.assignedCategories || []).some(
            cat => cat.trim().toLowerCase() === (c.category || '').trim().toLowerCase()
          );
        }
        if (userData.role === 'admin') return true;
        return false;
      });
      // Sort by ticketId descending
      filtered.sort((a, b) => (b.ticketId || '').localeCompare(a.ticketId || ''));
      AppState.complaintList = filtered;
      AppState.complaintsLoaded = true;

      // Cache locally for instant access
      try {
        if (userData.uid) {
          localStorage.setItem(`campusfix_complaints_${userData.uid}`, JSON.stringify(filtered));
        }
      } catch {}

      clearTimeout(safetyTimer);
      finish(filtered);

      // Re-render dashboards if currently on one
      const dashScreens = ['studentDashboard', 'staffDashboard', 'adminDashboard',
        'complaintHistory', 'assignedComplaints', 'performanceSummary'];
      if (dashScreens.includes(AppState.screen)) renderCurrentScreen();
    };

    // Targeted query for student role for fast network transfer
    const studentQuery = (userData.role === 'student' && userData.uid)
      ? query(collection(db, 'all_complaints'), where('studentUid', '==', userData.uid))
      : null;

    const generalQuery = query(collection(db, 'all_complaints'));
    const initialQuery = studentQuery || generalQuery;

    // Fast initial fetch using getDocs
    getDocs(initialQuery).then((snap) => {
      handleDocs(snap.docs);
    }).catch((err) => {
      console.warn('Targeted getDocs error, trying general query:', err);
      getDocs(generalQuery).then((snap) => handleDocs(snap.docs)).catch(() => {});
    });

    // Persistent real-time listener
    AppState.unsubscribeComplaints = onSnapshot(initialQuery, (snapshot) => {
      handleDocs(snapshot.docs);
    }, (err) => {
      console.warn('Listener on initial query failed, using general query:', err);
      AppState.unsubscribeComplaints = onSnapshot(generalQuery, (snapshot) => {
        handleDocs(snapshot.docs);
      }, (e) => {
        clearTimeout(safetyTimer);
        console.error('Complaint listener error:', e);
        finish(AppState.complaintList || []);
      });
    });
  });
}

// ---- Auth State Listener ----
onAuthStateChanged(auth, async (user) => {
  if (user) {
    try {
      if (!AppState.currentUserData || AppState.currentUserData.uid !== user.uid) {
        const userData = await getUserData(user.uid);
        AppState.currentUserData = userData;
        if (userData?.collegeName && !AppState.collegeName) {
          AppState.collegeName = userData.collegeName;
        }
        subscribeToComplaints(userData);
      }
    } catch (e) {
      AppState.currentUserData = null;
    }
  } else {
    AppState.currentUserData = null;
    if (AppState.unsubscribeComplaints) {
      AppState.unsubscribeComplaints();
      AppState.unsubscribeComplaints = null;
    }
    AppState.complaintList = [];
  }
});

// ---- Main Render ----
export function renderCurrentScreen() {
  const app = document.getElementById('app');
  app.innerHTML = '';

  const s = AppState.screen;

  if (s === 'college') {
    renderCollegeSelection(app);
  } else if (s === 'role') {
    renderRoleSelection(app);
  } else if (s === 'login') {
    renderLogin(app, 'Student Sign In', 'Student College Email', () => navigateTo('studentDashboard'), () => navigateTo('forgot'), () => navigateTo('role'), () => navigateTo('studentRegister'));
  } else if (s === 'staffLogin') {
    renderLogin(app, 'Staff Sign In', 'Staff College Email', () => navigateTo('staffDashboard'), () => navigateTo('forgot'), () => navigateTo('role'), () => navigateTo('staffRegister'));
  } else if (s === 'adminLogin') {
    renderLogin(app, 'Admin Sign In', 'Admin College Email', () => navigateTo('adminDashboard'), () => navigateTo('forgot'), () => navigateTo('role'), () => navigateTo('adminRegister'));
  } else if (s === 'studentRegister') {
    renderRegister(app, 'Student Register', 'Student College Email', goBack);
  } else if (s === 'staffRegister') {
    renderRegister(app, 'Staff Register', 'Staff College Email', goBack);
  } else if (s === 'adminRegister') {
    renderRegister(app, 'Admin Register', 'Admin College Email', goBack);
  } else if (s === 'forgot') {
    renderForgotPassword(app, goBack);
  } else if (s === 'studentDashboard') {
    renderStudentDashboard(app);
  } else if (s === 'staffDashboard') {
    renderStaffDashboard(app);
  } else if (s === 'adminDashboard') {
    renderAdminDashboard(app);
  } else if (s === 'newComplaint') {
    renderNewComplaint(app);
  } else if (s === 'reviewComplaint') {
    renderReviewComplaint(app);
  } else if (s === 'complaintStatus') {
    renderComplaintStatus(app);
  } else if (s === 'complaintHistory') {
    renderComplaintHistory(app);
  } else if (s === 'assignedComplaints') {
    renderAssignedComplaints(app);
  } else if (s === 'updateStatus') {
    renderUpdateStatus(app);
  } else if (s === 'uploadProof') {
    renderUploadProof(app);
  } else if (s === 'staffApproval') {
    renderStaffApproval(app);
  } else if (s === 'manageStudents') {
    renderManageStudents(app);
  } else if (s === 'performanceSummary') {
    renderPerformanceSummary(app);
  } else if (s === 'feedback') {
    renderFeedback(app);
  } else if (s === 'emergencyComplaint') {
    renderEmergencyComplaint(app);
  } else if (s === 'anonymousComplaint') {
    renderAnonymousComplaint(app);
  } else if (s === 'notification') {
    renderNotification(app);
  } else if (s === 'studentProfile') {
    renderStudentProfile(app);
  } else if (s === 'editProfile') {
    renderEditProfile(app);
  } else if (s === 'resolutionNotes') {
    renderResolutionNotes(app);
  } else if (s === 'escalateComplaint') {
    renderEscalateComplaint(app);
  } else if (s === 'globalSettings') {
    renderGlobalSettings(app);
  } else {
    renderCollegeSelection(app);
  }
}

// ---- Boot ----
// NOTE: ES modules always execute after HTML is parsed, so DOMContentLoaded
// may have already fired by the time this code runs. We use readyState check
// to boot immediately if DOM is ready, or wait otherwise.
function boot() {
  const splash = document.getElementById('splash');
  if (!splash) return; // already unmounted

  let splashHidden = false;
  const hideSplash = () => {
    if (!splashHidden) {
      splashHidden = true;
      // Add CSS hidden class (fade out)
      splash.style.transition = 'opacity 0.3s ease';
      splash.style.opacity = '0';
      setTimeout(() => {
        splash.style.display = 'none';
        renderCurrentScreen();
      }, 320);
    }
  };

  // Firebase auth resolves quickly — hide splash once we know auth state
  onAuthStateChanged(auth, () => setTimeout(hideSplash, 200));

  // Hard fallback: never stay stuck on splash longer than 2.5 seconds
  setTimeout(hideSplash, 2500);

  // Browser back button support
  window.addEventListener('popstate', () => goBack());
}

// Run boot immediately if DOM is already ready (likely for ES modules),
// otherwise wait for DOMContentLoaded
if (document.readyState === 'loading') {
  window.addEventListener('DOMContentLoaded', boot);
} else {
  boot();
}
