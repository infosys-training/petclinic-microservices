/**
 * PetClinic Microservices - Deployment Dashboard
 * Reads deployment data from a configurable JSON source and renders charts/tables.
 */

const CONFIG = {
    defaultDataSource: 'data/deployments.json',
    colors: {
        primary: '#2563eb',
        success: '#16a34a',
        danger: '#dc2626',
        warning: '#d97706',
        info: '#0891b2',
        palette: [
            '#2563eb', '#7c3aed', '#db2777', '#ea580c',
            '#16a34a', '#0891b2', '#4f46e5', '#c026d3'
        ]
    }
};

let chartInstances = {};

async function loadData() {
    const dataSource = document.getElementById('data-source').value || CONFIG.defaultDataSource;

    try {
        const response = await fetch(dataSource);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        const data = await response.json();
        renderDashboard(data);
    } catch (error) {
        console.error('Failed to load deployment data:', error);
        alert(`Failed to load data from "${dataSource}".\n\nError: ${error.message}\n\nMake sure the file exists and is valid JSON.`);
    }
}

function renderDashboard(data) {
    const deployments = data.deployments || [];
    const services = data.services || [...new Set(deployments.map(d => d.service))];

    updateSummaryCards(deployments);
    renderPerServiceChart(deployments, services);
    renderSuccessFailureChart(deployments, services);
    renderTrendsChart(deployments);
    renderFailureRateChart(deployments, services);
    renderStatusDistributionChart(deployments);
    renderTable(deployments);
}

function updateSummaryCards(deployments) {
    const total = deployments.length;
    const successful = deployments.filter(d => d.status === 'success').length;
    const failed = deployments.filter(d => d.status === 'failed').length;
    const rate = total > 0 ? ((successful / total) * 100).toFixed(1) : 0;

    document.getElementById('total-deployments').textContent = total;
    document.getElementById('successful-deployments').textContent = successful;
    document.getElementById('failed-deployments').textContent = failed;
    document.getElementById('success-rate').textContent = `${rate}%`;
}

function renderPerServiceChart(deployments, services) {
    const ctx = document.getElementById('chart-per-service').getContext('2d');

    const counts = services.map(service =>
        deployments.filter(d => d.service === service).length
    );

    destroyChart('chart-per-service');
    chartInstances['chart-per-service'] = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: services.map(formatServiceName),
            datasets: [{
                label: 'Total Deployments',
                data: counts,
                backgroundColor: CONFIG.colors.palette.slice(0, services.length),
                borderRadius: 6,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { stepSize: 1 }
                },
                x: {
                    ticks: { maxRotation: 45 }
                }
            }
        }
    });
}

function renderSuccessFailureChart(deployments, services) {
    const ctx = document.getElementById('chart-success-failure').getContext('2d');

    const successCounts = services.map(service =>
        deployments.filter(d => d.service === service && d.status === 'success').length
    );
    const failedCounts = services.map(service =>
        deployments.filter(d => d.service === service && d.status === 'failed').length
    );

    destroyChart('chart-success-failure');
    chartInstances['chart-success-failure'] = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: services.map(formatServiceName),
            datasets: [
                {
                    label: 'Success',
                    data: successCounts,
                    backgroundColor: CONFIG.colors.success,
                    borderRadius: 6,
                    borderSkipped: false
                },
                {
                    label: 'Failed',
                    data: failedCounts,
                    backgroundColor: CONFIG.colors.danger,
                    borderRadius: 6,
                    borderSkipped: false
                }
            ]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: 'top' }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { stepSize: 1 },
                    stacked: true
                },
                x: {
                    stacked: true,
                    ticks: { maxRotation: 45 }
                }
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
        if (!dateGroups[date]) {
            dateGroups[date] = { success: 0, failed: 0, total: 0 };
        }
        dateGroups[date].total++;
        if (dep.status === 'success') dateGroups[date].success++;
        else dateGroups[date].failed++;
    });

    const dates = Object.keys(dateGroups);
    const successData = dates.map(d => dateGroups[d].success);
    const failedData = dates.map(d => dateGroups[d].failed);
    const totalData = dates.map(d => dateGroups[d].total);

    destroyChart('chart-trends');
    chartInstances['chart-trends'] = new Chart(ctx, {
        type: 'line',
        data: {
            labels: dates,
            datasets: [
                {
                    label: 'Total',
                    data: totalData,
                    borderColor: CONFIG.colors.primary,
                    backgroundColor: 'rgba(37, 99, 235, 0.1)',
                    fill: true,
                    tension: 0.3,
                    pointRadius: 5,
                    pointHoverRadius: 7
                },
                {
                    label: 'Successful',
                    data: successData,
                    borderColor: CONFIG.colors.success,
                    backgroundColor: 'rgba(22, 163, 74, 0.1)',
                    fill: true,
                    tension: 0.3,
                    pointRadius: 5,
                    pointHoverRadius: 7
                },
                {
                    label: 'Failed',
                    data: failedData,
                    borderColor: CONFIG.colors.danger,
                    backgroundColor: 'rgba(220, 38, 38, 0.1)',
                    fill: true,
                    tension: 0.3,
                    pointRadius: 5,
                    pointHoverRadius: 7
                }
            ]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: 'top' }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { stepSize: 1 }
                },
                x: {
                    ticks: { maxRotation: 45 }
                }
            }
        }
    });
}

