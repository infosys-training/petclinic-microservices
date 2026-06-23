"""
Attachment Handler
Downloads and manages Excel attachments from emails.
"""

import os
import logging
from pathlib import Path
from datetime import datetime

from exchangelib import FileAttachment

logger = logging.getLogger(__name__)


class AttachmentHandler:
    """Handles downloading and organizing email attachments."""

    def __init__(self, config: dict):
        self.config = config
        self.download_dir = Path(config["output"]["download_dir"])
        self.allowed_extensions = config["email_filter"].get(
            "attachment_extensions", [".xlsx", ".xls"]
        )
        self._ensure_directories()

    def _ensure_directories(self):
        """Create output directories if they don't exist."""
        self.download_dir.mkdir(parents=True, exist_ok=True)

    def extract_attachments(self, emails: list) -> list:
        """
        Extract Excel attachments from a list of emails.

        Returns a list of dicts with metadata about each downloaded file:
        [
            {
                "file_path": "/path/to/file.xlsx",
                "original_name": "RLN_Report.xlsx",
                "email_subject": "RLN Payment - Jan 2024",
                "email_sender": "sender@company.com",
                "email_date": datetime(...),
                "email_id": "...",
            }
        ]
        """
        downloaded_files = []

        for email_item in emails:
            for attachment in email_item.attachments:
                if not isinstance(attachment, FileAttachment):
                    continue

                if not self._is_valid_extension(attachment.name):
                    logger.debug(
                        "Skipping attachment '%s' - not a valid extension",
                        attachment.name,
                    )
                    continue

                file_path = self._save_attachment(attachment, email_item)
                if file_path:
                    downloaded_files.append(
                        {
                            "file_path": str(file_path),
                            "original_name": attachment.name,
                            "email_subject": email_item.subject,
                            "email_sender": str(email_item.sender.email_address),
                            "email_date": email_item.datetime_received,
                            "email_id": email_item.message_id,
                        }
                    )

        logger.info("Downloaded %d valid attachments from %d emails",
                    len(downloaded_files), len(emails))
        return downloaded_files

    def _is_valid_extension(self, filename: str) -> bool:
        """Check if file extension is in allowed list."""
        if not filename:
            return False
        ext = os.path.splitext(filename)[1].lower()
        return ext in self.allowed_extensions

    def _save_attachment(self, attachment: FileAttachment, email_item) -> Path:
        """Save attachment to disk with a unique timestamped name."""
        try:
            # Create a unique filename: timestamp_originalname
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            safe_name = self._sanitize_filename(attachment.name)
            filename = f"{timestamp}_{safe_name}"
            file_path = self.download_dir / filename

            with open(file_path, "wb") as f:
                f.write(attachment.content)

            logger.info("Saved attachment: %s", file_path)
            return file_path

        except Exception as e:
            logger.error(
                "Failed to save attachment '%s' from email '%s': %s",
                attachment.name,
                email_item.subject,
                str(e),
            )
            return None

    def _sanitize_filename(self, filename: str) -> str:
        """Remove/replace characters that are unsafe for filenames."""
        unsafe_chars = '<>:"/\\|?*'
        for char in unsafe_chars:
            filename = filename.replace(char, "_")
        return filename

    def cleanup_old_files(self, days_to_keep: int = 30):
        """Remove downloaded files older than specified days."""
        cutoff = datetime.now().timestamp() - (days_to_keep * 86400)
        removed = 0

        for file_path in self.download_dir.iterdir():
            if file_path.is_file() and file_path.stat().st_mtime < cutoff:
                file_path.unlink()
                removed += 1

        if removed:
            logger.info("Cleaned up %d old files from downloads directory", removed)
