// ai-duplicate-engine.js — AI Semantic Matching Engine for Duplicate Complaints
// Mirrors AIDuplicateEngine.kt in Android to ensure consistent cross-platform detection.

const STOP_WORDS = new Set([
  'a', 'about', 'above', 'after', 'again', 'all', 'am', 'an', 'and', 'any', 'are', 'as', 'at',
  'be', 'because', 'been', 'before', 'being', 'below', 'between', 'both', 'but', 'by', 'can',
  'could', 'did', 'do', 'does', 'doing', 'down', 'during', 'each', 'few', 'for', 'from',
  'further', 'had', 'has', 'have', 'having', 'he', 'her', 'here', 'hers', 'herself', 'him',
  'himself', 'his', 'how', 'i', 'if', 'in', 'into', 'is', 'it', 'its', 'itself', 'me', 'more',
  'most', 'my', 'myself', 'no', 'nor', 'not', 'of', 'off', 'on', 'once', 'only', 'or', 'other',
  'our', 'ours', 'ourselves', 'out', 'over', 'own', 'same', 'she', 'should', 'so', 'some',
  'such', 'than', 'that', 'the', 'their', 'theirs', 'them', 'themselves', 'then', 'there',
  'these', 'they', 'this', 'those', 'through', 'to', 'too', 'under', 'until', 'up', 'very',
  'was', 'we', 'were', 'what', 'when', 'where', 'which', 'while', 'who', 'whom', 'why',
  'with', 'you', 'your', 'yours', 'yourself', 'yourselves', 'please', 'kindly', 'campus',
  'fix', 'issue', 'problem', 'help', 'urgently', 'urgent', 'sir', 'madam', 'complaint'
]);

const CANONICAL_MAP = {
  fan: 'fan', fans: 'fan', ceilingfan: 'fan',
  ac: 'aircon', aircon: 'aircon', airconditioner: 'aircon', cooling: 'aircon', chiller: 'aircon',
  light: 'light', lights: 'light', tubelight: 'light', bulb: 'light', lamp: 'light', led: 'light',
  switch: 'switch', socket: 'switch', plug: 'switch', wiring: 'switch', wire: 'switch', spark: 'switch',
  leak: 'leak', leaking: 'leak', leakage: 'leak', dripping: 'leak', overflow: 'leak', overflowing: 'leak',
  tap: 'tap', faucet: 'tap', pipe: 'tap', pipeline: 'tap', flush: 'tap', drain: 'tap', sink: 'tap',
  water: 'water', drinkingwater: 'water', purifier: 'water',
  broken: 'broken', break: 'broken', broke: 'broken', damage: 'broken', damaged: 'broken', repair: 'broken',
  notworking: 'broken', faulty: 'broken', spoiled: 'broken', defective: 'broken', cracked: 'broken',
  chair: 'furniture', chairs: 'furniture', bench: 'furniture', benches: 'furniture', desk: 'furniture', table: 'furniture',
  projector: 'projector', screen: 'projector', display: 'projector', monitor: 'projector', hdmi: 'projector',
  wifi: 'network', internet: 'network', lan: 'network', network: 'network', router: 'network', connection: 'network',
  door: 'door', handle: 'door', lock: 'door', latch: 'door', key: 'door',
  window: 'window', glass: 'window',
  dirty: 'cleanliness', garbage: 'cleanliness', trash: 'cleanliness', waste: 'cleanliness', dustbin: 'cleanliness', smell: 'cleanliness',
  lift: 'lift', elevator: 'lift'
};

const CATEGORY_GROUPS = {
  electrical: ['electrical', 'power', 'electronics', 'ac / air conditioning', 'air conditioning'],
  plumbing: ['plumbing', 'water supply', 'washroom', 'restroom', 'drainage'],
  civil: ['civil', 'infrastructure', 'civil / infrastructure', 'carpentry', 'masonry', 'painting'],
  it: ['it', 'network', 'it / network', 'wifi', 'internet', 'computer lab', 'audio / visual'],
  furniture: ['furniture', 'classroom maintenance'],
  cleanliness: ['cleanliness', 'housekeeping', 'sanitation', 'waste management'],
  hostel: ['hostel', 'hostel maintenance', 'mess', 'canteen'],
  security: ['security', 'safety', 'safety & security']
};

