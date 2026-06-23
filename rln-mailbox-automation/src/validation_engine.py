"""
Validation Engine
Applies configurable rules to Excel attachments and produces validation reports.
"""

import re
import logging
from datetime import datetime
from pathlib import Path

import pandas as pd
import openpyxl

logger = logging.getLogger(__name__)


class ValidationResult:
    """Stores the result of a single validation rule check."""

    def __init__(self, rule_name: str, passed: bool, details: str = "", failing_rows: list = None):
        self.rule_name = rule_name
        self.passed = passed
        self.details = details
        self.failing_rows = failing_rows or []

    def __repr__(self):
        status = "PASS" if self.passed else "FAIL"
        return f"[{status}] {self.rule_name}: {self.details}"


class ValidationEngine:
    """Applies validation rules to Excel files."""

    def __init__(self, config: dict):
        self.config = config
        self.rules = config.get("validation_rules", [])

    def validate_file(self, file_path: str) -> list:
        """
        Validate a single Excel file against all configured rules.

        Returns a list of ValidationResult objects.
        """
        results = []

        try:
            df = pd.read_excel(file_path, engine="openpyxl")
        except Exception as e:
            results.append(ValidationResult(
                rule_name="File Readability",
                passed=False,
                details=f"Cannot read Excel file: {str(e)}",
            ))
            return results

        logger.info("Validating file: %s (%d rows, %d columns)",
                    file_path, len(df), len(df.columns))

        for rule in self.rules:
            rule_type = rule["type"]
            rule_name = rule["name"]
            params = rule.get("params", {})

            try:
                result = self._apply_rule(df, rule_type, rule_name, params)
                results.append(result)
            except Exception as e:
                results.append(ValidationResult(
                    rule_name=rule_name,
                    passed=False,
                    details=f"Rule execution error: {str(e)}",
                ))

        return results

    def _apply_rule(self, df: pd.DataFrame, rule_type: str, rule_name: str, params: dict) -> ValidationResult:
        """Route to the appropriate validation method based on rule type."""
        validators = {
            "required_columns": self._validate_required_columns,
            "no_empty_cells": self._validate_no_empty_cells,
            "numeric_range": self._validate_numeric_range,
            "date_format": self._validate_date_format,
            "unique_values": self._validate_unique_values,
            "regex_match": self._validate_regex_match,
            "sum_check": self._validate_sum_check,
            "cross_reference": self._validate_cross_reference,
            "row_count": self._validate_row_count,
            "value_in_list": self._validate_value_in_list,
        }

        validator = validators.get(rule_type)
        if not validator:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"Unknown rule type: {rule_type}",
            )

        return validator(df, rule_name, params)

    def _validate_required_columns(self, df: pd.DataFrame, rule_name: str, params: dict) -> ValidationResult:
        """Check that all required columns exist in the DataFrame."""
        required = params.get("columns", [])
        actual_columns = [col.strip() for col in df.columns.tolist()]
        missing = [col for col in required if col not in actual_columns]

        if missing:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"Missing columns: {', '.join(missing)}. Found: {', '.join(actual_columns)}",
            )
        return ValidationResult(
            rule_name=rule_name,
            passed=True,
            details=f"All {len(required)} required columns present.",
        )

    def _validate_no_empty_cells(self, df: pd.DataFrame, rule_name: str, params: dict) -> ValidationResult:
        """Check that specified columns have no null/empty values."""
        columns = params.get("columns", [])
        failing_rows = []

        for col in columns:
            if col not in df.columns:
                continue
            empty_mask = df[col].isna() | (df[col].astype(str).str.strip() == "")
            empty_indices = df[empty_mask].index.tolist()
            for idx in empty_indices:
                failing_rows.append({"row": idx + 2, "column": col, "issue": "Empty/null value"})

        if failing_rows:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"{len(failing_rows)} empty cell(s) found in mandatory columns.",
                failing_rows=failing_rows,
            )
        return ValidationResult(
            rule_name=rule_name,
            passed=True,
            details="No empty cells in mandatory columns.",
        )

    def _validate_numeric_range(self, df: pd.DataFrame, rule_name: str, params: dict) -> ValidationResult:
        """Validate that numeric column values fall within specified range."""
        column = params.get("column")
        min_val = params.get("min")
        max_val = params.get("max")
        failing_rows = []

        if column not in df.columns:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"Column '{column}' not found.",
            )

        numeric_series = pd.to_numeric(df[column], errors="coerce")

        for idx, val in numeric_series.items():
            if pd.isna(val):
                failing_rows.append({"row": idx + 2, "column": column, "issue": "Non-numeric value"})
                continue
            if min_val is not None and val < min_val:
                failing_rows.append({"row": idx + 2, "column": column, "issue": f"Value {val} < min {min_val}"})
            if max_val is not None and val > max_val:
                failing_rows.append({"row": idx + 2, "column": column, "issue": f"Value {val} > max {max_val}"})

        if failing_rows:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"{len(failing_rows)} value(s) outside allowed range.",
                failing_rows=failing_rows,
            )
        return ValidationResult(
            rule_name=rule_name,
            passed=True,
            details=f"All values in '{column}' within range.",
        )

    def _validate_date_format(self, df: pd.DataFrame, rule_name: str, params: dict) -> ValidationResult:
        """Validate that date column matches expected format."""
        column = params.get("column")
        date_format = params.get("format", "%Y-%m-%d")
        failing_rows = []

        if column not in df.columns:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"Column '{column}' not found.",
            )

        for idx, val in df[column].items():
            if pd.isna(val):
                failing_rows.append({"row": idx + 2, "column": column, "issue": "Empty date"})
                continue
            try:
                if isinstance(val, datetime):
                    continue  # Already a valid datetime
                datetime.strptime(str(val).strip(), date_format)
            except ValueError:
                failing_rows.append({
                    "row": idx + 2,
                    "column": column,
                    "issue": f"Invalid date format: '{val}' (expected {date_format})",
                })

        if failing_rows:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"{len(failing_rows)} invalid date(s) in '{column}'.",
                failing_rows=failing_rows,
            )
        return ValidationResult(
            rule_name=rule_name,
            passed=True,
            details=f"All dates in '{column}' are valid.",
        )

    def _validate_unique_values(self, df: pd.DataFrame, rule_name: str, params: dict) -> ValidationResult:
        """Ensure no duplicate values in specified columns."""
        columns = params.get("columns", [])
        failing_rows = []

        for col in columns:
            if col not in df.columns:
                continue
            duplicates = df[df[col].duplicated(keep=False)]
            if not duplicates.empty:
                dup_values = duplicates[col].unique().tolist()
                for idx in duplicates.index:
                    failing_rows.append({
                        "row": idx + 2,
                        "column": col,
                        "issue": f"Duplicate value: '{df.at[idx, col]}'",
                    })

        if failing_rows:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"{len(failing_rows)} duplicate(s) found.",
                failing_rows=failing_rows,
            )
        return ValidationResult(
            rule_name=rule_name,
            passed=True,
            details="All values are unique.",
        )

    def _validate_regex_match(self, df: pd.DataFrame, rule_name: str, params: dict) -> ValidationResult:
        """Validate that column values match a regex pattern."""
        column = params.get("column")
        pattern = params.get("pattern")
        failing_rows = []

        if column not in df.columns:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"Column '{column}' not found.",
            )

        compiled_pattern = re.compile(pattern)
        for idx, val in df[column].items():
            if pd.isna(val):
                failing_rows.append({"row": idx + 2, "column": column, "issue": "Empty value"})
                continue
            if not compiled_pattern.match(str(val).strip()):
                failing_rows.append({
                    "row": idx + 2,
                    "column": column,
                    "issue": f"Value '{val}' doesn't match pattern '{pattern}'",
                })

        if failing_rows:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"{len(failing_rows)} value(s) don't match pattern.",
                failing_rows=failing_rows,
            )
        return ValidationResult(
            rule_name=rule_name,
            passed=True,
            details=f"All values in '{column}' match pattern.",
        )

    def _validate_sum_check(self, df: pd.DataFrame, rule_name: str, params: dict) -> ValidationResult:
        """Validate that a column's sum matches an expected total."""
        column = params.get("column")
        expected_total = params.get("expected_total")
        tolerance = params.get("tolerance", 0.01)

        if column not in df.columns:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"Column '{column}' not found.",
            )

        actual_total = pd.to_numeric(df[column], errors="coerce").sum()

        if abs(actual_total - expected_total) <= tolerance:
            return ValidationResult(
                rule_name=rule_name,
                passed=True,
                details=f"Column '{column}' sum = {actual_total} (expected {expected_total}).",
            )
        return ValidationResult(
            rule_name=rule_name,
            passed=False,
            details=f"Column '{column}' sum = {actual_total}, expected {expected_total} (tolerance: {tolerance}).",
        )

    def _validate_cross_reference(self, df: pd.DataFrame, rule_name: str, params: dict) -> ValidationResult:
        """Compare values against a reference Excel file."""
        reference_file = params.get("reference_file")
        match_column = params.get("match_column")
        validate_columns = params.get("validate_columns", [])
        failing_rows = []

        if not Path(reference_file).exists():
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"Reference file not found: {reference_file}",
            )

        try:
            ref_df = pd.read_excel(reference_file, engine="openpyxl")
        except Exception as e:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"Cannot read reference file: {str(e)}",
            )

        if match_column not in df.columns or match_column not in ref_df.columns:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"Match column '{match_column}' not found in both files.",
            )

        # Check each row in source against reference
        for idx, row in df.iterrows():
            match_val = row[match_column]
            ref_rows = ref_df[ref_df[match_column] == match_val]
            if ref_rows.empty:
                failing_rows.append({
                    "row": idx + 2,
                    "column": match_column,
                    "issue": f"Value '{match_val}' not found in reference file",
                })
                continue

            for vcol in validate_columns:
                if vcol in df.columns and vcol in ref_df.columns:
                    source_val = row[vcol]
                    ref_val = ref_rows.iloc[0][vcol]
                    if str(source_val) != str(ref_val):
                        failing_rows.append({
                            "row": idx + 2,
                            "column": vcol,
                            "issue": f"Mismatch: source='{source_val}', reference='{ref_val}'",
                        })

        if failing_rows:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"{len(failing_rows)} cross-reference mismatch(es).",
                failing_rows=failing_rows,
            )
        return ValidationResult(
            rule_name=rule_name,
            passed=True,
            details="All values match reference file.",
        )

    def _validate_row_count(self, df: pd.DataFrame, rule_name: str, params: dict) -> ValidationResult:
        """Ensure the file has a minimum/maximum number of rows."""
        min_rows = params.get("min")
        max_rows = params.get("max")
        actual_rows = len(df)

        if min_rows is not None and actual_rows < min_rows:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"Too few rows: {actual_rows} (minimum: {min_rows}).",
            )
        if max_rows is not None and actual_rows > max_rows:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"Too many rows: {actual_rows} (maximum: {max_rows}).",
            )
        return ValidationResult(
            rule_name=rule_name,
            passed=True,
            details=f"Row count ({actual_rows}) within expected range.",
        )

    def _validate_value_in_list(self, df: pd.DataFrame, rule_name: str, params: dict) -> ValidationResult:
        """Ensure column values are from an allowed set."""
        column = params.get("column")
        allowed_values = params.get("allowed_values", [])
        failing_rows = []

        if column not in df.columns:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"Column '{column}' not found.",
            )

        for idx, val in df[column].items():
            if pd.isna(val):
                failing_rows.append({"row": idx + 2, "column": column, "issue": "Empty value"})
                continue
            if str(val).strip() not in allowed_values:
                failing_rows.append({
                    "row": idx + 2,
                    "column": column,
                    "issue": f"Value '{val}' not in allowed list: {allowed_values}",
                })

        if failing_rows:
            return ValidationResult(
                rule_name=rule_name,
                passed=False,
                details=f"{len(failing_rows)} value(s) not in allowed list.",
                failing_rows=failing_rows,
            )
        return ValidationResult(
            rule_name=rule_name,
            passed=True,
            details=f"All values in '{column}' are valid.",
        )
