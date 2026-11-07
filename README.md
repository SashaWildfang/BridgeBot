# BridgeBot

> ⚠️ **Work in progress.** This is an early prototype and does not compile yet.

A headless Minecraft client for the **Bear Nation** network. It signs in with a Microsoft account, connects to the network's Velocity proxy, and moves itself onto the CTF server. If the connection drops, it reconnects on its own.

![Java](https://img.shields.io/badge/Java-21-orange) ![Build](https://img.shields.io/badge/build-Maven-C71A36)

## How it works

1. Reads a Microsoft **refresh token** from `config.json`
2. Signs in through MSA and fetches the Minecraft profile
3. Connects to the proxy using [MCProtocolLib](https://github.com/GeyserMC/MCProtocolLib)
4. Runs `/server ctf` once logged in
5. On disconnect, waits 5 seconds and reconnects

## Configuration

Create a `config.json` next to the jar. It is git-ignored, so it never gets committed.

```json
{
  "refreshToken": "<microsoft-refresh-token>"
}
```

## Building

```bash
mvn package
```

## Tech

- [MCProtocolLib](https://github.com/GeyserMC/MCProtocolLib) for the Minecraft protocol
- MCAuthLib for Microsoft authentication
- Gson and SLF4J

---

Part of the Bear Nation plugin suite.
