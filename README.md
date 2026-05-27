# Marzban Admin (Android)

A full-featured mobile admin client for [Marzban](https://github.com/Gozargah/Marzban) panels.

This app talks to the Marzban REST API and lets you manage everything the web panel can: users, admins, nodes, hosts, inbounds, core configuration, and live logs.

## Features

- Sign in to any Marzban panel with your admin credentials (JWT auth)
- Dashboard with system stats, core status, and bandwidth totals
- Users: create / edit / delete, paginated search and status filter, traffic ring, expiry, per-user usage chart, subscription QR + share + copy, reset usage, revoke subscription, activate next plan, set owner
- Expired users browser + bulk delete; bulk reset all users
- Admins (sudo): list / create / edit / delete, group disable/activate, per-admin usage + reset
- Nodes: list / create / edit / delete, reconnect, TLS certificate viewer, per-node live logs (WebSocket), bandwidth chart
- Hosts editor (grouped by inbound tag)
- Inbounds browser
- Core: status, JSON config editor, restart, live log stream (WebSocket)
- Settings: server URL, opt-in trust-all-certificates for self-signed panels

## Building

Requires JDK 17.

```bash
./gradlew assembleRelease
```

APKs land in `app/build/outputs/apk/release/`:

- `marzban-admin-<version>-universal-release.apk`
- `marzban-admin-<version>-arm64-v8a-release.apk`
- `marzban-admin-<version>-armeabi-v7a-release.apk`
- `marzban-admin-<version>-x86-release.apk`
- `marzban-admin-<version>-x86_64-release.apk`

## CI

`.github/workflows/build-apk.yml` runs on every push to `main`, on tags `v*`, and on manual dispatch. Per-ABI and universal APKs are uploaded as artifacts. Tag pushes also create a GitHub Release.

To sign release builds in CI, set repository secrets:

- `KEYSTORE_BASE64` — base64 of your `.jks` keystore
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Without these, CI builds fall back to debug signing so the workflow always succeeds.

## Compatibility

- minSdk 24 (Android 7.0)
- targetSdk 35 (Android 15)
- Marzban panel: latest stable (master branch)

## License

MIT
