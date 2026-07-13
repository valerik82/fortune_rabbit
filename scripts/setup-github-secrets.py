#!/usr/bin/env python3
"""Upload Android signing secrets to GitHub Actions (run once with GITHUB_TOKEN)."""

from __future__ import annotations

import base64
import json
import os
import sys
import urllib.request
from pathlib import Path

REPO = "valerik82/fortune_rabbit"
KEYSTORE = Path(__file__).resolve().parents[1] / "fortunnnn.jks"


def api_request(token: str, method: str, path: str, payload: dict | None = None) -> dict:
    url = f"https://api.github.com/repos/{REPO}{path}"
    data = None if payload is None else json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(
        url,
        data=data,
        method=method,
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github+json",
            "Content-Type": "application/json",
            "X-GitHub-Api-Version": "2022-11-28",
        },
    )
    with urllib.request.urlopen(request) as response:
        body = response.read().decode("utf-8")
        return json.loads(body) if body else {}


def encrypt_secret(public_key: str, secret_value: str) -> str:
    from nacl import encoding, public

    public_key_bytes = base64.b64decode(public_key)
    sealed_box = public.SealedBox(public.PublicKey(public_key_bytes))
    encrypted = sealed_box.encrypt(secret_value.encode("utf-8"))
    return base64.b64encode(encrypted).decode("utf-8")


def set_secret(token: str, name: str, value: str, key_id: str, public_key: str) -> None:
    api_request(
        token,
        "PUT",
        f"/actions/secrets/{name}",
        {
            "encrypted_value": encrypt_secret(public_key, value),
            "key_id": key_id,
        },
    )
    print(f"Set secret: {name}")


def main() -> None:
    token = os.environ.get("GITHUB_TOKEN")
    store_password = os.environ.get("RELEASE_STORE_PASSWORD")
    key_alias = os.environ.get("RELEASE_KEY_ALIAS")
    key_password = os.environ.get("RELEASE_KEY_PASSWORD")

    if not all([token, store_password, key_alias, key_password]):
        sys.exit("Required: GITHUB_TOKEN, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_KEY_PASSWORD")

    if not KEYSTORE.exists():
        sys.exit(f"Keystore not found: {KEYSTORE}")

    key_info = api_request(token, "GET", "/actions/secrets/public-key")
    keystore_base64 = base64.b64encode(KEYSTORE.read_bytes()).decode("utf-8")

    set_secret(token, "KEYSTORE_BASE64", keystore_base64, key_info["key_id"], key_info["key"])
    set_secret(token, "RELEASE_STORE_PASSWORD", store_password, key_info["key_id"], key_info["key"])
    set_secret(token, "RELEASE_KEY_ALIAS", key_alias, key_info["key_id"], key_info["key"])
    set_secret(token, "RELEASE_KEY_PASSWORD", key_password, key_info["key_id"], key_info["key"])
    print("All signing secrets uploaded.")


if __name__ == "__main__":
    main()
