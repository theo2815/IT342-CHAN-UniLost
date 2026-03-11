# Website Design System & Theming

## CSS Architecture

```
website/src/
├── styles/
│   ├── global.css       # Imports tokens + themes, resets, utilities, animations
│   ├── tokens.css       # Design tokens (spacing, radius, typography, z-index, transitions)
│   └── themes.css       # Light/dark color variables
├── index.css            # App-level overrides
├── App.css              # Root layout styles
└── components/**/*.css  # Co-located component styles (BEM-like naming)
```

**Import order** (main.jsx): `global.css` → `index.css` → component CSS via imports

**Methodology**: CSS Custom Properties only — no CSS-in-JS, no Tailwind, no Sass.

## Design Tokens (tokens.css)

### Spacing Scale
| Token | Value |
|-------|-------|
| `--spacing-2xs` | 2px |
| `--spacing-xs` | 4px |
| `--spacing-sm` | 8px |
| `--spacing-md` | 16px |
| `--spacing-lg` | 24px |
| `--spacing-xl` | 32px |
| `--spacing-2xl` | 48px |
| `--spacing-3xl` | 64px |

### Border Radius
| Token | Value |
|-------|-------|
| `--radius-xs` | 4px |
| `--radius-sm` | 6px |
| `--radius-md` | 12px |
| `--radius-lg` | 16px |
| `--radius-xl` | 24px |
| `--radius-full` | 9999px |

### Typography
**Font families**: Inter (body), Outfit (display)

| Token | Value |
|-------|-------|
| `--font-size-2xs` | 10px |
| `--font-size-xs` | 12px |
| `--font-size-sm` | 14px |
| `--font-size-md` | 16px |
| `--font-size-lg` | 18px |
| `--font-size-xl` | 20px |
| `--font-size-2xl` | 24px |
| `--font-size-3xl` | 32px |
| `--font-size-4xl` | 48px |

**Weights**: `--font-weight-light` (300), `--font-weight-normal` (400), `--font-weight-medium` (500), `--font-weight-semibold` (600), `--font-weight-bold` (700)

**Line heights**: `--line-height-tight` (1.25), `--line-height-normal` (1.5), `--line-height-relaxed` (1.625)

### Transitions
| Token | Value |
|-------|-------|
| `--transition-fast` | 150ms |
| `--transition-normal` | 300ms |
| `--transition-slow` | 500ms |

Cubic bezier easing functions available.

### Z-Index Scale
| Token | Value |
|-------|-------|
| `--z-dropdown` | 100 |
| `--z-sticky` | 200 |
| `--z-overlay` | 500 |
| `--z-modal` | 1000 |
| `--z-toast` | 9999 |

## Theme System (themes.css)

Themes set via `data-theme` attribute on `<html>`:
- Default (no attribute or `data-theme="light"`) → light mode
- `data-theme="dark"` → dark mode

### Color Tokens

#### Light Mode (`:root`)
| Token | Value | Usage |
|-------|-------|-------|
| `--color-primary` | #475569 | Slate blue — primary actions |
| `--color-secondary` | #84a98c | Sage green — secondary actions |
| `--color-bg-primary` | #f1f5f9 | Page background |
| `--color-bg-secondary` | #e2e8f0 | Section backgrounds |
| `--color-bg-input` | #ffffff | Input field background |
| `--color-text-primary` | #1e293b | Main text |
| `--color-text-secondary` | #64748b | Muted text |
| `--color-border` | #e2e8f0 | Borders |

#### Dark Mode (`[data-theme="dark"]`)
| Token | Value | Usage |
|-------|-------|-------|
| `--color-primary` | #94a3b8 | Light slate |
| `--color-secondary` | #a3c1ad | Light sage |
| `--color-bg-primary` | #0f172a | Dark navy |
| `--color-bg-secondary` | #1e293b | Section backgrounds |
| `--color-bg-input` | #1e293b | Input field background |
| `--color-text-primary` | #f8fafc | Light text |
| `--color-text-secondary` | #94a3b8 | Muted text |
| `--color-border` | #334155 | Borders |

### Functional Colors (both themes)
| Status | Base | Hover | Background | Border | Text |
|--------|------|-------|-----------|--------|------|
| Success | #10b981 | darker | semi-transparent | matching | for dark bg |
| Warning | #f59e0b | darker | semi-transparent | matching | for dark bg |
| Error | #ef4444 | darker | semi-transparent | matching | for dark bg |
| Info | #3b82f6 | darker | semi-transparent | matching | for dark bg |

Each has 5 variants: `--color-{status}`, `--color-{status}-hover`, `--color-{status}-bg`, `--color-{status}-border`, `--color-{status}-text`

### Shadows & Effects
- Shadow scale: `--shadow-xs` → `--shadow-xl`
- Gradients: `--gradient-primary`, `--gradient-secondary`
- Glassmorphism support via `.glass` utility class

## Global Styles (global.css)

### CSS Reset
- `box-sizing: border-box` on all elements
- Removed default margins/padding
- Smooth scrolling
- Form elements: `border: none; background: transparent; outline: none` (all inputs, selects, textareas reset — component CSS provides actual styling)

### Focus Management
- `:focus-visible` → 3px outline ring
- `:focus:not(:focus-visible)` → no ring (mouse users)

### Utility Classes
| Class | Effect |
|-------|--------|
| `.glass` | Backdrop blur (12px) + border + shadow |
| `.text-gradient` | Text clipped to gradient |
| `.flex-center` | Flexbox centering |
| `.sr-only` | Screen reader only |

### Animations (10 keyframes)
`fadeIn`, `fadeOut`, `slideUp`, `slideDown`, `scaleIn`, `spin` (360deg), `pulse` (opacity), `shimmer` (gradient)

## Component CSS Conventions

1. **Co-located**: Each component has `.jsx` + `.css` in the same directory
2. **BEM-like naming**: `.ui-button`, `.ui-button--primary`, `.ui-button__icon`
3. **Custom properties**: All colors, spacing, radius via CSS variables
4. **Theme-aware**: Components use `var(--color-*)` tokens → auto-adapt to dark mode
5. **No `!important`** (except for the global Input field reset to prevent specificity conflicts)
6. **Responsive**: Mostly fluid layouts, some media queries in page-level CSS

## Autofill Handling

Input component includes `:-webkit-autofill` overrides:
```css
-webkit-box-shadow: 0 0 0 1000px var(--color-bg-input) inset !important;
-webkit-text-fill-color: var(--color-text-primary) !important;
transition: background-color 5000s ease-in-out 0s;
```
Covers Chrome, Edge, Safari. Firefox does not require special handling.
