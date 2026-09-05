# Pete's Reps Release Signing & GitHub Releases

Pete's Reps uses one durable Android signing identity for release APKs. The private signing key must never be committed to this public repository.

The repository is wired so a `v*` tag can build, verify, and publish a signed APK as a GitHub Release once the required Actions secrets exist.

## Why the signing key matters

Android only permits an installed app to be upgraded by another APK signed with the same trusted signing identity. Losing or changing the Pete's Reps release key would break normal update continuity for users who already have training history on-device.

Treat the release keystore and its passwords as long-lived project credentials.

Pete's Reps also has an app-owned training backup format. Backup/restore is a recovery layer, not a replacement for stable signing. See **[BACKUP.md](BACKUP.md)**.

## 1. Create the durable release keystore

Create this outside the repository on a trusted machine:

```bash
keytool -genkeypair -v \
  -keystore petes-reps-release.jks \
  -alias petesreps \
  -keyalg RSA \
  -keysize 4096 \
  -validity 36500
```

Use strong passwords when prompted. Do not put passwords directly in shell history.

Back up the keystore and the credentials in at least two controlled locations. A password manager plus an offline encrypted backup is a reasonable pattern.

## 2. Encode the keystore for GitHub Actions

GitHub Actions stores the binary keystore as a Base64-encoded repository secret.

Linux:

```bash
base64 -w 0 petes-reps-release.jks > petes-reps-release.jks.base64
```

macOS:

```bash
base64 < petes-reps-release.jks | tr -d '\n' > petes-reps-release.jks.base64
```

PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("petes-reps-release.jks")) | Set-Content -NoNewline petes-reps-release.jks.base64
```

## 3. Create these GitHub Actions repository secrets

Open the Pete's Reps repository settings, then **Secrets and variables → Actions**, and create:

- `PETES_REPS_RELEASE_KEYSTORE_BASE64` — the complete Base64 text from the encoded keystore
- `PETES_REPS_RELEASE_STORE_PASSWORD` — the keystore password
- `PETES_REPS_RELEASE_KEY_ALIAS` — normally `petesreps`
- `PETES_REPS_RELEASE_KEY_PASSWORD` — the private-key password

The ChatGPT GitHub connection intentionally cannot read or write repository secrets, so this credential-provisioning step must be performed through GitHub's secure secret-management UI or another trusted administrative path.

Delete any temporary plaintext/Base64 copies from untrusted locations after the secrets and backups are verified.

## 4. Release contract

The release workflow lives at:

```text
.github/workflows/release.yml
```

It runs only for tags matching `v*` and performs these gates:

1. the tag must match `versionName` in `app/build.gradle.kts`
2. all four signing secrets must be present
3. unit tests must pass
4. the release APK must assemble with the durable signing key
5. Android `apksigner` must verify the resulting APK
6. a SHA-256 checksum is generated
7. the signed APK and checksum are uploaded as a workflow artifact
8. GitHub Release is created from the same tag and receives the APK and checksum

A missing signing secret causes the release workflow to fail rather than silently publishing an unsigned or debug-signed build.

## 5. Creating a release

Before tagging:

1. update `versionCode` and `versionName` in `app/build.gradle.kts`
2. merge that version change to `main`
3. ensure normal Android CI is green
4. create and push a matching tag, for example `v0.3.0`

The tag is the release trigger. The tag must exactly match the app's `versionName` with a leading `v`.

Do not create the first stable tag until the signing secrets have been installed and the keystore has been backed up.

## 6. Upgrade-preservation verification

Before calling the release channel dependable:

1. install a signed release APK
2. complete at least one Pete's Reps session so local history exists
3. export a `.preps` training backup and keep it outside the app as a recovery copy
4. record the app's displayed workout-history count
5. build a newer APK with a higher `versionCode` using the same signing key
6. install it over the existing app without uninstalling
7. verify Android accepts the update
8. verify the previous local training history is still present without needing the backup
9. retain the backup as an independent recovery check; optionally restore it on a disposable/test install to verify the exported history remains readable

This is the acceptance test for issue #5. A working backup does not excuse a broken in-place upgrade; both durability paths should work.

## Local signed build

If the four signing variables are available in the local environment, `assembleRelease` produces a signed release APK. The file path is supplied separately so the keystore can remain anywhere outside the repository.

Expected variables:

```text
PETES_REPS_RELEASE_STORE_FILE
PETES_REPS_RELEASE_STORE_PASSWORD
PETES_REPS_RELEASE_KEY_ALIAS
PETES_REPS_RELEASE_KEY_PASSWORD
```

Then run:

```bash
gradle :app:testDebugUnitTest :app:assembleRelease
```

Output:

```text
app/build/outputs/apk/release/app-release.apk
```

Without signing variables, the normal CI may assemble an unsigned release only as a configuration smoke test; it never publishes that file.
