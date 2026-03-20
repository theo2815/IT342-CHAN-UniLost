# Mobile Phase 8 - Campus Maps

> **Status:** PENDING
> **Depends On:** Backend Phase 8 (must be completed first)

---

## Objective

Implement an interactive map using Google Maps SDK on Android to display item pins, allow location selection when posting items, and filter by campus.

---

## Deliverables

| # | Task | Details |
|---|------|---------|
| 1 | Add Google Maps SDK dependency | `implementation 'com.google.maps.android:maps-compose'` |
| 2 | Create `MapScreen.kt` | Full map view with item pins |
| 3 | Create `MapViewModel.kt` | ViewModel for map data |
| 4 | Implement campus filtering | Toggle between city-wide and campus-specific views |
| 5 | Implement item markers | Color-coded pins (red=Lost, green=Found) |
| 6 | Implement marker tap -> detail | Bottom sheet with item summary |
| 7 | Implement location picker | Draggable pin for PostItem screen |
| 8 | Add to navigation graph | Map tab in bottom navigation |

---

## Technical Details

| Setting | Value |
|---------|-------|
| SDK | Google Maps Compose (`maps-compose`) |
| Default center | Cebu City (10.3157, 123.8854) |
| Default zoom | 13 (city-wide) |
| Campus zoom | 17 (building-level) |

---

## Acceptance Criteria

- [ ] Map displays item pins across Cebu City campuses
- [ ] Users can filter by specific campus
- [ ] Tapping a pin shows item summary
- [ ] Users can place a pin when reporting a new item