export function normalizeText(text) {
  if (!text) return [];
  const words = text
    .toLowerCase()
    .replace(/[^a-z0-9\s]/g, ' ')
    .split(/\s+/)
    .filter(w => w.length > 1 && !STOP_WORDS.has(w));
  return words.map(w => CANONICAL_MAP[w] || w);
}

export function calculateCategorySimilarity(cat1, cat2) {
  if (!cat1 || !cat2) return 0.0;
  const c1 = cat1.trim().toLowerCase();
  const c2 = cat2.trim().toLowerCase();
  if (c1 === c2) return 1.0;

  for (const group of Object.values(CATEGORY_GROUPS)) {
    const has1 = group.some(g => c1.includes(g) || g.includes(c1));
    const has2 = group.some(g => c2.includes(g) || g.includes(c2));
    if (has1 && has2) return 0.85;
  }

  if (c1.includes('general') || c2.includes('general')) return 0.50;
  return 0.0;
}

export function normalizeLocation(loc) {
  if (!loc) return { tokens: [], numbers: [], rawNorm: '' };
  let s = loc.toLowerCase().replace(/[^a-z0-9\s]/g, ' ');
  s = s.replace(/\bblk\b/g, 'block')
       .replace(/\brm\b/g, 'room')
       .replace(/\bflr\b/g, 'floor')
       .replace(/\bbldg\b/g, 'building')
       .replace(/\bdept\b/g, 'department')
       .replace(/\blab\b/g, 'laboratory')
       .replace(/\baudi\b/g, 'auditorium');
  const tokens = s.split(/\s+/).filter(w => w.length > 0 && !STOP_WORDS.has(w));
  const numbers = s.match(/\b\d+\b/g) || [];
  return { tokens, numbers, rawNorm: tokens.join(' ') };
}

export function calculateLocationSimilarity(loc1, loc2) {
  const n1 = normalizeLocation(loc1);
  const n2 = normalizeLocation(loc2);

  if (!n1.tokens.length || !n2.tokens.length) return 0.0;
  if (n1.rawNorm === n2.rawNorm) return 1.0;

  // Conflict penalty if different room/number detected
  if (n1.numbers.length > 0 && n2.numbers.length > 0) {
    const set1 = new Set(n1.numbers);
    const hasCommonNumber = n2.numbers.some(num => set1.has(num));
    if (!hasCommonNumber) {
      return 0.15;
    }
  }

  // Substring containment
  if (n1.rawNorm.includes(n2.rawNorm) || n2.rawNorm.includes(n1.rawNorm)) {
    return 0.90;
  }

  // Jaccard similarity of location tokens
  const setA = new Set(n1.tokens);
  const setB = new Set(n2.tokens);
  let intersection = 0;
  for (const t of setA) {
    if (setB.has(t)) intersection++;
  }
  const union = new Set([...n1.tokens, ...n2.tokens]).size;
  const jaccard = union > 0 ? intersection / union : 0.0;

  return Math.min(1.0, jaccard * 1.25);
}

export function calculateDescriptionSimilarity(title1, desc1, title2, desc2) {
  const text1 = `${title1 || ''} ${desc1 || ''}`;
  const text2 = `${title2 || ''} ${desc2 || ''}`;

  const tokens1 = normalizeText(text1);
  const tokens2 = normalizeText(text2);

  if (!tokens1.length || !tokens2.length) return 0.0;

  // Term frequency vectors
  const tf1 = {};
  tokens1.forEach(t => tf1[t] = (tf1[t] || 0) + 1);
  const tf2 = {};
  tokens2.forEach(t => tf2[t] = (tf2[t] || 0) + 1);

  let dot = 0;
  let mag1 = 0;
  let mag2 = 0;

  const allTokens = new Set([...Object.keys(tf1), ...Object.keys(tf2)]);
  for (const t of allTokens) {
    const v1 = tf1[t] || 0;
    const v2 = tf2[t] || 0;
    dot += v1 * v2;
    mag1 += v1 * v1;
    mag2 += v2 * v2;
  }

  const cosine = (mag1 > 0 && mag2 > 0) ? dot / (Math.sqrt(mag1) * Math.sqrt(mag2)) : 0;

  // Bigram overlap
  const getBigrams = (tokens) => {
    const bigrams = new Set();
    for (let i = 0; i < tokens.length - 1; i++) {
      bigrams.add(`${tokens[i]}_${tokens[i + 1]}`);
    }
    return bigrams;
  };

  const bg1 = getBigrams(tokens1);
  const bg2 = getBigrams(tokens2);

  let bgOverlap = 0;
  for (const b of bg1) {
    if (bg2.has(b)) bgOverlap++;
  }
  const bgUnion = new Set([...bg1, ...bg2]).size;
  const bigramSim = bgUnion > 0 ? bgOverlap / bgUnion : 0;

  return (0.70 * cosine) + (0.30 * bigramSim);
}

