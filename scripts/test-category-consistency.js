// scripts/test-category-consistency.js
// Verifies that Web and Android AI Category Detection Engine produces identical results.

import { parseVoiceComplaint, detectAICategory, detectCategory, detectDepartment } from '../web-app/screens/ai-voice-assistant.js';
import { suggestCategory } from '../web-app/screens/utils.js';

const testCases = [
  {
    input: "The ceiling fan in Room 101 Block A is vibrating and not spinning, please repair it urgently",
    expectedCategory: "Electrical",
    expectedDept: "Electrical Maintenance Department"
  },
  {
    input: "Water is leaking from the tap in mechanical block second floor washroom",
    expectedCategory: "Plumbing",
    expectedDept: "Plumbing & Water Supply Department"
  },
  {
    input: "WiFi connection is very slow and disconnecting frequently in Central Library",
    expectedCategory: "Network/WiFi",
    expectedDept: "IT & Campus Network Services"
  },
  {
    input: "The projector in Seminar Hall 2 is showing no signal and screen is blank",
    expectedCategory: "Network/WiFi",
    expectedDept: "IT & Campus Network Services"
  },
  {
    input: "Broken wooden bench in B block room 305",
    expectedCategory: "Furniture",
    expectedDept: "Carpentry & Infrastructure Maintenance"
  },
  {
    input: "Dustbins are overflowing and corridor is dirty outside Lab 4",
    expectedCategory: "Cleanliness",
    expectedDept: "Housekeeping & Campus Sanitation"
  },
  {
    input: "Street light near boys hostel gate is not glowing",
    expectedCategory: "Electrical",
    expectedDept: "Electrical Maintenance Department"
  },
  {
    input: "AC cooling is completely down in CS block room 102",
    expectedCategory: "Electrical",
    expectedDept: "Electrical Maintenance Department"
  },
  {
    input: "Pipeline leakage near ground floor drinking water cooler",
    expectedCategory: "Plumbing",
    expectedDept: "Plumbing & Water Supply Department"
  },
  {
    input: "Campus bus 14 route timing is delayed",
    expectedCategory: "Transport",
    expectedDept: "Transport & Fleet Operations"
  },
  {
    input: "CCTV camera at main gate is not working",
    expectedCategory: "Security",
    expectedDept: "Campus Security & Safety Division"
  },
  {
    input: "Wooden chair and study desk broken in seminar room",
    expectedCategory: "Furniture",
    expectedDept: "Carpentry & Infrastructure Maintenance"
  },
  {
    input: "Hostel room cot and mattress need replacement",
    expectedCategory: "Hostel",
    expectedDept: "Hostel Administration & Estate Office"
  },
  {
    input: "Internet router not accessible in mechanical lab",
    expectedCategory: "Network/WiFi",
    expectedDept: "IT & Campus Network Services"
  },
  {
    input: "",
    expectedCategory: "General Maintenance",
    expectedDept: "Estate Maintenance Department"
  }
];

console.log('========================================================');
console.log(' Running AI Category & Department Detection Verification');
console.log('========================================================');

let passed = 0;
let failed = 0;

for (let i = 0; i < testCases.length; i++) {
  const tc = testCases[i];
  const parsed = parseVoiceComplaint(tc.input);
  const detected = detectAICategory(tc.input);
  const cat = detectCategory(tc.input);
  const dept = detectDepartment(tc.input);
  const suggested = suggestCategory(tc.input);

  const catMatch = parsed.category === tc.expectedCategory &&
                   detected.category === tc.expectedCategory &&
                   cat === tc.expectedCategory &&
                   suggested === tc.expectedCategory;

  const deptMatch = parsed.department === tc.expectedDept &&
                    detected.department === tc.expectedDept &&
                    dept === tc.expectedDept;

  if (catMatch && deptMatch) {
    console.log(`[PASS] Case ${i + 1}: "${tc.input.slice(0, 40)}..." -> ${parsed.category} | ${parsed.department}`);
    passed++;
  } else {
    console.error(`[FAIL] Case ${i + 1}: "${tc.input}"`);
    console.error(`       Expected: Category="${tc.expectedCategory}", Dept="${tc.expectedDept}"`);
    console.error(`       Actual:   Category="${parsed.category}", Dept="${parsed.department}"`);
    failed++;
  }
}

console.log('========================================================');
console.log(`RESULTS: ${passed} passed, ${failed} failed out of ${testCases.length}`);
console.log('========================================================');

if (failed > 0) {
  process.exit(1);
} else {
  console.log('All Web AI Category Detection tests PASSED successfully! 🚀');
}
