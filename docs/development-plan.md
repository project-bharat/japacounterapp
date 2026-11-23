# Japa Counter development plan

Last updated 21 November 2026. This file is the running project handoff. Read it before starting a new development task and update it in the same change when a milestone, decision, test result, or blocker changes. Keep feature details in linked briefs and record build and commit references here.

## Product direction

Build a Marathi-first native Android Japa counter with English support. Keep the original Build #528 Japa interface as the visual foundation and make daily practice quick to start, reliable offline, and easy to review. The app uses Kotlin, Jetpack Compose, Room, and WorkManager. The application ID is `com.projectbharat.counterapp`.

Manual counting, local history, reminders, and local backup belong in the free experience. Pro adds private Google Drive backup and sync through a Play subscription, with Room as the local source of truth and no developer-operated backend. Pricing, premium analytics, and any voice allowance are open product decisions.

## Current baseline

Development is on `japa-ui-recovery-528`. The current UI candidate is **Build #550** at `60f36d8`. GitHub Actions compiled the debug APK, passed unit tests, and uploaded the APK and ZIP. Build #548 was tested on a phone, and its screenshot feedback informed Build #550. Phone testing of the redesign is pending. [Build #550 and artifacts](https://github.com/project-bharat/japacounterapp/actions/runs/44896922921)

| Area | Current state |
| --- | --- |
| Recovery and cleanup | Build #528 Japa interface preserved as the foundation. Baby Movement and Steps were removed from active routes, quick actions, notifications, reminders, and the widget. Legacy Room and backup records remain supported. |
| Practice | Manual and on-device voice counting, presets and custom mantras, persistent per-mantra sessions, and a Finish summary. |
| Progress | Editable daily goal starting at 108, separate practice and goal streaks, and a calendar that opens the selected day's history. |
| Local backup | Version-1 JSON export and restore through Android file pickers. Includes Japa sessions and goals as well as legacy data. Room schema is version 9. |
| Billing and cloud | Planned. Play Billing, the Pro paywall, Drive account linking, and background sync are pending. |
| Release | Debug APK verified. Signed release bundle, Play Console setup, and on-device validation are pending. |

## Delivery order

1. **Validate Build #550 on phones.** Test a fresh install and an upgrade with existing data. Check manual and voice counts across interruption, Finish, microphone denial, offline model availability, goal history, calendar navigation, reminders, widget refresh, and a local backup round trip.
2. **Sign off on the refreshed UI.** Review one-handed controls, clarity of voice states, feedback preferences, and recent and favourite mantras. Capture screenshots in Marathi and English and review large fonts, dark mode, and TalkBack before starting Play release work.
3. **Prepare the Play release and subscription.** Confirm the current target API and billing requirements, configure Play signing and test tracks, prepare the signed Android App Bundle and store materials, and verify purchase, restore, cancellation, and expiry in Play testing. Confirm prices and subscription periods before publishing.
4. **Implement and verify Pro Drive backup.** Follow the backup brief. Set up Google OAuth and the Drive API, preserve older local backups, and validate account linking, revoked access, retry behavior, and conflicts across devices. [Backup and Drive sync brief](backup-and-drive-sync-brief.md)
5. **Evaluate follow-up features.** Consider personal milestones, recap cards, and advanced goals after the core flows are validated.

## Milestone log

- **Build #544** completed the Japa-only recovery cleanup and passed debug CI. The APK and ZIP were verified. [Build #544](https://github.com/project-bharat/japacounterapp/actions/runs/44873765597)
- **Build #546** added persistent practice sessions, Finish summary, daily goals, and calendar-to-history navigation. Debug CI and artifact verification passed. [Build #546](https://github.com/project-bharat/japacounterapp/actions/runs/44876342188)
- **21 November 2026** recorded the offline-first backup and Pro Drive sync requirements for future implementation.

## UI implementation

Build #548 combines the navigation, Practice, Progress, and Settings changes for review. [Build #548](https://github.com/project-bharat/japacounterapp/actions/runs/44891244569)

- Added the Practice, Progress, and Settings navigation shell around the original Japa counter.
- Added a searchable mantra picker with recent and favourite selections, practice feedback controls, a shareable Finish summary, a daily Progress overview, and a monthly recap.
- Refreshed Device Backup and added debug-only Japa Plus screen states. Live Play Billing and Google Drive authorization remain in the release workstream.
- Phone screenshots and verification of upgrade, microphone, widget, accessibility, and the complete local backup flow are pending.

