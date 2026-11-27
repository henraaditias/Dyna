---
name: Technical Precision Bento
colors:
  surface: '#131315'
  surface-dim: '#131315'
  surface-bright: '#39393b'
  surface-container-lowest: '#0e0e10'
  surface-container-low: '#1c1b1d'
  surface-container: '#201f22'
  surface-container-high: '#2a2a2c'
  surface-container-highest: '#353437'
  on-surface: '#e5e1e4'
  on-surface-variant: '#bdc8d1'
  inverse-surface: '#e5e1e4'
  inverse-on-surface: '#313032'
  outline: '#87929a'
  outline-variant: '#3e484f'
  surface-tint: '#7bd0ff'
  primary: '#8ed5ff'
  on-primary: '#00354a'
  primary-container: '#38bdf8'
  on-primary-container: '#004965'
  inverse-primary: '#00668a'
  secondary: '#99cbff'
  on-secondary: '#003355'
  secondary-container: '#054a77'
  on-secondary-container: '#87b9ed'
  tertiary: '#98d3ff'
  on-tertiary: '#00344d'
  tertiary-container: '#43bbff'
  on-tertiary-container: '#00486a'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#c4e7ff'
  primary-fixed-dim: '#7bd0ff'
  on-primary-fixed: '#001e2c'
  on-primary-fixed-variant: '#004c69'
  secondary-fixed: '#cfe5ff'
  secondary-fixed-dim: '#99cbff'
  on-secondary-fixed: '#001d34'
  on-secondary-fixed-variant: '#054a77'
  tertiary-fixed: '#c9e6ff'
  tertiary-fixed-dim: '#89ceff'
  on-tertiary-fixed: '#001e2f'
  on-tertiary-fixed-variant: '#004c6e'
  background: '#131315'
  on-background: '#e5e1e4'
  surface-variant: '#353437'
  canvas-bg: '#09090b'
  surface-card: '#0c0c0c'
  surface-raised: '#141416'
  surface-border: '#222222'
  border-subtle: rgba(255, 255, 255, 0.08)
  text-primary: '#ffffff'
  text-secondary: '#8b8b8b'
  text-muted: '#52525b'
  accent-cyan: '#38bdf8'
  accent-ice: '#60a5fa'
  accent-glow: rgba(56, 189, 248, 0.15)
  status-online: '#10b981'
typography:
  headline-xl:
    fontFamily: Geist
    fontSize: 3.5rem
    fontWeight: '600'
    lineHeight: '1.1'
    letterSpacing: -0.03em
  headline-xl-mobile:
    fontFamily: Geist
    fontSize: 2.25rem
    fontWeight: '600'
    lineHeight: '1.15'
    letterSpacing: -0.025em
  headline-lg:
    fontFamily: Geist
    fontSize: 2.25rem
    fontWeight: '500'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Geist
    fontSize: 1.75rem
    fontWeight: '500'
    lineHeight: '1.25'
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Geist
    fontSize: 1.5rem
    fontWeight: '500'
    lineHeight: '1.3'
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Geist
    fontSize: 1.125rem
    fontWeight: '500'
    lineHeight: '1.4'
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 1rem
    fontWeight: '400'
    lineHeight: '1.6'
    letterSpacing: -0.005em
  body-md:
    fontFamily: Inter
    fontSize: 0.875rem
    fontWeight: '400'
    lineHeight: '1.5'
    letterSpacing: 0em
  body-sm:
    fontFamily: Inter
    fontSize: 0.75rem
    fontWeight: '400'
    lineHeight: '1.4'
    letterSpacing: 0em
  label-code:
    fontFamily: JetBrains Mono
    fontSize: 0.8125rem
    fontWeight: '400'
    lineHeight: '1.5'
    letterSpacing: 0.02em
  label-mono-caps:
    fontFamily: JetBrains Mono
    fontSize: 0.6875rem
    fontWeight: '500'
    lineHeight: '1.2'
    letterSpacing: 0.08em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-desktop: 1.25rem
  margin: 1rem
  margin-tablet: 2rem
  margin-desktop: 3rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
  space-2xl: 3rem
---

## Brand & Style

This design system embodies high-craft digital engineering and disciplined product architecture. Merging technical developer minimalism with architectural precision, the aesthetic draws immediate inspiration from the low-noise console interfaces of OpenCode and the geometric, modular bento arrangements seen in Chroma. 

