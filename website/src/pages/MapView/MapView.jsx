import { useState, useEffect, useCallback, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { Map, AdvancedMarker, InfoWindow, useMap } from "@vis.gl/react-google-maps";
import { MapPin, Loader, AlertCircle, Navigation } from "lucide-react";
import Header from "../../components/Header";
import useGoogleMaps from "../../hooks/useGoogleMaps";
import itemService from "../../services/itemService";
import campusService from "../../services/campusService";
import "./MapView.css";

const CEBU_CENTER = { lat: 10.3157, lng: 123.8854 };
const DEFAULT_ZOOM = 13;
const CAMPUS_ZOOM = 16;
const MAP_ID = import.meta.env.VITE_GOOGLE_MAPS_MAP_ID || undefined;

function MapView() {
  const navigate = useNavigate();
  const map = useMap("main-map");

  const [items, setItems] = useState([]);
  const [campuses, setCampuses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedItem, setSelectedItem] = useState(null);
  const [selectedCampusMarker, setSelectedCampusMarker] = useState(null);
  const [activeFilter, setActiveFilter] = useState("ALL");
  const [activeCampus, setActiveCampus] = useState("");
  const [userLocation, setUserLocation] = useState(null);
  const [locationError, setLocationError] = useState("");

  const { isLoaded, loadError } = useGoogleMaps();
  const circleRef = useRef(null);

  // Request user's current location
  useEffect(() => {
    if (!navigator.geolocation) {
      setLocationError("Geolocation is not supported by your browser");
      return;
    }

    const watchId = navigator.geolocation.watchPosition(
      (position) => {
        setUserLocation({
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        });
        setLocationError("");
      },
      (err) => {
        if (err.code === err.PERMISSION_DENIED) {
          setLocationError("Location access denied");
        }
      },
      { enableHighAccuracy: true, maximumAge: 30000 }
    );

    return () => navigator.geolocation.clearWatch(watchId);
  }, []);

  // Load campuses once
  useEffect(() => {
    const loadCampuses = async () => {
      const result = await campusService.getAllCampuses();
      if (result.success) {
        setCampuses(result.data);
      }
    };
    loadCampuses();
  }, []);

  // Load map items when filters change
  useEffect(() => {
    const loadItems = async () => {
      setLoading(true);
      setError("");
      const params = {};
      if (activeCampus) params.campusId = activeCampus;
      if (activeFilter !== "ALL") params.type = activeFilter;

      const result = await itemService.getMapItems(params);
      if (result.success) {
        setItems(result.data);
      } else {
        setError(result.error);
      }
      setLoading(false);
    };
    loadItems();
  }, [activeFilter, activeCampus]);

  // User location accuracy circle (imperative — vis.gl has no Circle component)
  useEffect(() => {
    if (!map || !userLocation) {
      if (circleRef.current) {
        circleRef.current.setMap(null);
        circleRef.current = null;
      }
      return;
    }

    if (!circleRef.current) {
      circleRef.current = new google.maps.Circle({
        map,
        center: userLocation,
        radius: 80,
        fillColor: "#3b82f6",
        fillOpacity: 0.12,
        strokeColor: "#3b82f6",
        strokeOpacity: 0.3,
        strokeWeight: 1,
        clickable: false,
      });
    } else {
      circleRef.current.setCenter(userLocation);
    }

    return () => {
      if (circleRef.current) {
        circleRef.current.setMap(null);
        circleRef.current = null;
      }
    };
  }, [map, userLocation]);

  const handleCampusChange = (campusId) => {
    setActiveCampus(campusId);
    setSelectedItem(null);
    setSelectedCampusMarker(null);

    if (campusId && map) {
      const campus = campuses.find((c) => c.id === campusId);
      if (campus?.centerCoordinates) {
        map.panTo({
          lat: campus.centerCoordinates[1],
          lng: campus.centerCoordinates[0],
        });
        map.setZoom(CAMPUS_ZOOM);
      }
    } else if (map) {
      map.panTo(CEBU_CENTER);
      map.setZoom(DEFAULT_ZOOM);
    }
  };

  const handleMarkerClick = (item) => {
    setSelectedItem(item);
    setSelectedCampusMarker(null);
    if (map) {
      map.panTo({ lat: item.latitude, lng: item.longitude });
    }
  };

  const handleCampusMarkerClick = (campus) => {
    setSelectedCampusMarker(campus);
    setSelectedItem(null);
    if (map && campus.centerCoordinates) {
      map.panTo({
        lat: campus.centerCoordinates[1],
        lng: campus.centerCoordinates[0],
      });
    }
  };

  const handleItemCardClick = (item) => {
    setSelectedItem(item);
    setSelectedCampusMarker(null);
    if (map && item.latitude && item.longitude) {
      map.panTo({ lat: item.latitude, lng: item.longitude });
      map.setZoom(CAMPUS_ZOOM);
    }
  };

  const panToUserLocation = () => {
    if (userLocation && map) {
      map.panTo(userLocation);
      map.setZoom(CAMPUS_ZOOM);
    }
  };

  // Count items per campus for the info window
  const itemCountByCampus = {};
  items.forEach((item) => {
    if (item.campusId) {
      itemCountByCampus[item.campusId] = (itemCountByCampus[item.campusId] || 0) + 1;
    }
  });

  const formatTimeAgo = (dateStr) => {
    if (!dateStr) return "";
    const diff = Date.now() - new Date(dateStr).getTime();
    if (diff < 0) return "just now";
    const mins = Math.floor(diff / 60000);
    if (mins < 60) return `${mins}m ago`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return `${hrs}h ago`;
    const days = Math.floor(hrs / 24);
    return `${days}d ago`;
  };

  if (loadError) {
    return (
      <div className="map-view-page">
        <Header />
        <main className="map-error-state">
          <AlertCircle size={48} />
          <h2>Failed to load Google Maps</h2>
          <p>Please check your internet connection and try again.</p>
        </main>
      </div>
    );
  }

  return (
    <div className="map-view-page overflow-hidden">
      <Header />

      <main className="map-view-main">
        {/* Left Panel: Feed */}
        <div className="map-feed-panel">
          <div className="feed-filters-header">
            <div className="filters-top-row">
              <h1 className="feed-title">
                <MapPin size={20} style={{ display: "inline", verticalAlign: "middle" }} /> Map View
              </h1>
              <span className="item-count">{items.length} items</span>
            </div>

            {/* Campus Filter */}
            <div className="form-group map-filter-group">
              <select
                value={activeCampus}
                onChange={(e) => handleCampusChange(e.target.value)}
                className="map-select"
              >
                <option value="">All Campuses</option>
                {campuses.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>

            {/* Type Filter */}
            <div className="filters-scroll-row custom-scrollbar">
              {["ALL", "LOST", "FOUND"].map((filter) => (
                <button
                  key={filter}
                  className={`filter-chip ${activeFilter === filter ? "active" : ""}`}
                  onClick={() => setActiveFilter(filter)}
                >
                  {filter === "ALL" ? "All Items" : filter === "LOST" ? "Lost" : "Found"}
                </button>
              ))}
            </div>
          </div>

          {/* Item List */}
          <div className="feed-list custom-scrollbar">
            {loading ? (
              <div className="map-loading-state">
                <Loader size={24} className="spin" />
                <span>Loading items...</span>
              </div>
            ) : error ? (
              <div className="map-feed-error">
                <AlertCircle size={20} />
                <span>{error}</span>
              </div>
            ) : items.length === 0 ? (
              <div className="map-empty-state">
                <MapPin size={32} />
                <p>No items with location data found.</p>
                <p className="text-muted">Items posted with a map pin will appear here.</p>
              </div>
            ) : (
              items.map((item) => (
                <div
                  key={item.id}
                  className={`map-feed-card ${selectedItem?.id === item.id ? "selected" : ""}`}
                  onClick={() => handleItemCardClick(item)}
                >
                  <div className="card-image-col">
                    <div
                      className="item-image"
                      style={{
                        backgroundImage: item.imageUrls?.[0]
                          ? `url(${item.imageUrls[0]})`
                          : "none",
                      }}
                    />
                  </div>
                  <div className="card-content-col">
                    <div className="status-time-row">
                      <span className={`status-badge-small status-${item.type?.toLowerCase()}`}>
                        <span className="status-dot"></span>
                        {item.type}
                      </span>
                      <span className="time-ago-text">{formatTimeAgo(item.createdAt)}</span>
                    </div>
                    <h4 className="item-title-text">{item.title}</h4>
                    <div className="card-content-bottom">
                      <MapPin size={12} />
                      <span>{item.location || "Unknown location"}</span>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Right Panel: Google Map */}
        <div className="map-graphic-panel">
          {!isLoaded ? (
            <div className="map-loading-state full">
              <Loader size={32} className="spin" />
              <span>Loading map...</span>
            </div>
          ) : (
            <Map
              id="main-map"
              mapId={MAP_ID}
              className="google-map-container"
              defaultCenter={CEBU_CENTER}
              defaultZoom={DEFAULT_ZOOM}
              disableDefaultUI={false}
              zoomControl={true}
              mapTypeControl={false}
              streetViewControl={false}
              fullscreenControl={true}
              clickableIcons={false}
              onClick={() => {
                setSelectedItem(null);
                setSelectedCampusMarker(null);
              }}
            >
              {/* Campus Markers */}
              {campuses.map((campus) => {
                if (!campus.centerCoordinates) return null;
                const pos = {
                  lat: campus.centerCoordinates[1],
                  lng: campus.centerCoordinates[0],
                };
                const shortName = campus.shortLabel || campus.name;
                return (
                  <AdvancedMarker
                    key={`campus-${campus.id}`}
                    position={pos}
                    onClick={() => handleCampusMarkerClick(campus)}
                    zIndex={1}
                  >
                    <div className="adv-campus-marker">
                      <span className="adv-campus-label">{shortName}</span>
                      <svg viewBox="0 0 24 36" width="28" height="42">
                        <path
                          d="M12 0C5.4 0 0 5.4 0 12c0 9 12 24 12 24s12-15 12-24C24 5.4 18.6 0 12 0zm0 17c-2.8 0-5-2.2-5-5s2.2-5 5-5 5 2.2 5 5-2.2 5-5 5z"
                          fill="#6366f1"
                          stroke="#ffffff"
                          strokeWidth="1.5"
                        />
                      </svg>
                    </div>
                  </AdvancedMarker>
                );
              })}

              {/* Campus InfoWindow */}
              {selectedCampusMarker && selectedCampusMarker.centerCoordinates && (
                <InfoWindow
                  position={{
                    lat: selectedCampusMarker.centerCoordinates[1],
                    lng: selectedCampusMarker.centerCoordinates[0],
                  }}
                  onCloseClick={() => setSelectedCampusMarker(null)}
                  pixelOffset={[0, -45]}
                >
                  <div className="map-info-window campus-info-window">
                    <div className="campus-info-icon">🏫</div>
                    <h4>{selectedCampusMarker.name}</h4>
                    {selectedCampusMarker.address && (
                      <p className="info-location" style={{ marginBottom: '0.25rem' }}>{selectedCampusMarker.address}</p>
                    )}
                    <p className="info-location">
                      {itemCountByCampus[selectedCampusMarker.id] || 0} pinned items on map
                    </p>
                    <button
                      className="info-view-btn"
                      onClick={() => handleCampusChange(selectedCampusMarker.id)}
                    >
                      View Campus Items
                    </button>
                  </div>
                </InfoWindow>
              )}

              {/* Item Markers */}
              {items.map((item) => (
                <AdvancedMarker
                  key={item.id}
                  position={{ lat: item.latitude, lng: item.longitude }}
                  onClick={() => handleMarkerClick(item)}
                  zIndex={2}
                >
                  <svg viewBox="0 0 24 36" width="24" height="36">
                    <path
                      d="M12 0C5.4 0 0 5.4 0 12c0 9 12 24 12 24s12-15 12-24C24 5.4 18.6 0 12 0zm0 17c-2.8 0-5-2.2-5-5s2.2-5 5-5 5 2.2 5 5-2.2 5-5 5z"
                      fill={item.type === "LOST" ? "#ef4444" : "#22c55e"}
                      stroke="#ffffff"
                      strokeWidth="1.5"
                    />
                  </svg>
                </AdvancedMarker>
              ))}

              {/* Item InfoWindow */}
              {selectedItem && (
                <InfoWindow
                  position={{ lat: selectedItem.latitude, lng: selectedItem.longitude }}
                  onCloseClick={() => setSelectedItem(null)}
                  pixelOffset={[0, -38]}
                >
                  <div className="map-info-window">
                    {selectedItem.imageUrls?.[0] && (
                      <img
                        src={selectedItem.imageUrls[0]}
                        alt={selectedItem.title}
                        className="info-window-img"
                      />
                    )}
                    <h4>{selectedItem.title}</h4>
                    <p className="info-type">
                      <span className={`info-badge ${selectedItem.type?.toLowerCase()}`}>
                        {selectedItem.type}
                      </span>
                    </p>
                    <p className="info-location">{selectedItem.location}</p>
                    <button
                      className="info-view-btn"
                      onClick={() => navigate(`/items/${selectedItem.id}`)}
                    >
                      View Details
                    </button>
                  </div>
                </InfoWindow>
              )}

              {/* User Location */}
              {userLocation && (
                <AdvancedMarker position={userLocation} zIndex={10}>
                  <div className="user-location-marker">
                    <div className="user-location-pulse"></div>
                    <div className="user-location-dot"></div>
                  </div>
                </AdvancedMarker>
              )}
            </Map>
          )}

          {/* My Location Button */}
          {userLocation && (
            <button
              className="my-location-btn"
              onClick={panToUserLocation}
              title="Go to my location"
            >
              <Navigation size={18} />
            </button>
          )}

          {/* Location error indicator */}
          {locationError && (
            <div className="location-error-badge" title={locationError}>
              <Navigation size={14} />
              <span>Location unavailable</span>
            </div>
          )}

          {/* Map Legend */}
          <div className="map-legend-container">
            <div className="map-legend glass">
              <div className="legend-item">
                <span className="legend-dot dot-danger"></span>
                <span className="legend-text">Lost</span>
              </div>
              <div className="legend-divider"></div>
              <div className="legend-item">
                <span className="legend-dot dot-success"></span>
                <span className="legend-text">Found</span>
              </div>
              <div className="legend-divider"></div>
              <div className="legend-item">
                <span className="legend-dot dot-campus"></span>
                <span className="legend-text">Campus</span>
              </div>
              {userLocation && (
                <>
                  <div className="legend-divider"></div>
                  <div className="legend-item">
                    <span className="legend-dot dot-user"></span>
                    <span className="legend-text">You</span>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

export default MapView;
