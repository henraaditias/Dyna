# Implementation Plan — Dyna

## 1. Project Overview

**Project name:** Dyna (working name)  
**Platform:** Android  
**Primary goal:** Build an Android app that provides an iPhone-inspired Dynamic Island-style floating pill, visible over other apps, for showing supported live activities and notifications.

The app should be developed incrementally. The first milestone focuses on a reliable floating pill, permission onboarding, and a small set of demonstrable activities. Additional activity types and customization can be added later.

## 2. Product Goals

- Display a compact, pill-shaped overlay outside the app.
- Expand or collapse the pill when the user taps it.
- Show useful activity states such as a timer and supported notification events.
- Let users customize the pill's appearance and behavior.
- Keep the overlay unobtrusive, battery-conscious, and easy to disable.
- Explain and request Android permissions clearly; do not silently enable restricted access.

## 3. Non-Goals for the Initial Release

- Reproducing every iOS Dynamic Island behavior.
- Accessing private notification content without the user's explicit permission.
- Reading or controlling other apps' private UI.
- Building a complete notification-management replacement.
- Guaranteeing identical behavior across every Android manufacturer or OS version.

## 4. Proposed Technology

- **Language:** Kotlin
- **UI:** Jetpack Compose for in-app screens and overlay content
- **Overlay:** Android `WindowManager` with an appropriate overlay window type
- **Background work:** Foreground service only where required for a user-visible, ongoing activity
- **Preferences:** DataStore
- **Architecture:** MVVM with repository interfaces for settings and activity state
- **Minimum SDK:** Decide after confirming target devices and required APIs; verify overlay and notification behavior across supported Android versions.

Keep Android platform APIs behind small interfaces so overlay, notification, and activity handling can be tested separately.

## 5. Core User Flows

### First launch
1. Show a short explanation of what the overlay does.
2. Let the user preview the pill inside the app.
3. Explain the “Display over other apps” permission and open the relevant system settings screen.
4. Ask for notification access only when the user enables notification features.
5. Confirm each permission state and provide a clear way to continue without it.

### Everyday use
1. User enables Dyna from the app.
2. The app displays the collapsed pill.
3. A supported activity begins, and the pill updates.
4. Tapping the pill expands its content.
5. Tapping outside or using the collapse control returns it to the compact state.
6. The user can pause or disable Dyna from the app or its persistent notification, when applicable.

## 6. Main Screens

- **Home:** Enable/disable control, overlay status, quick preview, and permission status.
- **Appearance:** Size, corner radius, color, text/icon style, and animation preferences.
- **Activities:** Enable supported activity types and configure their display behavior.
- **Permissions:** Explain and link to the required Android settings.
- **About & Help:** Version, limitations, troubleshooting, and privacy explanation.

## 7. Overlay Behavior

### Collapsed state
- Compact horizontal pill near the top edge.
- Shows only the most important icon or short status.
- Uses a stable position and respects display cutouts and system bars.
- Has a clear tap target and avoids blocking essential system controls.

### Expanded state
- Opens into a larger panel with activity details and available actions.
- Uses a short, restrained transition.
- Supports collapse and dismissal where appropriate.
- Does not cover more of the screen than needed.

### Interaction and lifecycle
- Handle screen rotation, display-size changes, app process recreation, and permission revocation.
- Re-check overlay permission before attaching the window.
- Remove the overlay when disabled, when permission is revoked, or when the service is stopped.
- Avoid keeping a background service alive without an active, user-visible reason.

## 8. Activity Support

### MVP activities
1. **Demo activity:** A built-in sample state for testing layout and interactions.
2. **Timer:** Show remaining time and a basic start/pause/stop flow.
3. **Supported notifications:** Show a concise notification summary only after the user grants notification access.

### Later candidates
- Music playback status and controls, subject to Android media-session support.
- Download or other ongoing-task progress where a supported API is available.
- Additional activity integrations based on explicit user consent and technical feasibility.

Each activity should expose a normalized state model, such as title, subtitle, icon, progress, available actions, and priority. The overlay UI should not need to know which source produced the activity.

## 9. Suggested Architecture

```text
app/
  ui/
    home/
    appearance/
    activities/
    permissions/
    overlay/
  domain/
    model/
    usecase/
  data/
    settings/
    activities/
  platform/
    overlay/
    notifications/
    timer/
  service/
```