export function evaluateComplaintAgainstExisting(newComplaint, existingComplaint) {
  const sCat = calculateCategorySimilarity(newComplaint.category, existingComplaint.category);
  const sLoc = calculateLocationSimilarity(newComplaint.location, existingComplaint.location);
  const sDesc = calculateDescriptionSimilarity(
    newComplaint.title, newComplaint.details,
    existingComplaint.title, existingComplaint.details
  );

  let score = (0.45 * sDesc) + (0.35 * sLoc) + (0.20 * sCat);

  // Boost for high locality and category with matching topic
  if (sLoc >= 0.85 && sCat >= 0.85 && sDesc >= 0.35) {
    score = Math.min(0.98, score + 0.15);
  }

  // Location conflict penalty: if room numbers differ, cannot be duplicate
  const loc1 = normalizeLocation(newComplaint.location);
  const loc2 = normalizeLocation(existingComplaint.location);
  if (loc1.numbers.length > 0 && loc2.numbers.length > 0) {
    const set1 = new Set(loc1.numbers);
    if (!loc2.numbers.some(n => set1.has(n))) {
      score = Math.min(score, 0.40);
    }
  }

  const confidence = Math.round(score * 100);
  const isDuplicate = confidence >= 65;

  let reason = '';
  if (isDuplicate) {
    const reasons = [];
    if (sCat >= 0.8) reasons.push(`matching category ("${existingComplaint.category}")`);
    if (sLoc >= 0.8) reasons.push(`same location ("${existingComplaint.location}")`);
    if (sDesc >= 0.4) reasons.push('similar description');
    reason = `AI detected ${confidence}% match: ${reasons.join(', ')}.`;
  }

  return {
    isDuplicate,
    confidence,
    score,
    sCat,
    sLoc,
    sDesc,
    reason,
    matchedComplaint: existingComplaint
  };
}

export function findBestDuplicateMatch(newComplaint, existingComplaintsList) {
  if (!existingComplaintsList || !existingComplaintsList.length) {
    return { isDuplicate: false, confidence: 0, matchedComplaint: null, reason: '' };
  }

  let bestResult = null;

  for (const existing of existingComplaintsList) {
    // Skip if same ticketId (should not happen on new creation)
    if (existing.ticketId && existing.ticketId === newComplaint.ticketId) continue;

    // Check status: ignore rejected complaints
    const st = (existing.status || '').toLowerCase();
    if (st.includes('rejected')) continue;

    const res = evaluateComplaintAgainstExisting(newComplaint, existing);
    if (!bestResult || res.confidence > bestResult.confidence) {
      bestResult = res;
    }
  }

  if (bestResult && bestResult.isDuplicate) {
    return bestResult;
  }

  return {
    isDuplicate: false,
    confidence: bestResult ? bestResult.confidence : 0,
    matchedComplaint: null,
    reason: ''
  };
}

/**
 * AI-Powered Duplicate Grouping Engine for Staff Dashboard
 * Identifies clusters of similar complaints based on title, description, category, and location,
 * and groups duplicates under a single main complaint.
 */
