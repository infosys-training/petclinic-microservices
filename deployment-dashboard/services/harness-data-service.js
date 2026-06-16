/**
 * HarnessDataService - Abstraction layer for deployment data.
 *
 * This module provides a unified interface for fetching deployment data.
 * Currently uses mock data that simulates Harness CI/CD pipeline responses.
 *
 * To integrate with the real Harness API:
 * 1. Set `HarnessDataService.config.apiKey` and `.accountId`
 * 2. Set `HarnessDataService.config.useMockData = false`
 * 3. Optionally set `.orgIdentifier` and `.projectIdentifier`
 *
 * Only this file needs to change when switching from mock to real data.
 */
const HarnessDataService = (() => {

    // ─── Configuration ───────────────────────────────────────────────────────────
    const config = {
        useMockData: true,
        // Harness API configuration (populate when integrating with real API)
        baseUrl: 'https://app.harness.io',
        apiKey: '',           // x-api-key header
        accountId: '',        // Harness account identifier
        orgIdentifier: 'default',
        projectIdentifier: 'petclinic_microservices',
    };

    // ─── Public Interface ────────────────────────────────────────────────────────

    /**
     * Fetch all deployment executions within a date range.
     * @param {Object} options
     * @param {Date} options.startDate - Start of the date range
     * @param {Date} options.endDate - End of the date range
     * @param {string[]} options.services - Filter by service names (optional)
     * @param {string[]} options.environments - Filter by environments (optional)
     * @returns {Promise<{metadata: Object, deployments: Object[]}>}
     */
    async function getDeployments(options = {}) {
        if (config.useMockData) {
            return getMockDeployments(options);
        }
        return fetchHarnessDeployments(options);
    }

    /**
     * Get aggregated deployment statistics.
     * @param {Object} options - Same filter options as getDeployments
     * @returns {Promise<Object>} Aggregated stats per service
     */
    async function getDeploymentStats(options = {}) {
        const { deployments } = await getDeployments(options);
        return aggregateStats(deployments);
    }

    /**
     * Get deployment trend data grouped by day.
     * @param {Object} options - Same filter options as getDeployments
     * @returns {Promise<Object[]>} Daily deployment counts
     */
    async function getDeploymentTrends(options = {}) {
        const { deployments } = await getDeployments(options);
        return aggregateTrends(deployments);
    }

    // ─── Real Harness API Integration (placeholder) ──────────────────────────────

    async function fetchHarnessDeployments(options) {
        const { startDate, endDate } = options;

        const url = `${config.baseUrl}/pipeline/api/pipelines/execution/summary` +
            `?accountIdentifier=${config.accountId}` +
            `&orgIdentifier=${config.orgIdentifier}` +
            `&projectIdentifier=${config.projectIdentifier}`;

        const body = {
            filterType: 'PipelineExecution',
            startTime: startDate ? startDate.getTime() : undefined,
            endTime: endDate ? endDate.getTime() : undefined,
        };

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'x-api-key': config.apiKey,
            },
            body: JSON.stringify(body),
        });

        if (!response.ok) {
            throw new Error(`Harness API error: ${response.status} ${response.statusText}`);
        }

        const result = await response.json();
        return transformHarnessResponse(result);
    }

    function transformHarnessResponse(apiResponse) {
        const executions = apiResponse.data?.content || [];
        const deployments = executions.map(exec => ({
            id: exec.planExecutionId,
            service: extractServiceName(exec),
            pipelineName: exec.pipelineIdentifier,
            timestamp: new Date(exec.startTs).toISOString(),
            endTimestamp: exec.endTs ? new Date(exec.endTs).toISOString() : null,
            status: mapHarnessStatus(exec.status),
            environment: extractEnvironment(exec),
            triggerType: exec.executionTriggerInfo?.triggerType || 'manual',
            version: exec.runSequence ? `build-${exec.runSequence}` : null,
            duration_seconds: exec.endTs ? Math.round((exec.endTs - exec.startTs) / 1000) : null,
            error: exec.failureInfo?.message || null,
        }));

        return {
            metadata: {
                project: 'petclinic-microservices',
                source: 'harness-api',
                accountId: config.accountId,
                fetchedAt: new Date().toISOString(),
            },
            deployments,
        };
    }

    function mapHarnessStatus(harnessStatus) {
        const statusMap = {
            'Success': 'success',
            'Failed': 'failed',
            'Running': 'running',
            'Aborted': 'failed',
            'Expired': 'failed',
            'AbortedByFreeze': 'failed',
            'Paused': 'running',
            'AsyncWaiting': 'running',
            'TaskWaiting': 'running',
            'TimedWaiting': 'running',
        };
        return statusMap[harnessStatus] || 'unknown';
    }

    function extractServiceName(execution) {
        const pipelineId = execution.pipelineIdentifier || '';
        const serviceMap = {
            'deploy_customers': 'customers-service',
            'deploy_vets': 'vets-service',
            'deploy_visits': 'visits-service',
            'deploy_gateway': 'api-gateway',
            'deploy_config': 'config-server',
            'deploy_discovery': 'discovery-server',
            'deploy_genai': 'genai-service',
            'deploy_admin': 'admin-server',
        };
        for (const [key, value] of Object.entries(serviceMap)) {
            if (pipelineId.includes(key)) return value;
        }
        return pipelineId;
    }

    function extractEnvironment(execution) {
        // Harness stores environment info in stage details
        return execution.moduleInfo?.cd?.envIdentifiers?.[0] || 'production';
    }

    // ─── Mock Data Generation ────────────────────────────────────────────────────

    function getMockDeployments(options) {
        const { startDate, endDate, services, environments } = options;
        let deployments = generateMockData();

        if (startDate) {
            deployments = deployments.filter(d => new Date(d.timestamp) >= startDate);
        }
        if (endDate) {
            deployments = deployments.filter(d => new Date(d.timestamp) <= endDate);
        }
        if (services && services.length > 0) {
            deployments = deployments.filter(d => services.includes(d.service));
        }
        if (environments && environments.length > 0) {
            deployments = deployments.filter(d => environments.includes(d.environment));
        }

        return {
            metadata: {
                project: 'petclinic-microservices',
                source: 'mock-data',
                generatedAt: new Date().toISOString(),
                note: 'Simulated Harness CI/CD data. Replace HarnessDataService config to use real API.',
            },
            deployments,
        };
    }

    function generateMockData() {
        const services = [
            { name: 'customers-service', pipeline: 'deploy-customers-pipeline', weight: 1.2 },
            { name: 'vets-service', pipeline: 'deploy-vets-pipeline', weight: 1.0 },
            { name: 'visits-service', pipeline: 'deploy-visits-pipeline', weight: 1.1 },
            { name: 'api-gateway', pipeline: 'deploy-api-gateway-pipeline', weight: 1.3 },
            { name: 'config-server', pipeline: 'deploy-config-server-pipeline', weight: 0.7 },
            { name: 'discovery-server', pipeline: 'deploy-discovery-server-pipeline', weight: 0.7 },
            { name: 'genai-service', pipeline: 'deploy-genai-service-pipeline', weight: 0.9 },
            { name: 'admin-server', pipeline: 'deploy-admin-server-pipeline', weight: 0.6 },
        ];

        const environments = ['dev', 'staging', 'prod'];
        const triggerTypes = ['manual', 'webhook', 'scheduled'];
        const triggerWeights = [0.25, 0.55, 0.20]; // webhook most common

        const errorMessages = [
            'Health check failed after deployment',
            'Container startup timeout exceeded',
            'Database migration script failed',
            'Service dependency unavailable',
            'OOM killed during startup',
            'Readiness probe failed',
            'Image pull error: authentication required',
            'Port binding conflict detected',
            'Configuration validation error',
            'Integration test suite failed',
            'Rollback triggered by canary analysis',
            'Approval step timeout',
        ];

        const deployments = [];
        const now = new Date();
        const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);

        // Use seeded random for reproducibility
        let seed = 42;
        function seededRandom() {
            seed = (seed * 1664525 + 1013904223) & 0xffffffff;
            return (seed >>> 0) / 0xffffffff;
        }

        function weightedChoice(items, weights) {
            const total = weights.reduce((a, b) => a + b, 0);
            let r = seededRandom() * total;
            for (let i = 0; i < items.length; i++) {
                r -= weights[i];
                if (r <= 0) return items[i];
            }
            return items[items.length - 1];
        }

        let depId = 1;

        // Generate deployments day by day
        for (let day = 0; day < 30; day++) {
            const date = new Date(thirtyDaysAgo.getTime() + day * 24 * 60 * 60 * 1000);

            // Each service gets 0-3 deployments per day depending on weight
            for (const svc of services) {
                const deploymentsToday = Math.floor(seededRandom() * 2.5 * svc.weight);

                for (let i = 0; i < deploymentsToday; i++) {
                    const hour = 8 + Math.floor(seededRandom() * 10); // 8am-6pm
                    const minute = Math.floor(seededRandom() * 60);
                    const timestamp = new Date(date);
                    timestamp.setHours(hour, minute, 0, 0);

                    // ~85% success rate, slightly worse for genai-service
                    const failureChance = svc.name === 'genai-service' ? 0.22 : 0.15;
                    const isRunning = day === 29 && seededRandom() < 0.05; // small chance on latest day
                    const isFailed = !isRunning && seededRandom() < failureChance;
                    const status = isRunning ? 'running' : (isFailed ? 'failed' : 'success');

                    const env = weightedChoice(environments, [0.4, 0.3, 0.3]);
                    const trigger = weightedChoice(triggerTypes, triggerWeights);
                    const baseDuration = svc.name.includes('gateway') ? 130 : 95;
                    const duration = status === 'running' ? null :
                        Math.round(baseDuration + (seededRandom() * 60) - 20);

                    deployments.push({
                        id: `exec-${String(depId).padStart(4, '0')}`,
                        service: svc.name,
                        pipelineName: svc.pipeline,
                        timestamp: timestamp.toISOString(),
                        endTimestamp: duration ? new Date(timestamp.getTime() + duration * 1000).toISOString() : null,
                        status,
                        environment: env,
                        triggerType: trigger,
                        version: `4.0.${Math.floor(day / 7) + 1}-build.${depId}`,
                        duration_seconds: duration,
                        error: isFailed ? errorMessages[Math.floor(seededRandom() * errorMessages.length)] : null,
                    });
                    depId++;
                }
            }
        }

        return deployments.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
    }

    // ─── Aggregation Helpers ─────────────────────────────────────────────────────

    function aggregateStats(deployments) {
        const stats = {};
        for (const dep of deployments) {
            if (!stats[dep.service]) {
                stats[dep.service] = { total: 0, success: 0, failed: 0, running: 0 };
            }
            stats[dep.service].total++;
            stats[dep.service][dep.status]++;
        }
        // Calculate rates
        for (const svc of Object.keys(stats)) {
            const s = stats[svc];
            s.successRate = s.total > 0 ? ((s.success / s.total) * 100).toFixed(1) : 0;
            s.failureRate = s.total > 0 ? ((s.failed / s.total) * 100).toFixed(1) : 0;
        }
        return stats;
    }

    function aggregateTrends(deployments) {
        const daily = {};
        for (const dep of deployments) {
            const date = dep.timestamp.split('T')[0];
            if (!daily[date]) {
                daily[date] = { date, total: 0, success: 0, failed: 0, running: 0 };
            }
            daily[date].total++;
            daily[date][dep.status]++;
        }
        return Object.values(daily).sort((a, b) => a.date.localeCompare(b.date));
    }

    // ─── Expose Public API ───────────────────────────────────────────────────────

    return {
        config,
        getDeployments,
        getDeploymentStats,
        getDeploymentTrends,
    };

})();

// Export for module systems (Node.js, bundlers)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = HarnessDataService;
}
