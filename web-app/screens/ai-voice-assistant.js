// ai-voice-assistant.js — AI Voice-Powered Campus Assistant Engine
// Handles English Speech-to-Text conversion and NLP Entity Extraction.

export function parseVoiceComplaint(speech) {
  if (!speech || !speech.trim()) {
    return {
      rawSpeech: '',
      title: 'Maintenance Issue',
      description: '',
      category: '',
      department: '',
      location: 'Campus Premises',
      confidence: 50
    };
  }

  const raw = speech.trim();
  const lower = raw.toLowerCase();

  // 1. Category & Department detection
  let category = '';
  let department = '';

  const categoryRules = [
    {
      cat: 'Electrical',
      dept: 'Electrical Maintenance Department',
      keywords: ['fan', 'fans', 'light', 'lights', 'bulb', 'bulbs', 'tube', 'tubelight', 'current', 'power', 'switch', 'socket', 'plug', 'wiring', 'wire', 'spark', 'ac', 'aircon', 'air conditioner', 'cooling', 'voltage', 'mcb', 'trip', 'street light', 'fuse', 'geyser', 'heater', 'electrical']
    },
    {
      cat: 'Plumbing',
      dept: 'Plumbing & Water Supply Department',
      keywords: ['water', 'tap', 'taps', 'faucet', 'leak', 'leaking', 'leakage', 'pipe', 'pipeline', 'dripping', 'flush', 'drain', 'drainage', 'sink', 'washbasin', 'sewage', 'restroom', 'washroom', 'toilet', 'tank', 'motor', 'drinking water', 'purifier', 'cooler', 'plumbing']
    },
    {
      cat: 'Network/WiFi',
      dept: 'IT & Campus Network Services',
      keywords: ['wifi', 'wi-fi', 'internet', 'network', 'lan', 'ethernet', 'router', 'connection', 'signal', 'hotspot', 'disconnect', 'disconnecting', 'slow internet', 'bandwidth', 'portal', 'wifi slow']
    },
    {
      cat: 'Cleanliness',
      dept: 'Housekeeping & Campus Sanitation',
      keywords: ['dirty', 'dust', 'dustbin', 'trash', 'garbage', 'waste', 'cleaning', 'sweep', 'sweeping', 'broom', 'smell', 'stink', 'stinking', 'spill', 'stain', 'sanitation', 'unhygienic', 'litter', 'overflowing', 'corridor dirty', 'clean']
    },
    {
      cat: 'Furniture',
      dept: 'Carpentry & Infrastructure Maintenance',
      keywords: ['bench', 'benches', 'chair', 'chairs', 'desk', 'desks', 'table', 'tables', 'furniture', 'board', 'blackboard', 'whiteboard', 'podium', 'shelf', 'cupboard', 'almirah', 'wooden', 'cot', 'broken leg']
    },
    {
      cat: 'Laboratory',
      dept: 'Academic Laboratories & AV Support',
      keywords: ['lab', 'laboratory', 'projector', 'screen', 'hdmi', 'vga', 'display', 'experiment', 'system', 'computer', 'pc', 'monitor', 'cpu', 'mouse', 'keyboard', 'apparatus', 'instrument', 'microscope', 'chemicals']
    },
    {
      cat: 'Hostel',
      dept: 'Hostel Administration & Estate Office',
      keywords: ['hostel', 'mess', 'warden', 'bed', 'mattress', 'roommate', 'dining', 'hostel room', 'boys hostel', 'girls hostel']
    },
    {
      cat: 'Security',
      dept: 'Campus Security & Safety Division',
      keywords: ['security', 'gate', 'guard', 'theft', 'lost', 'stolen', 'cctv', 'camera', 'id card', 'trespass', 'safety', 'lock', 'padlock', 'key', 'stranger', 'safe']
    },
    {
      cat: 'Transport',
      dept: 'Transport & Fleet Operations',
      keywords: ['bus', 'transport', 'van', 'driver', 'vehicle', 'route', 'stop', 'timing', 'pass', 'parking']
    }
  ];

  let bestCatScore = 0;
  for (const rule of categoryRules) {
    let score = 0;
    for (const kw of rule.keywords) {
      if (lower.includes(kw)) score += kw.split(' ').length;
    }
    if (score > bestCatScore) {
      bestCatScore = score;
      category = rule.cat;
      department = rule.dept;
    }
  }

  // 2. Location Extraction
  let location = '';
  const blockMatch = raw.match(/\b(?:(?:main|mech|mechanical|cse|ece|eee|civil|it|pg|mba|[a-zA-Z0-9])\s+block|block\s+[a-zA-Z0-9]{1,3})\b/i);
  const roomMatch = raw.match(/\b(?:room\s+[a-zA-Z0-9]+|rm\s+[a-zA-Z0-9]+)\b/i);
  const floorMatch = raw.match(/\b(?:ground|first|second|third|fourth|fifth|[0-9]+(?:st|nd|rd|th)?)\s+floor\b/i);
  const placeMatch = raw.match(/\b(?:seminar\s+hall\s*[a-zA-Z0-9]*|auditorium|central\s+library|library|canteen|cafeteria|mess|washroom|restroom|lab\s*[a-zA-Z0-9]*|laboratory\s*[a-zA-Z0-9]*|(?:boys|girls)?\s*hostel(?:\s+gate)?)\b/i);

  const locParts = [];
  if (blockMatch) locParts.push(capitalizeWords(blockMatch[0]));
  if (floorMatch) locParts.push(capitalizeWords(floorMatch[0]));
  if (roomMatch) locParts.push(capitalizeWords(roomMatch[0]));
  if (placeMatch && !locParts.some(p => p.toLowerCase().includes(placeMatch[0].toLowerCase()))) {
    locParts.push(capitalizeWords(placeMatch[0]));
  }

  if (locParts.length > 0) {
    location = locParts.join(', ');
  } else {
    const prepMatch = raw.match(/\b(?:in|at|near|outside|inside|on)\s+([a-zA-Z0-9\s,-]+?)(?:\s+(?:is|was|are|has|have|need|needs|please|kindly|$))/i);
    if (prepMatch && prepMatch[1] && prepMatch[1].trim().length > 2) {
      location = capitalizeWords(prepMatch[1].trim());
    } else {
      location = 'Campus Premises';
    }
  }

  // 3. Title Generation
  let cleanSpeech = raw
    .replace(/^(?:hello|hi|hey|good\s+morning|good\s+afternoon|good\s+evening|campusfix|please|kindly|i\s+want\s+to\s+report\s+that|i\s+would\s+like\s+to\s+report|there\s+is\s+a\s+problem\s+with|there\s+is\s+an\s+issue\s+with|issue\s+regarding|report\s+regarding)\s*/i, '')
    .replace(/\s*(?:please\s+repair\s+it\s+urgently|please\s+fix\s+it\s+urgently|please\s+help|urgently|as\s+soon\s+as\s+possible)\s*$/i, '');

  let title = '';
  if (lower.includes('fan')) {
    title = 'Ceiling Fan Issue';
    if (lower.includes('vibrating') || lower.includes('noise')) title = 'Ceiling Fan Vibrating / Noisy';
    else if (lower.includes('not spinning') || lower.includes('not working') || lower.includes('broken')) title = 'Ceiling Fan Not Working';
  } else if (lower.includes('leak') || lower.includes('tap') || lower.includes('pipe')) {
    title = 'Water Leakage / Tap Issue';
    if (lower.includes('tap')) title = 'Water Tap Leaking';
    else if (lower.includes('pipe')) title = 'Pipeline Leakage';
  } else if (lower.includes('wifi') || lower.includes('internet')) {
    title = 'WiFi / Internet Connectivity Problem';
    if (lower.includes('slow')) title = 'Slow WiFi Connection';
    else if (lower.includes('disconnect')) title = 'WiFi Disconnecting Frequently';
  } else if (lower.includes('projector') || lower.includes('screen')) {
    title = 'Projector Display Malfunction';
  } else if (lower.includes('bench') || lower.includes('chair') || lower.includes('desk')) {
    title = 'Damaged Classroom Furniture';
  } else if (lower.includes('light') || lower.includes('bulb')) {
    title = 'Lighting / Tubelight Failure';
  } else if (lower.includes('dustbin') || lower.includes('dirty') || lower.includes('garbage')) {
    title = 'Cleanliness and Garbage Clearance Required';
  } else {
    const words = cleanSpeech.split(/\s+/).slice(0, 6);
    title = capitalizeWords(words.join(' '));
  }

  // 4. Polished description
  const description = raw.charAt(0).toUpperCase() + raw.slice(1);

  return {
    rawSpeech: raw,
    title,
    description,
    category,
    department,
    location,
    confidence: bestCatScore > 0 ? 95 : 75
  };
}