The aesthetic is built on:
- **Discipline & Clarity**: Pitch black surfaces punctuated by razor-sharp hairline borders, eliminating visual bloat and prioritizing pure layout integrity.
- **Architectural Modularity**: Distinct content cells framed within an explicit, visible grid structure reminiscent of blueprint technical drawings and modern terminal dashboards.
- **Developer Sophistication**: High contrast typography, monospace utility metadata, subtle glowing indicators, and electric cyan accents that communicate real-time precision and deep technical competence.

## Colors

The palette revolves around deep pitch black and zinc tones, establishing an environment of quiet confidence where every pixel and border carries purpose.

- **Primary Canvas & Surfaces**: The base backdrop relies on `#09090b` paired with `#0c0c0c` and `#141416` container fills. Contrast between parent canvas and nested cards is reinforced through sharp hairline borders (`#222222` or `rgba(255, 255, 255, 0.08)`) rather than heavy tonal jumps.
- **Electric Accents**: Cyan `#38bdf8` functions as the high-visibility focal driver for active states, link targets, selection states, and live telemetry badges. Secondary steel blue `#4e81b1` and ice blue `#60a5fa` soften secondary highlights and nested data points.
- **Text Hierarchy**: Foreground text uses absolute `#ffffff` for primary headlines and data values, dropping down to neutral `#8b8b8b` for body content, and `#52525b` for tertiary labels and grid coordinate trackers.

## Typography

The typographic hierarchy pairs crisp sans-serif letterforms with fixed-pitch monospaced utility type:

- **Primary Display & Headings (Geist)**: Delivers geometric, clinical legibility with slightly compressed horizontal rhythm and tight tracking (`-0.02em` to `-0.03em`), lending headers a deliberate technical edge.
- **Body Copy (Inter)**: Handles explanatory project paragraphs, case study reflections, and lists with neutral legibility and balanced line heights.
- **Labels, Tags & Code Accents (JetBrains Mono)**: Applied universally to section headers, coordinate badges, status indicators, file paths, and metadata keys. Section titles and micro-labels utilize all-caps with generous letter-spacing (`0.08em`) to evoke technical schematics.

## Layout & Spacing

The layout model is founded strictly upon a modular bento grid structure:

- **Desktop (1024px+)**: 12-column grid inside a maximum container width of `1280px`. Cells adapt to variable column spans (e.g., 4-col, 6-col, 8-col, or full 12-col spans) with consistent gutters (`1.25rem`). Cards align on identical horizontal axes to retain seamless hairline grid borders.
- **Tablet (768px - 1023px)**: Reflows into an 8-column or 6-column bento configuration with `2rem` side canvas margins.
- **Mobile (< 768px)**: Stacks down into a single fluid column with a strict `1rem` outer margin. Bento cells preserve vertical rhythm with compact internal padding (`space-md`).
- **Internal Density**: Bento cards maintain a structured header-content-footer division using `space-lg` padding on desktop and `space-md` on mobile.

## Elevation & Depth

Visual hierarchy rejects heavy drop shadows in favor of precise structural boundaries and optical luminance:

- **Hairline Outlines**: Boundaries are articulated via 1px solid borders (`#222222` or `rgba(255, 255, 255, 0.08)`). No card floats without a visible frame.
- **Tonal Contrast**: Hover states elevate subtly by shifting the background from `#0c0c0c` to `#141416`, while simultaneously brightening the border to `rgba(255, 255, 255, 0.18)`.
- **Cyan Glow Effects**: Reserved strictly for interactive anchors and active indicators. Focused or hovered interactive nodes produce a soft, diffuse cyan perimeter glow (`0 0 16px rgba(56, 189, 248, 0.12)`).
- **Subtle Surface Grid Pattern**: Background canvas layers may include a repeating 24px micro-dot or faint Cartesian grid overlay rendered at `rgba(255, 255, 255, 0.025)` to reinforce the console theme.

## Shapes

The design system enforces a restrained, soft-cornered geometry (Level 1):

- **Bento Cells & Outer Containers**: Border radius is fixed at `0.5rem` (`rounded-lg`), delivering crisp architectural framing without appearing overly circular or overly sharp.
- **Nested Controls, Badges & Chips**: Form controls, buttons, and micro-tags carry `0.25rem` radius, preserving their mechanical, tool-grade feel.
- **Full Pill Exceptions**: Status indicator dots and toggles leverage complete rounding (`rounded-full`) purely as functional counterpoints.

## Components

### Bento Grid Cards
- **Structure**: High-density containers with `1px solid #222222` borders and `#0c0c0c` background fills. Each card features an optional header zone containing uppercase monospace metadata (`label-mono-caps`) and right-aligned action icons.
- **Interaction**: On pointer hover, the border brightens to `rgba(255, 255, 255, 0.18)` and an optional internal radial light follows the cursor position.