### Build #548 review follow-up

- Reviewed the five device screenshots and the original 17-image reference set. The redesign uses a continuous gradient, floating primary navigation, a full-screen session and summary, and a full-page mantra library with details.
- Added branded PNG sharing for saved sessions, daily progress, and monthly recaps. Session images include the selected mantra and saved time in UTC. Aggregate cards identify their scope.
- Progress analytics now uses weekly and monthly filters, and the daily goal has a completion state. The voice notification has a Stop action and replaces the quick-start notification while active.
- Google and Drive branding appears in the backup and Pro preview. Phone review remains pending for system Back, notification behavior, and image sharing.

### Bhajame name and language settings

- Changed the app label to **Bhajame - Daily Japa & Bhakti**. Replacement horizontal artwork is pending the final asset files.
- Added separate settings for app language (Marathi, Hindi, English) and mantra display script (Devanagari, English script). Script changes affect presentation and retain stored counting keys.
- Voice counting uses the same existing Devanagari Vosk model with either display script. The current Devanagari model is the Hindi acoustic model already used for Marathi chanting.
- **Build #553** passed Android compilation and unit tests. Phone review should confirm that switching either dropdown preserves the current mantra and its count, and that both scripts use the same voice model. [Build #553](https://github.com/project-bharat/japacounterapp/actions/runs/44898609742)

### UI rollback after phone feedback

- Restored the Build #548 header, tab bar, in-place Practice counter, voice switch, and dialogs for mantra selection and session completion.
- Retained the Bhajame app name, independent language and mantra-script settings, the existing voice model, and image sharing.

### Targeted UI polish after Build #555

- Separated the language and mantra-script dropdowns. The counter stays on Practice, with a Magic Count switch and boxed actions below the listening panel.
- Updated the floating footer with a gradient and colored glow. Applied light and dark gradients to daily-goal, milestone, and other Progress figures.
- **Build #556** passed Android compilation and unit tests. The control spacing, footer glow, and KPI contrast still need a phone review. [Build #556](https://github.com/project-bharat/japacounterapp/actions/runs/44915997580)

### Bluetooth microphone and mantra library

- Magic Count requests Bluetooth headset input when Android exposes a compatible call-audio route. The listening panel reports the microphone actually used, including handset fallback.
- Choose Mantra opens a full page with search, filters, favourites, and a Start Japa button. Practice places the logo, counter, and controls directly on the app gradient.
- Physical Bluetooth verification is pending for Classic SCO and LE Audio headsets.
- **Build #559** passed Android compilation and unit tests, including legacy Bluetooth PCM conversion. Check the microphone indicator, recognition, and routing after Stop on a physical headset. [Build #559](https://github.com/project-bharat/japacounterapp/actions/runs/44918472738)

### Combined redesign review build

- Build #562 includes the bundled sunrise backdrop, system light/dark palette, centered Practice navigation, local achievement milestones, one selectable progress chart, and refreshed Settings grouping and share artwork.
- Android compilation and unit tests passed. Phone review remains open for layout, accessibility, Bluetooth, share sheets, and migration from Build #559. The remaining detailed design refinements include the full Settings child-page treatment and the agreed KPI order.

### Practice layout review

- Build #566 places Magic Count beside the wordmark, keeps one mantra script visible, adds glass-style metrics and a notched footer, and uses sunrise imagery in the mantra library and goal card. CI compilation and unit tests passed.
- Phone review is needed for notch alignment, small-screen and large-text spacing, dark contrast, Bluetooth recognition, and the updated images. [Build #566](https://github.com/project-bharat/japacounterapp/actions/runs/44944266158)

### Reference comparison

- Refined selected mantra contrast in both system themes and added space around the main mantra and lower controls.
- The reference comparison used the supplied screenshots and source inspection. Android emulator and physical-device review remain outstanding.

### Single Japa counter and Build #566 layout

- Restored the Build #566 screen layout, removed the Settings counter selector and management entry, and select the Japa category on existing installations.
- Simplified headers to the logo and an icon-only Back control on child screens. Adjusted text and chart colors for light and dark surfaces.
- Existing non-Japa categories and records remain in the local database and backups. Phone review remains open for both system themes.
