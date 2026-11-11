# Secrets

Nothing sensitive is committed in plaintext. Two supported paths:

## 1. Local: git-crypt

```
brew install git-crypt
git-crypt init            # once, per repo; keep the printed key in a password manager
git-crypt add-gpg-user <you@example.com>
```

`.gitattributes` marks `secrets/**` as encrypted, so `secrets/appium.env` is readable in your working
tree and ciphertext in git.

Put secrets in `secrets/appium.env`:

```
SLACK_WEBHOOK_URL=...
SLACK_BOT_TOKEN=xoxb-...
ARTIFACT_USER=...
ARTIFACT_TOKEN=...
```

Source it before a run (`set -a; . secrets/appium.env; set +a`) - the framework reads everything from
the environment, never from a committed file.

## 2. CI: SECRETS_KEY

The device pipeline decrypts with `SECRETS_KEY` (base64 of the exported git-crypt key) through
`ci/unlock-secrets.sh`. The key exists only in the CI secret store and in your password manager.

## Rules

- Never log a secret: `RetrofitClient` redacts `Authorization`, `Cookie` and `X-Api-Key`.
- Never bake a secret into `*.conf`; use the `${?ENV_VAR}` override next to the default.
- `secrets/*.conf` is gitignored; only encrypted blobs belong in git.
- Rotate any secret that lands in a log or a screenshot.
