# Phase 8 - Interactive Campus Maps (Backend + Website)

> **Status:** COMPLETED
> **Priority:** COULD HAVE
> **Depends On:** Phase 3 (Item Management)

---

## Objective

Implement an interactive map interface using Google Maps that visualizes lost and found items across Cebu City campuses, allows pin placement when reporting items, and provides campus-specific filtering.

---

## Pre-Existing Work

- **Backend:** `CampusEntity.java` stores `centerCoordinates` as `GeoJsonPoint` (latitude/longitude for each of 8 seeded universities)
- **Backend:** `ItemEntity.java` had a `location` field (String — building/room description) but no coordinates
- **Backend:** `CampusService.java` and `CampusController.java` exist with full CRUD (Phase 2)
- **Website:** `MapView/MapView.jsx` + `MapView.css` existed with mock data (hardcoded pins, background image)

---

## Backend (Spring Boot) — Implemented

| # | Task | Status | Details |
|---|------|--------|---------|
| 1 | Add `latitude`/`longitude` to `ItemEntity` | DONE | Optional `Double` fields for GPS coordinates |
| 2 | Add `latitude`/`longitude` to `ItemDTO` | DONE | Coordinates included in all item API responses |
| 3 | Add `latitude`/`longitude` to `ItemRequest` | DONE | Optional — users can post items with or without map pin |
| 4 | Map items endpoint | DONE | `GET /api/items/map` — returns active items with coordinates (max 200) |
| 5 | Handle coordinates in create/update | DONE | `ItemService.createItem()` and `updateItem()` set coordinates from request |

### API Endpoints (Implemented)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/items/map` | Public | Get items with coordinates for map rendering (filter by `campusId`, `type`) |

**Note:** The map endpoint is public (follows same pattern as `GET /api/items`) since item data is already publicly accessible. It returns only ACTIVE, non-deleted items that have both `latitude` and `longitude` set, limited to 200 results sorted by `createdAt` descending.

## Website (React + Vite) — Implemented

| # | Task | Status | Details |
|---|------|--------|---------|
| 1 | Install `@react-google-maps/api` | DONE | Google Maps React wrapper |
| 2 | Rewrite `MapView` page | DONE | Full rewrite from mock to real Google Maps + API |
| 3 | Campus filter dropdown | DONE | Selects campus, pans map to campus center coordinates |
| 4 | Type filter (All/Lost/Found) | DONE | Filter chips that re-fetch map items |
| 5 | Color-coded markers | DONE | Red for Lost, green for Found (SVG path markers) |
| 6 | Marker click → InfoWindow | DONE | Shows image, title, type badge, location, "View Details" button |
| 7 | Item list sidebar | DONE | Scrollable list synced with map — click card to pan to marker |
| 8 | `LocationPicker` component | DONE | Click-to-place map pin on PostItem form |
| 9 | Wire PostItem with coordinates | DONE | `latitude`/`longitude` included in item creation request |
| 10 | Add `getMapItems()` to `itemService.js` | DONE | API wrapper for map endpoint |
| 11 | Google Maps API key via `.env` | DONE | `VITE_GOOGLE_MAPS_API_KEY` in `website/.env` (gitignored) |

---

## Technical Details

### Item Entity Coordinate Fields (Added)
| Field | Type | Description |
|-------|------|-------------|
| `latitude` | Double (nullable) | GPS latitude — optional, only set when user places a map pin |
| `longitude` | Double (nullable) | GPS longitude — optional, only set when user places a map pin |

### Map Configuration (Google Maps)

| Setting | Value |
|---------|-------|
| Default center | Cebu City (10.3157, 123.8854) |
| Default zoom | 13 (city-wide) |
| Campus zoom | 16 (building-level) |
| Map provider | Google Maps JavaScript API |
| React library | `@react-google-maps/api` |
| API key env var | `VITE_GOOGLE_MAPS_API_KEY` |

### Pin Color Coding
| Item Type | Color | Details |
|-----------|-------|---------|
| Lost | Red (#ef4444) | SVG pin marker |
| Found | Green (#22c55e) | SVG pin marker |

### LocationPicker Component
- **Location:** `website/src/components/LocationPicker.jsx`
- Collapsible — shows "Add Map Pin (optional)" button by default
- Expands to show Google Map with click-to-place interaction
- Displays coordinates when pin is placed
- Clear and Done buttons for UX
- Reuses same `useJsApiLoader` (Google Maps handles deduplication)

### Map View Architecture
```
MapView Page:
  ├── Left Panel (380px): Sidebar with filters + scrollable item list
  │   ├── Campus dropdown (pans map to campus center)
  │   ├── Type filter chips (ALL / LOST / FOUND)
  │   └── Item cards (click to pan map to item marker)
  └── Right Panel (flex): Google Map
      ├── Color-coded MarkerF for each item with coordinates
      ├── InfoWindowF on marker click (image, title, type, location, view button)
      └── Legend overlay (Lost = red, Found = green)
```

### Environment Variables Required
```
# website/.env (gitignored)
VITE_GOOGLE_MAPS_API_KEY=your_google_maps_api_key
```

---

## Acceptance Criteria

- [x] Map displays item pins across all Cebu City campuses
- [x] Users can filter the map by specific campus
- [x] Clicking a pin shows item summary with link to detail
- [x] Users can place a pin when reporting a new item
- [x] Items without coordinates are handled gracefully (not shown on map)
- [x] Campus dropdown pans map to campus center coordinates

---

## Deferred Items

| Item | Deferred To | Notes |
|------|-------------|-------|
| Heatmap overlay (density visualization) | Future | Would need aggregation endpoint + heatmap layer |
| MongoDB geospatial queries (`$near`, `$geoWithin`) | Future | Current approach uses simple lat/lng filtering; geospatial queries needed at scale |
| `2dsphere` index on items collection | Future | Not needed until geospatial queries are added |
| Nearby items radius search | Future | `GET /api/items/nearby?lat=X&lng=Y&radius=500` |
