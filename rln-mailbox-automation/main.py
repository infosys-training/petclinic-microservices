"""
RLN Mailbox Automation - Main Entry Point
==========================================
Automates reading Exchange mailbox for RLN (Remittance Letter Notes),
downloads Excel attachments, and validates them against configurable rules.

Usage:
    python main.py                          # Run with default config.yaml
    python main.py --config custom.yaml     # Run with custom config
    python main.py --dry-run                # Validate config without connecting
    python main.py --validate-only file.xlsx # Validate a local file directly
"""

import argparse
import logging
import sys
from pathlib import Path

import yaml

from src.exchange_connector import ExchangeConnector
from src.attachment_handler import AttachmentHandler
from src.validation_engine import ValidationEngine
from src.report_generator import ReportGenerator

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler("rln_automation.log"),
    ],
)
logger = logging.getLogger(__name__)


def load_config(config_path: str) -> dict:
    """Load and validate configuration from YAML file."""
    path = Path(config_path)
    if not path.exists():
        raise FileNotFoundError(f"Config file not found: {config_path}")

    with open(path, "r") as f:
        config = yaml.safe_load(f)

    # Basic validation
    required_sections = ["mailbox", "email_filter", "output", "validation_rules"]
    for section in required_sections:
        if section not in config:
            raise ValueError(f"Missing required config section: '{section}'")

    return config


def run_full_pipeline(config: dict):
    """Execute the complete RLN validation pipeline."""
    logger.info("=" * 60)
    logger.info("RLN Mailbox Automation - Starting Pipeline")
    logger.info("=" * 60)

    # Step 1: Connect to Exchange
    logger.info("Step 1: Connecting to Exchange mailbox...")
    connector = ExchangeConnector(config)
    connector.connect()

    try:
        # Step 2: Fetch matching emails
        logger.info("Step 2: Fetching RLN emails...")
        emails = connector.fetch_emails()

        if not emails:
            logger.info("No matching emails found. Nothing to process.")
            return

        # Step 3: Download attachments
        logger.info("Step 3: Downloading Excel attachments...")
        handler = AttachmentHandler(config)
        downloaded_files = handler.extract_attachments(emails)

        if not downloaded_files:
            logger.info("No valid Excel attachments found in matching emails.")
            return

        # Step 4: Validate each attachment
        logger.info("Step 4: Validating attachments...")
        engine = ValidationEngine(config)
        report_gen = ReportGenerator(config)

        all_results = []
        for file_info in downloaded_files:
            logger.info("Validating: %s", file_info["original_name"])
            results = engine.validate_file(file_info["file_path"])
            all_results.append((file_info, results))

            # Generate individual report
            report_path = report_gen.generate_report(file_info, results)
            logger.info("Report saved: %s", report_path)

            # Log summary
            passed = sum(1 for r in results if r.passed)
            failed = len(results) - passed
            status = "PASS" if failed == 0 else "FAIL"
            logger.info(
                "  Result: %s (%d/%d rules passed)",
                status, passed, len(results),
            )

        # Step 5: Generate consolidated report
        logger.info("Step 5: Generating consolidated report...")
        consolidated_path = report_gen.generate_consolidated_report(all_results)
        logger.info("Consolidated report: %s", consolidated_path)

        # Print final summary
        logger.info("=" * 60)
        logger.info("PIPELINE COMPLETE")
        logger.info("Files processed: %d", len(all_results))
        total_pass = sum(1 for _, r in all_results if all(x.passed for x in r))
        total_fail = len(all_results) - total_pass
        logger.info("Files passed all rules: %d", total_pass)
        logger.info("Files with failures: %d", total_fail)
        logger.info("=" * 60)

    finally:
        connector.disconnect()


def validate_local_file(config: dict, file_path: str):
    """Validate a local Excel file without connecting to mailbox."""
    logger.info("Validating local file: %s", file_path)

    if not Path(file_path).exists():
        logger.error("File not found: %s", file_path)
        sys.exit(1)

    engine = ValidationEngine(config)
    results = engine.validate_file(file_path)

    report_gen = ReportGenerator(config)
    metadata = {
        "file_path": file_path,
        "original_name": Path(file_path).name,
        "email_subject": "Local file validation",
        "email_sender": "N/A",
        "email_date": "N/A",
    }
    report_path = report_gen.generate_report(metadata, results)

    # Print results to console
    print("\n" + "=" * 60)
    print(f"VALIDATION RESULTS: {Path(file_path).name}")
    print("=" * 60)
    for r in results:
        status = "✓ PASS" if r.passed else "✗ FAIL"
        print(f"  {status} | {r.rule_name}")
        if not r.passed:
            print(f"         {r.details}")
    print("=" * 60)
    passed = sum(1 for r in results if r.passed)
    print(f"Overall: {passed}/{len(results)} rules passed")
    print(f"Report saved: {report_path}")
    print("=" * 60)


def dry_run(config: dict):
    """Validate configuration without connecting to the mailbox."""
    print("\n--- DRY RUN: Configuration Validation ---\n")
    print(f"Mailbox: {config['mailbox']['email']}")
    print(f"Server: {config['mailbox']['server']}")
    print(f"Auth type: {config['mailbox'].get('auth_type', 'basic')}")
    print(f"Folder: {config['email_filter']['folder']}")
    print(f"Subject filter: '{config['email_filter']['subject_contains']}'")
    print(f"Days back: {config['email_filter']['days_back']}")
    print(f"Allowed extensions: {config['email_filter']['attachment_extensions']}")
    print(f"Report format: {config['output']['report_format']}")
    print(f"\nValidation rules ({len(config['validation_rules'])}):")
    for i, rule in enumerate(config["validation_rules"], 1):
        print(f"  {i}. [{rule['type']}] {rule['name']}")
    print("\n--- Configuration is valid! ---")


def main():
    parser = argparse.ArgumentParser(
        description="RLN Mailbox Automation - Validate Excel attachments from Exchange emails"
    )
    parser.add_argument(
        "--config", "-c",
        default="config.yaml",
        help="Path to configuration file (default: config.yaml)",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Validate configuration without connecting to mailbox",
    )
    parser.add_argument(
        "--validate-only",
        metavar="FILE",
        help="Validate a local Excel file directly (skip mailbox connection)",
    )
    parser.add_argument(
        "--verbose", "-v",
        action="store_true",
        help="Enable verbose/debug logging",
    )

    args = parser.parse_args()

    if args.verbose:
        logging.getLogger().setLevel(logging.DEBUG)

    try:
        config = load_config(args.config)
    except (FileNotFoundError, ValueError) as e:
        logger.error("Configuration error: %s", e)
        sys.exit(1)

    if args.dry_run:
        dry_run(config)
    elif args.validate_only:
        validate_local_file(config, args.validate_only)
    else:
        run_full_pipeline(config)


if __name__ == "__main__":
    main()