export function capitalizeWords(str) {
  if (!str) return '';
  return str.replace(/\b\w/g, l => l.toUpperCase());
}

/**
 * AI Category Detection Engine - Extracts complaint category and department
 * Matches the Android AIVoiceAssistant NLP engine.
 */
export function detectAICategory(text) {
  if (!text || !text.trim()) {
    return {
      category: '',
      department: '',
      hasDetectedCategory: false,
      confidence: 0
    };
  }
  const parsed = parseVoiceComplaint(text);
  const hasCategory = Boolean(parsed.category && parsed.category !== 'General Maintenance');
  return {
    category: hasCategory ? parsed.category : '',
    department: parsed.department,
    hasDetectedCategory: hasCategory,
    confidence: parsed.confidence
  };
}

export function detectCategory(text) {
  return parseVoiceComplaint(text).category;
}

export function detectDepartment(text) {
  return parseVoiceComplaint(text).department;
}

/**
 * Creates and returns a cross-browser Web Speech Recognition instance
 */
export function createSpeechRecognizer({ onResult, onError, onStart, onEnd }) {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SpeechRecognition) {
    return null;
  }

  const recognizer = new SpeechRecognition();
  recognizer.continuous = false;
  recognizer.interimResults = false;
  recognizer.lang = 'en-US';

  recognizer.onstart = () => {
    if (onStart) onStart();
  };

  recognizer.onresult = (event) => {
    const transcript = event.results[0][0].transcript;
    if (onResult) onResult(transcript);
  };

  recognizer.onerror = (event) => {
    if (onError) onError(event.error);
  };

  recognizer.onend = () => {
    if (onEnd) onEnd();
  };

  return recognizer;
}

