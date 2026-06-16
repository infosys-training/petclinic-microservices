/**
 * PetClinic Microservices - Deployment Dashboard
 * Consumes data from HarnessDataService and renders charts/tables.
 */

const CHART_COLORS = {
    primary: '#2563eb',
    success: '#16a34a',
    danger: '#dc2626',
    warning: '#d97706',
    info: '#0891b2',
    running: '#7c3aed',
    palette: [
        '#2563eb', '#7c3aed', '#db2777', '#ea580c',
        '#16a34a', '#0891b2', '#4f46e5', '#c026d3'
    ]
};

let chartInstances = {};
let currentDeployments = [];

async function loadDashboard() {
    try {
        document.getElementById('loading-indicator').style.display = 'flex';

        const now = new Date();
        const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);

        const envFilter = document.getElementById('env-filter').value;
        const triggerFilter = document.getElementById('trigger-filter').value;

        const options = {
            startDate: thirtyDaysAgo,
            endDate: now,
        };

        if (envFilter) options.environments = [envFilter];

        const { metadata, deployments } = await HarnessDataService.getDeployments(options);

        // Apply trigger filter client-side
        currentDeployments = triggerFilter
            ? deployments.filter(d => d.triggerType === triggerFilter)
            : deployments;

        renderDashboard(currentDeployments, metadata);
    } catch (error) {
        console.error('Dashboard load failed:', error);
        alert(`Failed to load deployment data.\n\nError: ${error.message}`);
    } finally {
        document.getElementById('loading-indicator').style.display = 'none';
    }
}

function renderDashboard(deployments, metadata) {
    const services = [...new Set(deployments.map(d => d.service))].sort();

    updateSummaryCards(deployments);
    renderPerServiceChart(deployments, services);
    renderSuccessFailureChart(deployments, services);
    renderTrendsChart(deployments);
    renderFailureRateChart(deployments, services);
    renderStatusDistributionChart(deployments);
    renderEnvironmentChart(deployments);
    renderTriggerChart(deployments);
    renderTable(deployments);
    updateMetadata(metadata, deployments);
}

function updateSummaryCards(deployments) {
    const total = deployments.length;
    const successful = deployments.filter(d => d.status === 'success').length;
    const failed = deployments.filter(d => d.status === 'failed').length;
    const running = deployments.filter(d => d.status === 'running').length;
    const rate = total > 0 ? ((successful / total) * 100).toFixed(1) : 0;

    document.getElementById('total-deployments').textContent = total;
    document.getElementById('successful-deployments').textContent = successful;
    document.getElementById('failed-deployments').textContent = failed;
    document.getElementById('running-deployments').textContent = running;
    document.getElementById('success-rate').textContent = `${rate}%`;
}

function updateMetadata(metadata, deployments) {
    const el = document.getElementById('data-source-info');
    if (el) {
        const source = metadata.source === 'mock-data' ? 'Mock Data (Harness Simulation)' : 'Harness API';
        el.textContent = `Source: ${source} | ${deployments.length} records | Last updated: ${new Date().toLocaleTimeString()}`;
    }
}

function renderPerServiceChart(deployments, services) {
    const ctx = document.getElementById('chart-per-service').getContext('2d');
    const counts = services.map(s => deployments.filter(d => d.service === s).length);

    destroyChart('chart-per-service');
    chartInstances['chart-per-service'] = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: services.map(formatServiceName),
            datasets: [{
                label: 'Total Deployments',
                data: counts,
                backgroundColor: CHART_COLORS.palette.slice(0, services.length),
                borderRadius: 6,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, ticks: { stepSize: 1 } },
                x: { ticks: { maxRotation: 45 } }
            }
        }
    });
}

