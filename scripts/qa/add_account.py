"""Adds or replaces one account in qa.credentials.json, asking for the password without echoing it.

    python3 scripts/qa/add_account.py R playreview

The agent never writes this file (R11): the human runs this and types the password.
"""
import getpass
import json
import os
import sys

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
PATH = os.path.join(ROOT, "qa.credentials.json")

key, username = sys.argv[1], sys.argv[2]
password = getpass.getpass(f"Password for {username}: ")
creds = json.load(open(PATH)) if os.path.exists(PATH) else {}
creds[key] = {"username": username, "password": password}
fd = os.open(PATH, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
with os.fdopen(fd, "w") as fh:
    json.dump(creds, fh, indent=2)
print(f"{key} saved ({username}); accounts now: {', '.join(sorted(creds))}")