### Buttons & Interactive Triggers
- **Primary Button**: Solid white `#ffffff` fill with pitch black `#000000` text, weight 500, `0.25rem` radius, and compact padding (`0.5rem 1rem`). On hover, transitions to subtle off-white `#f4f4f5` with an instant, zero-lag feel.
- **Secondary / Console Button**: Dark fill `#141416`, `1px solid #222222`, text `#ffffff`. Hover shifts border to `rgba(255, 255, 255, 0.25)`.
- **Text Link / Arrow Action**: Cyan `#38bdf8` typography accompanied by directional glyphs (`->`), animating rightward by 2px on hover.

### Monospace Tags & Status Badges
- **Status Badges**: Pill or soft-square tags featuring a `6px` glowing dot (`#10b981` or `#38bdf8`), accompanied by `label-mono-caps` copy (e.g., `SYSTEM: READY`, `STABLE`, `v2.4.0`).
- **Code Chips**: Enclosed in `#141416` with `1px solid #222222` outline, rendered in JetBrains Mono at `0.8125rem`.

### Form Controls & Switches
- **Input Fields**: Inset dark background `#09090b` framed with `#222222`. Placeholder text in `#52525b`. On focus, the border shifts to `#38bdf8` with an ambient glow (`box-shadow: 0 0 0 1px #38bdf8`).
- **Toggle Switches**: Compact rectangular toggles with inset channel track (`#141416`) and a crisp white/cyan thumb indicator, inspired directly by OpenCode console switch configurations.

### Code & Terminal Blocks
- **Inline Console Panes**: Dark surface blocks featuring faint dotted grid backgrounds, command prompt symbols (`$`, `>`), syntax tokens highlighted in `#38bdf8` and `#60a5fa`, and copy actions pinned to the top-right corner.

---

## Android Dynamic Island Design Extension

This section extends the original Technical Precision Bento design system for the Dyna Android application. All original tokens and design rules above remain part of the project. The additions below define how those rules apply to the native Android dashboard and the floating Dynamic Island overlay.

## Product & Interface Principles

- **Native Android first**: The settings application must feel like a polished Android utility built with Kotlin and Jetpack Compose. Use native system behavior for permissions, navigation, and accessibility.
- **Overlay is the product centerpiece**: The floating island is the primary visual element. Keep the dashboard focused on configuring, previewing, and managing it.
- **Real states, not decoration**: Idle, expanded, detailed, notification, and timer views must represent real application state. Do not show fake live notifications or unsupported controls.
- **Customizable, but coherent**: Users may change the island's theme and geometry, while spacing, contrast, touch targets, and motion remain readable and usable.
- **Progressive disclosure**: Show essential information first. Reveal secondary information and controls only when the island expands or the user opens a detailed view.

## Android Color Mapping

Retain the original Technical Precision Bento tokens as the default dashboard palette. The following mapping defines their intended use in Dyna:

- **Application canvas**: `canvas-bg` (`#09090b`).
- **Main dashboard surfaces**: `surface-card` (`#0c0c0c`) and `surface-raised` (`#141416`).
- **Nested settings surfaces**: `surface-container-low` through `surface-container-highest`.
- **Primary text**: `text-primary` (`#ffffff`) or `on-surface` (`#e5e1e4`).
- **Secondary text**: `text-secondary` (`#8b8b8b`) and `on-surface-variant` (`#bdc8d1`).
- **Primary interaction accent**: `primary` / `accent-cyan` (`#38bdf8`).
- **Selected and active indicators**: `accent-cyan` with restrained `accent-glow`.
- **Success and permission-ready state**: `status-online` (`#10b981`).
- **Error and permission failure**: `error` (`#ffb4ab`) and `error-container` (`#93000a`).

The overlay has its own appearance settings and must not automatically inherit every dashboard surface color. By default, the island uses a near-black fill, white foreground content, and a cyan accent for active progress or selected controls. The user-selected theme may override these defaults.

## Dynamic Island Visual States

### Idle

- Render a compact horizontal pill centered near the top edge of the display, below the status-bar safe area as appropriate for the device.
- Use a near-black fill and a fully rounded shape.
- Keep idle content minimal: a small status indicator or optional user-selected icon.
- Avoid unnecessary glow, labels, or animation while idle.
- The idle pill must remain visually distinct from the Android status bar and must not obscure important system indicators.

### Notification Expanded

- Expand horizontally and, when needed, vertically with a smooth spring transition.
- Show the source app icon, a short title, and a concise preview when allowed by notification visibility settings.
- Keep text to a readable maximum width; truncate long content rather than making the island excessively wide.
- Use a restrained cyan highlight for the active notification state.
- If several notifications arrive, prioritize the newest eligible notification and provide a clear route to additional notifications.

