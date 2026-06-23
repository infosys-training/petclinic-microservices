# RLN Mailbox Automation

Automates reading an Exchange mailbox for RLN (Remittance Letter Notes), downloads Excel attachments, and validates them against configurable business rules — eliminating the need for manual checking.

## Features

- **Exchange Connectivity**: Connects to Microsoft Exchange/Office 365 via Basic Auth or OAuth2
- **Smart Filtering**: Finds RLN emails by subject, sender, date range, and attachment type
- **Configurable Validation Rules**: 10 built-in rule types covering data integrity checks
- **Detailed Reporting**: Generates Excel/CSV/JSON reports with per-row failure details
- **Consolidated Reports**: Summarizes all validated files in a single overview report
- **Local File Validation**: Test your rules against local Excel files without mailbox access

## Quick Start

### 1. Install Dependencies

```bash
pip install -r requirements.txt
```

### 2. Configure

Edit `config.yaml` to set:
- Your Exchange mailbox connection details
- Email filtering criteria (subject, sender, date range)
- Validation rules for your RLN format

### 3. Set Environment Variables

```bash
# For Basic Auth:
export RLN_MAILBOX_PASSWORD="your_password"

# For OAuth2:
export RLN_OAUTH_CLIENT_SECRET="your_client_secret"
```

### 4. Run

```bash
# Full pipeline: connect → fetch emails → download → validate → report
python main.py

# Dry run (validate config only):
python main.py --dry-run

# Validate a local Excel file directly:
python main.py --validate-only path/to/file.xlsx

# With verbose logging:
python main.py -v
```

## Validation Rule Types

| Rule Type | Description |
|-----------|-------------|
| `required_columns` | Checks that all specified column headers exist |
| `no_empty_cells` | Ensures mandatory columns have no blank/null values |
| `numeric_range` | Validates numbers are within min/max bounds |
| `date_format` | Checks dates match expected format (e.g., YYYY-MM-DD) |
| `unique_values` | Ensures no duplicate values in specified columns |
| `regex_match` | Validates values against a regex pattern |
| `sum_check` | Verifies column sum matches expected total |
| `cross_reference` | Compares values against a reference Excel file |
| `row_count` | Ensures file has expected number of data rows |
| `value_in_list` | Checks values belong to an allowed set |

## Configuration Example

```yaml
validation_rules:
  - name: "Required columns present"
    type: "required_columns"
    params:
      columns: ["RLN Number", "Date", "Amount", "Currency"]

  - name: "No empty RLN numbers"
    type: "no_empty_cells"
    params:
      columns: ["RLN Number", "Amount"]

  - name: "Amount must be positive"
    type: "numeric_range"
    params:
      column: "Amount"
      min: 0.01
      max: null
```

## Output

Reports are generated in `./reports/` with:
- **Summary sheet**: File metadata, overall pass/fail status, rule counts
- **Rule Results sheet**: Per-rule pass/fail with details
- **Failures Detail sheet**: Every failing row with exact issue description
- **Consolidated report**: Overview of all files processed in one run

## Scheduling (Cron)

To run automatically every day at 8 AM:

```bash
0 8 * * * cd /path/to/rln-mailbox-automation && /usr/bin/python3 main.py >> /var/log/rln_automation.log 2>&1
```

## Project Structure

```
rln-mailbox-automation/
├── main.py                    # Entry point / orchestrator
├── config.yaml                # Configuration (rules, mailbox, filters)
├── requirements.txt           # Python dependencies
├── README.md                  # This file
└── src/
    ├── __init__.py
    ├── exchange_connector.py  # Exchange mailbox connection
    ├── attachment_handler.py  # Download & manage attachments
    ├── validation_engine.py   # Rule execution engine
    └── report_generator.py    # Report output (Excel/CSV/JSON)
```

## Extending

To add a custom validation rule:
1. Add the rule method to `src/validation_engine.py` following the `_validate_*` pattern
2. Register it in the `validators` dict within `_apply_rule()`
3. Add the rule to your `config.yaml` with appropriate params

## Authentication Notes

### Basic Auth
- Set `auth_type: "basic"` in config
- Provide password via `RLN_MAILBOX_PASSWORD` env var
- Note: Microsoft is deprecating basic auth for Exchange Online

### OAuth2 (Recommended for Office 365)
- Register an app in Azure AD
- Grant `Mail.Read` permission
- Set `auth_type: "oauth2"` and provide `client_id`, `tenant_id`
- Set client secret via `RLN_OAUTH_CLIENT_SECRET` env var