### Key components
- `OverlayController`: creates, updates, and removes the overlay window.
- `OverlayViewModel`: converts activity state and settings into UI state.
- `ActivityRepository`: provides the current supported activity.
- `SettingsRepository`: reads and writes user preferences.
- `PermissionStateProvider`: reports permission state without assuming permission was granted.
- `TimerController`: owns timer state and lifecycle.
- `NotificationActivitySource`: converts permitted notification events into normalized activity state.

Use interfaces for platform-dependent components and inject implementations into the app logic.

## 10. Data Models

### ActivityState
- `id`
- `type`
- `title`
- `subtitle`
- `icon`
- `progress` (optional)
- `isOngoing`
- `priority`
- `actions` (optional)

### UserSettings
- `isEnabled`
- `appearance`
- `enabledActivityTypes`
- `animationEnabled`
- `expandedByDefault` (optional)

Persist user settings. Keep transient activity state in memory unless a specific activity requires restoration.

## 11. Permissions and Privacy

- Request overlay permission only after explaining why it is needed.
- Request notification listener access only if the user enables notification-based features.
- Never treat permission as granted until Android confirms it.
- Do not collect, upload, or retain notification contents beyond what the enabled feature needs.
- Keep processing on-device for the initial release.
- Provide an in-app path to disable the overlay and explain how to revoke permissions.
- Avoid misleading accessibility-service usage; do not use an accessibility service as a shortcut for reading other apps.

## 12. Milestones

### Milestone 1 — Project foundation
- Create the Android project and establish package structure.
- Add Compose navigation and the basic app theme.
- Define settings and activity-state models.
- Add a simple Home screen and in-app pill preview.

**Done when:** The app builds and the preview responds to basic UI state.

### Milestone 2 — Overlay proof of concept
- Implement overlay permission explanation and system-settings handoff.
- Create and remove a basic `WindowManager` overlay.
- Implement collapsed and expanded states.
- Handle permission denial and revocation gracefully.

**Done when:** The pill can be enabled, displayed over another app, expanded, collapsed, and safely disabled.

### Milestone 3 — Timer activity
- Implement timer state and controls.
- Connect timer state to the overlay.
- Restore or clearly reset timer state after process recreation, based on the chosen behavior.

**Done when:** Timer updates remain accurate and the overlay reflects timer state.

### Milestone 4 — Notification activity
- Add optional notification access onboarding.
- Convert supported notification events into concise activity states.
- Add controls to enable or disable notification display.
- Test with access denied, granted, and revoked.

**Done when:** Supported notifications appear only when the user has enabled the feature and granted access.

### Milestone 5 — Appearance settings
- Add settings for colors, dimensions, corner radius, and animation.
- Apply changes immediately to the overlay.
- Add sensible defaults and reset-to-default behavior.

**Done when:** Appearance changes persist and are reflected without restarting the app.

### Milestone 6 — Reliability and release preparation
- Test lifecycle, rotation, display cutouts, different screen sizes, and Android versions.
- Check battery use and prevent unnecessary background work.
- Add empty states, error messages, and permission troubleshooting.
- Prepare app icon, screenshots, privacy disclosures, and release build.

**Done when:** The app handles common permission and lifecycle failures without crashing or leaving a stuck overlay.

## 13. Testing Checklist

- Overlay permission granted, denied, and revoked while running.
- App force-stopped and relaunched.
- Screen locked and unlocked.
- Rotation and display-size changes.
- Collapsed/expanded interaction and touch behavior.
- Timer start, pause, resume, stop, and completion.
- Notification access enabled and disabled.
- Overlay disabled from both the app and any applicable system notification.
- Battery and memory behavior during extended use.
- Verify the overlay does not interfere with important system controls.

## 14. Risks and Constraints

- Android overlay behavior and permission screens vary by OS version and manufacturer.
- Some devices may restrict background execution more aggressively.
- Notification access is sensitive and must be optional, clearly explained, and narrowly used.
- Overlay placement must account for cutouts, status bars, and differing screen dimensions.
- Some activity integrations may require separate platform APIs and should not be promised until validated.

## 15. Immediate Next Step

Start with **Milestone 1**: create the Kotlin/Compose project, define the shared activity and settings models, and build the Home screen with an in-app pill preview. Do not implement notification access or advanced integrations until the overlay proof of concept is stable.
