# PetClinic Microservices - Deployment Dashboard

A lightweight deployment monitoring dashboard for the PetClinic Microservices project, designed to integrate with **Harness CI/CD**. Built with vanilla HTML, CSS, and JavaScript using [Chart.js](https://www.chartjs.org/) for visualizations.

## Features

- **Summary Cards**: Total, successful, failed, and running deployments with success rate
- **Per-Service Breakdown**: Deployment counts for all 8 microservices
- **Success/Failure/Running Stacked Chart**: Status distribution by service
- **Deployment Trends**: 30-day line chart showing daily deployment activity
- **Failure Rate Analysis**: Identify services with high failure rates
- **Environment Breakdown**: Deployments by dev/staging/prod
- **Trigger Type Distribution**: Webhook, manual, and scheduled triggers
- **Recent Deployments Table**: Detailed view with pipeline names, environments, triggers, and error messages
- **Interactive Filters**: Filter by environment and trigger type
- **Abstracted Data Layer**: `HarnessDataService` module for easy API integration

## Quick Start

```bash
cd deployment-dashboard
python3 -m http.server 8000
# Open http://localhost:8000
```

Or use any static file server (`npx serve`, nginx, etc.).

## Architecture

```
deployment-dashboard/
├── index.html                      # Dashboard page
├── styles.css                      # UI styles
├── dashboard.js                    # Chart rendering & UI logic
├── services/
│   └── harness-data-service.js     # Data abstraction layer (swap mock for real API here)
└── README.md
```

### Data Flow

```
HarnessDataService (services/harness-data-service.js)
       │
       ├── config.useMockData = true  → generates realistic mock data
       │
       └── config.useMockData = false → calls Harness Pipeline Execution API
              │
              └── requires: apiKey, accountId, orgIdentifier, projectIdentifier
```

## Connecting to Real Harness API

Only **one file** needs to change: `services/harness-data-service.js`

1. Open the file and update the `config` object:

```javascript
const config = {
    useMockData: false,                           // ← Switch to real API
    baseUrl: 'https://app.harness.io',            // or your Harness instance URL
    apiKey: 'YOUR_HARNESS_API_KEY',               // x-api-key
    accountId: 'YOUR_ACCOUNT_ID',                 // Harness account identifier
    orgIdentifier: 'your_org',                    // Harness org
    projectIdentifier: 'petclinic_microservices', // Harness project
};
```

2. That's it. The dashboard will automatically fetch real pipeline execution data from Harness.

### Required Harness Permissions

The API key needs these permissions:
- `pipeline: execute` (read-only access to pipeline executions)
- `project: viewer` (view project resources)

### Harness API Reference

The service uses the [Pipeline Execution Summary API](https://apidocs.harness.io/tag/Pipeline-Execution-Details#operation/getListOfExecutions):
```
POST /pipeline/api/pipelines/execution/summary?accountIdentifier={accountId}&orgIdentifier={org}&projectIdentifier={project}
```

## Mock Data Details

The mock data generator simulates:
- **8 services**: customers-service, vets-service, visits-service, api-gateway, config-server, discovery-server, genai-service, admin-server
- **30 days** of deployment history
- **~85% success rate** (slightly lower for genai-service)
- **3 environments**: dev (40%), staging (30%), prod (30%)
- **3 trigger types**: webhook (55%), manual (25%), scheduled (20%)
- **Realistic pipeline names**: e.g., `deploy-customers-pipeline`
- **Varied deployment durations** and **realistic error messages**
- **Seeded random** for reproducible data across refreshes

## Extending the Dashboard

### Adding a new data source

Implement the same interface as `HarnessDataService`:

```javascript
const MyDataService = {
    async getDeployments(options) {
        // options: { startDate, endDate, services, environments }
        // Return: { metadata: {...}, deployments: [...] }
    },
    async getDeploymentStats(options) { ... },
    async getDeploymentTrends(options) { ... },
};
```

### Deployment record schema

Each deployment object has:

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Unique execution ID |
| `service` | string | Service name (e.g., `customers-service`) |
| `pipelineName` | string | Harness pipeline identifier |
| `timestamp` | ISO 8601 | Deployment start time |
| `endTimestamp` | ISO 8601 | Deployment end time |
| `status` | string | `success`, `failed`, or `running` |
| `environment` | string | `dev`, `staging`, or `prod` |
| `triggerType` | string | `webhook`, `manual`, or `scheduled` |
| `version` | string | Build version |
| `duration_seconds` | number | Duration in seconds |
| `error` | string/null | Error message for failed deployments |

## Browser Support

Works in all modern browsers (Chrome, Firefox, Safari, Edge). No build step required.