function renderSuccessFailureChart(deployments, services) {
    const ctx = document.getElementById('chart-success-failure').getContext('2d');

    const successCounts = services.map(s => deployments.filter(d => d.service === s && d.status === 'success').length);
    const failedCounts = services.map(s => deployments.filter(d => d.service === s && d.status === 'failed').length);
    const runningCounts = services.map(s => deployments.filter(d => d.service === s && d.status === 'running').length);

    destroyChart('chart-success-failure');
    chartInstances['chart-success-failure'] = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: services.map(formatServiceName),
            datasets: [
                { label: 'Success', data: successCounts, backgroundColor: CHART_COLORS.success, borderRadius: 6, borderSkipped: false },
                { label: 'Failed', data: failedCounts, backgroundColor: CHART_COLORS.danger, borderRadius: 6, borderSkipped: false },
                { label: 'Running', data: runningCounts, backgroundColor: CHART_COLORS.running, borderRadius: 6, borderSkipped: false },
            ]
        },
        options: {
            responsive: true,
            plugins: { legend: { position: 'top' } },
            scales: {
                y: { beginAtZero: true, ticks: { stepSize: 1 }, stacked: true },
                x: { stacked: true, ticks: { maxRotation: 45 } }
            }
        }
    });
}

function renderTrendsChart(deployments) {
    const ctx = document.getElementById('chart-trends').getContext('2d');
    const sorted = [...deployments].sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));

    const dateGroups = {};
    sorted.forEach(dep => {
        const date = dep.timestamp.split('T')[0];
        if (!dateGroups[date]) dateGroups[date] = { success: 0, failed: 0, running: 0, total: 0 };
        dateGroups[date].total++;
        dateGroups[date][dep.status]++;
    });

    const dates = Object.keys(dateGroups);

    destroyChart('chart-trends');
    chartInstances['chart-trends'] = new Chart(ctx, {
        type: 'line',
        data: {
            labels: dates,
            datasets: [
                {
                    label: 'Total',
                    data: dates.map(d => dateGroups[d].total),
                    borderColor: CHART_COLORS.primary,
                    backgroundColor: 'rgba(37, 99, 235, 0.1)',
                    fill: true, tension: 0.3, pointRadius: 4, pointHoverRadius: 6
                },
                {
                    label: 'Successful',
                    data: dates.map(d => dateGroups[d].success),
                    borderColor: CHART_COLORS.success,
                    backgroundColor: 'rgba(22, 163, 74, 0.1)',
                    fill: true, tension: 0.3, pointRadius: 4, pointHoverRadius: 6
                },
                {
                    label: 'Failed',
                    data: dates.map(d => dateGroups[d].failed),
                    borderColor: CHART_COLORS.danger,
                    backgroundColor: 'rgba(220, 38, 38, 0.1)',
                    fill: true, tension: 0.3, pointRadius: 4, pointHoverRadius: 6
                }
            ]
        },
        options: {
            responsive: true,
            plugins: { legend: { position: 'top' } },
            scales: {
                y: { beginAtZero: true, ticks: { stepSize: 1 } },
                x: { ticks: { maxRotation: 45, maxTicksLimit: 15 } }
            }
        }
    });
}

function renderFailureRateChart(deployments, services) {
    const ctx = document.getElementById('chart-failure-rate').getContext('2d');
    const failureRates = services.map(s => {
        const svcDeps = deployments.filter(d => d.service === s);
        const failed = svcDeps.filter(d => d.status === 'failed').length;
        return svcDeps.length > 0 ? ((failed / svcDeps.length) * 100).toFixed(1) : 0;
    });

    destroyChart('chart-failure-rate');
    chartInstances['chart-failure-rate'] = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: services.map(formatServiceName),
            datasets: [{
                label: 'Failure Rate (%)',
                data: failureRates,
                backgroundColor: failureRates.map(r =>
                    r > 25 ? CHART_COLORS.danger : r > 10 ? CHART_COLORS.warning : CHART_COLORS.success
                ),
                borderRadius: 6, borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, max: 100, ticks: { callback: v => v + '%' } },
                x: { ticks: { maxRotation: 45 } }
            }
        }
    });
}

