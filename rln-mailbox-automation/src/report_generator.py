"""
Report Generator
Produces validation reports in Excel, CSV, or JSON format.
"""

import json
import logging
from datetime import datetime
from pathlib import Path

import pandas as pd

from .validation_engine import ValidationResult

logger = logging.getLogger(__name__)


class ReportGenerator:
    """Generates validation reports from validation results."""

    def __init__(self, config: dict):
        self.config = config
        self.report_dir = Path(config["output"]["report_dir"])
        self.report_format = config["output"].get("report_format", "excel")
        self.report_dir.mkdir(parents=True, exist_ok=True)

    def generate_report(self, file_metadata: dict, results: list) -> str:
        """
        Generate a validation report for a single file.

        Args:
            file_metadata: Dict with file info (path, email subject, sender, etc.)
            results: List of ValidationResult objects

        Returns:
            Path to the generated report file.
        """
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        original_name = Path(file_metadata.get("original_name", "unknown")).stem

        if self.report_format == "excel":
            return self._generate_excel_report(file_metadata, results, timestamp, original_name)
        elif self.report_format == "csv":
            return self._generate_csv_report(file_metadata, results, timestamp, original_name)
        elif self.report_format == "json":
            return self._generate_json_report(file_metadata, results, timestamp, original_name)
        else:
            logger.warning("Unknown report format '%s', defaulting to Excel.", self.report_format)
            return self._generate_excel_report(file_metadata, results, timestamp, original_name)

    def _generate_excel_report(self, metadata: dict, results: list, timestamp: str, name: str) -> str:
        """Generate an Excel validation report with summary and details sheets."""
        report_path = self.report_dir / f"validation_report_{name}_{timestamp}.xlsx"

        with pd.ExcelWriter(report_path, engine="openpyxl") as writer:
            # Summary sheet
            summary_data = self._build_summary_data(metadata, results)
            summary_df = pd.DataFrame(summary_data)
            summary_df.to_excel(writer, sheet_name="Summary", index=False)

            # Detailed results sheet
            details_data = self._build_details_data(results)
            if details_data:
                details_df = pd.DataFrame(details_data)
                details_df.to_excel(writer, sheet_name="Rule Results", index=False)

            # Failing rows detail sheet
            failures_data = self._build_failures_data(results)
            if failures_data:
                failures_df = pd.DataFrame(failures_data)
                failures_df.to_excel(writer, sheet_name="Failures Detail", index=False)

        logger.info("Excel report generated: %s", report_path)
        return str(report_path)

    def _generate_csv_report(self, metadata: dict, results: list, timestamp: str, name: str) -> str:
        """Generate a CSV validation report."""
        report_path = self.report_dir / f"validation_report_{name}_{timestamp}.csv"

        details_data = self._build_details_data(results)
        df = pd.DataFrame(details_data)
        df.to_csv(report_path, index=False)

        logger.info("CSV report generated: %s", report_path)
        return str(report_path)

    def _generate_json_report(self, metadata: dict, results: list, timestamp: str, name: str) -> str:
        """Generate a JSON validation report."""
        report_path = self.report_dir / f"validation_report_{name}_{timestamp}.json"

        report = {
            "metadata": {
                "file": metadata.get("original_name"),
                "email_subject": metadata.get("email_subject"),
                "email_sender": metadata.get("email_sender"),
                "email_date": str(metadata.get("email_date", "")),
                "validation_timestamp": datetime.now().isoformat(),
            },
            "summary": {
                "total_rules": len(results),
                "passed": sum(1 for r in results if r.passed),
                "failed": sum(1 for r in results if not r.passed),
                "overall_status": "PASS" if all(r.passed for r in results) else "FAIL",
            },
            "results": [
                {
                    "rule": r.rule_name,
                    "status": "PASS" if r.passed else "FAIL",
                    "details": r.details,
                    "failing_rows": r.failing_rows,
                }
                for r in results
            ],
        }

        with open(report_path, "w") as f:
            json.dump(report, f, indent=2, default=str)

        logger.info("JSON report generated: %s", report_path)
        return str(report_path)

    def _build_summary_data(self, metadata: dict, results: list) -> list:
        """Build summary data for the report."""
        total = len(results)
        passed = sum(1 for r in results if r.passed)
        failed = total - passed
        status = "PASS" if failed == 0 else "FAIL"

        return [
            {"Field": "File Name", "Value": metadata.get("original_name", "N/A")},
            {"Field": "Email Subject", "Value": metadata.get("email_subject", "N/A")},
            {"Field": "Email Sender", "Value": metadata.get("email_sender", "N/A")},
            {"Field": "Email Date", "Value": str(metadata.get("email_date", "N/A"))},
            {"Field": "Validation Date", "Value": datetime.now().strftime("%Y-%m-%d %H:%M:%S")},
            {"Field": "Overall Status", "Value": status},
            {"Field": "Total Rules", "Value": total},
            {"Field": "Passed", "Value": passed},
            {"Field": "Failed", "Value": failed},
        ]

    def _build_details_data(self, results: list) -> list:
        """Build per-rule detail data."""
        return [
            {
                "Rule Name": r.rule_name,
                "Status": "PASS" if r.passed else "FAIL",
                "Details": r.details,
                "Failing Rows Count": len(r.failing_rows),
            }
            for r in results
        ]

    def _build_failures_data(self, results: list) -> list:
        """Build failing rows detail data."""
        failures = []
        for r in results:
            if not r.passed and r.failing_rows:
                for row_info in r.failing_rows:
                    failures.append({
                        "Rule": r.rule_name,
                        "Row Number": row_info.get("row"),
                        "Column": row_info.get("column"),
                        "Issue": row_info.get("issue"),
                    })
        return failures

    def generate_consolidated_report(self, all_results: list) -> str:
        """
        Generate a single consolidated report for all validated files.

        Args:
            all_results: List of tuples (file_metadata, validation_results)

        Returns:
            Path to the consolidated report.
        """
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        report_path = self.report_dir / f"consolidated_report_{timestamp}.xlsx"

        with pd.ExcelWriter(report_path, engine="openpyxl") as writer:
            # Overview sheet
            overview_data = []
            for metadata, results in all_results:
                passed = sum(1 for r in results if r.passed)
                failed = len(results) - passed
                overview_data.append({
                    "File": metadata.get("original_name"),
                    "Email Subject": metadata.get("email_subject"),
                    "Sender": metadata.get("email_sender"),
                    "Date Received": str(metadata.get("email_date", "")),
                    "Rules Passed": passed,
                    "Rules Failed": failed,
                    "Status": "PASS" if failed == 0 else "FAIL",
                })

            overview_df = pd.DataFrame(overview_data)
            overview_df.to_excel(writer, sheet_name="Overview", index=False)

            # All failures sheet
            all_failures = []
            for metadata, results in all_results:
                for r in results:
                    if not r.passed:
                        for row_info in r.failing_rows:
                            all_failures.append({
                                "File": metadata.get("original_name"),
                                "Rule": r.rule_name,
                                "Row": row_info.get("row"),
                                "Column": row_info.get("column"),
                                "Issue": row_info.get("issue"),
                            })

            if all_failures:
                failures_df = pd.DataFrame(all_failures)
                failures_df.to_excel(writer, sheet_name="All Failures", index=False)

        logger.info("Consolidated report generated: %s", report_path)
        return str(report_path)