function renderFailureRateChart(deployments, services) {
    const ctx = document.getElementById('chart-failure-rate').getContext('2d');

    const failureRates = services.map(service => {
        const serviceDeployments = deployments.filter(d => d.service === service);
        const failed = serviceDeployments.filter(d => d.status === 'failed').length;
        return serviceDeployments.length > 0
            ? ((failed / serviceDeployments.length) * 100).toFixed(1)
            : 0;
    });

    destroyChart('chart-failure-rate');
    chartInstances['chart-failure-rate'] = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: services.map(formatServiceName),
            datasets: [{
                label: 'Failure Rate (%)',
                data: failureRates,
                backgroundColor: failureRates.map(rate =>
                    rate > 25 ? CONFIG.colors.danger :
                    rate > 10 ? CONFIG.colors.warning :
                    CONFIG.colors.success
                ),
                borderRadius: 6,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: 100,
                    ticks: {
                        callback: value => value + '%'
                    }
                },
                x: {
                    ticks: { maxRotation: 45 }
                }
            }
        }
    });
}

function renderStatusDistributionChart(deployments) {
    const ctx = document.getElementById('chart-status-distribution').getContext('2d');

    const successful = deployments.filter(d => d.status === 'success').length;
    const failed = deployments.filter(d => d.status === 'failed').length;

    destroyChart('chart-status-distribution');
    chartInstances['chart-status-distribution'] = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Successful', 'Failed'],
            datasets: [{
                data: [successful, failed],
                backgroundColor: [CONFIG.colors.success, CONFIG.colors.danger],
                borderWidth: 0,
                spacing: 2
            }]
        },
        options: {
            responsive: true,
            cutout: '65%',
            plugins: {
                legend: { position: 'bottom' }
            }
        }
    });
}

function renderTable(deployments) {
    const tbody = document.getElementById('deployments-tbody');
    const sorted = [...deployments].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

    tbody.innerHTML = sorted.map(dep => `
        <tr>
            <td><code>${dep.id}</code></td>
            <td>${formatServiceName(dep.service)}</td>
            <td>${formatTimestamp(dep.timestamp)}</td>
            <td>${dep.version || '-'}</td>
            <td><span class="status-badge status-${dep.status}">${dep.status}</span></td>
            <td>${dep.duration_seconds ? dep.duration_seconds + 's' : '-'}</td>
            <td>${dep.error || '-'}</td>
        </tr>
    `).join('');
}

function formatServiceName(service) {
    return service
        .replace(/-/g, ' ')
        .replace(/\b\w/g, c => c.toUpperCase());
}

function formatTimestamp(ts) {
    const date = new Date(ts);
    return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function destroyChart(id) {
    if (chartInstances[id]) {
        chartInstances[id].destroy();
        delete chartInstances[id];
    }
}

// Load data on page load
document.addEventListener('DOMContentLoaded', loadData);