function renderStatusDistributionChart(deployments) {
    const ctx = document.getElementById('chart-status-distribution').getContext('2d');
    const success = deployments.filter(d => d.status === 'success').length;
    const failed = deployments.filter(d => d.status === 'failed').length;
    const running = deployments.filter(d => d.status === 'running').length;

    destroyChart('chart-status-distribution');
    chartInstances['chart-status-distribution'] = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Successful', 'Failed', 'Running'],
            datasets: [{
                data: [success, failed, running],
                backgroundColor: [CHART_COLORS.success, CHART_COLORS.danger, CHART_COLORS.running],
                borderWidth: 0, spacing: 2
            }]
        },
        options: {
            responsive: true, cutout: '65%',
            plugins: { legend: { position: 'bottom' } }
        }
    });
}

function renderEnvironmentChart(deployments) {
    const ctx = document.getElementById('chart-environments').getContext('2d');
    const envs = ['dev', 'staging', 'prod'];
    const envColors = { dev: '#0891b2', staging: '#d97706', prod: '#2563eb' };

    const successByEnv = envs.map(e => deployments.filter(d => d.environment === e && d.status === 'success').length);
    const failedByEnv = envs.map(e => deployments.filter(d => d.environment === e && d.status === 'failed').length);

    destroyChart('chart-environments');
    chartInstances['chart-environments'] = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: envs.map(e => e.charAt(0).toUpperCase() + e.slice(1)),
            datasets: [
                { label: 'Success', data: successByEnv, backgroundColor: CHART_COLORS.success, borderRadius: 6, borderSkipped: false },
                { label: 'Failed', data: failedByEnv, backgroundColor: CHART_COLORS.danger, borderRadius: 6, borderSkipped: false }
            ]
        },
        options: {
            responsive: true,
            plugins: { legend: { position: 'top' } },
            scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
        }
    });
}

function renderTriggerChart(deployments) {
    const ctx = document.getElementById('chart-triggers').getContext('2d');
    const triggers = ['webhook', 'manual', 'scheduled'];
    const triggerColors = ['#2563eb', '#7c3aed', '#d97706'];
    const counts = triggers.map(t => deployments.filter(d => d.triggerType === t).length);

    destroyChart('chart-triggers');
    chartInstances['chart-triggers'] = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: triggers.map(t => t.charAt(0).toUpperCase() + t.slice(1)),
            datasets: [{
                data: counts,
                backgroundColor: triggerColors,
                borderWidth: 0, spacing: 2
            }]
        },
        options: {
            responsive: true, cutout: '65%',
            plugins: { legend: { position: 'bottom' } }
        }
    });
}

function renderTable(deployments) {
    const tbody = document.getElementById('deployments-tbody');
    const sorted = [...deployments].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
    const recent = sorted.slice(0, 50); // Show last 50

    tbody.innerHTML = recent.map(dep => `
        <tr>
            <td><code>${dep.id}</code></td>
            <td>${formatServiceName(dep.service)}</td>
            <td class="pipeline-name">${dep.pipelineName || '-'}</td>
            <td>${formatTimestamp(dep.timestamp)}</td>
            <td>${dep.version || '-'}</td>
            <td><span class="env-badge env-${dep.environment}">${dep.environment}</span></td>
            <td><span class="trigger-badge trigger-${dep.triggerType}">${dep.triggerType}</span></td>
            <td><span class="status-badge status-${dep.status}">${dep.status}</span></td>
            <td>${dep.duration_seconds ? dep.duration_seconds + 's' : '-'}</td>
            <td class="error-cell">${dep.error || '-'}</td>
        </tr>
    `).join('');
}

function formatServiceName(service) {
    return service.replace(/-/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
}

function formatTimestamp(ts) {
    return new Date(ts).toLocaleDateString('en-US', {
        month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
    });
}

function destroyChart(id) {
    if (chartInstances[id]) {
        chartInstances[id].destroy();
        delete chartInstances[id];
    }
}

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', loadDashboard);
