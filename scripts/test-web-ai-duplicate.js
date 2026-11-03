// test-web-ai-duplicate.js — Test web AI duplicate engine implementation
const assert = require('assert');

// Simulate ES Module imports or test logic directly
async function runTests() {
  console.log('Testing Web AI Duplicate Engine...');
  const {
    calculateCategorySimilarity,
    calculateLocationSimilarity,
    calculateDescriptionSimilarity,
    evaluateComplaintAgainstExisting,
    findBestDuplicateMatch
  } = await import('../web-app/screens/ai-duplicate-engine.js');

  // Test 1: Category similarity
  assert.strictEqual(calculateCategorySimilarity('Electrical', 'Electrical'), 1.0);
  assert.strictEqual(calculateCategorySimilarity('Power', 'Electrical'), 0.85);
  assert.strictEqual(calculateCategorySimilarity('Plumbing', 'Electrical'), 0.0);
  console.log('✔ Category similarity passed');

  // Test 2: Location similarity
  assert.strictEqual(calculateLocationSimilarity('Block A, Room 101', 'Block A, Room 101'), 1.0);
  assert.strictEqual(calculateLocationSimilarity('Blk A Rm 101', 'Block A Room 101'), 1.0);
  assert(calculateLocationSimilarity('Block A, Room 101', 'Block A, Room 305') < 0.20, 'Room numbers conflicting should have penalty');
  console.log('✔ Location similarity & conflict penalty passed');

  // Test 3: Description similarity
  const descSim = calculateDescriptionSimilarity('Fan not working', 'Ceiling fan broken', 'Broken fan', 'Fan stopped spinning');
  assert(descSim >= 0.45, `Semantic overlap should have high description similarity, was ${descSim}`);
  console.log(`✔ Description semantic similarity passed (${(descSim * 100).toFixed(1)}%)`);

  // Test 4: True Duplicate Evaluation
  const existing1 = {
    ticketId: 'CMP100234',
    title: 'Ceiling Fan Not Working',
    details: 'The ceiling fan in Room 101 is not spinning and making humming sound',
    category: 'Electrical',
    location: 'Block A, Room 101',
    status: 'In Progress'
  };

  const new1 = {
    title: 'Broken fan',
    details: 'Fan stopped working in room 101 block a',
    category: 'Electrical',
    location: 'Block A, Room 101'
  };

  const eval1 = evaluateComplaintAgainstExisting(new1, existing1);
  assert.strictEqual(eval1.isDuplicate, true, 'Should detect duplicate');
  assert(eval1.confidence >= 70, `Confidence was ${eval1.confidence}%, expected >= 70%`);
  assert.strictEqual(eval1.matchedComplaint.ticketId, 'CMP100234');
  assert.strictEqual(eval1.matchedComplaint.status, 'In Progress');
  console.log(`✔ True duplicate test passed (Confidence: ${eval1.confidence}%)`);

  // Test 5: Different room conflict
  const new2 = {
    title: 'Broken fan',
    details: 'Fan stopped working in room 305 block a',
    category: 'Electrical',
    location: 'Block A, Room 305'
  };
  const eval2 = evaluateComplaintAgainstExisting(new2, existing1);
  assert.strictEqual(eval2.isDuplicate, false, 'Different room should NOT be duplicate');
  assert(eval2.confidence < 65, 'Confidence should be below 65');
  console.log(`✔ Different room penalty test passed (Confidence: ${eval2.confidence}%)`);

  // Test 6: List Matching
  const complaintsList = [
    { ticketId: 'CMP001', category: 'Civil', location: 'Hall A', title: 'Wall crack', status: 'Pending' },
    existing1,
    { ticketId: 'CMP003', category: 'Plumbing', location: 'Washroom 2', title: 'Water leak', status: 'Assigned' }
  ];

  const bestMatch = findBestDuplicateMatch(new1, complaintsList);
  assert.strictEqual(bestMatch.isDuplicate, true);
  assert.strictEqual(bestMatch.matchedComplaint.ticketId, 'CMP100234');
  console.log('✔ Best match search from complaints list passed');

  console.log('\nAll Web AI Duplicate Engine tests PASSED successfully! 🚀');
}

runTests().catch(err => {
  console.error('Test failed:', err);
  process.exit(1);
});
