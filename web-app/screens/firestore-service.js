// firestore-service.js — Mirrors FirestoreService.kt
import { db, auth } from '../firebase-config.js';
import {
  collection, doc, getDoc, getDocs, setDoc, updateDoc, deleteDoc,
  addDoc, query, where, orderBy, serverTimestamp, runTransaction,
  increment
} from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js';

export function getCollegeId(college) {
  return (college || '').trim().toLowerCase()
    .replace(/sch+o+l/g, 'school')
    .replace(/school of engineering/g, '')
    .replace(/college/g, '')
    .replace(/university/g, '')
    .replace(/institute/g, '')
    .replace(/technology/g, '')
    .replace(/ of /g, '')
    .replace(/ssaveetha/g, 'saveetha')
    .replace(/\s/g, '')
    .replace(/_/g, '')
    .replace(/[^a-z0-9]/g, '')
    || 'general';
}

async function mapDocToUserData(docSnap) {
  try {
    const data = docSnap.data();
    if (!data) return null;
    if (!data.collegeName) {
      data.collegeName = data.college || '';
    }
    data.isApproved = data.approved ?? data.isApproved ?? false;
    data.uid = docSnap.id;
    return data;
  } catch { return null; }
}

export async function getUserData(uid) {
  const colls = ['approvedStaff', 'users', 'pendingStaffRequests'];
  for (const col of colls) {
    try {
      const snap = await getDoc(doc(db, col, uid));
      if (snap.exists()) return mapDocToUserData(snap);
    } catch { /* continue */ }
  }
  return null;
}

async function findBestStaffForCategory(college, category) {
  try {
    const collegeId = getCollegeId(college);
    const snap = await getDocs(collection(db, 'approvedStaff'));
    const staff = snap.docs.map(d => ({ uid: d.id, ...d.data() }));
    const matches = staff.filter(s =>
      getCollegeId(s.collegeName || '') === collegeId &&
      (s.assignedCategories || []).some(c => c.trim().toLowerCase() === category.trim().toLowerCase())
    );
    if (!matches.length) return null;
    return matches.reduce((a, b) => (a.activeComplaintCount || 0) <= (b.activeComplaintCount || 0) ? a : b);
  } catch { return null; }
}

export async function submitComplaint(complaint, photoFile, audioBlob) {
  const user = auth.currentUser;
  if (!user) throw new Error('Not authenticated');
  const uid = user.uid;
  const email = user.email || '';

  const userData = await getUserData(uid);
  if (!userData) throw new Error('User profile not found.');

  const rawCollege = userData.collegeName.trim();
  const collegeId = getCollegeId(rawCollege);

  let photoUrl = '';
  if (photoFile) {
    const { resizeImageToBase64 } = await import('./utils.js');
    photoUrl = await resizeImageToBase64(photoFile);
  }

  let voiceUrl = '';
  if (audioBlob) {
    const { fileToBase64 } = await import('./utils.js');
    const arr = await audioBlob.arrayBuffer();
    const b64 = btoa(String.fromCharCode(...new Uint8Array(arr)));
    voiceUrl = 'base64:' + b64;
  }

  const assignedStaff = await findBestStaffForCategory(rawCollege, complaint.category);
  const finalStatus = assignedStaff ? 'Assigned' : 'Waiting for Staff Assignment';
  const ticketId = 'CMP' + String(Date.now()).slice(-6) + String(Math.floor(Math.random() * 90) + 10);

  const isAnon = complaint.studentUid === 'anonymous';
  const finalComplaint = {
    ticketId,
    title: complaint.title || '',
    details: complaint.details || '',
    location: complaint.location || '',
    category: complaint.category || '',
    priority: complaint.priority || 'Medium',
    status: finalStatus,
    studentEmail: isAnon ? 'anonymous@campus.edu' : email,
    studentUid: isAnon ? 'anonymous' : uid,
    collegeName: rawCollege,
    collegeId,
    photoUrl,
    voiceUrl,
    videoUrl: '',
    assignedStaffId: assignedStaff?.uid || '',
    assignedStaffEmail: assignedStaff?.email || '',
    proofPhotoUrl: '',
    proofVideoUrl: '',
    createdAt: serverTimestamp(),
  };

  await setDoc(doc(db, 'all_complaints', ticketId), finalComplaint);

  if (assignedStaff) {
    try {
      await updateDoc(doc(db, 'approvedStaff', assignedStaff.uid), {
        activeComplaintCount: increment(1)
      });
    } catch { /* non-critical */ }
  }

  return { ...finalComplaint };
}

