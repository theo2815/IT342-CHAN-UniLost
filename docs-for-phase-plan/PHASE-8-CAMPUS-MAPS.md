# Phase 8 - Interactive Campus Maps (Backend + Website)

> **Status:** PENDING
> **Priority:** COULD HAVE
> **Depends On:** Phase 3 (Item Management)

---

## Objective

Implement an interactive map interface using Leaflet.js that visualizes lost and found items across Cebu City campuses, allows pin placement when reporting items, and provides heatmap views of high-activity zones.

---

## Pre-Existing Work

- **Backend:** `CampusEntity` already stores GeoJsonPoint coordinates; `ItemEntity` has a `coordinates` field
- **Website:** `MapView/MapView.jsx` built with mock data

---

## Backend (Spring Boot)

| # | Task | Details |
|---|------|---------|
| 1 | Create map-specific endpoints | Return items with coordinates for map rendering |
| 2 | GeoJSON spatial queries | MongoDB `$near` and `$geoWithin` queries |
| 3 | Campus boundary data | Return polygon boundaries for campus overlays |
| 4 | Heatmap data endpoint | Aggregate lost item density by location |
| 5 | Create `2dsphere` index on items collection | Required for geo-spatial queries |

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/maps/items` | Get items with coordinates (supports bounding box filter) |
| `GET` | `/api/maps/items/nearby` | Get items near a specific lat/long within a radius |
| `GET` | `/api/maps/locations/{campusId}` | Get campus boundary and building locations |
| `GET` | `/api/maps/heatmap` | Aggregated location density data for heatmap |

## Website (React + Vite)

| # | Task | Details |
|---|------|---------|
| 1 | Install and configure Leaflet.js | `npm install leaflet react-leaflet` |
| 2 | Wire `MapView` page to real API | Replace mock data with geo-queried items |
| 3 | Implement campus toggle/filter | Switch between city-wide and campus-specific views |
| 4 | Implement item pins on map | Color-coded: red for Lost, green for Found |
| 5 | Implement pin click -> item detail | Popup with item summary, link to full detail |
| 6 | Implement pin placement on PostItem form | Drag-to-place pin for location selection |
| 7 | Implement zone heatmap overlay | Show high-activity areas |

---

## Technical Details

### MongoDB GeoJSON Storage

```javascript
// Item document with coordinates
{
  "title": "Lost Laptop",
  "coordinates": {
    "type": "Point",
    "coordinates": [123.8854, 10.2948]  // [longitude, latitude]
  }
}
```

### Spatial Query Examples

```java
// Find items within 500 meters of a point
@Query("{ 'coordinates': { $near: { $geometry: { type: 'Point', coordinates: [?0, ?1] }, $maxDistance: ?2 } } }")
List<ItemEntity> findNearby(double longitude, double latitude, double maxDistanceMeters);

// Find items within a campus boundary
@Query("{ 'coordinates': { $geoWithin: { $geometry: ?0 } } }")
List<ItemEntity> findWithinBoundary(GeoJsonPolygon boundary);
```

### Map Configuration (Leaflet.js)

| Setting | Value |
|---------|-------|
| Default center | Cebu City (10.3157, 123.8854) |
| Default zoom | 13 (city-wide) |
| Campus zoom | 17 (building-level) |
| Tile source | OpenStreetMap |

### Pin Color Coding
| Item Type | Color | Icon |
|-----------|-------|------|
| Lost | Red | Warning marker |
| Found | Green | Check marker |
| Turned Over to Office | Blue | Building marker |
| Resolved/Returned | Gray | Dimmed marker |

### Heatmap Data Format
```json
{
  "points": [
    { "lat": 10.2948, "lng": 123.8854, "intensity": 15 },
    { "lat": 10.3001, "lng": 123.8910, "intensity": 8 }
  ]
}
```

---

## Acceptance Criteria

- [ ] Map displays item pins across all Cebu City campuses
- [ ] Users can filter the map by specific campus
- [ ] Clicking a pin shows item summary with link to detail
- [ ] Users can place a pin when reporting a new item
- [ ] Heatmap overlay shows areas with most lost items
- [ ] Geo-spatial queries return items within a specified radius
