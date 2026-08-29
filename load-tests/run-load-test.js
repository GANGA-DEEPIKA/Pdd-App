const autocannon = require('autocannon');
const fs = require('fs');

async function runLoadTest() {
    console.log("Starting Baseline Load Test...");
    console.log("• 100 virtual users");
    console.log("• Running continuously for 1 minute");
    console.log("Target: http://localhost:3000\\n");

    const instance = autocannon({
        url: 'http://localhost:3000',
        connections: 100, // 100 virtual users
        duration: 60,     // 1 minute
    });

    autocannon.track(instance, { renderProgressBar: true });

    instance.on('done', (result) => {
        console.log("\\n========================================");
        console.log("LOAD TEST RESULTS");
        console.log("========================================");
        
        console.log("\\nRequests per second (RPS)");
        console.log(`Example:`);
        console.log(`${Math.round(result.requests.average)} req/sec`);
        console.log(`Meaning your API is handling about ${Math.round(result.requests.average)} requests every second.`);

        console.log("\\n----------------------------------------");
        
        console.log("Response Time");
        console.log("Example:");
        console.log(`Average: ${result.latency.average}ms`);
        console.log(`Min: ${result.latency.min}ms`);
        console.log(`Max: ${result.latency.max}ms`);
        
        console.log("\\nMeaning:");
        console.log(`• Fastest response = ${result.latency.min}ms`);
        console.log(`• Average = ${result.latency.average}ms`);
        console.log(`• Slowest = ${result.latency.max}ms`);
        
        console.log("\\nTotal Requests Sent:", result.requests.total);
        console.log("Errors:", result.errors);
        console.log("Timeouts:", result.timeouts);
    });
}

runLoadTest();
