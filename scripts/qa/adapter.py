#!/usr/bin/env python3
"""API oracle for the QA campaign (qa-campaign skill, adapters/README.md).

Asks the AdventureLog server what is true, independently of the app.

    scripts/qa/adapter.py GET /auth/user-metadata/
    scripts/qa/adapter.py GET "/api/locations/?page_size=100" --account A
    scripts/qa/adapter.py DELETE /api/locations/<uuid>/

Server: `server.url` in qa.config.json (the Django backend, :3447 — never the web frontend, see
quirks). Accounts: qa.credentials.json, gitignored, shaped {"A": {"username": "...", "password": "..."}}
or {"A": {"sessionToken": "..."}}. The token a password buys is cached in qa.tokens.json (gitignored)
so the password is sent once. Nothing here prints a password or a token.

Server quirks, each of which produced a wrong conclusion once:
- The web frontend (:3445) proxies /auth but strips Set-Cookie: login answers 200 with a user and NO
  session. Always the backend port.
- A session is the `sessionid` cookie; the backend accepts it back as `X-Session-Token`
  (XSessionTokenMiddleware). There is no logout endpoint the app calls.
- /api/locations/all/ returned 3 of 22 places; /api/locations/?page_size=100 returns them all.
- DELETE /api/visitedregion/<numeric id>/ is 404 — it is keyed by region code (AF-BDS).
- Date and decimal fields reject "" — send null or omit. `timezone` must be an IANA zone; "UTC" is 400.
- /api/recommendations/ has no list action (404); only /api/recommendations/query/.
- /api/stats/counts/<username>/ answered 404 "No CustomUser matches" for `claude` (private profile),
  with or without a session, slash or not (2026-09-15).
- A missing or ended session is NOT one answer: /api/locations/ gives 200 with an EMPTY list,
  /api/collections/ gives 400 {"error":"User is not authenticated"}, /api/stats/dashboard/ and
  /api/countries/ give 401. Only /auth/user-metadata/ is a reliable "is this session alive?".
- DELETE /auth/browser/v1/auth/session with X-Session-Token revokes that session (answers 401 by
  allauth's convention). The adapter's cached token then answers 401 and `request` logs in again.
"""
import json
import os
import re
import ssl
import sys
import urllib.error
import urllib.request

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
CONFIG = os.path.join(ROOT, "qa.config.json")
CREDENTIALS = os.path.join(ROOT, "qa.credentials.json")
TOKENS = os.path.join(ROOT, "qa.tokens.json")
SECRET_KEY_RE = re.compile(r"password|token|secret|session|cookie|key", re.I)


def base_url():
    try:
        url = json.load(open(CONFIG))["server"]["url"]
    except (OSError, KeyError, ValueError) as e:
        raise SystemExit(f"no server.url in {CONFIG}: {e}")
    if url.rstrip("/").endswith(":3445"):
        raise SystemExit("server.url is the web frontend (:3445); the API oracle needs the backend (:3447)")
    return url.rstrip("/")


def account(key="A"):
    """What signs in `key` — from qa.credentials.json. Never printed."""
    try:
        creds = json.load(open(CREDENTIALS))
    except OSError:
        raise SystemExit(f"{CREDENTIALS} does not exist: the human writes it (R11), the agent never does")
    except ValueError as e:
        raise SystemExit(f"{CREDENTIALS} is not valid JSON: {e}")
    if key not in creds:
        raise SystemExit(f"account {key!r} is not in qa.credentials.json (has: {', '.join(sorted(creds)) or 'nothing'})")
    return creds[key]


