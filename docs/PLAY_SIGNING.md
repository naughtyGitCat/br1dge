# Play Signing Setup

This project supports release signing from either:

- `keystore.properties` in the repo root
- Environment variables in CI

## Local signing

1. Copy `keystore.properties.example` to `keystore.properties`
2. Fill in:
   - `storeFile`
   - `storePassword`
   - `keyAlias`
   - `keyPassword`
3. Keep `keystore.properties` out of git
4. Build:
   - `./gradlew :app:bundleRelease`

## CI signing

Provide these environment variables:

- `NB_UPLOAD_STORE_FILE`
- `NB_UPLOAD_STORE_PASSWORD`
- `NB_UPLOAD_KEY_ALIAS`
- `NB_UPLOAD_KEY_PASSWORD`

For GitHub Actions, the most practical setup is:

1. Store the keystore as a base64 secret
2. Decode it during the workflow into a temporary file
3. Export `NB_UPLOAD_STORE_FILE` to that temporary path
4. Export the remaining passwords and alias from GitHub Secrets

## Generating an upload key

Example command:

```bash
keytool -genkeypair \
  -v \
  -keystore notifybridge-upload.jks \
  -keyalg RSA \
  -keysize 4096 \
  -validity 3650 \
  -alias notifybridge-upload
```

Recommended practice:

- Keep the upload key separate from any previous debug key
- Enable Play App Signing in Play Console
- Use this upload key only for uploading bundles to Play