export function groupComplaintsWithAI(complaintList) {
  if (!complaintList || !complaintList.length) return [];

  const activeComplaints = complaintList.filter(c => {
    const st = (c.status || '').toLowerCase();
    return !st.includes('rejected');
  });

  const complaintMap = new Map();
  activeComplaints.forEach(c => complaintMap.set(c.ticketId, c));

  const processed = new Set();
  const groups = [];

  // 1. Process pre-confirmed or persisted groups from Firestore
  for (const c of activeComplaints) {
    if (processed.has(c.ticketId)) continue;

    const isMaster = Boolean(c.isGroupMaster || (Array.isArray(c.linkedTicketIds) && c.linkedTicketIds.length > 0));
    if (isMaster) {
      processed.add(c.ticketId);
      const linked = [];
      const linkedIds = Array.isArray(c.linkedTicketIds) ? c.linkedTicketIds : [];

      linkedIds.forEach(id => {
        const linkedC = complaintMap.get(id);
        if (linkedC) {
          linked.push(linkedC);
          processed.add(id);
        }
      });

      activeComplaints.forEach(other => {
        if (!processed.has(other.ticketId) && other.parentTicketId === c.ticketId) {
          linked.push(other);
          processed.add(other.ticketId);
        }
      });

      if (linked.length > 0) {
        const studentIdentifiers = new Set();
        if (c.studentEmail) studentIdentifiers.add(c.studentEmail.toLowerCase());
        else if (c.studentUid) studentIdentifiers.add(c.studentUid);

        linked.forEach(l => {
          if (l.studentEmail) studentIdentifiers.add(l.studentEmail.toLowerCase());
          else if (l.studentUid) studentIdentifiers.add(l.studentUid);
        });

        const totalStudentsAffected = Math.max(studentIdentifiers.size, 1 + linked.length);

        groups.push({
          groupId: c.ticketId,
          mainComplaint: c,
          linkedComplaints: linked,
          confidence: c.duplicateConfidence || 95,
          matchReason: c.duplicateMatchReason || 'Confirmed duplicate group by staff',
          isConfirmed: true,
          totalStudentsAffected
        });
      }
    }
  }

  // 2. For remaining unlinked complaints, perform AI pairwise similarity clustering
  const remaining = activeComplaints.filter(c => !processed.has(c.ticketId) && !c.parentTicketId);

  const adj = new Map();
  remaining.forEach(c => adj.set(c.ticketId, []));

  for (let i = 0; i < remaining.length; i++) {
    for (let j = i + 1; j < remaining.length; j++) {
      const c1 = remaining[i];
      const c2 = remaining[j];
      const evalResult = evaluateComplaintAgainstExisting(c1, c2);
      if (evalResult.isDuplicate) {
        adj.get(c1.ticketId).push({ target: c2, result: evalResult });
        adj.get(c2.ticketId).push({ target: c1, result: evalResult });
      }
    }
  }

  const visitedInClustering = new Set();
  for (const c of remaining) {
    if (visitedInClustering.has(c.ticketId)) continue;

    const cluster = [];
    const queue = [c];
    visitedInClustering.add(c.ticketId);

    let maxConfidence = 0;
    let bestReason = '';

    while (queue.length > 0) {
      const curr = queue.shift();
      cluster.push(curr);

      const neighbors = adj.get(curr.ticketId) || [];
      for (const edge of neighbors) {
        if (edge.result.confidence > maxConfidence) {
          maxConfidence = edge.result.confidence;
          bestReason = edge.result.reason;
        }
        if (!visitedInClustering.has(edge.target.ticketId)) {
          visitedInClustering.add(edge.target.ticketId);
          queue.push(edge.target);
        }
      }
    }

    if (cluster.length >= 2) {
      cluster.sort((a, b) => {
        const rank = (s) => (s === 'In Progress' ? 3 : s === 'Assigned' ? 2 : 1);
        const rankDiff = rank(b.status) - rank(a.status);
        if (rankDiff !== 0) return rankDiff;
        return (a.ticketId || '').localeCompare(b.ticketId || '');
      });

      const mainComplaint = cluster[0];
      const linkedComplaints = cluster.slice(1);

      const studentIdentifiers = new Set();
      cluster.forEach(comp => {
        if (comp.studentEmail) studentIdentifiers.add(comp.studentEmail.toLowerCase());
        else if (comp.studentUid) studentIdentifiers.add(comp.studentUid);
      });
      const totalStudentsAffected = Math.max(studentIdentifiers.size, cluster.length);

      groups.push({
        groupId: mainComplaint.ticketId,
        mainComplaint,
        linkedComplaints,
        confidence: maxConfidence || 75,
        matchReason: bestReason || 'AI identified high similarity in title, description, category, and location.',
        isConfirmed: false,
        totalStudentsAffected
      });
    }
  }

  return groups.sort((a, b) => b.totalStudentsAffected - a.totalStudentsAffected);
}

