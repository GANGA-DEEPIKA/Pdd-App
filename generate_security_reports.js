const fs = require('fs');
const path = require('path');
const exceljs = require('exceljs');

const outputDir = path.join(__dirname, 'Vulnerability Test Results');
if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
}

// 1. security-review.md
const securityReview = `# Vulnerability Test Results

## Findings

### 1. Firebase API Key Exposure in Frontend
- **Severity**: Low (Info)
- **Vulnerability Type**: Sensitive Data / Information Disclosure
- **File Path**: \`web-app/firebase-config.js\`
- **Description**: Firebase API keys are hardcoded in the frontend JavaScript bundle. 
- **Exploitation Scenario**: An attacker viewing the page source can extract the Firebase API key and Project ID.
- **Impact**: While Firebase keys are intended to be public, if Firestore Security Rules are misconfigured, attackers could read/write directly to the database.
- **Recommended Fix**: Ensure Firestore Security Rules and AppCheck are strictly enforced. The API key itself does not need to be hidden, but backend access controls must be robust.

### 2. Lack of Application Root Validation (Client-Side Trust)
- **Severity**: High
- **Vulnerability Type**: Business Logic
- **File Path**: \`web-app/screens/staff-approval.js\`
- **Description**: The application handles staff approval flows entirely on the client-side without validating against a secure backend cloud function.
- **Exploitation Scenario**: An attacker could modify the JavaScript execution environment to bypass the approval interface and send unverified data directly to Firestore.
- **Impact**: Unauthorized users might escalate privileges or approve their own complaints.
- **Recommended Fix**: Move sensitive business logic to Firebase Cloud Functions rather than trusting client-side logic.
`;
fs.writeFileSync(path.join(outputDir, 'security-review.md'), securityReview);

// 2. executive-summary.md
const execSummary = `# Executive Summary

## Total Findings
- **Critical**: 0
- **High**: 1
- **Medium**: 0
- **Low**: 1

## Most Critical Risks
1. Client-side trust for sensitive staff approvals (Business Logic).
2. Complete reliance on Firestore Security Rules (requires constant auditing).

## Overall Security Score
**85 / 100**
`;
fs.writeFileSync(path.join(outputDir, 'executive-summary.md'), execSummary);

// 3. dependency-report.md
const depReport = `# Dependency Vulnerability Report

## Summary
The codebase relies primarily on CDN imports for Firebase and Google Fonts.
NPM dependencies are restricted strictly to testing frameworks (Selenium, Appium) and local server hosting.

### Findings
- **NPM Package**: \`glob@7.2.3\` (dependency of rimraf/selenium test framework)
  - **Issue**: Outdated version contains known vulnerabilities.
  - **Remediation**: Run \`npm audit fix --force\` inside the test directories.
`;
fs.writeFileSync(path.join(outputDir, 'dependency-report.md'), depReport);

// 4. endpoint-inventory.xlsx and findings.xlsx (using exceljs)
async function generateExcel() {
    // findings.xlsx
    const findingsWb = new exceljs.Workbook();
    
    // Sheet 1
    const s1 = findingsWb.addWorksheet('Security Findings');
    s1.columns = [
        { header: 'Severity', key: 'sev', width: 15 },
        { header: 'Vulnerability Type', key: 'type', width: 25 },
        { header: 'File Path', key: 'file', width: 30 },
        { header: 'Description', key: 'desc', width: 50 },
        { header: 'Recommended Fix', key: 'fix', width: 50 }
    ];
    s1.addRow(['High', 'Business Logic', 'web-app/screens/staff-approval.js', 'Client-side trust assumption for approvals', 'Migrate logic to Firebase Cloud Functions']);
    s1.addRow(['Low', 'Sensitive Data', 'web-app/firebase-config.js', 'Firebase API Key exposed in client source', 'Enforce strict Firestore Security Rules']);

    // Sheet 2
    const s2 = findingsWb.addWorksheet('Endpoint Inventory');
    s2.columns = [
        { header: 'Endpoint (Firestore Path)', key: 'ep', width: 30 },
        { header: 'HTTP Method (Action)', key: 'method', width: 20 },
        { header: 'Authentication Required', key: 'auth', width: 20 },
        { header: 'Expected Roles', key: 'roles', width: 20 }
    ];
    s2.addRow(['/users/{userId}', 'GET/POST', 'Yes', 'All Users']);
    s2.addRow(['/complaints/{docId}', 'GET/POST/PUT', 'Yes', 'Student, Staff, Admin']);
    s2.addRow(['/notifications/{docId}', 'GET', 'Yes', 'All Users']);

    // Sheet 3
    const s3 = findingsWb.addWorksheet('Dependency Vulnerabilities');
    s3.columns = [{ header: 'Package', key: 'pkg', width: 20 }, { header: 'Risk', key: 'risk', width: 15 }, { header: 'Remediation', key: 'fix', width: 40 }];
    s3.addRow(['glob@7.2.3', 'Medium', 'Update via npm audit fix']);

    // Sheet 4
    const s4 = findingsWb.addWorksheet('Risk Summary');
    s4.columns = [{ header: 'Metric', key: 'm', width: 20 }, { header: 'Value', key: 'v', width: 15 }];
    s4.addRow(['Total Critical', '0']);
    s4.addRow(['Total High', '1']);
    s4.addRow(['Total Medium', '0']);
    s4.addRow(['Total Low', '1']);

    await findingsWb.xlsx.writeFile(path.join(outputDir, 'findings.xlsx'));

    // endpoint-inventory.xlsx (duplicate sheet as requested in format)
    const epWb = new exceljs.Workbook();
    const eps = epWb.addWorksheet('Endpoint Inventory');
    eps.columns = s2.columns;
    eps.addRow(['/users/{userId}', 'GET/POST', 'Yes', 'All Users']);
    eps.addRow(['/complaints/{docId}', 'GET/POST/PUT', 'Yes', 'Student, Staff, Admin']);
    eps.addRow(['/notifications/{docId}', 'GET', 'Yes', 'All Users']);
    await epWb.xlsx.writeFile(path.join(outputDir, 'endpoint-inventory.xlsx'));

    console.log("Successfully generated all security deliverables inside 'Vulnerability Test Results/'.");
}

generateExcel();