def _raw(method, url, body=None, headers=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Accept", "application/json")
    req.add_header("X-Is-Mobile", "true")
    if data is not None:
        req.add_header("Content-Type", "application/json")
    for k, v in (headers or {}).items():
        req.add_header(k, v)
    try:
        with urllib.request.urlopen(req, timeout=30, context=ssl.create_default_context()) as res:
            return res.status, res.headers, res.read()
    except urllib.error.HTTPError as e:
        return e.code, e.headers, e.read()


def _parse(raw):
    text = raw.decode(errors="replace")
    try:
        return json.loads(text) if text else None
    except ValueError:
        return text


def _cached_tokens():
    try:
        return json.load(open(TOKENS))
    except (OSError, ValueError):
        return {}


def token(key="A", fresh=False):
    """The session token for `key`: sessionToken from the credentials, the cached one, or a login."""
    acc = account(key)
    if acc.get("sessionToken"):
        return acc["sessionToken"]
    cache = _cached_tokens()
    url = base_url()
    if not fresh and cache.get(key, {}).get("server") == url and cache[key].get("token"):
        return cache[key]["token"]
    if not (acc.get("username") and acc.get("password")):
        raise SystemExit(f"account {key!r} needs username+password or sessionToken in qa.credentials.json")
    status, headers, raw = _raw("POST", f"{url}/auth/browser/v1/auth/login",
                                {"username": acc["username"], "password": acc["password"]},
                                {"Referer": url})
    if status != 200:
        raise SystemExit(f"login as {key} answered {status} (body not printed)")
    sid = None
    for cookie in headers.get_all("Set-Cookie") or []:
        m = re.search(r"sessionid=([^;]+)", cookie)
        if m:
            sid = m.group(1)
    if not sid:
        raise SystemExit(f"login as {key} answered 200 with no sessionid cookie — is server.url the backend?")
    cache[key] = {"server": url, "token": sid}
    fd = os.open(TOKENS, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
    with os.fdopen(fd, "w") as fh:
        json.dump(cache, fh)
    return sid


def request(method, path, account="A", params=None, json=None, headers=None):
    """Returns (status, body). Body is parsed JSON when it is JSON, text otherwise.
    `account=None` sends no session (anonymous)."""
    from urllib.parse import urlencode
    url = base_url() + path + (("&" if "?" in path else "?") + urlencode(params) if params else "")
    h = dict(headers or {})
    if account:
        h["X-Session-Token"] = token(account)
    status, _, raw = _raw(method, url, json, h)
    if status in (401, 403) and account and "X-Session-Token" in h and not globals()["account"](account).get("sessionToken"):
        # A cached token can expire: log in again once, then trust the answer.
        h["X-Session-Token"] = token(account, fresh=True)
        status, _, raw = _raw(method, url, json, h)
    return status, _parse(raw)


def upload(path, fields, files, account="A"):
    """multipart/form-data POST: `fields` {name: str}, `files` {name: (filename, bytes, mime)}.
    Returns (status, body). For fixtures only the API can make (images, attachments)."""
    import uuid
    boundary = uuid.uuid4().hex
    parts = []
    for k, v in fields.items():
        parts.append(f'--{boundary}\r\nContent-Disposition: form-data; name="{k}"\r\n\r\n{v}\r\n'.encode())
    for k, (fname, data, mime) in files.items():
        parts.append(f'--{boundary}\r\nContent-Disposition: form-data; name="{k}"; filename="{fname}"\r\n'
                     f'Content-Type: {mime}\r\n\r\n'.encode() + data + b"\r\n")
    body = b"".join(parts) + f"--{boundary}--\r\n".encode()
    req = urllib.request.Request(base_url() + path, data=body, method="POST")
    req.add_header("Content-Type", f"multipart/form-data; boundary={boundary}")
    req.add_header("Accept", "application/json")
    req.add_header("X-Session-Token", token(account))
    try:
        with urllib.request.urlopen(req, timeout=60, context=ssl.create_default_context()) as res:
            return res.status, _parse(res.read())
    except urllib.error.HTTPError as e:
        return e.code, _parse(e.read())


def redact(value):
    """For printing only: hides values under secret-looking keys."""
    if isinstance(value, dict):
        return {k: ("<hidden>" if SECRET_KEY_RE.search(k) and v not in (None, "", True, False) else redact(v))
                for k, v in value.items()}
    if isinstance(value, list):
        return [redact(v) for v in value]
    return value


def main():
    import argparse
    p = argparse.ArgumentParser(description="API oracle: one request, status and body (secrets hidden)")
    p.add_argument("method")
    p.add_argument("path")
    p.add_argument("--account", default="A", help="A/B/C from qa.credentials.json, or 'none' for anonymous")
    p.add_argument("--json", help="request body as JSON")
    a = p.parse_args()
    status, body = request(a.method.upper(), a.path, None if a.account == "none" else a.account,
                           json=globals()["json"].loads(a.json) if a.json else None)
    print(status)
    print(globals()["json"].dumps(redact(body), indent=2, ensure_ascii=False) if not isinstance(body, str) else body[:2000])
    sys.exit(0 if 200 <= status < 300 else 1)


if __name__ == "__main__":
    main()
