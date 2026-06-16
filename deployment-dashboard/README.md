# PetClinic Microservices - Deployment Dashboard

A lightweight, standalone deployment monitoring dashboard for the PetClinic Microservices project. Built with vanilla HTML, CSS, and JavaScript using [Chart.js](https://www.chartjs.org/) for visualizations.

## Features

- **Summary Cards**: Total deployments, successful, failed, and success rate at a glance
- **Per-Service Breakdown**: Bar chart showing deployment counts per microservice
- **Success vs Failure Stacked Chart**: Visualize success/failure distribution by service
- **Deployment Trends**: Line chart showing deployment activity over time
- **Failure Rate Analysis**: Identify services with high failure rates
- **Status Distribution**: Doughnut chart for overall success/failure ratio
- **Recent Deployments Table**: Sortable table with deployment details and error messages
- **Configurable Data Source**: Load data from any JSON file or API endpoint

## Quick Start

### Option 1: Open directly in a browser

```bash
cd deployment-dashboard
# Use any local HTTP server (required for JSON fetch)
python3 -m http.server 8000
# Then open http://localhost:8000
```

### Option 2: Use any static file server

```bash
npx serve deployment-dashboard
```

## Data Source Configuration

The dashboard reads deployment data from a configurable JSON source. By default, it loads from `data/deployments.json`.

You can change the data source at runtime by entering a URL or file path in the header input field and clicking **Load**.

### JSON Schema

The data source must conform to this structure:

```json
{
  "metadata": {
    "project": "petclinic-microservices",
    "generated_at": "2026-06-16T14:00:00Z",
    "data_source": "CI/CD Pipeline"
  },
  "services": [
    "api-gateway",
    "customers-service",
    "vets-service"
  ],
  "deployments": [
    {
      "id": "dep-001",
      "service": "api-gateway",
      "timestamp": "2026-06-01T08:30:00Z",
      "status": "success",
      "version": "4.0.1",
      "environment": "production",
      "duration_seconds": 120,
      "error": null
    }
  ]
}
```

### Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | string | Yes | Unique deployment identifier |
| `service` | string | Yes | Service name (e.g., `api-gateway`) |
| `timestamp` | string (ISO 8601) | Yes | When the deployment occurred |
| `status` | string | Yes | `"success"` or `"failed"` |
| `version` | string | No | Deployed version |
| `environment` | string | No | Target environment |
| `duration_seconds` | number | No | Deployment duration |
| `error` | string | No | Error message (for failed deployments) |

## Integrating with CI/CD

To populate real deployment data, you can:

1. **GitHub Actions**: Add a step that appends to `deployments.json` after each deployment
2. **Jenkins**: Use a post-build action to write deployment records
3. **Custom API**: Point the dashboard to an API endpoint that returns the JSON schema above
4. **Database**: Use a lightweight API layer to query a database and return the expected format

### Example GitHub Actions integration

```yaml
- name: Record deployment
  run: |
    echo '{"id":"dep-${{ github.run_id }}","service":"${{ matrix.service }}","timestamp":"'$(date -u +%Y-%m-%dT%H:%M:%SZ)'","status":"${{ job.status }}","version":"${{ github.sha }}"}' >> deployment-dashboard/data/deployments.json
```

## Project Structure

```
deployment-dashboard/
├── index.html          # Main dashboard page
├── styles.css          # Dashboard styles
├── dashboard.js        # Chart rendering and data loading logic
├── data/
│   └── deployments.json  # Sample deployment data
└── README.md           # This file
```

## Browser Support

Works in all modern browsers (Chrome, Firefox, Safari, Edge). No build step required.
