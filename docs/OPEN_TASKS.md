# Open Tasks

This is the actionable backlog for the next NotifyBridge sessions.

## Highest Priority

### 1. Rebuild the tester recruitment form

Status: DONE. Published 2026-07-01 under the Google account that owns the ngcat Play developer account (confirmed correct). Responders = "Anyone with the link". Short link: https://forms.gle/efAdc7BtDn4zTxWh8 (full: /forms/d/1yth5NOuD5MV0SxWmkXraE2w0nQEKsJbj7-Uayma-Qvs).

Promotion round 1 (2026-07-02, posted from Reddit u/Longjumping_Pass_694 and the user's Telegram):
- r/betatests — posted (t3_1ukpcf7).
- r/TestersCommunity — posted with required "Testers Needed" flair (t3_1ukryhj).
- Telegram @GooglePlay20Testers — joined group + posted message with form link.
- r/androiddev — SKIPPED on purpose: its Rule 2 forbids app promotion / recruiting testers, and the monthly showcase thread also bans tester recruitment; posting would risk removal or account action. Only viable there as a pure feedback showcase (no tester ask / no form link).

Self-join flow LIVE (2026-07-02): created public Google Group `notifybridge-testers@googlegroups.com` (Anyone can join, Anyone on the web); Closed testing - Alpha tester list switched to that group and the Play change was reviewed + published same day. Testers self-join via:
- Group: https://groups.google.com/g/notifybridge-testers
- Play opt-in: https://play.google.com/apps/testing/uk.ngcat.notifybridge
All 6 previous email-list testers (from the old `public` + `v2 testers` lists) were directly added to the group (7 members incl. owner), so nobody lost closed-testing access. README recruitment section updated to this flow (replacing the stale old form link).

Remaining: watch form responses and TG reciprocal replies; do not mass-post without per-channel confirmation.

Why: the previous Google Forms draft was partially corrupted by browser editing and should not be shared.

Done:

1. Created a fresh English-only Google Form from scratch (did NOT patch the old one).
2. Title, description, 4 questions, and confirmation message all match `docs/TESTER_RECRUITMENT_FORM.md`.
3. Q1 email validation ("Must be a valid email") verified working in Preview.
4. Verified the whole form item-by-item in Preview.

Open follow-ups:

1. Confirm the form was created under the intended Google account (it was built under the Chrome-signed-in account, which is the Play developer account owner — confirmed correct). Rebuild under the correct account if needed.
2. Do NOT publish or share the responder link until the owner explicitly approves.
3. After approval: publish, collect tester emails, add them to the Play Console Closed testing tester list, send opt-in + GitHub Releases links.

Form (not published, not shared): `https://docs.google.com/forms/d/1yth5NOuD5MV0SxWmkXraE2w0nQEKsJbj7-Uayma-Qvs/edit`

Reference: `docs/TESTER_RECRUITMENT_FORM.md`

### 2. Prepare closed testing recruitment

Status: in progress.

Goal: collect enough eligible Google Play tester accounts for Closed testing.

Next action:

1. Share the recruitment text in suitable communities.
2. Collect Google Play account email addresses through the form.
3. Add accepted testers to the Play Console tester list.
4. Send testers the Play opt-in link and GitHub Releases fallback link.
5. Ask testers to remain opted in for at least 14 days.

### 3. Upload the next Play internal release

Status: DONE. 0.1.2 (versionCode 3) built + uploaded via the `v0.1.2` tag workflow and published to the Internal testing track in Play Console on 2026-07-01 (11:22 PM) — "Available to internal testers". Testers receive it within ~1h. The 2 pre-publish warnings (missing deobfuscation file, missing native debug symbols) are informational, not blockers.

Current local version:

- `versionName`: `0.1.2`
- `versionCode`: `3`
- Package: `uk.ngcat.notifybridge`

Next action:

1. Build or trigger the signed AAB workflow.
2. Upload `0.1.1` to Internal testing.
3. Confirm release notes.
4. Check Play warnings.
5. Roll out to internal testers before promoting to closed testing.

Reference: `docs/PLAY_INTERNAL_AUTOMATION.md`

## Product And Code Follow-ups

### 4. Verify old-to-new app data migration

Status: partially discussed.

Context: the package changed from earlier experimental package names to `uk.ngcat.notifybridge`. Android treats this as a separate app, so app-private DataStore/Room data does not automatically transfer.

Options:

- Use in-app settings export/import if available.
- Use ADB backup-like manual extraction only if debug builds and device permissions allow it.
- Reconfigure manually if the data cannot be safely extracted.

Avoid promising automatic migration unless verified on the attached device.

### 5. Confirm Play Data safety answers after feature changes

Status: needs review before production.

Key disclosures likely required:

- Notification contents may be forwarded to user-configured third-party destinations.
- App list/app package names are used for filtering.
- Device/app metadata may be included in payloads.
- Delivery logs are stored locally.

Reference: `docs/GOOGLE_PLAY_DATA_SAFETY_NOTES.md`

### 6. Re-check release assets

Status: mostly done.

Known assets:

- `store-assets/notifybridge-play-icon-512.png`
- `store-assets/notifybridge-feature-graphic-1024x500.png`
- `store-assets/notifybridge-feature-graphic-2048x1000.png`

The `2048x1000` file was untracked when this document was created. Decide whether to keep and commit it.

### 7. Optional: add Play API full release automation

Status: partly done.

Current automation uploads draft internal releases. Full automatic rollout may be possible after the app is no longer a draft app and Play API permissions are fully active.

Do not automate production rollout until the user explicitly wants it.

### 11. App picker: installed apps missing + CJK names not searchable

Status: Layer 1 shipped in 0.1.2 (verified on device; uploaded to internal track as a draft — needs Console publish). Layer 2 (pinyin search) still pending. Two layered causes.

Layer 1 done: added `<queries>` MAIN/LAUNCHER to AndroidManifest.xml and switched `InstalledAppsProvider` to `queryIntentActivities(MAIN/LAUNCHER)`. Verified on a Redmi Note 11 Pro (Android 13): "bili" now lists 哔哩哔哩 / tv.danmaku.bili in the picker. "danmaku"/"哔哩" also match; "bilibili" (romanized full spelling) still needs Layer 2. Shipped as 0.1.2 / versionCode 3 (commit af539df, tag v0.1.2). Build note: use JDK 17 (system default JDK 25 is unsupported by Gradle 8.7).

Symptom: In Settings app-filter search, "bilibili" finds nothing — and so does "danmaku", even though it is a substring of the package `tv.danmaku.bili`. That both fail means Bilibili is absent from the list, not merely unmatched.

Primary cause (list is truncated): Android 11 package visibility. `targetSdk=35` (>=30) and the manifest declares neither `<queries>` nor `QUERY_ALL_PACKAGES`, so `InstalledAppsProvider.getInstalledApps()` -> `PackageManager.getInstalledApplications()` returns only self + a few default-visible packages. Third-party apps like Bilibili are invisible on Android 11+, so no query term can find them.

Secondary cause (once list is fixed): romanized search doesn't match CJK labels. Filter is `appName.contains(query) || packageName.contains(query)`; Bilibili's label is "哔哩哔哩" (no ASCII match) and package `tv.danmaku.bili` contains "bili" but not "bilibili". No pinyin/transliteration.

Proposed fix (two layers):
1. Package visibility: add a `<queries>` element with the MAIN/LAUNCHER intent (Play-safe, no restricted permission) and switch the provider to `queryIntentActivities(MAIN/LAUNCHER)`. Avoid `QUERY_ALL_PACKAGES` (Play sensitive-permission review). Optional enhancement: also build a "known apps" set from packages observed by the NotificationListenerService (covers notifiers without a launcher).
2. Searchable index: precompute `searchKeys` (label, package, pinyin full spelling + initials) at load in a testable helper, move match logic to a pure matcher + the ViewModel. Pinyin via TinyPinyin (~30KB; ICU Transliterator is API 29+ so it misses minSdk 26-28).

Verify: confirm on an Android 11+ device the picker currently lists very few apps.

Files: `app/src/main/AndroidManifest.xml`, `app/src/main/java/com/example/notifybridge/system/util/InstalledAppsProvider.kt`, `app/src/main/java/com/example/notifybridge/feature/settings/SettingsScreen.kt` (search ~lines 220-230) and `SettingsViewModel.kt`.

### 12. Tester feedback round 1 (2026-07-03, reciprocal tester on a Samsung-style device)

Status: DONE — both fixes shipped in 0.1.3 (versionCode 4): consent screen made scrollable with a pinned agree button (PrivacyScreens.kt + safeDrawing insets), and Test send gated behind per-channel required fields with a localized hint. Reported by the first external closed tester; both acknowledged with the tester.

1. BUG — first-run disclosure/consent screen cannot scroll to the bottom on some devices (screenshot shows top text clipped and the tester says "can't scroll more"; the agree button was still reachable but cramped). Check the consent screen's scroll container / insets / small-screen + large-font behavior.
2. UX — tester tapped "Test send" right after granting notification access, with no delivery channel configured, and read the failure as a bug. The settings screen does show "Please fix invalid settings first", but the expectation gap is real. Consider: disable Test send until the active channel validates, and/or a first-run pointer that a channel must be configured before anything can be sent.
   - FIXED in working tree (2026-07-03, not yet committed): Test send is now disabled while the active channel's required fields are blank (`SettingsDraft.hasRequiredChannelFields()` in `SettingsScreen.kt`), with a localized inline hint (en + zh-CN, `settings_test_send_requires_channel`) naming the channel and pointing to the channel section above. Format validation still runs on tap via `SettingsViewModel.validate()`.

## Lower Priority

### 8. Polish public download page

Status: optional.

The website already has a basic homepage, privacy policy, and data deletion page on GitHub Pages at `https://ngcat.uk/`.

Potential improvements:

- Add a visible GitHub Releases download link.
- Add tester recruitment link after the form is rebuilt.
- Add quick setup screenshots.

### 9. Improve Play listing screenshots

Status: optional.

Current screenshots were generated and committed previously. Revisit after UI stabilizes or if Play rejects assets.

### 10. Future monetization decision

Status: deferred.

Current app should be declared as free and no ads if no Google Ads SDK/ad placements exist in the build. If Google Ads is added later, update Play declarations and Data safety.

