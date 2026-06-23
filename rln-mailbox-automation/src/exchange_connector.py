"""
Exchange Mailbox Connector
Handles authentication and connection to Microsoft Exchange server.
"""

import os
import logging
from datetime import datetime, timedelta

from exchangelib import (
    Account,
    Configuration,
    Credentials,
    DELEGATE,
    Build,
    Version,
    Identity,
    OAuth2Credentials,
    OAuth2AuthorizationCodeCredentials,
)
from exchangelib.protocol import BaseProtocol

logger = logging.getLogger(__name__)


class ExchangeConnector:
    """Manages connection to an Exchange mailbox."""

    def __init__(self, config: dict):
        self.config = config
        self.account = None

    def connect(self) -> "Account":
        """Establish connection to Exchange server."""
        mailbox_cfg = self.config["mailbox"]
        auth_type = mailbox_cfg.get("auth_type", "basic")

        if auth_type == "oauth2":
            self.account = self._connect_oauth2(mailbox_cfg)
        else:
            self.account = self._connect_basic(mailbox_cfg)

        logger.info("Connected to mailbox: %s", mailbox_cfg["email"])
        return self.account

    def _connect_basic(self, mailbox_cfg: dict) -> Account:
        """Connect using basic authentication (username/password)."""
        password = os.environ.get("RLN_MAILBOX_PASSWORD")
        if not password:
            raise EnvironmentError(
                "RLN_MAILBOX_PASSWORD environment variable not set. "
                "Set it with: export RLN_MAILBOX_PASSWORD='your_password'"
            )

        credentials = Credentials(
            username=mailbox_cfg["email"],
            password=password,
        )

        config = Configuration(
            server=mailbox_cfg["server"],
            credentials=credentials,
        )

        return Account(
            primary_smtp_address=mailbox_cfg["email"],
            config=config,
            autodiscover=False,
            access_type=DELEGATE,
        )

    def _connect_oauth2(self, mailbox_cfg: dict) -> Account:
        """Connect using OAuth2 (recommended for Office 365)."""
        client_id = mailbox_cfg.get("client_id")
        tenant_id = mailbox_cfg.get("tenant_id")
        client_secret_env = mailbox_cfg.get("client_secret_env", "RLN_OAUTH_CLIENT_SECRET")
        client_secret = os.environ.get(client_secret_env)

        if not all([client_id, tenant_id, client_secret]):
            raise EnvironmentError(
                f"OAuth2 requires client_id, tenant_id in config and "
                f"{client_secret_env} environment variable set."
            )

        credentials = OAuth2Credentials(
            client_id=client_id,
            client_secret=client_secret,
            tenant_id=tenant_id,
            identity=Identity(primary_smtp_address=mailbox_cfg["email"]),
        )

        config = Configuration(
            server=mailbox_cfg["server"],
            credentials=credentials,
        )

        return Account(
            primary_smtp_address=mailbox_cfg["email"],
            config=config,
            autodiscover=False,
            access_type=DELEGATE,
        )

    def fetch_emails(self) -> list:
        """Fetch emails matching the configured filter criteria."""
        if not self.account:
            raise RuntimeError("Not connected. Call connect() first.")

        filter_cfg = self.config["email_filter"]
        folder_name = filter_cfg.get("folder", "Inbox")
        days_back = filter_cfg.get("days_back", 7)
        subject_contains = filter_cfg.get("subject_contains", "")
        sender_filter = filter_cfg.get("sender_filter", "")

        # Get the target folder
        folder = self._get_folder(folder_name)

        # Calculate date range
        since_date = datetime.now() - timedelta(days=days_back)

        # Build query
        query = folder.filter(datetime_received__gte=since_date)

        if subject_contains:
            query = query.filter(subject__contains=subject_contains)

        if sender_filter:
            query = query.filter(sender__contains=sender_filter)

        # Only get emails that have attachments
        query = query.filter(has_attachments=True)

        emails = list(query.order_by("-datetime_received"))
        logger.info(
            "Found %d emails matching criteria (subject contains '%s', last %d days)",
            len(emails),
            subject_contains,
            days_back,
        )
        return emails

    def _get_folder(self, folder_name: str):
        """Resolve folder by name."""
        if folder_name.lower() == "inbox":
            return self.account.inbox
        elif folder_name.lower() == "sent":
            return self.account.sent
        else:
            # Try to find custom folder
            return self.account.root / "Top of Information Store" / folder_name

    def disconnect(self):
        """Clean up connection."""
        if self.account:
            self.account.protocol.close()
            logger.info("Disconnected from mailbox.")
