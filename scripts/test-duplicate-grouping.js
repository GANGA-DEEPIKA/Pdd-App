// test-duplicate-grouping.js — Tests AI Duplicate Grouping in Web App
const assert = require('assert');

async function runTests() {
  console.log('Testing Web AI Duplicate Grouping Engine...');
  const { groupComplaintsWithAI } = await import('../web-app/screens/ai-duplicate-engine.js');

  const c1 = {
    ticketId: 'CMP101',
    title: 'Slow WiFi Connection',
    details: 'WiFi is very slow and keeps disconnecting in Central Library',
    category: 'Network/WiFi',
    location: 'Central Library',
    studentEmail: 'student1@campus.edu',
    status: 'In Progress'
  };
  const c2 = {
    ticketId: 'CMP102',
    title: 'Internet not connecting',
    details: 'WiFi signal drops continuously in Central Library',
    category: 'Network/WiFi',
    location: 'Central Library',
    studentEmail: 'student2@campus.edu',
    status: 'Pending'
  };
  const c3 = {
    ticketId: 'CMP103',
    title: 'Library WiFi down',
    details: 'Cannot access campus internet portal from Central Library',
    category: 'Network/WiFi',
    location: 'Central Library',
    studentEmail: 'student3@campus.edu',
    status: 'Assigned'
  };
  const c4 = {
    ticketId: 'CMP104',
    title: 'Ceiling fan vibrating',
    details: 'Fan is vibrating loudly and not spinning in Room 101 Block A',
    category: 'Electrical',
    location: 'Block A, Room 101',
    studentEmail: 'student4@campus.edu',
    status: 'Pending'
  };
  const c5 = {
    ticketId: 'CMP105',
    title: 'Broken ceiling fan',
    details: 'Fan making noise in room 101 block a',
    category: 'Electrical',
    location: 'Block A, Room 101',
    studentEmail: 'student5@campus.edu',
    status: 'Pending'
  };
  const uniqueComplaint = {
    ticketId: 'CMP106',
    title: 'Water tap leaking',
    details: 'Faucet dripping constantly in 2nd floor washroom',
    category: 'Plumbing',
    location: 'Mech Block, 2nd Floor Washroom',
    studentEmail: 'student6@campus.edu',
    status: 'Pending'
  };

  const groups = groupComplaintsWithAI([c1, c2, c3, c4, c5, uniqueComplaint]);

  // Test 1: Exactly 2 duplicate groups detected
  assert.strictEqual(groups.length, 2, `Expected 2 groups, got ${groups.length}`);
  console.log('✔ Correct number of duplicate groups detected (2 groups)');

  // Test 2: Library WiFi group has 3 affected students
  const libraryGroup = groups.find(g => g.mainComplaint.category === 'Network/WiFi');
  assert(libraryGroup, 'Library WiFi group should exist');
  assert.strictEqual(libraryGroup.mainComplaint.ticketId, 'CMP101', 'CMP101 should be chosen as main complaint');
  assert.strictEqual(libraryGroup.linkedComplaints.length, 2, 'Should have 2 linked complaints');
  assert.strictEqual(libraryGroup.totalStudentsAffected, 3, 'Should have 3 unique affected students');
  console.log('✔ Library WiFi group correctly identified with 3 affected students');

  // Test 3: Fan group has 2 affected students
  const fanGroup = groups.find(g => g.mainComplaint.category === 'Electrical');
  assert(fanGroup, 'Fan group should exist');
  assert.strictEqual(fanGroup.totalStudentsAffected, 2, 'Should have 2 affected students');
  console.log('✔ Fan group correctly identified with 2 affected students');

  // Test 4: Pre-confirmed group persistence
  const preConfirmed = [
    {
      ticketId: 'CMP301',
      title: 'Lab Projector Error',
      details: 'No display signal',
      category: 'Laboratory',
      location: 'Seminar Hall 2',
      isGroupMaster: true,
      linkedTicketIds: ['CMP302'],
      studentEmail: 'alpha@campus.edu'
    },
    {
      ticketId: 'CMP302',
      title: 'Projector Black Screen',
      details: 'Screen is black',
      category: 'Laboratory',
      location: 'Seminar Hall 2',
      parentTicketId: 'CMP301',
      studentEmail: 'beta@campus.edu'
    }
  ];

  const confirmedGroups = groupComplaintsWithAI(preConfirmed);
  assert.strictEqual(confirmedGroups.length, 1);
  assert.strictEqual(confirmedGroups[0].isConfirmed, true);
  assert.strictEqual(confirmedGroups[0].totalStudentsAffected, 2);
  console.log('✔ Pre-confirmed duplicate group handled with isConfirmed=true');

  console.log('\nAll Web AI Duplicate Grouping tests PASSED successfully! 🚀');
}

runTests().catch(err => {
  console.error('Test failed:', err);
  process.exit(1);
});