### Timer Expanded

- Display a legible remaining-time value as the main element.
- Place timer controls in a stable, predictable order: pause/resume, then stop.
- Use a subtle progress indicator only when it improves time comprehension.
- At completion, show a clear finished state and respect the user's sound and vibration preferences.

### Detailed Activity

- Use a wider, taller surface only when the user explicitly opens the activity.
- Separate activity information from its controls using spacing or a hairline divider.
- Provide a clear collapse or dismiss action.
- Do not imply that third-party activity controls work unless the relevant Android API or app integration is available.

### Transition Rules

- Use short, smooth transitions for compact-to-expanded changes.
- Use spring motion with restrained overshoot; avoid bouncy or playful motion.
- Fade and translate secondary content slightly after the container begins expanding.
- Preserve the island's top-center anchor during width and height changes.
- Respect Android animation scale settings and provide reduced-motion behavior where feasible.
- Never animate the island continuously when there is no active event.

## Overlay Geometry & Touch Behavior

- The overlay's default width, height, corner radius, and vertical offset must be defined as design tokens and exposed through customization controls where supported.
- Keep the island horizontally centered by default.
- Provide a safe default position that avoids camera cutouts and important system status indicators across different Android devices.
- Use window insets and display cutout information rather than assuming every device has the same top-center camera layout.
- Maintain sufficient touch target sizes for interactive controls. Compact mode may be visually small, but controls must remain accessible when expanded.
- Prevent the overlay from intercepting touches outside its visible and interactive bounds.
- Support tap to expand, tap to collapse, and appropriate swipe gestures without conflicting with system navigation.
- Do not create a full-screen transparent touch layer.

## Dashboard Information Architecture

### Beranda

- A top app bar with the Dyna name and a concise status indicator.
- A prominent live island preview card.
- A clear overlay enable/disable switch.
- Permission status for overlay access and notification access.
- Quick links to Tampilan, Tema, Notifikasi, and Aktivitas.
- Use a clear empty state when permissions have not been granted.

### Tampilan

- Group geometry settings together: width, height, corner radius, and position.
- Group behavior settings together: animation speed, expansion duration, and auto-collapse.
- Show the live preview near the controls on larger screens and above the controls on narrow screens.
- Use sliders for continuous values and segmented controls or choices for discrete options.
- Show numeric values when they help the user understand a setting.
- Provide a reset-to-default action for this section.

### Tema

- Show theme options as preview cards, not text-only names.
- Include Midnight, Minimal, Aurora, and Custom as the initial theme choices.
- Clearly indicate the currently selected theme.
- Provide color controls for background, text, accent, and icon colors in Custom.
- Preview both idle and expanded states so users understand the effect of a theme.
- Keep theme selection independent from the dashboard's fixed readability and accessibility requirements.

### Notifikasi

- Explain why notification access is needed before sending the user to Android settings.
- Show permission status and a direct action to open the relevant system settings.
- Organize app filtering, preview privacy, expansion behavior, and duration into separate groups.
- Make disabled and unavailable controls visually distinct from active controls.
- Never show private notification content in the settings preview unless the user explicitly enables a sample preview.

### Aktivitas

- Present the timer as the first supported activity.
- Use a clear activity status and simple start, pause, resume, and stop actions.
- List future integration categories only as planned or unavailable; do not present them as working features.
- Keep each activity integration modular so future additions do not require redesigning the whole screen.

### Pengaturan

- Include overlay startup behavior, restart overlay, reset customization, and about information.
- Use confirmation for destructive or reset actions.
- Explain system-level restrictions in plain Indonesian.
- Keep technical diagnostics secondary to user-facing controls.

## Component Specifications

### Island Preview Card

- Use a dark, bordered bento card consistent with the original design system.
- Include a small monospace status label such as `PREVIEW / IDLE`.
- Provide a compact preview area showing the island in its selected theme.
- Include preview state controls for Idle, Notifikasi, and Timer.
- Preview controls must be clearly labeled as simulations when they are not connected to real system events.
- Do not allow preview simulations to create fake Android notifications.

### Permission Status Row

- Include a permission name, concise explanation, current status, and one action.
- Use `status-online` only when permission is actually granted.
- Use a neutral state for not-yet-requested permissions and an error state only when appropriate.
- Keep the action label explicit, such as `Buka Pengaturan`.
- Do not repeatedly launch system settings without a user action.

### Setting Row

- Use a title, short supporting description, and the appropriate control.
- Align labels and controls consistently.
- Use hairline dividers between related settings rather than enclosing every row in a separate card.
- Keep destructive actions visually separate from everyday settings.

