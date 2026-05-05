# Mobile Phase 1 — Design System & UI Foundation

> **Goal:** Establish the complete visual design system for the Android app before writing any feature code or backend connections. Everything in this phase is pure UI — colors, typography, spacing, components, and screen structure. No API calls. No mock data replacement. Just the design foundation.
>
> **Status:** COMPLETE (font files pending manual download)
> **Depends on:** Nothing (this is the starting point)
> **Blocks:** All other phases (everything builds on this)

---

## Table of Contents

1. [Why Design First](#1-why-design-first)
2. [Color System](#2-color-system)
3. [Typography](#3-typography)
4. [Spacing & Shape](#4-spacing--shape)
5. [Elevation & Shadows](#5-elevation--shadows)
6. [Theme System (Light & Dark)](#6-theme-system-light--dark)
7. [Material 3 Color Scheme Mapping](#7-material-3-color-scheme-mapping)
8. [Component Design Spec](#8-component-design-spec)
9. [App Shell Design](#9-app-shell-design)
10. [Screen-by-Screen Design Structure](#10-screen-by-screen-design-structure)
11. [Current Theme Issues to Fix](#11-current-theme-issues-to-fix)
12. [Deliverables Checklist](#12-deliverables-checklist)

---

## 1. Why Design First

The current mobile theme (`Color.kt`, `Theme.kt`, `Type.kt`) has several problems:

| Problem | Detail |
|---------|--------|
| **Wrong color scheme** | `Theme.kt` still uses default Purple/Pink Material colors — not the UniLost slate palette |
| **Dynamic color enabled** | Android 12+ dynamic color is ON, which overrides the custom colors entirely |
| **Missing colors** | `Color.kt` only has 8 colors; the web has 40+ semantic color tokens |
| **No dark mode colors** | Dark theme uses the purple defaults, not the web's `#0f172a` dark palette |
| **Typography not set** | `Type.kt` only defines `bodyLarge`; all other text styles use Material defaults |
| **No font branding** | Web uses **Inter** (body) + **Outfit** (display/headings); mobile uses system font |
| **No spacing system** | No defined spacing scale; each screen uses ad-hoc padding values |

This phase fixes all of that so every screen built afterward is consistent, branded, and matches the web.

---

## 2. Color System

### 2.1 Raw Color Palette (Kotlin Constants)

These are the base hex values extracted from the web's `themes.css`. Define all of these in `Color.kt`.

```
── Slate Scale ──
Slate050  #f8fafc
Slate100  #f1f5f9   ← Light mode background
Slate200  #e2e8f0   ← Borders (light mode)
Slate300  #cbd5e1   ← Borders strong (light mode)
Slate400  #94a3b8   ← Text muted / Primary lighter
Slate500  #64748b   ← Primary light
Slate600  #475569   ← PRIMARY (light mode)
Slate700  #334155   ← Primary hover / Dark bg card
Slate800  #1e293b   ← Text primary (light) / Dark bg card elevated
Slate900  #0f172a   ← Dark mode background

── Sage Green (Secondary) ──
Sage300   #a3c1ad   ← Secondary light
Sage400   #84a98c   ← SECONDARY
Sage500   #52796f   ← Secondary hover

── Status Colors ──
Success   #10b981   ← FOUND items, handover completed
SuccessHover #059669
SuccessBg rgba(16,185,129, 0.08)
SuccessText #065f46  (light)  /  #6ee7b7  (dark)

Warning   #f59e0b   ← Pending claims, flagged items
WarningHover #d97706
WarningBg rgba(245,158,11, 0.08)
WarningText #92400e  (light)  /  #fcd34d  (dark)

Error     #ef4444   ← LOST items, errors, destructive actions
ErrorHover  #dc2626
ErrorBg   rgba(239,68,68, 0.08)
ErrorText #991b1b  (light)  /  #fca5a5  (dark)

Info      #3b82f6   ← Active items, informational
InfoHover #2563eb
InfoBg    rgba(59,130,246, 0.08)
InfoText  #1e40af  (light)  /  #93c5fd  (dark)

── Neutrals ──
White     #ffffff
Black     #000000
Transparent rgba(0,0,0,0)
```

### 2.2 Semantic Color Tokens

Map raw colors to **named semantic tokens** so screens never hardcode hex values. These are the values components reference, not the raw palette.

#### Light Mode Semantic Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `colorPrimary` | `Slate600` `#475569` | Buttons, links, active nav tab, filled elements |
| `colorPrimaryHover` | `Slate700` `#334155` | Button pressed state |
| `colorPrimaryLight` | `Slate500` `#64748b` | Secondary text, subtitle |
| `colorPrimaryLighter` | `Slate400` `#94a3b8` | Placeholder text, muted icons |
| `colorPrimaryBg` | `Slate600 @ 8%` | Chip backgrounds, subtle tints |
| `colorSecondary` | `Sage400` `#84a98c` | Karma score, success states, FOUND item accent |
| `colorSecondaryHover` | `Sage500` `#52796f` | Secondary button pressed |
| `colorSecondaryLight` | `Sage300` `#a3c1ad` | Secondary tint |
| `colorBgBase` | `Slate100` `#f1f5f9` | Screen background |
| `colorBgCard` | `White` `#ffffff` | Card background, bottom sheets |
| `colorBgInput` | `White` `#ffffff` | Input field background |
| `colorBgElevated` | `White` `#ffffff` | Dialogs, modals, elevated surfaces |
| `colorBgOverlay` | `Slate900 @ 50%` | Modal scrim/backdrop |
| `colorBgHover` | `Black @ 4%` | Row hover/ripple tint |
| `colorTextPrimary` | `Slate800` `#1e293b` | Main text, headings |
| `colorTextSecondary` | `Slate600` `#475569` | Subtitles, secondary labels |
| `colorTextMuted` | `Slate400` `#94a3b8` | Captions, placeholders, timestamps |
| `colorTextInverse` | `White` `#ffffff` | Text on dark/colored backgrounds |
| `colorBorder` | `Slate200` `#e2e8f0` | Dividers, card outlines |
| `colorBorderFocus` | `Slate600` `#475569` | Input focus ring |
| `colorBorderStrong` | `Slate300` `#cbd5e1` | Stronger dividers |
| `colorSuccess` | `#10b981` | FOUND item badge, handover complete |
| `colorWarning` | `#f59e0b` | Pending claim badge, flagged |
| `colorError` | `#ef4444` | LOST item badge, errors, delete actions |
| `colorInfo` | `#3b82f6` | Active item badge, informational toasts |

#### Dark Mode Semantic Tokens

| Token | Light Value → | Dark Value |
|-------|--------------|------------|
| `colorPrimary` | `#475569` | `Slate400` `#94a3b8` |
| `colorPrimaryHover` | `#334155` | `Slate300` `#cbd5e1` |
| `colorBgBase` | `#f1f5f9` | `Slate900` `#0f172a` |
| `colorBgCard` | `#ffffff` | `Slate800` `#1e293b` |
| `colorBgInput` | `#ffffff` | `Slate900` `#0f172a` |
| `colorBgElevated` | `#ffffff` | `Slate800` `#1e293b` |
| `colorBgHover` | `Black @ 4%` | `White @ 5%` |
| `colorTextPrimary` | `#1e293b` | `Slate050` `#f8fafc` |
| `colorTextSecondary` | `#475569` | `Slate300` `#cbd5e1` |
| `colorTextMuted` | `#94a3b8` | `Slate500` `#64748b` |
| `colorTextInverse` | `#ffffff` | `Slate900` `#0f172a` |
| `colorBorder` | `#e2e8f0` | `Slate700` `#334155` |
| `colorBorderFocus` | `#475569` | `Slate400` `#94a3b8` |
| `colorBorderStrong` | `#cbd5e1` | `Slate600` `#475569` |
| `colorSuccessText` | `#065f46` | `#6ee7b7` |
| `colorWarningText` | `#92400e` | `#fcd34d` |
| `colorErrorText` | `#991b1b` | `#fca5a5` |
| `colorInfoText` | `#1e40af` | `#93c5fd` |

### 2.3 Status / Item Type Color Mapping

Consistent badge colors used throughout the app:

| Status / Type | Color | Hex | Usage |
|---------------|-------|-----|-------|
| **LOST** | Error Red | `#ef4444` | Item type badge, LOST section headers |
| **FOUND** | Secondary Sage | `#84a98c` | Item type badge, FOUND section headers |
| **ACTIVE** | Info Blue | `#3b82f6` | Item status chip |
| **CLAIMED** | Warning Amber | `#f59e0b` | Item status chip |
| **PENDING** | Warning Amber | `#f59e0b` | Claim status chip |
| **ACCEPTED** | Success Green | `#10b981` | Claim status chip |
| **REJECTED** | Error Red | `#ef4444` | Claim status chip |
| **RETURNED / COMPLETED** | Success Green | `#10b981` | Claim / item status chip |
| **EXPIRED** | Muted Slate | `#94a3b8` | Item status chip |
| **HIDDEN / DELETED** | Muted Slate | `#94a3b8` | Item status chip |
| **DISPUTED** | Warning Amber | `#f59e0b` | Handover dispute chip |
| **ADMIN role** | Primary Slate | `#475569` | Role badge |
| **FACULTY role** | Info Blue | `#3b82f6` | Role badge |
| **STUDENT role** | Sage Green | `#84a98c` | Role badge |

---

## 3. Typography

### 3.1 Font Families

The web uses two fonts — both must be added to the Android project as downloadable fonts or bundled `.ttf` files:

| Role | Font | Weights Needed | Android Source |
|------|------|----------------|----------------|
| **Body / UI text** | **Inter** | 300, 400, 500, 600, 700 | Google Fonts downloadable font |
| **Display / Headings** | **Outfit** | 400, 500, 600, 700 | Google Fonts downloadable font |

> **Implementation:** Use `res/font/` directory with `FontFamily` definition in Kotlin, or use the Google Fonts provider via `Downloadable Fonts` in Android Studio.

### 3.2 Type Scale (mapped from web tokens)

All sizes use `sp` units in Android (scales with user accessibility settings).

| Role | Web Size | Android sp | Font | Weight | Line Height | Usage |
|------|----------|------------|------|--------|-------------|-------|
| `displayLarge` | 48px | 40sp | Outfit | Bold 700 | 1.25 | Hero banners only |
| `displayMedium` | 32px | 28sp | Outfit | Bold 700 | 1.25 | Landing hero |
| `headlineLarge` | 24px | 22sp | Outfit | Bold 700 | 1.25 | Screen titles |
| `headlineMedium` | 20px | 18sp | Outfit | SemiBold 600 | 1.25 | Section headings |
| `headlineSmall` | 18px | 16sp | Outfit | SemiBold 600 | 1.25 | Card titles, dialog headings |
| `titleLarge` | 16px | 15sp | Outfit | SemiBold 600 | 1.4 | List item titles |
| `titleMedium` | 14px | 14sp | Inter | Medium 500 | 1.4 | Sub-section labels |
| `bodyLarge` | 16px | 16sp | Inter | Normal 400 | 1.5 | Primary body text |
| `bodyMedium` | 14px | 14sp | Inter | Normal 400 | 1.5 | Secondary body, descriptions |
| `bodySmall` | 12px | 12sp | Inter | Normal 400 | 1.5 | Captions, helper text |
| `labelLarge` | 14px | 14sp | Inter | Medium 500 | 1.25 | Button labels |
| `labelMedium` | 12px | 12sp | Inter | Medium 500 | 1.25 | Status chips, nav labels |
| `labelSmall` | 10px | 10sp | Inter | Medium 500 | 1.25 | Tiny badges, timestamps |

### 3.3 Typography Behavior

- **Letter spacing:** Slightly negative for display sizes (−0.25sp); 0 for body; +0.5sp for label sizes
- **Headings:** Always use `colorTextPrimary`
- **Body:** Use `colorTextPrimary` or `colorTextSecondary`
- **Captions/timestamps:** Use `colorTextMuted`
- **Inverse text:** Use `colorTextInverse` (white) on dark/colored backgrounds

---

## 4. Spacing & Shape

### 4.1 Spacing Scale

Consistent padding/margin values mapped from web `tokens.css`:

| Token | Value | dp | Usage |
|-------|-------|----|-------|
| `spacing2xs` | 2px | 2dp | Tiny gaps, icon-text gap |
| `spacingXs` | 4px | 4dp | Chip internal padding, small gaps |
| `spacingSm` | 8px | 8dp | Card internal padding small, icon margins |
| `spacingMd` | 16px | 16dp | Standard content padding, card body padding |
| `spacingLg` | 24px | 24dp | Section spacing, card vertical padding |
| `spacingXl` | 32px | 32dp | Large section gaps, hero padding |
| `spacing2xl` | 48px | 48dp | Screen top/bottom padding |
| `spacing3xl` | 64dp | 64dp | Hero banner heights |

> **Screen edge padding:** `spacingMd` (16dp) on left/right for all content.
> **Card internal padding:** `spacingMd` (16dp) all sides, or `spacingLg` (24dp) for larger cards.

### 4.2 Border Radius Scale

| Token | Value | dp | Usage |
|-------|-------|----|-------|
| `radiusXs` | 4px | 4dp | Small tags, tight chips |
| `radiusSm` | 6px | 6dp | Buttons (small), inner elements |
| `radiusMd` | 12px | 12dp | Cards, input fields, standard chips |
| `radiusLg` | 16px | 16dp | Large cards, bottom sheets, modals |
| `radiusXl` | 24px | 24dp | Hero cards, image cards |
| `radiusFull` | 9999px | 9999dp | Pill badges, avatar circles, FABs |

### 4.3 Minimum Touch Target

All interactive elements: **minimum 48dp × 48dp** (Android accessibility guideline).
- Icon buttons: 48dp container even if icon is 24dp
- Chips/badges that are tappable: minimum 36dp height, 12dp horizontal padding

---

## 5. Elevation & Shadows

Material 3 uses elevation tonal overlays. Map web shadows to Android elevation levels:

| Web Shadow | Android Elevation | Usage |
|------------|-------------------|-------|
| `shadow-xs` (0 1px 2px) | 1dp | Subtle cards, list items |
| `shadow-sm` (0 1px 2px stronger) | 2dp | Standard cards |
| `shadow-md` (0 4px 6px) | 4dp | Floating chips, filter bars |
| `shadow-lg` (0 10px 15px) | 8dp | Bottom sheets, app bar on scroll |
| `shadow-xl` (0 20px 25px) | 12dp | Dialogs, modals |

> In **dark mode**, tonal elevation (surface color lightening) replaces shadows — Material 3 handles this automatically when `colorScheme.surface` is correctly defined.

---

## 6. Theme System (Light & Dark)

### 6.1 Theme Architecture

The app must support both **Light** and **Dark** modes, following the same logic as the web:

```
System dark mode setting
         │
         ▼
   isSystemInDarkTheme()
         │
    ┌────┴────┐
    │         │
 Dark Mode  Light Mode
    │         │
    ▼         ▼
UniLostDark  UniLostLight
ColorScheme  ColorScheme
```

**Important:** Disable `dynamicColor` — it overrides the custom palette on Android 12+. The app must always use UniLost's brand colors regardless of system wallpaper.

### 6.2 Theme Toggle (User Preference)

- User can manually toggle Light/Dark from **Settings screen**
- Preference stored in **DataStore** (same as token storage)
- Overrides system setting when explicitly set
- Default: follow system setting

### 6.3 Status Bar & Navigation Bar

| Mode | Status Bar Color | Navigation Bar Color | Icon Style |
|------|-----------------|---------------------|------------|
| **Light** | `colorBgCard` `#ffffff` (or `Slate700` for opaque) | `colorBgCard` `#ffffff` | Dark icons |
| **Dark** | `Slate900` `#0f172a` | `Slate900` `#0f172a` | Light icons |

Use `WindowCompat.setDecorFitsSystemWindows(window, false)` for edge-to-edge layout with proper insets.

---

## 7. Material 3 Color Scheme Mapping

Material 3's `ColorScheme` has specific named roles. Map UniLost semantic tokens to these roles:

### Light ColorScheme

```kotlin
lightColorScheme(
  // Primary role → UniLost slate blue
  primary          = Slate600,         // #475569  — buttons, FAB, active states
  onPrimary        = White,            // #ffffff  — text/icon on primary
  primaryContainer = Slate100,         // #f1f5f9  — chips, selected backgrounds
  onPrimaryContainer = Slate800,       // #1e293b  — text on primary container

  // Secondary role → UniLost sage green
  secondary        = Sage400,          // #84a98c  — secondary buttons, karma
  onSecondary      = White,            // #ffffff
  secondaryContainer = Sage300_8pct,   // sage @ 8% opacity
  onSecondaryContainer = Sage500,      // #52796f

  // Tertiary → Info blue (for special accents)
  tertiary         = Info,             // #3b82f6
  onTertiary       = White,            // #ffffff
  tertiaryContainer = Info_8pct,       // info @ 8%
  onTertiaryContainer = InfoText,      // #1e40af

  // Error role → already matches web
  error            = Error,            // #ef4444
  onError          = White,
  errorContainer   = ErrorBg,          // error @ 8%
  onErrorContainer = ErrorText,        // #991b1b

  // Background & Surface
  background       = Slate100,         // #f1f5f9
  onBackground     = Slate800,         // #1e293b
  surface          = White,            // #ffffff  — cards, sheets
  onSurface        = Slate800,         // #1e293b
  surfaceVariant   = Slate100,         // #f1f5f9  — input backgrounds
  onSurfaceVariant = Slate600,         // #475569

  // Outline
  outline          = Slate200,         // #e2e8f0  — borders, dividers
  outlineVariant   = Slate300,         // #cbd5e1  — strong borders

  // Inverse (for snackbars, tooltips)
  inverseSurface   = Slate800,         // #1e293b
  inverseOnSurface = White,            // #ffffff
  inversePrimary   = Slate400,         // #94a3b8

  // Scrim (modal overlay)
  scrim            = Slate900_50pct,   // #0f172a @ 50%
)
```

### Dark ColorScheme

```kotlin
darkColorScheme(
  primary          = Slate400,         // #94a3b8  — lighter for dark bg
  onPrimary        = Slate900,         // #0f172a
  primaryContainer = Slate700,         // #334155
  onPrimaryContainer = Slate100,       // #f1f5f9

  secondary        = Sage300,          // #a3c1ad
  onSecondary      = Slate900,
  secondaryContainer = Sage400_10pct,
  onSecondaryContainer = Sage300,

  tertiary         = Info,             // #3b82f6 (same)
  onTertiary       = White,
  tertiaryContainer = Info_12pct,
  onTertiaryContainer = InfoTextDark,  // #93c5fd

  error            = Error,            // #ef4444 (same)
  onError          = Slate900,
  errorContainer   = Error_12pct,
  onErrorContainer = ErrorTextDark,    // #fca5a5

  background       = Slate900,         // #0f172a
  onBackground     = Slate050,         // #f8fafc
  surface          = Slate800,         // #1e293b
  onSurface        = Slate050,         // #f8fafc
  surfaceVariant   = Slate900,         // #0f172a  — input backgrounds
  onSurfaceVariant = Slate300,         // #cbd5e1

  outline          = Slate700,         // #334155
  outlineVariant   = Slate600,         // #475569

  inverseSurface   = Slate100,         // #f1f5f9
  inverseOnSurface = Slate800,         // #1e293b
  inversePrimary   = Slate600,         // #475569

  scrim            = Black_60pct,
)
```

---

## 8. Component Design Spec

Design rules for every reusable component. These specs define what the component looks like; actual Kotlin code is written in subsequent phases.

### 8.1 Buttons

| Variant | Background | Text | Border | Use Case |
|---------|-----------|------|--------|----------|
| **Primary (Filled)** | `colorPrimary` `#475569` | `colorTextInverse` White | None | Main actions: Login, Submit, Save |
| **Secondary (Outlined)** | Transparent | `colorPrimary` | `colorBorder` 1dp | Secondary actions: Cancel, Register |
| **Danger (Filled)** | `colorError` `#ef4444` | White | None | Destructive: Delete, Reject |
| **Ghost / Text** | Transparent | `colorPrimary` | None | Tertiary actions: "Skip", "Learn more" |
| **Disabled** | `colorBorder` | `colorTextMuted` | None | All variants when disabled |

**Sizing:**
- Height: 48dp (standard), 40dp (compact in dense UIs)
- Corner radius: `radiusMd` (12dp)
- Horizontal padding: `spacingMd` (16dp)
- Font: `labelLarge` (Inter Medium 14sp)
- Icon + label gap: `spacingSm` (8dp)

**States:** Normal → Pressed (darken 10%) → Loading (spinner replaces label) → Disabled

---

### 8.2 Input Fields

```
┌─────────────────────────────────────────────┐
│  🔍  Search items...                        │  ← placeholder (colorTextMuted)
└─────────────────────────────────────────────┘
  ↑ 1dp border (colorBorder)     ↑ radius 12dp
  On focus: border becomes 2dp (colorBorderFocus = colorPrimary)
```

| State | Border | Background | Label |
|-------|--------|-----------|-------|
| **Default** | 1dp `colorBorder` | `colorBgInput` | `colorTextMuted` (placeholder) |
| **Focused** | 2dp `colorBorderFocus` | `colorBgInput` | `colorPrimary` (floating label) |
| **Filled** | 1dp `colorBorder` | `colorBgInput` | `colorTextSecondary` (floating label) |
| **Error** | 2dp `colorError` | `colorBgInput` | `colorError` (label + helper text) |
| **Disabled** | 1dp `colorBorder @ 50%` | `colorBgBase` | `colorTextMuted @ 50%` |

**Sizing:**
- Height: 56dp (standard for login/register forms), 48dp (inline search/filter)
- Corner radius: `radiusMd` (12dp)
- Horizontal padding: `spacingMd` (16dp)
- Font: `bodyLarge` (Inter 16sp)

---

### 8.3 Cards

**Standard Item Card:**
```
┌───────────────────────────────────────────┐
│  [Image 80dp × 80dp]  Title (titleLarge)  │
│                        Category chip       │
│                        School · Time ago   │
│                        📍 Location         │
└───────────────────────────────────────────┘
```

- Background: `colorBgCard` White
- Border radius: `radiusLg` (16dp)
- Elevation: 2dp (shadow-sm)
- Padding: `spacingMd` (16dp)
- Left accent strip: 4dp wide — `colorError` for LOST, `colorSecondary` for FOUND
- Image: Rounded 8dp, fixed 80×80dp, Glide `centerCrop`
- Pressed state: `colorBgHover` overlay ripple

---

### 8.4 Status Chips / Badges

Small pill-shaped labels that show item/claim status.

```
┌──────────────┐
│  ● ACTIVE    │   ← dot + label
└──────────────┘
```

| Status | Background (8% opacity) | Text Color | Dot Color |
|--------|------------------------|------------|-----------|
| ACTIVE / FOUND | `colorInfo @ 8%` | `colorInfo` | `colorInfo` |
| LOST | `colorError @ 8%` | `colorError` | `colorError` |
| CLAIMED / PENDING | `colorWarning @ 8%` | `colorWarning` | `colorWarning` |
| RETURNED / COMPLETED / ACCEPTED | `colorSuccess @ 8%` | `colorSuccess` | `colorSuccess` |
| EXPIRED / HIDDEN / REJECTED | `colorBorder` (gray) | `colorTextMuted` | `colorTextMuted` |
| DISPUTED | `colorWarning @ 8%` | `colorWarning` | `colorWarning` |

**Sizing:** Height 24dp, corner radius `radiusFull`, horizontal padding 8dp, font `labelMedium` (12sp Medium).

---

### 8.5 Bottom Navigation Bar

```
┌─────┬─────┬──────┬─────┬─────┐
│Feed │ Map │  ➕  │Board│Prof │
│ 📋  │ 🗺  │  ◎  │ 🏆  │ 👤  │
│Feed │ Map │ Post │Rank │Me   │ ← labels (labelSmall 10sp)
└─────┴─────┴──────┴─────┴─────┘
```

- Background: `colorBgCard` White (with top border 1dp `colorBorder`)
- Height: 64dp + bottom system inset
- Active icon + label: `colorPrimary` `#475569`
- Inactive icon + label: `colorTextMuted` `#94a3b8`
- **Post Item tab:** Filled circle `colorPrimary`, icon `White`, size 56dp, elevation 4dp (FAB style)
- Guest view: shows only Feed, Map, Leaderboard — 3 evenly distributed tabs

---

### 8.6 Top App Bar (Header)

**Authenticated:**
```
┌──────────────────────────────────────────┐
│  🔵 UniLost           [🔔 badge] [💬 badge] │
└──────────────────────────────────────────┘
```

- Background: `colorBgCard` White (light) / `Slate800` (dark)
- Logo: 32dp × 32dp, rounded 8dp
- App Name: `headlineSmall` Outfit SemiBold 16sp, `colorTextPrimary`
- Icon buttons: 48dp touch target, icon `colorTextPrimary` 24dp
- Badge: 18dp diameter, `colorError`, white text `labelSmall`
- Bottom border: 1dp `colorBorder` (visible on scroll, hidden at top)
- Elevation: 0dp at top; 4dp when scrolled

**Guest:**
```
┌──────────────────────────────────────────┐
│  🔵 UniLost           [Login] [Register]  │
└──────────────────────────────────────────┘
```

- Login: Ghost/text button style, `colorPrimary`, `labelLarge`
- Register: Outlined button, `colorPrimary` border, 32dp height compact

---

### 8.7 Bottom Sheets

Used for: filters, location picker, claim submission, confirmation dialogs.

- Background: `colorBgCard`
- Corner radius (top only): `radiusXl` (24dp)
- Handle bar: 4dp × 32dp, `colorBorder`, 8dp from top
- Content padding: `spacingLg` (24dp)
- Max height: 85% of screen height
- Scrim: `colorBgOverlay` (Slate900 @ 50%)

---

### 8.8 Notification / Toast (Snackbar)

- Background: `Slate800` `#1e293b` (same in both modes for contrast)
- Text: `colorTextInverse` White
- Action text: `Sage400` `#84a98c`
- Corner radius: `radiusMd` (12dp)
- Duration: 3s (info), 5s (error / action required)

---

### 8.9 Avatar / Profile Picture

- Shape: Circle (`radiusFull`)
- Fallback: Initials (first letter of first + last name) on `colorPrimaryBg` background, `colorPrimary` text
- Sizes: 32dp (chat/comment), 40dp (list rows), 56dp (profile header), 80dp (profile page large)

---

### 8.10 Loading States

**Shimmer Skeleton:**
- Animated shimmer from `colorBgHover` to `colorBorder` and back
- Matches card/list layout exactly (same padding, same heights)
- Used for: Item Feed, Chat List, Notification List, Profile

**Inline Spinner:**
- `CircularProgressIndicator` 24dp, `colorPrimary`
- Used inside buttons (replaces label on loading), and small inline loads

**Full Screen:**
- Centered `CircularProgressIndicator` 48dp
- Below: `bodyMedium` text in `colorTextMuted` (e.g., "Loading items...")
- Used for initial screen loads

---

## 9. App Shell Design

### 9.1 Screen Scaffold Structure

Every screen follows this scaffold:

```
┌────────────────────────────────────┐  ← Status bar (edge-to-edge)
│  Top App Bar (Header)              │  ← 56dp + top inset
├────────────────────────────────────┤
│                                    │
│                                    │
│   Screen Content                   │  ← Scrollable, fills remaining space
│   (background: colorBgBase)        │
│                                    │
│                                    │
├────────────────────────────────────┤
│  Bottom Navigation Bar             │  ← 64dp + bottom inset
└────────────────────────────────────┘  ← Navigation bar (edge-to-edge)
```

- Edge-to-edge enabled: content draws behind status bar and nav bar
- Proper `WindowInsets` padding applied to scrollable content
- No white/black bars — status and nav bar are transparent with correct icon colors

### 9.2 Screen Background

- All screens: `colorBgBase` (`#f1f5f9` light, `#0f172a` dark)
- Cards/surfaces sit on top with `colorBgCard` (`#ffffff` light, `#1e293b` dark)
- The contrast between `colorBgBase` and `colorBgCard` creates the card "lifted" effect

---

## 10. Screen-by-Screen Design Structure

A brief visual layout spec for each screen. No API calls — just structure.

### 10.1 Landing / Home (Guest)

```
[Header: Logo | Login | Register]
│
├── Hero Section (colorPrimary gradient background, white text)
│     Title: "Lost Something on Campus?"
│     Subtitle: "Connect with your campus community"
│     CTA: [Report Lost] [I Found Something] — both ghost/outline buttons
│
├── Section: "How It Works" (3 step cards)
│     Each: icon circle + step title + description
│
└── Section: "Recently Found" (horizontal scroll)
      ItemCard × N (from feed)
```

### 10.2 Dashboard (Authenticated Home)

```
[Header: Logo | 🔔 | 💬]
│
├── Greeting Banner (colorPrimary, white text)
│     "Hi, {firstName}! 👋"
│     "{karmaScore} karma points"
│
├── Section: "Campus Activity" (hot zones)
│     Horizontal scroll of campus cards with pulse indicators
│
├── Section: "Community Pulse" (recent items)
│     Vertical list of ItemCards (4-5 items)
│     "View All" link → ItemFeed
│
└── Section: "Quick Actions"
      [Post Lost Item]  [Post Found Item]  [Browse Map]
```

### 10.3 Item Feed

```
[Header: Logo | 🔔 | 💬]
│
├── SearchBar (sticky below header)
│     "🔍 Search items..."
│
├── Filter Row (horizontal scroll chips)
│     [All] [Lost] [Found] | [Electronics] [Wallets] ...
│     + Filter icon → opens Filter Bottom Sheet
│
├── Result count: "Showing 15 items"
│
└── LazyColumn of ItemCards
      [ItemCard]
      [ItemCard]
      ...
      [Loading shimmer when fetching next page]
```

### 10.4 Item Detail

```
[Back arrow + title]
│
├── ImagePager (swipeable, 16:9 ratio, dots indicator)
│
├── Content (scrollable)
│     Type badge (LOST/FOUND) + Status badge
│     Title (headlineLarge)
│     Category chip | Campus chip
│     📍 Location text
│     📅 Date lost/found
│     Description (expandable if long)
│     Divider
│     Reporter: [avatar] Name · Campus · Time ago
│     Related items (horizontal scroll)
│
└── Sticky Bottom Bar
      [Claim This Item] ← Primary button (for non-reporter)
      [Edit] [Delete]   ← for item reporter
```

### 10.5 Post Item

```
[Back arrow | "Post Item" title | "Preview" text button]
│
├── Step 1: Type selector
│     [  LOST  ]  [  FOUND  ]  ← toggle chips
│
├── Step 2: Details form
│     Title input
│     Description input (multiline)
│     Category dropdown
│     Date picker
│     Secret detail input (optional, for FOUND)
│
├── Step 3: Location
│     Map preview (tappable → opens LocationPickerSheet)
│     "Tap to set location" placeholder
│
└── Step 4: Images
      [+ Add Photo]  [+ Camera]
      Image preview grid (max 3, with × remove)
      Upload progress bar

[Submit] button at bottom (sticky)
```

### 10.6 Chat List (Messages Tab)

```
[Header: "Messages" | 🔔 | 💬]
│
└── LazyColumn
      For each chat:
      ┌──────────────────────────────────────────┐
      │ [ItemThumb] OtherPersonName              │
      │             "Last message preview..."     │
      │             Item: {itemTitle}   2m ago   │
      │             [Unread badge if > 0]         │
      └──────────────────────────────────────────┘
```

### 10.7 Chat Detail

```
[Back | OtherPersonName (item title below)]
│
├── Messages (LazyColumn, reverse)
│     Right-aligned: own messages (colorPrimary bg, white text)
│     Left-aligned: other's messages (colorBgCard, colorTextPrimary)
│     Centered gray: system messages (claim accepted, handover, etc.)
│
├── Claim Status Card (pinned between header and messages)
│     Status chip + handover action buttons (contextual)
│
└── Input Bar (sticky bottom)
      [Message input field]  [Send button]
```

### 10.8 Map View

```
[Header: Logo | 🔔 | 💬]
│
├── Full-screen Google Map
│     Campus center default (Cebu lat: 10.3157, lng: 123.8854)
│     Red markers: LOST items
│     Green markers: FOUND items
│     Blue dot: user location
│
├── Floating filter chips (top overlay)
│     [All] [Lost] [Found]  |  [Campus dropdown]
│
├── Tap marker → InfoWindow:
│     Item title, category, time ago
│     [View Item] button
│
└── [My Location] FAB (bottom right)
```

### 10.9 Notifications

```
[Header: "Notifications" | Mark all read]
│
├── Filter tabs: [All] [Unread] [Claims] [Items]
│
└── LazyColumn
      For each notification:
      ┌─────────────────────────────────────────┐
      │ [Icon circle]  Title (bold if unread)    │
      │                Message body              │
      │                2 hours ago               │
      │ ← unread blue dot on left edge           │
      └─────────────────────────────────────────┘
```

### 10.10 Profile

```
[Header: "Profile" | Settings icon]
│
├── Profile Card
│     [Avatar 80dp]  Name (headlineMedium)
│     Email · Campus · Member since
│     Karma: {score} (with progress bar to next rank)
│
├── Tabs: [LOST] [FOUND] [CLAIMS]
│     Each tab: filtered LazyColumn of ItemCards / ClaimCards
│
└── Bottom Section
      Leaderboard preview (top 3 in campus)
```

### 10.11 Settings

```
[Back arrow | "Settings" title]
│
├── Section: Account
│     [Profile picture + change] row
│     Full Name  → inline edit or navigate
│     Email (read-only)
│     Campus (read-only)
│
├── Section: Security
│     Change Password →
│
├── Section: Appearance
│     Theme toggle: [System] [Light] [Dark]
│
├── Section: Notifications
│     Toggle: Enable push notifications
│
└── Section: Account Actions
      [Logout] — danger text button
      [Delete Account] — danger text button
```

### 10.12 Leaderboard

```
[Header: "Leaderboard" | Campus filter]
│
├── Top 3 Podium (visual podium: 2nd | 1st | 3rd)
│     Each: avatar, name, karma score
│
└── Ranked List (4th onward)
      [Rank] [Avatar] Name · Campus    {score} karma
```

---

## 11. Current Theme Issues to Fix

These are the specific code-level changes needed in Phase 1:

| File | Current State | What to Change |
|------|--------------|----------------|
| `Color.kt` | Only 8 colors, Purple colors at top | Add full palette (40+ colors), remove Purple/Pink |
| `Theme.kt` | Purple colors, `dynamicColor = true` | Replace with UniLost ColorSchemes, set `dynamicColor = false` |
| `Type.kt` | Only `bodyLarge` defined | Define full type scale (all 12 styles) with Inter + Outfit fonts |
| `BottomNavBar.kt` | Basic implementation, no FAB-style Post button | Redesign with spec: 3-tab guest / 5-tab auth, elevated Post button |
| `AuthComponents.kt` | `AuthInput` and `PrimaryButton` using generic colors | Update to use semantic tokens from revised Color.kt |
| Status bar | Set to purple `colorScheme.primary` | Set to `colorBgCard` (light) / `Slate900` (dark) |
| Font resources | No font files added | Add Inter and Outfit via Google Fonts / `res/font/` |

---

## 12. Deliverables Checklist

Before Phase 2 (API connections) begins, all items below must be complete:

### Theme Files
- [x] `Color.kt` — Full palette (slate scale, sage, status colors, all semantic tokens) — 87+ color constants
- [x] `Theme.kt` — `UniLostLightColorScheme` + `UniLostDarkColorScheme`, `dynamicColor = false`, reads `LocalThemePreference`
- [x] `Type.kt` — Full 12-style type scale with Inter + Outfit font families (falls back to system font until .ttf files added)
- [ ] Font files — Inter and Outfit `.ttf` must be manually downloaded from Google Fonts into `res/font/` *(cannot be automated)*

### Core Components
- [x] `UniLostButton.kt` — Primary, Secondary, Danger, Ghost variants with loading state
- [x] `UniLostTextField.kt` — Default, focused, error states with floating label, leading/trailing icons
- [x] `StatusChip.kt` — All 15+ status/type color variants with auto-mapping
- [x] `ItemCard.kt` — Standard card with type accent strip + compact variant
- [x] `UniLostTopBar.kt` — Authenticated (notification+chat badges) + Guest (Login/Register) + Detail (back+title+actions) variants
- [x] `BottomNavBar.kt` — 5-tab auth + 3-tab guest, elevated Post FAB button
- [x] `AvatarView.kt` — Initials fallback with gradient, multiple sizes (32-96dp)
- [x] `SkeletonLoader.kt` — shimmerBrush, ShimmerBox, ShimmerItemCard, ShimmerListRow, FullScreenLoading
- [x] `EmptyState.kt` — Icon + title + message + optional CTA UniLostButton
- [x] `BottomSheetScaffold` usage defined — standard sheet padding/handle via UniLostShapes.bottomSheet

### App Shell
- [x] `MainActivity.kt` — Edge-to-edge enabled, correct status/nav bar colors, provides `LocalThemePreference`
- [x] `NavGraph.kt` — All screens registered including Landing, ChatList, ChatDetail, Map, Leaderboard
- [x] Guest nav correctly shows 3 tabs; auth nav shows 5 tabs (BottomNavBar.kt)
- [x] Theme toggle implemented in Settings — 3-way System/Light/Dark via `LocalThemePreference` (session-only; DataStore persistence deferred to Phase 2)

### Screen Shells (Layout Only — No Data)
- [x] All screens have correct scaffold structure matching Section 10 specs:
  - [x] 10.1 LandingScreen (guest hero + how-it-works + CTA)
  - [x] 10.2 DashboardScreen (greeting + campus activity + quick actions)
  - [x] 10.3 ItemFeedScreen (search + filter chips + item cards)
  - [x] 10.4 ItemDetailScreen (image area + content + claim CTA)
  - [x] 10.5 PostItemScreen (type toggle + form fields + images + submit)
  - [x] 10.6 ChatListScreen (chat rows with avatars + unread badges)
  - [x] 10.7 ChatDetailScreen (messages + claim status card + input bar)
  - [x] 10.8 MapScreen (map placeholder + filter chips + FAB + nearby card)
  - [x] 10.9 NotificationsScreen (filter tabs + notification rows)
  - [x] 10.10 ProfileScreen (avatar + info + stats + action buttons)
  - [x] 10.11 SettingsScreen (edit profile + 3-way theme toggle + sections)
  - [x] 10.12 LeaderboardScreen (podium top 3 + ranked list)
- [x] All screens use the design system components (no hardcoded colors or sizes)
- [x] Dark mode support via theme toggle (visual verification recommended on device)

---

> **Next Phase:** Phase 2 — Authentication Screens (Login, Register, Forgot Password) connected to real API, built using the Phase 1 design system.
