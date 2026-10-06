#!/usr/bin/env python3
"""A fake AdventureLog server for what the real one must not be made to do (SKILL.md R10, R11).

The harness's fake_server.py answers routes but sends no headers of its own, and signing in to
AdventureLog needs one: the session is the `sessionid` in Set-Cookie. This is the same idea with a
`headers` field per route, and a default answer for everything unrouted.

    scripts/qa/fake_adventurelog.py --port 18099 --routes scripts/qa/fake/account-b.json
    scripts/qa/fake_adventurelog.py --port 18099 --routes scripts/qa/fake/account-b.json \
        --override "GET /api/stats/dashboard/" '{"delay": 150}'

Routes: {"METHOD /path": {"status": 200, "body": <json or string>, "headers": {...}, "delay": s}}.
The emulator reaches it at http://10.0.2.2:<port>. Every request is printed: method, path, status.
Only invented accounts and invented data live here - nothing real is served or accepted.
"""
import argparse
import json
import socketserver
import sys
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer


class NoLookupServer(ThreadingHTTPServer):
    daemon_threads = True

    def server_bind(self):
        # HTTPServer.server_bind() resolves the host name first, which hung on macOS (see fake_server.py).
        socketserver.TCPServer.server_bind(self)
        self.server_name, self.server_port = self.server_address[:2]


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--host", default="127.0.0.1")
    p.add_argument("--port", type=int, required=True)
    p.add_argument("--routes", required=True)
    p.add_argument("--override", nargs=2, action="append", default=[], metavar=("ROUTE", "JSON"),
                   help="merge JSON into one route, e.g. a delay or a different body")
    a = p.parse_args()
    spec = json.load(open(a.routes))
    routes, default = spec.get("routes", {}), spec.get("default", {"status": 404, "body": {"detail": "Not found."}})
    for route, extra in a.override:
        routes[route] = {**routes.get(route, {}), **json.loads(extra)}

    class Handler(BaseHTTPRequestHandler):
        protocol_version = "HTTP/1.1"

        def answer(self):
            length = int(self.headers.get("Content-Length") or 0)
            if length:
                self.rfile.read(length)  # never printed: a login body carries a (invented) password
            path = self.path.split("?")[0]
            route = routes.get(f"{self.command} {path}", default)
            status, body = route.get("status", 200), route.get("body", "")
            raw = (body if isinstance(body, str) else json.dumps(body)).encode()
            print(f"{time.strftime('%H:%M:%S')} {self.command} {self.path} -> {status}"
                  + ("" if f"{self.command} {path}" in routes else "  (default)"), flush=True)
            if route.get("delay"):
                time.sleep(route["delay"])
            self.send_response(status)
            self.send_header("Content-Type", route.get("type", "application/json"))
            for k, v in route.get("headers", {}).items():
                self.send_header(k, v)
            self.send_header("Content-Length", str(len(raw)))
            self.end_headers()
            self.wfile.write(raw)

        do_GET = do_POST = do_PUT = do_PATCH = do_DELETE = answer

        def log_message(self, *args):
            pass

    server = NoLookupServer((a.host, a.port), Handler)
    print(f"fake AdventureLog on http://{a.host}:{a.port} ({len(routes)} routes)", flush=True)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        sys.exit(0)


if __name__ == "__main__":
    main()
