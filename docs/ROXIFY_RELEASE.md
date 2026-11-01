# ROXIFY — signed release build (GitHub Actions)

This clone builds the **SimpleTune** client (`SimpleTune/`, Kotlin + Compose) into a
signed release APK. No Android SDK or JDK is needed locally — GitHub Actions provides
JDK 17 and the SDK.

---

## 1. Point the updater at your repository

The updater must read releases from the repository that hosts **this clone**. Set the
same `owner/repo` in both places:

| File | Constant |
|---|---|
| `SimpleTune/app/src/main/java/com/vibeon/music/util/AppUpdate.kt` | `ROXIFY_GITHUB_REPO` |
| `mobile/lib/update.ts` | `REPO` |

Both are set to `Rohitkuma07/ROXIFY`.

> This repository currently hosts an older, different offline-first player (v1.0.0).
> Pushing this clone replaces that code. The old `v1.0.0` release stays until you
> delete it; the updater only offers versions newer than the installed one, so a 2.x
> build will never be offered the old APK.

The release URL and GitHub API URL are derived from this one value.

---

## 2. Add the signing secrets

A release keystore was generated for this project and kept **outside** the repo
(`signing/` next to the clone). Its alias is `roxify`.

In the target repository: **Settings → Secrets and variables → Actions → New repository secret**

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | entire contents of `signing/KEYSTORE_BASE64.txt` (single line) |
| `KEYSTORE_PASSWORD` | entire contents of `signing/KEYSTORE_PASSWORD.txt` |

The workflow writes `SimpleTune/keystore/roxify.jks` + `keystore/key.properties` from
these secrets and **fails early** if either is missing, so a silently debug-signed
release cannot happen.

To use your own key instead, replace the keystore with one whose alias is `roxify`
(or update `keyAlias` in `.github/workflows/release.yml`).

---

## 3. Push the code

```bash
git remote add origin https://github.com/Rohitkuma07/ROXIFY.git
# The existing repo has unrelated history, so the first push must be forced.
# This replaces the old offline-first code on main (existing releases are kept).
git push --force origin main
```

---

## 4. Trigger the build

**Option A — tag (creates a GitHub Release automatically):**

```bash
git tag v2.0.4
git push origin v2.0.4
```

**Option B — manual:**

GitHub → **Actions** → **Release APK** → **Run workflow** → enter `2.0.4` → **Run**.

The tag is always `v<version>`; the APK's `versionName` is taken from the same value,
so in-app update checks compare like-for-like.

---

## 5. Download the APK

GitHub → **Releases** → the tag you built → **Assets** → `ROXIFY_v2.0.4.apk`.

The workflow keeps only the newest `ROXIFY_<tag>.apk` on a release and removes stale
APK assets for that tag.

---

## What the workflow does

1. Checks out the repo and installs JDK 17.
2. Restores the keystore from secrets.
3. Runs `./gradlew :app:assembleRelease` with `-PVIBEON_VERSION_NAME=<tag without v>`.
4. Renames `app-release.apk` → `ROXIFY_<tag>.apk`.
5. Creates/updates the GitHub Release and uploads the APK.

`SimpleTune/app/build.gradle.kts` signs `release` with the restored keystore whenever
`keystore/key.properties` is present.

---

## Identity notes

- `applicationId` is `com.roxify.music` (Kotlin namespace stays `com.vibeon.music`).
  It installs alongside VibeOn because the package differs.
- The Expo client uses `applicationId` `com.roxify.socialmusic`.
- If an older ROXIFY build with the same package but a different signing key is already
  installed, uninstall it once before installing this build (signature mismatch).
