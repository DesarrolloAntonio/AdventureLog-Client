#!/usr/bin/env python3
"""Writes a logged-in session into the debug app WITHOUT typing a password (R11, route 2).

    scripts/qa/inject_session.py --device qa [--account A]       # qa = avd:… in qa.config.json
    scripts/qa/inject_session.py --device qa --clear      # removes the session key only

Why this route: the app keeps its session as JSON under `user_session` in a plain SharedPreferences
file (multiplatform-settings → `<package>_preferences.xml`), which `run-as` can write on a debuggable
build. The token comes from the API adapter (qa.credentials.json → one login → qa.tokens.json).

What it does: force-stops the app (so it can't write its in-memory prefs over ours), pulls the prefs
file, sets `user_session` and nothing else — theme and the remember-me keys are left as they are —
and pushes the file back through stdin, never on a command line. It never prints the token.
"""
import argparse
import json
import os
import subprocess
import sys
import xml.etree.ElementTree as ET

sys.path.insert(0, os.path.dirname(__file__))
import adapter  # noqa: E402

KEY = "user_session"


def config():
    return json.load(open(adapter.CONFIG))


def adb(serial, *args, stdin=None, check=True):
    res = subprocess.run(["adb", "-s", serial, *args], input=stdin, capture_output=True, timeout=60)
    if check and res.returncode != 0:
        raise SystemExit(f"adb {args[0]} → {res.returncode}: {res.stderr.decode(errors='replace').strip()}")
    return res.stdout


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--device", required=True, help="alias from `devices` in qa.config.json")
    p.add_argument("--account", default="A")
    p.add_argument("--clear", action="store_true")
    a = p.parse_args()

    cfg = config()
    devices = {k: v for k, v in cfg.get("devices", {}).items() if not k.startswith("_")}
    if a.device not in devices:
        raise SystemExit(f"unknown device alias {a.device!r}; devices: {', '.join(sorted(devices)) or 'nothing'}")
    # An emulator is listed as avd:<name>; the harness knows which serial that means right now.
    ui = os.path.expanduser("~/.claude/skills/qa-campaign/harness/android/ui.py")
    serial = subprocess.run(["python3", ui, "serial", a.device], capture_output=True, text=True,
                            cwd=os.path.dirname(adapter.CONFIG)).stdout.strip()
    if not serial:
        raise SystemExit(f"no serial for device alias {a.device!r} (is the emulator running?)")
    pkg = cfg["android"]["package"]
    prefs = cfg["android"]["files"]["prefs"]

    if not a.clear:
        # The request first: it logs in again when the cached token is dead (a revoked session
        # answers 401), and only then is the cached token the one to hand to the app. Reading the
        # token first injected a revoked one once, and the app went straight to Login (measured).
        status, me = adapter.request("GET", "/auth/user-metadata/", a.account)
        if status != 200 or not isinstance(me, dict):
            raise SystemExit(f"/auth/user-metadata/ as {a.account} answered {status}")
        token = adapter.token(a.account)
        session = {  # UserDetails (core/model), as UserDetailsDTO.toDomainModel maps it
            "pk": me.get("id"), "profilePic": me.get("profile_pic"), "uuid": me["uuid"],
            "publicProfile": bool(me.get("public_profile")),
            "measurementSystem": me.get("measurement_system") or "metric",
            "username": me["username"], "email": me.get("email"),
            "firstName": me.get("first_name") or "", "lastName": me.get("last_name") or "",
            "dateJoined": me.get("date_joined") or "", "isStaff": bool(me.get("is_staff")),
            "disablePassword": bool(me.get("disable_password")), "hasPassword": me.get("has_password", True),
            "sessionToken": token, "serverUrl": adapter.base_url(),
        }
        for src, dst in (("default_currency", "defaultCurrency"), ("map_style", "mapStyle")):
            if me.get(src):
                session[dst] = me[src]

    adb(serial, "shell", "am", "force-stop", pkg)
    current = adb(serial, "exec-out", "run-as", pkg, "cat", prefs, check=False)
    if current.startswith((b"cat:", b"run-as:")) or not current.strip():
        if current.startswith(b"run-as:"):
            raise SystemExit(current.decode(errors="replace").strip())
        root = ET.Element("map")
    else:
        root = ET.fromstring(current)
    for el in list(root):
        if el.get("name") == KEY:
            root.remove(el)
    if not a.clear:
        el = ET.SubElement(root, "string", name=KEY)
        el.text = json.dumps(session, separators=(",", ":"))

    body = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n" + "".join(
        f"    {ET.tostring(child, encoding='unicode').strip()}\n" for child in root) + "</map>\n"
    folder = prefs.rsplit("/", 1)[0]
    adb(serial, "shell", f"run-as {pkg} mkdir -p {folder} && run-as {pkg} sh -c 'cat > {prefs}'",
        stdin=body.encode())
    written = adb(serial, "exec-out", "run-as", pkg, "cat", prefs)
    has = any(e.get("name") == KEY for e in ET.fromstring(written))
    if has == a.clear:
        raise SystemExit(f"write did not take: {KEY} is {'still there' if has else 'missing'} in {prefs}")
    print(f"{'cleared' if a.clear else 'wrote'} {KEY} in {pkg}:{prefs}"
          + ("" if a.clear else f" for account {a.account} ({session['username']}); token not printed"))


if __name__ == "__main__":
    main()