export async function updateComplaintStatus(ticketId, newStatus) {
  const docRef = doc(db, 'all_complaints', ticketId);
  await runTransaction(db, async (tx) => {
    const snap = await tx.get(docRef);
    const staffId = snap.data()?.assignedStaffId || '';
    tx.update(docRef, { status: newStatus });
    if (newStatus === 'Resolved' && snap.data()?.status !== 'Resolved' && staffId) {
      tx.update(doc(db, 'approvedStaff', staffId), { activeComplaintCount: increment(-1) });
    }
  });
}

export async function uploadStaffProof(ticketId, photoFile) {
  let proofPhotoUrl = '';
  if (photoFile) {
    const { resizeImageToBase64 } = await import('./utils.js');
    proofPhotoUrl = await resizeImageToBase64(photoFile);
  }
  const docRef = doc(db, 'all_complaints', ticketId);
  await updateDoc(docRef, { status: 'Resolved', proofPhotoUrl });

  const snap = await getDoc(docRef);
  const staffId = snap.data()?.assignedStaffId || '';
  if (staffId) {
    await updateDoc(doc(db, 'approvedStaff', staffId), { activeComplaintCount: increment(-1) });
  }
}

export async function createPendingStaffRequest(userData) {
  const data = {
    uid: userData.uid, name: userData.name, email: userData.email,
    phone: userData.phone || '', collegeName: userData.collegeName.trim(),
    role: 'staff', approved: false, assignedCategories: [], activeComplaintCount: 0
  };
  await setDoc(doc(db, 'pendingStaffRequests', userData.uid), data);
  await setDoc(doc(db, 'users', userData.uid), data);
}

export async function approveStaffRequest(user, categories) {
  const data = {
    uid: user.uid, name: user.name, email: user.email,
    phone: user.phone || '', collegeName: user.collegeName.trim(),
    role: 'staff', approved: true, assignedCategories: categories, activeComplaintCount: 0
  };
  await setDoc(doc(db, 'approvedStaff', user.uid), data);
  await deleteDoc(doc(db, 'pendingStaffRequests', user.uid));
  await setDoc(doc(db, 'users', user.uid), data);
}

export async function rejectStaffRequest(uid) {
  await deleteDoc(doc(db, 'pendingStaffRequests', uid));
  await deleteDoc(doc(db, 'users', uid));
}

export async function checkStaffStatusByEmail(email) {
  const pendingSnap = await getDocs(query(collection(db, 'pendingStaffRequests'), where('email', '==', email)));
  if (!pendingSnap.empty) return 'PENDING';
  const approvedSnap = await getDocs(query(collection(db, 'approvedStaff'), where('email', '==', email)));
  if (!approvedSnap.empty) return 'APPROVED';
  return null;
}

export async function submitFeedback(rating, comments, collegeName) {
  const user = auth.currentUser;
  await addDoc(collection(db, 'feedbacks'), {
    rating, comments, collegeName,
    userEmail: user?.email || 'Anonymous',
    timestamp: serverTimestamp()
  });
}

export async function getPendingStaffRequests(college) {
  const collegeId = getCollegeId(college);
  const snap = await getDocs(collection(db, 'pendingStaffRequests'));
  return snap.docs
    .map(d => ({ uid: d.id, ...d.data() }))
    .filter(r => getCollegeId(r.collegeName || '') === collegeId);
}

export async function getStudentsByCollege(college) {
  const collegeId = getCollegeId(college);
  const snap = await getDocs(query(collection(db, 'users'), where('role', '==', 'student')));
  return snap.docs
    .map(d => ({ uid: d.id, ...d.data() }))
    .filter(s => getCollegeId(s.collegeName || '') === collegeId);
}

export async function removeUser(uid) {
  await deleteDoc(doc(db, 'users', uid));
}

export async function updateUserProfile(uid, updates) {
  // Update in all relevant collections
  for (const col of ['users', 'approvedStaff', 'pendingStaffRequests']) {
    try {
      const snap = await getDoc(doc(db, col, uid));
      if (snap.exists()) await updateDoc(doc(db, col, uid), updates);
    } catch { /* continue */ }
  }
}

export async function addResolutionNote(ticketId, note) {
  await updateDoc(doc(db, 'all_complaints', ticketId), { resolutionNote: note });
}

export async function escalateComplaint(ticketId, reason) {
  await updateDoc(doc(db, 'all_complaints', ticketId), {
    status: 'Escalated',
    escalationReason: reason
  });
}
