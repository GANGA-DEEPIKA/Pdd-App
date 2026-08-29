const autocannon = require('autocannon');
const exceljs = require('exceljs');

async function runLoadTest() {
    console.log("Starting Baseline Load Test...");

    const instance = autocannon({
        url: 'http://localhost:3000',
        connections: 100,
        duration: 60,
    });

    autocannon.track(instance, { renderProgressBar: true });

    instance.on('done', async (result) => {
        console.log("Load test complete! Generating load-test-report.xlsx...");
        
        const workbook = new exceljs.Workbook();
        const sheet = workbook.addWorksheet('Load Test Results');
        
        sheet.columns = [
            { header: 'Metric', key: 'metric', width: 30 },
            { header: 'Value', key: 'value', width: 30 }
        ];

        sheet.addRow({ metric: 'Requests per Second (Average)', value: Math.round(result.requests.average) });
        sheet.addRow({ metric: 'Average Response Time', value: \`\${result.latency.average} ms\` });
        sheet.addRow({ metric: 'Minimum Response Time', value: \`\${result.latency.min} ms\` });
        sheet.addRow({ metric: 'Maximum Response Time', value: \`\${result.latency.max} ms\` });
        sheet.addRow({ metric: 'Total Requests Sent', value: result.requests.total });
        sheet.addRow({ metric: 'Total Errors', value: result.errors });
        sheet.addRow({ metric: 'Total Timeouts', value: result.timeouts });

        sheet.getRow(1).font = { bold: true };
        sheet.getRow(1).fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFD3D3D3' } };

        await workbook.xlsx.writeFile('load-test-report.xlsx');
        console.log("Successfully generated load-test-report.xlsx");
    });
}

runLoadTest();
