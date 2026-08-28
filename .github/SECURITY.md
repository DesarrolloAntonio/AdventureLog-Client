# Security Policy

This repository is **AdventureLog Client** — an unofficial Android and iOS client for
[AdventureLog](https://github.com/seanmorley15/AdventureLog). It talks to a server you host
yourself, and it keeps your session and your server's address on your own device.

## What this policy covers

The mobile client, and only the mobile client.

A vulnerability in the AdventureLog **server** — the Django API, the web frontend, the published
Docker images — belongs upstream, where the people who can fix it will see it:
<https://github.com/seanmorley15/AdventureLog/security>

How you have configured your own server is out of scope here.

## Supported versions

The app is pre-release. There are no maintenance branches: fixes land on the latest build and
nowhere else, and older alphas are not patched.

| Version           | Supported          |
| ----------------- | ------------------ |
| Latest release    | :white_check_mark: |
| Anything earlier  | :x:                |

## Reporting a vulnerability

Please report privately rather than opening a public issue:

**[Open a draft security advisory](https://github.com/DesarrolloAntonio/AdventureLog-Client/security/advisories/new)**

Useful things to include:

- What kind of vulnerability it is, and what an attacker gets out of it
- The affected file or screen, and the commit or release you saw it on
- Steps to reproduce it, and a proof of concept if you have one
- Anything unusual about the setup needed to trigger it

## What happens next

This is a one-person project built in spare time, so please don't expect a service-level
agreement. I will acknowledge your report as soon as I reasonably can, tell you whether I can
reproduce it, and let you know when a fix ships. If you would like credit in the advisory, say
so — and if you would rather stay anonymous, that is fine too.

Please give me a chance to ship a fix before disclosing publicly.

English and Spanish are both welcome.