### Theme Preview Tile

- Show a miniature island preview using the theme's actual colors.
- Include the theme name and selected state.
- Use a cyan outline or selected indicator without adding unnecessary glow.
- Ensure the preview remains legible at small sizes.

### Notification Activity Card

- Show the app icon, app name, title, and permitted preview text.
- Keep notification actions limited to those actually supported by the source notification.
- Provide a clear visual distinction between an active notification and a completed or dismissed item.
- Avoid showing sensitive notification text on the dashboard by default.

### Timer Activity Card

- Display remaining time using tabular numerals where available.
- Keep pause/resume and stop actions clearly separated.
- Use cyan for active progress and neutral text for supporting information.
- Use the error color only if a real error occurs; timer completion is not an error.

## Typography in the Android Application

Retain the original typography tokens and use them as the source of truth. Map them to Jetpack Compose typography styles with device-appropriate scaling.

- Use Geist-style display typography for large dashboard headings where the font is available; otherwise use a close system sans-serif fallback.
- Use Inter-style body typography for descriptions and settings text where available; otherwise use a readable system sans-serif fallback.
- Use JetBrains Mono for technical labels, status metadata, and diagnostic values where available; otherwise use a system monospace fallback.
- Do not use monospace for long explanatory paragraphs.
- Respect Android font scaling and avoid fixed-height text containers that clip enlarged text.
- Keep the overlay's text hierarchy simpler than the dashboard's hierarchy.

## Responsive Behavior

Although Dyna is an Android application, it must adapt to different device sizes and orientations.

- **Compact phones**: Use a single-column settings layout with a full-width preview and grouped setting rows.
- **Large phones and foldables**: Allow wider preview cards and two-column settings groups where appropriate.
- **Landscape**: Keep the preview visible without forcing the user to scroll past an oversized header.
- **Display cutouts**: Use actual window insets and cutout data to position the overlay safely.
- **Font scaling**: Allow content to grow vertically; do not truncate setting labels to preserve a rigid grid.
- **Overlay sizing**: Use device-independent units and screen bounds to keep the island within the display.

## Accessibility & Usability

- Maintain readable contrast for all text and controls.
- Provide content descriptions for meaningful icons and overlay actions.
- Ensure controls can be reached with Android accessibility services.
- Do not communicate permission state, selection, or errors through color alone.
- Support system font scaling and sensible touch targets.
- Provide clear labels for switches, sliders, and color controls.
- Respect reduced-motion preferences where feasible.
- Ensure the expanded island remains usable with screen readers.
- Avoid relying on swipe gestures as the only way to perform an action.

## Empty, Loading, Error & Permission States

Design and implement consistent states for:

- Overlay permission not granted.
- Notification access not granted.
- Overlay service stopped.
- No active notification.
- No active timer.
- Notification access unavailable or revoked.
- Activity integration unsupported.
- Settings reset in progress.
- An unexpected overlay or service error.

Each state must explain what happened in concise Indonesian and offer a relevant next step when one exists. Do not show success indicators until the corresponding Android operation succeeds.

## Motion & Feedback

- Use motion to communicate state changes, not as decoration.
- Keep expansion and collapse transitions consistent across notification and timer activities.
- Use subtle haptic feedback only for meaningful user actions and only when supported.
- Avoid repeated vibration or sound for ordinary overlay transitions.
- Provide immediate visual feedback for toggles and setting changes.
- Persist settings after the user changes them, with no misleading success message before persistence completes.

## Theme Customization Constraints

- User customization may change island appearance, but must not change system permission behavior or bypass Android restrictions.
- Keep a safe default theme available at all times.
- Validate custom colors for readable foreground/background contrast.
- Provide a reset option that restores the default theme and geometry.
- Persist selected theme and custom values locally.
- Apply changes to the live overlay and preview consistently.
- If a theme setting cannot be applied to a particular Android state, explain the limitation rather than silently ignoring it.

## Design Deliverables

The implementation should include:

1. A reusable Jetpack Compose design system based on the original color, typography, shape, and spacing tokens.
2. A native Android dashboard with the screens described above.
3. A live island preview with clearly distinguished simulated states.
4. A real floating overlay with idle and expanded visual states.
5. Reusable components for permission status, setting rows, theme previews, notification cards, and timer controls.
6. A documented mapping between design tokens and Compose theme values.
7. A clear separation between dashboard styling and user-customizable overlay styling.

This extension is additive. Preserve the original Technical Precision Bento design system and use these specifications to adapt it to Dyna's Android-specific interface and behavior.
